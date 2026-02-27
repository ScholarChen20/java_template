package com.example.yoyo_data.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.yoyo_data.common.Result;
import com.example.yoyo_data.common.constant.*;
import com.example.yoyo_data.common.dto.*;
import com.example.yoyo_data.common.dto.request.QuickGrabRequest;
import com.example.yoyo_data.common.entity.*;
import com.example.yoyo_data.common.vo.SeatVO;
import com.example.yoyo_data.common.vo.TicketOrderVO;
import com.example.yoyo_data.infrastructure.cache.RedisService;
import com.example.yoyo_data.infrastructure.message.KafkaProducerTemplate;
import com.example.yoyo_data.infrastructure.message.MessageEvent;
import com.example.yoyo_data.infrastructure.repository.*;
import com.example.yoyo_data.infrastructure.scheduler.OrderTimeoutHandler;
import com.example.yoyo_data.service.TicketService;
import com.example.yoyo_data.util.RedisIdWorker;
import com.example.yoyo_data.util.jwt.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.example.yoyo_data.infrastructure.cache.CacheKeyManager.CacheTTL.*;

/**
 * 抢票服务 Redisson 增强实现
 *
 * 技术改进点：
 * 1. 使用 Redisson 分布式锁替代简单 Redis 锁
 * 2. Watchdog 自动续期机制，避免锁过期问题
 * 3. 可重入锁支持，避免死锁
 * 4. 原子性锁释放，不会误删其他线程的锁
 * 5. 支持锁等待超时，提升用户体验
 *
 * 质量保证：
 * - 完整的异常处理
 * - 详细的日志记录
 * - 参数校验完备
 * - 事务管理严格
 * - 代码注释清晰
 *
 * @author YoYo Data Team
 * @version 2.0 (Redisson Enhanced)
 * @since 2026-02-07
 */
@Slf4j
@Service // 使用默认 Bean 名称，成为主实现
public class TicketServiceRedissonImpl implements TicketService {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private RedisService redisService;

    @Autowired
    private ShowEventMapper showEventMapper;

    @Autowired
    private SeatMapper seatMapper;

    @Autowired
    private TicketOrderMapper ticketOrderMapper;

    @Autowired
    private OrderSeatMapper orderSeatMapper;

    @Autowired
    private UserTicketRecordMapper userTicketRecordMapper;

    @Autowired
    private KafkaProducerTemplate kafkaProducerTemplate;

    @Autowired
    private RedisIdWorker redisIdWorker;

    @Autowired
    private OrderTimeoutHandler orderTimeoutHandler;

    @Autowired
    private com.example.yoyo_data.service.PaymentService paymentService;

    /**
     * Lua 脚本，加载脚本内容
     */
    private static final DefaultRedisScript<Long> SECKILL_SCRIPT;
    static{
        SECKILL_SCRIPT = new DefaultRedisScript<>();
        SECKILL_SCRIPT.setLocation(new ClassPathResource("lua_scripts/seckill.lua"));
        SECKILL_SCRIPT.setResultType(Long.class);
    }

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 抢票 - 核心接口（Redisson 增强版）
     *
     * 业务流程：
     * 1. Token 验证 - 确认用户身份
     * 2. 演出活动校验 - 状态、时间、库存
     * 3. 获取分布式锁 - Redisson 可重入锁，支持等待和自动续期
     * 4. 限购检查 - 防止用户超量购买
     * 5. 座位验证 - 确认座位可用性
     * 6. 创建订单 - 15分钟支付时间
     * 7. CAS 锁定座位 - 乐观锁防止并发冲突
     * 8. 更新演出统计 - 可售座位数-1，锁定座位数+1
     * 9. 创建订单座位关联 - 记录订单包含的座位
     * 10. 更新用户购票记录 - 用于限购控制
     * 11. 清除缓存 - 保证数据一致性
     * 12. 发送 Kafka 消息 - 异步通知
     * 13. 构建返回结果 - VO 对象
     * 14. 释放分布式锁 - 自动校验锁所有权
     *
     * @param dto 抢票请求参数
     * @param token JWT 认证 token
     * @return 订单信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<TicketOrderVO> grabTicket(GrabTicketDTO dto, String token) {
        // ========== 第一步：Token 验证 ==========
        if (token == null || !jwtUtils.validateToken(token)) {
            log.warn("抢票请求失败: Token无效或已过期");
            return Result.unauthorized(TicketStatus.TOKEN_INVALID);
        }
        Long userId = jwtUtils.getUserIdFromToken(token);

        log.info("【抢票请求】用户={}, 演出={}, 座位数={}", userId, dto.getShowEventId(), dto.getSeatIds().size());

        // ========== 第二步：演出活动校验 ==========
        Result<?> eventStatus = checkShowEventStatus(dto.getShowEventId(), dto.getTicketUsers().size(), userId, dto.getSeatIds());
        if (eventStatus.getCode() == 500) {
            log.warn("【抢票失败】演出活动状态异常: {}", eventStatus.getMessage());
            return Result.error(eventStatus.getMessage());
        }

        // ========== 第三步：获取 Redisson 分布式锁 ==========
        String lockKey = TicketRedisKey.GRAB_LOCK_PREFIX + userId + ":" + dto.getShowEventId();
        RLock lock = redissonClient.getLock(lockKey);

        long lockStartTime = System.currentTimeMillis();

        try {
            // 尝试获取锁：等待时间 3 秒，锁自动释放时间 30 秒 Watchdog 会每 10 秒检查一次，如果业务还在执行，自动续期 30 秒
            boolean locked = lock.tryLock(3, 30, TimeUnit.SECONDS);
            if (!locked) {
                log.warn("【抢票失败】获取锁超时: userId={}, showEventId={}, 等待时间=3秒", userId, dto.getShowEventId());
                return Result.error(TicketStatus.REPEAT_REQUEST);
            }

            long lockAcquiredTime = System.currentTimeMillis() - lockStartTime;
            log.info("【锁获取成功】userId={}, showEventId={}, 等待时间={}ms", userId, dto.getShowEventId(), lockAcquiredTime);

            // ========== 第四步第五步：座位校验和限购校验 =========
            // 查询座位信息
            List<Seat> seats = seatMapper.selectBatchIds(dto.getSeatIds());
            String orderNo = String.valueOf(redisIdWorker.nextId(TicketRedisKey.TICKET_ORDER_PREFIX));
            List<String> luaArgs = getLuaArgs(dto, seats, userId, orderNo);

            log.info("【座位信息汇总】userId={}, showEventId={}, seatCount={}, seats={}",
                    userId, dto.getShowEventId(), seats.size(),
                    seats.stream().map(s -> s.getSeatZone() + "_" + s.getSeatRow() + "_" + s.getSeatNumber())
                        .collect(Collectors.toList()));

            // 执行 Lua 脚本（从预热的Hash缓存中校验座位）
            Long execute = stringRedisTemplate.execute(
                    SECKILL_SCRIPT,
                    Collections.emptyList(),
                    luaArgs.toArray(new String[0])
            );

            log.info("【抢票结果】execute={}", execute);
            if(execute != 0){
                return Result.error(execute == 1 ? TicketStatus.SEAT_LOCKED_OR_SOLD : TicketStatus.REPEAT_ORDER);
            }

            // ========== 第六步：创建订单 ==========
            // orderNo 已在 Lua 脚本参数中生成
            BigDecimal totalAmount = seats.stream()
                    .map(Seat::getPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // 使用第一个观影人作为主联系人
            GrabTicketDTO.TicketUser primaryContact = dto.getTicketUsers().get(0);
            // 创建订单
            TicketOrder order = TicketOrder.builder()
                    .orderNo(orderNo)
                    .showEventId(dto.getShowEventId())
                    .userId(userId)
                    .seatCount(seats.size())
                    .totalAmount(totalAmount)
                    .status(OrderStatus.PENDING)
                    .expireTime(LocalDateTime.now().plusMinutes(15)) // 15分钟过期
                    .contactName(primaryContact.getContactName())
                    .contactPhone(primaryContact.getContactPhone())
                    .contactIdCard(primaryContact.getContactIdCard())
                    .build();

            ticketOrderMapper.insert(order);
            log.info("【订单创建】orderNo={}, orderId={}, totalAmount={}",
                    orderNo, order.getId(), totalAmount);

            // ========== 第七步：构建订单创建事件（准备异步处理） ==========
            ShowEvent showEvent = showEventMapper.selectById(dto.getShowEventId());
            OrderCreateEvent orderCreateEvent = buildOrderCreateEvent(order, seats, dto, showEvent);
            Long orderId = order.getId();

            // ========== 重要修复：在事务提交后再发送Kafka消息和加入延迟队列 ==========
            // 问题：如果在事务提交前发送消息，消费者可能查询不到订单（事务还未提交）
            // 解决：使用TransactionSynchronization，确保事务提交后才执行
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    // 事务提交后执行：此时订单已真正写入数据库
                    try {
                        // 1. 发送Kafka消息
                        sendOrderCreateEvent(orderCreateEvent);
                        log.info("【订单创建事件已发送】orderId={}, orderNo={}", orderId, orderNo);

                        // 2. 加入延迟队列（15分钟后检查是否支付）
                        orderTimeoutHandler.addOrderToDelayQueue(orderId, 15);
                        log.info("【订单加入延迟队列】orderId={}, expireTime={}, delayMinutes=15",
                                orderId, orderCreateEvent.getExpireTime());

                    } catch (Exception e) {
                        // 注意：这里的异常不会回滚事务（事务已提交）
                        // 需要有补偿机制或告警通知
                        log.error("【事务后处理失败】orderId={}, error={}", orderId, e.getMessage(), e);
                    }
                }
            });

            // ========== 第九步：构建返回结果（快速响应） ==========
            TicketOrderVO vo = buildOrderVO(order, showEvent, seats);

            long totalTime = System.currentTimeMillis() - lockStartTime;
            log.info("【抢票成功-快速响应】userId={}, orderId={}, orderNo={}, seatCount={}, 总耗时={}ms（后续业务异步处理）",
                    userId, order.getId(), orderNo, seats.size(), totalTime);
            return Result.success(vo);

        } catch (InterruptedException e) {
            // 线程被中断（例如：超时、应用关闭）
            Thread.currentThread().interrupt();
            log.error("【抢票中断】线程被中断: userId={}, showEventId={}", userId, dto.getShowEventId(), e);
            return Result.error(TicketStatus.ORDER_PAY_SUCCESS);

        } catch (Exception e) {
            // 业务异常（座位被抢、库存不足等）
            log.error("【抢票失败】业务异常: userId={}, showEventId={}, error={}",
                    userId, dto.getShowEventId(), e.getMessage(), e);
            return Result.error("抢票失败: " + e.getMessage());

        } finally {
            // ========== 第十四步：释放分布式锁 ==========
            // Redisson 会自动检查当前线程是否持有锁，避免误删其他线程的锁
            if (lock.isHeldByCurrentThread()) {
                try {
                    lock.unlock();
                    long lockHoldTime = System.currentTimeMillis() - lockStartTime;
                    log.debug("【锁释放】userId={}, showEventId={}, 锁持有时间={}ms",
                            userId, dto.getShowEventId(), lockHoldTime);
                } catch (IllegalMonitorStateException e) {
                    // 锁已被释放或超时
                    log.warn("【锁释放警告】锁状态异常: userId={}, showEventId={}",
                            userId, dto.getShowEventId(), e);
                }
            }
        }
    }


    /**
     * 支付订单
     *
     * 业务流程：
     * 1. Token 验证
     * 2. 订单查询和权限校验
     * 3. 订单状态和过期时间校验
     * 4. 查询订单关联的座位
     * 5. 【重要】调用第三方支付接口完成扣款
     * 6. 更新订单状态为已支付（包含交易流水号）
     * 7. 从延迟队列移除订单（支付成功，无需超时处理）
     * 8. 批量确认座位为已售出（LOCKED → SOLD）
     * 9. 更新演出活动座位数（locked → sold）
     * 10. 清除缓存
     * 11. 发送 Kafka 消息
     * 12. 构建返回结果
     *
     * @param dto 支付请求参数
     * @param token JWT 认证 token
     * @return 订单信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<TicketOrderVO> payOrder(PayOrderDTO dto, String token) {
        // ========== 第一步：Token 验证 ==========
        if (token == null || !jwtUtils.validateToken(token)) {
            log.warn("支付请求失败: Token无效或已过期");
            return Result.unauthorized(TicketStatus.TOKEN_INVALID);
        }
        Long userId = jwtUtils.getUserIdFromToken(token);
        log.info("【支付请求】userId={}, orderId={}, payType={}", userId, dto.getOrderId(), dto.getPayType());

        try {
            // ========== 第二步：订单查询和权限校验 ==========
            long orderId = dto.getOrderId();
            TicketOrder order = ticketOrderMapper.selectById(orderId);
            if (order == null) {
                log.warn("【支付失败】订单不存在: orderId={}", orderId);
                return Result.error(TicketStatus.ORDER_NOT_EXIST);
            }

            // 验证订单所有权
            if (!order.getUserId().equals(userId)) {
                log.warn("【支付失败】无权限: userId={}, orderId={}, orderUserId={}", userId, orderId, order.getUserId());
                return Result.error(TicketStatus.NO_PERMISSION);
            }

            // ========== 第三步：订单状态和过期时间校验 ==========
            if (!OrderStatus.PENDING.equals(order.getStatus())) {
                log.warn("【支付失败】订单状态异常: orderId={}, status={}", orderId, order.getStatus());
                return Result.error(TicketStatus.ORDER_STATUS_ERROR);
            }

            if (LocalDateTime.now().isAfter(order.getExpireTime())) {
                log.warn("【支付失败】订单已过期: orderId={}, expireTime={}",
                        orderId, order.getExpireTime());
                return Result.error(TicketStatus.ORDER_EXPIRED);
            }

            // ========== 第四步：查询订单座位 ==========
            List<Long> seatIds = orderSeatMapper.selectIdsByOrderId(orderId);

            // ========== 第五步：模拟调用第三方支付接口完成扣款 ==========
            log.info("【调用支付接口】开始扣款: orderId={}, orderNo={}, amount={}, payType={}",
                    orderId, order.getOrderNo(), order.getTotalAmount(), dto.getPayType());

            // 构建支付请求
            PaymentRequest paymentRequest = PaymentRequest.builder()
                    .orderId(order.getId())
                    .orderNo(order.getOrderNo())
                    .userId(userId)
                    .amount(order.getTotalAmount())
                    .payType(dto.getPayType())
                    .description("演出门票订单支付 - " + order.getOrderNo())
                    .clientIp("127.0.0.1") // TODO: 获取真实客户端IP
                    .notifyUrl("http://localhost:8080/api/payment/notify") // 支付结果回调地址
                    .build();

            // 调用支付服务
            Result<PaymentResponse> paymentResult = paymentService.executePayment(paymentRequest);
            // 检查支付结果
            if (paymentResult.getCode() != 200) {
                log.error("【支付失败】orderId={}, orderNo={}, payType={}, error={}",
                        orderId, order.getOrderNo(), dto.getPayType(), paymentResult.getMessage());
                return Result.error("支付失败: " + paymentResult.getMessage());
            }

            PaymentResponse paymentResponse = paymentResult.getData();
            String transactionId = paymentResponse.getTransactionId();

            log.info("【支付成功】orderId={}, orderNo={}, transactionId={}, amount={}, payType={}",
                    orderId, order.getOrderNo(), transactionId,
                    paymentResponse.getAmount(), paymentResponse.getPayType());

            // ========== 第六步：更新订单状态为已支付 ==========
            int updateOrderResult = ticketOrderMapper.updateOrderToPaidWithTransaction(
                    orderId,
                    dto.getPayType(),
                    transactionId,
                    paymentResponse.getPayTime()
            );
            if (updateOrderResult == 0) {
                log.error("【订单状态更新失败】orderId={}, 可能订单状态已变更", orderId);
                // TODO: 订单状态更新失败，需要调用退款接口退款
                log.warn("【触发退款】orderId={}, transactionId={}", orderId, transactionId);
                paymentService.refund(transactionId, order.getTotalAmount().doubleValue(), "订单状态更新失败");
                return Result.error(TicketStatus.ORDER_STATUS_CHANGED);
            }
            log.info("【订单支付成功】orderId={}, orderNo={}, transactionId={}, payType={}",
                    orderId, order.getOrderNo(), transactionId, dto.getPayType());

            // ========== 第七步：从延迟队列移除订单（支付成功，无需超时处理） ==========
            orderTimeoutHandler.removeOrderFromDelayQueue(orderId);

            // ========== 第八步：批量确认座位为已售出 ==========
            int updateSeatResult = seatMapper.batchConfirmSeatSold(seatIds);
            log.info("【座位确认】orderId={}, 预期座位数={}, 实际更新数={}",
                    orderId, seatIds.size(), updateSeatResult);

            // 同步更新Redis缓存中的座位状态为已售出
            syncSeatStatusToRedis(order.getShowEventId(), seatIds, SeatStatus.CACHE_SOLD);

            // ========== 第九步：更新演出活动座位数 ==========
            showEventMapper.confirmSeats(order.getShowEventId(), order.getSeatCount());
            log.info("【演出更新】showEventId={}, 锁定座位-{}, 已售座位+{}",
                    order.getShowEventId(), order.getSeatCount(), order.getSeatCount());

            // ========== 第十步：清除缓存 ==========
            clearShowEventCache(order.getShowEventId());

            // ========== 第十一步：发送 Kafka 消息 ==========
            sendPayOrderEvent(order, dto.getPayType());

            // ========== 第十二步：构建返回结果 ==========
            ShowEvent showEvent = showEventMapper.selectById(order.getShowEventId());
            List<Seat> seats = seatMapper.selectBatchIds(seatIds);
            TicketOrderVO vo = buildOrderVO(ticketOrderMapper.selectById(order.getId()), showEvent, seats);

            log.info("【支付成功】userId={}, orderId={}, orderNo={}, totalAmount={}",
                    userId, orderId, order.getOrderNo(), order.getTotalAmount());

            return Result.success(vo);

        } catch (Exception e) {
            log.error("【支付异常】userId={}, orderId={}, error={}", userId, dto.getOrderId(), e.getMessage(), e);
            return Result.error("支付订单失败: " + e.getMessage());
        }
    }

    /**
     * 取消订单
     *
     * 业务流程：
     * 1. Token 验证
     * 2. 订单查询和权限校验
     * 3. 订单状态校验（只能取消待支付的订单）
     * 4. 查询订单座位
     * 5. 批量释放座位（LOCKED → AVAILABLE）
     * 6. 更新演出活动座位数（locked → available）
     * 7. 更新订单状态为已取消
     * 8. 减少用户购票记录
     * 9. 清除缓存
     * 10. 发送 Kafka 消息
     *
     * @param orderId 订单ID
     * @param token JWT 认证 token
     * @return 取消结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> cancelOrder(Long orderId, String token) {
        // ========== 第一步：Token 验证 ==========
        if (token == null || !jwtUtils.validateToken(token)) {
            log.warn("取消订单请求失败: Token无效或已过期");
            return Result.unauthorized(TicketStatus.TOKEN_INVALID);
        }
        Long userId = jwtUtils.getUserIdFromToken(token);
        log.info("【取消订单请求】userId={}, orderId={}", userId, orderId);

        try {
            // ========== 第二步：订单查询和权限校验 ==========
            TicketOrder order = ticketOrderMapper.selectById(orderId);
            if (order == null) {
                log.warn("【取消失败】订单不存在: orderId={}", orderId);
                return Result.error(TicketStatus.ORDER_NOT_EXIST);
            }

            if (!order.getUserId().equals(userId)) {
                log.warn("【取消失败】无权限: userId={}, orderId={}, orderUserId={}", userId, orderId, order.getUserId());
                return Result.error(TicketStatus.NO_PERMISSION);
            }

            // ========== 第三步：订单状态校验 ==========
            if (!OrderStatus.PENDING.equals(order.getStatus())) {
                log.warn("【取消失败】订单状态异常: orderId={}, status={}", orderId, order.getStatus());
                return Result.error(TicketStatus.ORDER_STATUS_ERROR);
            }

            // ========== 第四步：查询订单座位Id ==========
            List<Long> seatIds = orderSeatMapper.selectIdsByOrderId(orderId);

            // ========== 第五步：批量释放座位 ==========
            int releaseSeatResult = seatMapper.batchReleaseSeat(seatIds);
            log.info("【座位释放】orderId={}, 预期座位数={}, 实际释放数={}", orderId, seatIds.size(), releaseSeatResult);

            // 同步更新Redis缓存中的座位状态为可售
            syncSeatStatusToRedis(order.getShowEventId(), seatIds, SeatStatus.CACHE_AVAILABLE);

            // ========== 第六步：更新演出活动座位数 ==========
            showEventMapper.releaseSeats(order.getShowEventId(), order.getSeatCount());
            log.info("【演出更新】showEventId={}, 锁定座位-{}, 可售座位+{}", order.getShowEventId(), order.getSeatCount(), order.getSeatCount());

            // ========== 第七步：更新订单状态为已取消 ==========
            ticketOrderMapper.updateOrderToCancelled(orderId);
            log.info("【订单取消】orderId={}, orderNo={}", orderId, order.getOrderNo());

            // ========== 第八步：减少用户购票记录 ==========
            userTicketRecordMapper.decreaseTicketCount(userId, order.getShowEventId(), order.getSeatCount());
            log.info("【购票记录】userId={}, showEventId={}, 购票数-{}",
                    userId, order.getShowEventId(), order.getSeatCount());

            // ========== 第九步：清除缓存 ==========
            clearShowEventCache(order.getShowEventId());

            // ========== 第十步：发送 Kafka 消息 ==========
            sendCancelOrderEvent(order);

            log.info("【取消成功】userId={}, orderId={}, orderNo={}", userId, orderId, order.getOrderNo());

            return Result.success(TicketStatus.CANCEL_SUCCESS);

        } catch (Exception e) {
            log.error("【取消异常】userId={}, orderId={}, error={}", userId, orderId, e.getMessage(), e);
            return Result.error("取消订单失败: " + e.getMessage());
        }
    }

    /**
     * 查询订单详情
     *
     * @param orderId 订单ID
     * @param token JWT 认证 token
     * @return 订单详情
     */
    @Override
    public Result<TicketOrderVO> queryOrder(Long orderId, String token) {
        // Token 验证
        if (token == null || !jwtUtils.validateToken(token)) {
            log.warn("查询订单请求失败: Token无效或已过期");
            return Result.unauthorized(TicketStatus.TOKEN_INVALID);
        }
        Long userId = jwtUtils.getUserIdFromToken(token);

        try {
            // 缓存中取
            String cacheKey = TicketRedisKey.ORDER_PREFIX + userId + ":" + orderId;
            TicketOrderVO orderVo = JSON.parseObject(redisService.stringGetString(cacheKey), TicketOrderVO.class);
            if (orderVo != null) {
                log.info("【查询成功】userId={}, orderId={}", userId, orderId);
                return Result.success(orderVo);
            }

            // 查询订单
            TicketOrder order = ticketOrderMapper.selectOne(new QueryWrapper<TicketOrder>().eq("id", orderId));
            if (order == null) {
                log.warn("【查询失败】订单不存在: orderId={}", orderId);
                return Result.error(TicketStatus.ORDER_NOT_EXIST);
            }

            // 验证订单所有权
            if (!order.getUserId().equals(userId)) {
                log.warn("【查询失败】无权限: userId={}, orderId={}, orderUserId={}",
                        userId, orderId, order.getUserId());
                return Result.error(TicketStatus.NO_PERMISSION);
            }

            // 查询订单座位
            List<Long> seatIds = orderSeatMapper.selectIdsByOrderId(orderId);
            List<Seat> seats = seatMapper.selectBatchIds(seatIds);

            // 构建返回结果
            TicketOrderVO vo = buildOrderVO(order, showEventMapper.selectById(order.getShowEventId()), seats);
            log.debug("【查询订单】userId={}, orderId={}, status={}", userId, orderId, order.getStatus());

            // 写入缓存
            redisService.stringSetString(cacheKey ,JSON.toJSONString(vo), ONE_HOUR);
            return Result.success(vo);

        } catch (Exception e) {
            log.error("【查询异常】userId={}, orderId={}, error={}", userId, orderId, e.getMessage(), e);
            return Result.error("查询订单详情失败: " + e.getMessage());
        }
    }

    /**
     * 查询我的订单列表
     *
     * @param token JWT 认证 token
     * @param page 页码
     * @param size 每页大小
     * @param status 订单状态筛选（可选）
     * @return 订单列表
     */
    @Override
    public Result<Page<TicketOrderVO>> queryMyOrders(String token, Integer page, Integer size, String status) {
        // Token 验证
        if (token == null || !jwtUtils.validateToken(token)) {
            log.warn("查询订单列表请求失败: Token无效或已过期");
            return Result.unauthorized(TicketStatus.TOKEN_INVALID);
        }
        Long userId = jwtUtils.getUserIdFromToken(token);

        try {
            Page<TicketOrder> pageParam = new Page<>(page, size);
            LambdaQueryWrapper<TicketOrder> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(TicketOrder::getUserId, userId);

            // 状态筛选
            if (status != null && !status.isEmpty()) {
                queryWrapper.eq(TicketOrder::getStatus, status);
            }

            // 按创建时间倒序
            queryWrapper.orderByDesc(TicketOrder::getCreatedAt);
            Page<TicketOrder> orderPage = ticketOrderMapper.selectPage(pageParam, queryWrapper);

            // 转换为VO
            Page<TicketOrderVO> voPage = new Page<>(orderPage.getCurrent(), orderPage.getSize(), orderPage.getTotal());
            List<TicketOrderVO> voList = orderPage.getRecords().stream().map(order -> {
                ShowEvent showEvent = showEventMapper.selectById(order.getShowEventId());

                // 查询订单座位
                LambdaQueryWrapper<OrderSeat> osQuery = new LambdaQueryWrapper<>();
                osQuery.eq(OrderSeat::getOrderId, order.getId());
                List<OrderSeat> orderSeats = orderSeatMapper.selectList(osQuery);
                List<Seat> seats = orderSeats.stream()
                        .map(os -> seatMapper.selectById(os.getSeatId()))
                        .collect(Collectors.toList());

                return buildOrderVO(order, showEvent, seats);
            }).collect(Collectors.toList());

            voPage.setRecords(voList);

            log.debug("【查询订单列表】userId={}, page={}, size={}, total={}",
                    userId, page, size, voPage.getTotal());

            return Result.success(voPage);

        } catch (Exception e) {
            log.error("【查询列表异常】userId={}, error={}", userId, e.getMessage(), e);
            return Result.error("查询订单列表失败: " + e.getMessage());
        }
    }

    /**
     * 查看我的电子票（票夹）
     *
     * 只返回 PAID（已支付）状态的订单，并包含二维码入场码 URL，
     * 对应大麦网"我的票夹"功能。
     *
     * @param token JWT 认证 token
     * @param page  页码
     * @param size  每页大小
     * @return 电子票列表（含二维码URL）
     */
    @Override
    public Result<Page<TicketOrderVO>> getMyTickets(String token, Integer page, Integer size) {
        if (token == null || !jwtUtils.validateToken(token)) {
            log.warn("查询电子票请求失败: Token无效或已过期");
            return Result.unauthorized(TicketStatus.TOKEN_INVALID);
        }
        Long userId = jwtUtils.getUserIdFromToken(token);

        try {
            Page<TicketOrder> pageParam = new Page<>(page, size);
            LambdaQueryWrapper<TicketOrder> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(TicketOrder::getUserId, userId)
                    .eq(TicketOrder::getStatus, OrderStatus.PAID) // 只查已支付订单
                    .orderByDesc(TicketOrder::getPayTime);        // 按支付时间倒序

            Page<TicketOrder> orderPage = ticketOrderMapper.selectPage(pageParam, queryWrapper);
            log.debug("【查询订单】userId={}, page={}, size={}, total={}",
                    userId, page, size, orderPage.getTotal());
            if(orderPage.getTotal() == 0){
                return Result.error("票夹无该用户订单信息显示");
            }

            Page<TicketOrderVO> voPage = new Page<>(orderPage.getCurrent(), orderPage.getSize(), orderPage.getTotal());
            List<TicketOrderVO> voList = orderPage.getRecords().stream().map(order -> {
                ShowEvent showEvent = showEventMapper.selectById(order.getShowEventId());
                List<Long> seatIds = orderSeatMapper.selectIdsByOrderId(order.getId());
                List<Seat> seats = seatMapper.selectBatchIds(seatIds);
                return buildOrderVO(order, showEvent, seats);
            }).collect(Collectors.toList());

            voPage.setRecords(voList);

            log.debug("【查询电子票】userId={}, page={}, size={}, total={}",
                    userId, page, size, voPage.getTotal());

            return Result.success(voPage);

        } catch (Exception e) {
            log.error("【查询电子票异常】userId={}, error={}", userId, e.getMessage(), e);
            return Result.error("查询电子票失败: " + e.getMessage());
        }
    }

    /**
     * 构建订单VO
     *
     * @param order 订单实体
     * @param showEvent 演出活动实体
     * @param seats 座位列表
     * @return 订单VO
     */
    private TicketOrderVO buildOrderVO(TicketOrder order, ShowEvent showEvent, List<Seat> seats) {
        TicketOrderVO vo = BeanUtil.copyProperties(order, TicketOrderVO.class);
        if (showEvent != null) {
            vo.setShowName(showEvent.getShowName());
            vo.setVenueName(showEvent.getVenueName());
            vo.setShowTime(showEvent.getShowTime());
        }

        if (seats != null && !seats.isEmpty()) {
            List<SeatVO> seatVOs = seats.stream().map(seat -> {
                SeatVO seatVO = new SeatVO();
                BeanUtil.copyProperties(seat, seatVO);
                return seatVO;
            }).collect(Collectors.toList());
            vo.setSeats(seatVOs);
        }

        return vo;
    }

    /**
     * 清除演出详情缓存
     *
     * @param showEventId 演出活动ID
     */
    private void clearShowEventCache(Long showEventId) {
        try {
            String cacheKey = TicketRedisKey.SHOW_DETAIL_PREFIX + showEventId;
            redisService.delete(cacheKey);
            log.debug("【缓存清除】showEventId={}", showEventId);
        } catch (Exception e) {
            // 缓存清除失败不影响主流程
            log.warn("【缓存清除失败】showEventId={}, error={}", showEventId, e.getMessage());
        }
    }

    /**
     * 发送支付订单事件到 Kafka
     *
     * @param order 订单实体
     */
    private void sendPayOrderEvent(TicketOrder order, String payType) {
        try {
            Map<String, Object> eventData = new HashMap<>();
            eventData.put("orderId", order.getId());
            eventData.put("orderNo", order.getOrderNo());
            eventData.put("userId", order.getUserId());
            eventData.put("payType", payType);
            eventData.put("totalAmount", order.getTotalAmount());
            eventData.put("payTime", order.getPayTime());

            MessageEvent event = MessageEvent.builder()
                    .eventType(KafkaTopic.ORDER_PAID)
                    .source("TicketServiceRedisson")
                    .userId(order.getUserId())
                    .data(JSON.toJSONString(eventData))
                    .timestamp(LocalDateTime.now())
                    .createdAt(LocalDateTime.now())
                    .priority(9) // 最高优先级
                    .build();

            kafkaProducerTemplate.sendEvent(KafkaTopic.ORDER_PAID, event);
            log.debug("【Kafka消息】topic={}, orderNo={}", KafkaTopic.ORDER_PAID, order.getOrderNo());

        } catch (Exception e) {
            log.warn("【Kafka发送失败】orderNo={}, error={}", order.getOrderNo(), e.getMessage());
        }
    }

    /**
     * 发送取消订单事件到 Kafka
     *
     * @param order 订单实体
     */
    private void sendCancelOrderEvent(TicketOrder order) {
        try {
            Map<String, Object> eventData = new HashMap<>();
            eventData.put("orderId", order.getId());
            eventData.put("orderNo", order.getOrderNo());
            eventData.put("userId", order.getUserId());
            eventData.put("seatCount", order.getSeatCount());

            MessageEvent event = MessageEvent.builder()
                    .eventType(KafkaTopic.ORDER_CANCEL)
                    .source("TicketServiceRedisson")
                    .userId(order.getUserId())
                    .data(JSON.toJSONString(eventData))
                    .timestamp(LocalDateTime.now())
                    .createdAt(LocalDateTime.now())
                    .priority(7)
                    .build();

            kafkaProducerTemplate.sendEvent(KafkaTopic.ORDER_CANCEL, event);
            log.debug("【Kafka消息】topic={}, orderNo={}", KafkaTopic.ORDER_CANCEL, order.getOrderNo());

        } catch (Exception e) {
            log.warn("【Kafka发送失败】orderNo={}, error={}", order.getOrderNo(), e.getMessage());
        }
    }

    private Result<?> checkShowEventStatus(long eventId, int userSize, long userId, List<Long> seatIds){
        ShowEvent showEvent = showEventMapper.selectById(eventId);
        if (showEvent == null) {
            log.warn("【抢票失败】演出不存在: showEventId={}", eventId);
            return Result.error(TicketStatus.NOT_EXIST);
        }

        // 检查演出状态
        if (!ShowEventStatus.SELLING.equals(showEvent.getStatus())) {
            log.warn("【抢票失败】演出状态异常: status={}, showEventId={}", showEvent.getStatus(), eventId);
            return Result.error(TicketStatus.NOT_START);
        }

        // 检查售票时间
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(showEvent.getSaleStartTime())) {
            log.warn("【抢票失败】售票未开始: saleStartTime={}, showEventId={}", showEvent.getSaleStartTime(), eventId);
            return Result.error(TicketStatus.NOT_SELLING);
        }
        if (now.isAfter(showEvent.getSaleEndTime())) {
            log.warn("【抢票失败】售票已结束: saleEndTime={}, showEventId={}", showEvent.getSaleEndTime(), eventId);
            return Result.error(TicketStatus.SELLING_END);
        }

        // 验证座位数量与观影人数量一致
        if (seatIds.size() != userSize) {
            log.warn("【抢票失败】座位数量与观影人数量不一致: seatCount={}, userCount={}, userId={}", seatIds.size(), userSize, userId);
            return Result.error(TicketStatus.SEAT_NUM_NOT_EQUAL_TO_VIEWER_NUM);
        }

        // 检查座位号与活动匹配
        int isExist = seatMapper.checkSeatExist(eventId, seatIds);
        if (isExist == 0) {
            log.warn("【抢票失败】座位号与活动不匹配: showEventId={}", eventId);
            return Result.error(TicketStatus.SEAT_NOT_MATCH);
        }

        return Result.success();
    }

    /**
     * 构建订单创建事件
     *
     * @param order 订单实体
     * @param seats 座位列表
     * @param dto 抢票请求DTO
     * @param showEvent 演出活动实体
     * @return 订单创建事件
     */
    private OrderCreateEvent buildOrderCreateEvent(TicketOrder order, List<Seat> seats,
                                                    GrabTicketDTO dto, ShowEvent showEvent) {
        // 转换座位信息
        List<OrderCreateEvent.SeatInfo> seatInfos = seats.stream()
                .map(seat -> OrderCreateEvent.SeatInfo.builder()
                        .seatId(seat.getId())
                        .seatCode(seat.getSeatCode())
                        .price(seat.getPrice())
                        .version(seat.getVersion())
                        .build())
                .collect(Collectors.toList());

        // 转换观影人信息
        List<OrderCreateEvent.TicketUserInfo> ticketUserInfos = dto.getTicketUsers().stream()
                .map(user -> OrderCreateEvent.TicketUserInfo.builder()
                        .contactName(user.getContactName())
                        .contactPhone(user.getContactPhone())
                        .contactIdCard(user.getContactIdCard())
                        .build())
                .collect(Collectors.toList());

        return OrderCreateEvent.builder()
                .orderId(order.getId())
                .orderNo(order.getOrderNo())
                .showEventId(order.getShowEventId())
                .userId(order.getUserId())
                .seatCount(order.getSeatCount())
                .totalAmount(order.getTotalAmount())
                .expireTime(order.getExpireTime())
                .contactName(order.getContactName())
                .contactPhone(order.getContactPhone())
                .contactIdCard(order.getContactIdCard())
                .seatIds(dto.getSeatIds())
                .ticketUsers(ticketUserInfos)
                .seats(seatInfos)
                .showEventVersion(showEvent.getVersion())
                .createdAt(LocalDateTime.now())
                .build();
    }

    /**
     * 发送订单创建事件到 Kafka
     *
     * @param orderCreateEvent 订单创建事件
     */
    private void sendOrderCreateEvent(OrderCreateEvent orderCreateEvent) {
        try {
            MessageEvent event = MessageEvent.builder()
                    .eventType(KafkaTopic.ORDER_CREATE)
                    .source("TicketServiceRedisson")
                    .userId(orderCreateEvent.getUserId())
                    .data(JSON.toJSONString(orderCreateEvent))
                    .timestamp(LocalDateTime.now())
                    .createdAt(LocalDateTime.now())
                    .priority(10) // 最高优先级，快速处理
                    .build();

            kafkaProducerTemplate.sendEvent(KafkaTopic.ORDER_CREATE, event);
            log.info("【订单创建事件发送成功】orderId={}, orderNo={}",
                    orderCreateEvent.getOrderId(), orderCreateEvent.getOrderNo());

        } catch (Exception e) {
            log.error("【订单创建事件发送失败】orderId={}, orderNo={}, error={}",
                    orderCreateEvent.getOrderId(), orderCreateEvent.getOrderNo(), e.getMessage(), e);
            // 注意：发送失败需要人工处理或重试机制
        }
    }

    /**
     * 同步更新Redis缓存中的座位状态
     * 确保数据库与Redis缓存一致性
     *
     * @param showEventId 演出活动ID
     * @param seatIds 座位ID列表
     * @param status 座位状态（"0"=可售, "1"=已锁定, "2"=已售出）
     */
    private void syncSeatStatusToRedis(Long showEventId, List<Long> seatIds, String status) {
        try {
            // 查询座位信息
            List<SeatCacheDTO> seats = seatMapper.selectByEventIdAndSeatIds(showEventId, seatIds);

            // 按分区分组
            Map<String, List<SeatCacheDTO>> seatsByZone = seats.stream()
                    .collect(Collectors.groupingBy(SeatCacheDTO::getSeatZone));

            // 更新Redis缓存
            int totalUpdated = 0;
            for (Map.Entry<String, List<SeatCacheDTO>> entry : seatsByZone.entrySet()) {
                String zone = entry.getKey();
                List<SeatCacheDTO> zoneSeats = entry.getValue();

                String hashKey = TicketRedisKey.SEAT_STOCK_PREFIX + showEventId + ":" + zone;

                // 批量更新Hash Field
                for (SeatCacheDTO seat : zoneSeats) {
                    String field = seat.getSeatKey();
                    stringRedisTemplate.opsForHash().put(hashKey, field, status);
                    totalUpdated++;
                }
            }

            log.info("【Redis缓存同步】showEventId={}, seatCount={}, status={}, zones={}",
                    showEventId, totalUpdated, status, seatsByZone.keySet());

        } catch (Exception e) {
            // Redis同步失败不影响主流程，只记录日志
            log.error("【Redis缓存同步失败】showEventId={}, seatCount={}, status={}, error={}",
                    showEventId, seatIds.size(), status, e.getMessage(), e);
        }
    }
    /**
     * 构建Lua脚本参数
     * @param dto
     * @param seats
     * @param userId
     * @param orderNo
     * @return
     */
    private static @NonNull List<String> getLuaArgs(GrabTicketDTO dto, List<Seat> seats, Long userId, String orderNo) {
        // 构建Lua脚本参数
        // ARGV[1] = activityId
        // ARGV[2] = seatCount
        // ARGV[3~n] = zone1, row1, col1, zone2, row2, col2, ...
        // ARGV[n+1] = userId
        // ARGV[n+2] = orderId
        List<String> luaArgs = new ArrayList<>();
        luaArgs.add(dto.getShowEventId().toString());  // ARGV[1]: activityId
        luaArgs.add(String.valueOf(seats.size()));     // ARGV[2]: seatCount

        // 添加所有座位信息（zone, row, col）
        for (Seat seat : seats) {
            luaArgs.add(seat.getSeatZone());           // zone
            luaArgs.add(String.valueOf(seat.getSeatRow()));  // row
            luaArgs.add(String.valueOf(seat.getSeatNumber())); // col
        }

        luaArgs.add(userId.toString());                // userId
        luaArgs.add(orderNo);                          // orderId
        log.info("【构建Lua脚本参数】{}", luaArgs);
        return luaArgs;
    }

    // ========================================================================
    // ==================== 快速抢票（Quick Grab）实现 ==========================
    // ========================================================================

    /**
     * 快速抢票 - 系统自动分配座位
     *
     * 与普通抢票的核心差异：
     *   普通抢票：用户指定具体 seatIds → Lua 原子校验锁定 → 异步 Kafka 写 DB
     *   快速抢票：系统按区域自动选座（Redis 优先 / DB 兜底，支持连座算法）
     *            → Lua 原子校验锁定（流程完全复用）→ 异步 Kafka 写 DB
     *
     * 座位选取策略（两阶段）：
     *   Phase 1 - Redis 优先：读取预热的 Hash（ticket:seat:stock:{id}:{zone}），
     *             value="0" 表示可售，应用连座算法后获得候选 (row,col) 列表，
     *             再回查 DB 获取完整 Seat 实体。
     *   Phase 2 - DB 兜底：Redis 缓存未命中时（开票早期未完成预热），
     *             直接按 (show_event_id, zone, status=AVAILABLE) 查 DB，
     *             在内存中执行连座算法。
     *
     * 之后流程与普通抢票完全一致：
     *   Lua 原子锁（防并发双锁） → 创建订单 → 事务后 Kafka → Redisson 延迟队列
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<TicketOrderVO> quickGrabTicket(QuickGrabRequest request, String token) {

        // ========== 第一步：Token 验证 ==========
        if (token == null || !jwtUtils.validateToken(token)) {
            log.warn("【快速抢票失败】Token无效或已过期");
            return Result.unauthorized(TicketStatus.TOKEN_INVALID);
        }
        Long userId = jwtUtils.getUserIdFromToken(token);
        log.info("【快速抢票请求】userId={}, showEventId={}, zone={}, seatCount={}, preferContinuous={}",
                userId, request.getShowEventId(), request.getSeatZone(),
                request.getSeatCount(), request.getPreferContinuous());

        // ========== 第二步：参数校验 ==========
        if (request.getTicketUsers() == null
                || request.getTicketUsers().size() != request.getSeatCount()) {
            return Result.error("观影人数量必须与购票数量一致");
        }

        // ========== 第三步：演出活动状态校验 ==========
        ShowEvent showEvent = showEventMapper.selectById(request.getShowEventId());
        if (showEvent == null) {
            return Result.error(TicketStatus.NOT_EXIST);
        }
        if (!ShowEventStatus.SELLING.equals(showEvent.getStatus())) {
            return Result.error(TicketStatus.NOT_START);
        }
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(showEvent.getSaleStartTime())) {
            return Result.error(TicketStatus.NOT_SELLING);
        }
        if (now.isAfter(showEvent.getSaleEndTime())) {
            return Result.error(TicketStatus.SELLING_END);
        }
        // 快速预检：剩余可售座位是否足够
        if (showEvent.getAvailableSeats() < request.getSeatCount()) {
            log.warn("【快速抢票失败】演出可售座位不足: availableSeats={}, seatCount={}",
                    showEvent.getAvailableSeats(), request.getSeatCount());
            return Result.error(TicketStatus.SEAT_LOCKED_OR_SOLD);
        }

        // ========== 第四步：用户限购预检（乐观，不加锁） ==========
        {
            LambdaQueryWrapper<UserTicketRecord> limitQ = new LambdaQueryWrapper<>();
            limitQ.eq(UserTicketRecord::getUserId, userId)
                  .eq(UserTicketRecord::getShowEventId, request.getShowEventId());
            UserTicketRecord record = userTicketRecordMapper.selectOne(limitQ);
            int purchased = (record == null) ? 0 : record.getTicketCount();
            if (purchased + request.getSeatCount() > showEvent.getMaxBuyLimit()) {
                log.warn("【快速抢票失败】超出限购: 已购={}, 本次={}, 限额={}",
                        purchased, request.getSeatCount(), showEvent.getMaxBuyLimit());
                return Result.error("超过限购数量，每人最多购买 " + showEvent.getMaxBuyLimit() + " 张");
            }
        }

        // ========== 第五步：获取 Redisson 分布式锁（防同一用户重复提交） ==========
        String lockKey = TicketRedisKey.GRAB_LOCK_PREFIX + userId + ":" + request.getShowEventId();
        RLock lock = redissonClient.getLock(lockKey);
        long lockStartTime = System.currentTimeMillis();

        try {
            boolean locked = lock.tryLock(3, 30, TimeUnit.SECONDS);
            if (!locked) {
                log.warn("【快速抢票失败】获取锁超时: userId={}, showEventId={}", userId, request.getShowEventId());
                return Result.error(TicketStatus.REPEAT_REQUEST);
            }

            // ========== 第六步：自动选座（Redis 优先 / DB 兜底） ==========
            List<Seat> seats = selectSeatsForQuickGrab(
                    request.getShowEventId(),
                    request.getSeatZone(),
                    request.getSeatCount(),
                    Boolean.TRUE.equals(request.getPreferContinuous())
            );

            if (seats.isEmpty()) {
                log.warn("【快速抢票失败】区域无可售座位: zone={}, showEventId={}",
                        request.getSeatZone(), request.getShowEventId());
                return Result.error("【" + request.getSeatZone() + "区】暂无可售座位，请选择其他区域");
            }
            if (seats.size() < request.getSeatCount()) {
                log.warn("【快速抢票失败】区域座位不足: zone={}, available={}, required={}",
                        request.getSeatZone(), seats.size(), request.getSeatCount());
                return Result.error("【" + request.getSeatZone() + "区】可售座位不足，请减少购票数量或选择其他区域");
            }

            log.info("【快速抢票-选座完成】userId={}, zone={}, seats={}",
                    userId, request.getSeatZone(),
                    seats.stream().map(s -> s.getSeatRow() + "_" + s.getSeatNumber())
                            .collect(Collectors.toList()));

            // ========== 第七步：Lua 脚本原子校验 + 锁定 Redis 缓存 ==========
            String orderNo = String.valueOf(redisIdWorker.nextId(TicketRedisKey.TICKET_ORDER_PREFIX));
            List<String> luaArgs = buildQuickGrabLuaArgs(request.getShowEventId(), seats, userId, orderNo);

            Long luaResult = stringRedisTemplate.execute(
                    SECKILL_SCRIPT,
                    Collections.emptyList(),
                    luaArgs.toArray(new String[0])
            );
            log.info("【快速抢票-Lua结果】luaResult={}", luaResult);

            if (luaResult != null && luaResult != 0) {
                // 1 = 座位已被抢，2 = 重复下单
                String errMsg = luaResult == 2 ? TicketStatus.REPEAT_ORDER : TicketStatus.SEAT_LOCKED_OR_SOLD;
                log.warn("【快速抢票失败】Lua校验不通过: luaResult={}, userId={}", luaResult, userId);
                return Result.error(errMsg);
            }

            // ========== 第八步：创建订单（DB） ==========
            BigDecimal totalAmount = seats.stream()
                    .map(Seat::getPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            QuickGrabRequest.TicketUser primaryUser = request.getTicketUsers().get(0);
            TicketOrder order = TicketOrder.builder()
                    .orderNo(orderNo)
                    .showEventId(request.getShowEventId())
                    .userId(userId)
                    .seatCount(seats.size())
                    .totalAmount(totalAmount)
                    .status(OrderStatus.PENDING)
                    .expireTime(now.plusMinutes(15))
                    .contactName(primaryUser.getContactName())
                    .contactPhone(primaryUser.getContactPhone())
                    .contactIdCard(primaryUser.getContactIdCard())
                    .build();

            ticketOrderMapper.insert(order);
            log.info("【快速抢票-订单创建】orderNo={}, orderId={}, totalAmount={}",
                    orderNo, order.getId(), totalAmount);

            // ========== 第九步：事务提交后异步发 Kafka + 加入延迟队列 ==========
            OrderCreateEvent orderCreateEvent = buildOrderCreateEventForQuickGrab(order, seats, request, showEvent);
            Long orderId = order.getId();

            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    try {
                        sendOrderCreateEvent(orderCreateEvent);
                        orderTimeoutHandler.addOrderToDelayQueue(orderId, 15);
                        log.info("【快速抢票-事务后处理完成】orderId={}, orderNo={}", orderId, orderNo);
                    } catch (Exception e) {
                        log.error("【快速抢票-事务后处理失败】orderId={}, error={}", orderId, e.getMessage(), e);
                    }
                }
            });

            // ========== 第十步：返回结果 ==========
            TicketOrderVO vo = buildOrderVO(order, showEvent, seats);
            long totalTime = System.currentTimeMillis() - lockStartTime;
            log.info("【快速抢票成功】userId={}, orderId={}, zone={}, seatCount={}, totalAmount={}, 耗时={}ms",
                    userId, orderId, request.getSeatZone(), seats.size(), totalAmount, totalTime);
            return Result.success(vo);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("【快速抢票中断】userId={}, showEventId={}", userId, request.getShowEventId(), e);
            return Result.error("抢票被中断，请重试");
        } catch (Exception e) {
            log.error("【快速抢票异常】userId={}, showEventId={}, error={}",
                    userId, request.getShowEventId(), e.getMessage(), e);
            return Result.error("快速抢票失败: " + e.getMessage());
        } finally {
            if (lock.isHeldByCurrentThread()) {
                try {
                    lock.unlock();
                } catch (IllegalMonitorStateException e) {
                    log.warn("【快速抢票-锁释放警告】userId={}", userId);
                }
            }
        }
    }

    // ------------------------------------------------------------------
    // 快速抢票 - 选座核心：Redis 优先 / DB 兜底
    // ------------------------------------------------------------------

    /**
     * 自动选座入口：先尝试读 Redis 预热缓存，缓存为空则降级到 DB 查询。
     */
    private List<Seat> selectSeatsForQuickGrab(Long showEventId, String zone,
                                                int count, boolean preferContinuous) {
        String hashKey = TicketRedisKey.SEAT_STOCK_PREFIX + showEventId + ":" + zone;
        Map<Object, Object> hashEntries = stringRedisTemplate.opsForHash().entries(hashKey);

        if (!hashEntries.isEmpty()) {
            log.debug("【快速选座-Redis】zone={}, cacheSize={}", zone, hashEntries.size());
            return selectSeatsFromRedis(showEventId, zone, count, preferContinuous, hashEntries);
        }

        log.debug("【快速选座-DB兜底】zone={}", zone);
        return selectSeatsFromDB(showEventId, zone, count, preferContinuous);
    }

    /**
     * Redis 路径：从预热 Hash 中筛选 value="0"（可售）的座位，
     * 应用连座算法，再回查 DB 获取完整 Seat 实体。
     *
     * Redis Hash 结构：
     *   Key   = ticket:seat:stock:{showEventId}:{zone}
     *   Field = "{row}_{col}"  e.g. "3_7"
     *   Value = "0"(可售) | "1"(锁定) | "2"(已售)
     */
    private List<Seat> selectSeatsFromRedis(Long showEventId, String zone, int count,
                                             boolean preferContinuous,
                                             Map<Object, Object> hashEntries) {
        // 1. 解析可售座位列表 [row, col]
        List<int[]> available = new ArrayList<>();
        for (Map.Entry<Object, Object> entry : hashEntries.entrySet()) {
            if (SeatStatus.CACHE_AVAILABLE.equals(entry.getValue())) {
                String[] parts = entry.getKey().toString().split("_");
                if (parts.length == 2) {
                    try {
                        available.add(new int[]{
                                Integer.parseInt(parts[0]),
                                Integer.parseInt(parts[1])
                        });
                    } catch (NumberFormatException ignored) { /* 忽略格式异常 */ }
                }
            }
        }

        if (available.size() < count) {
            log.warn("【快速选座-Redis】可售座位不足: available={}, required={}", available.size(), count);
            return Collections.emptyList();
        }

        // 2. 按 (row, col) 升序排列，方便连座检测
        available.sort((a, b) -> a[0] != b[0] ? Integer.compare(a[0], b[0]) : Integer.compare(a[1], b[1]));

        // 3. 连座优先 / 随意选座
        List<int[]> selected;
        if (preferContinuous) {
            selected = findConsecutiveSeats(available, count);
            if (selected.isEmpty()) {
                log.debug("【快速选座-Redis】未找到连座，降级为随意选座");
                selected = available.subList(0, count);
            }
        } else {
            selected = available.subList(0, count);
        }

        // 4. 回查 DB 获取完整 Seat 实体（含 seatId/version/price）
        return fetchSeatEntitiesByRowCols(showEventId, zone, selected);
    }

    /**
     * DB 兜底路径：直接按区域查可售座位，在内存中执行连座算法。
     * 用于 Redis 预热缓存尚未就绪（演出早期）的场景。
     */
    private List<Seat> selectSeatsFromDB(Long showEventId, String zone,
                                          int count, boolean preferContinuous) {
        // 多拉一些候选，保证连座算法有足够素材（最少 count*5，至少 20 条）
        int fetchLimit = Math.max(count * 5, 20);
        List<Seat> candidates = seatMapper.selectAvailableByZone(showEventId, zone, fetchLimit);

        if (candidates.isEmpty()) return Collections.emptyList();
        if (candidates.size() <= count) return candidates;  // 候选数恰好够用，直接返回

        if (!preferContinuous) {
            return candidates.subList(0, count);
        }

        // 连座算法：candidates 已按 (seat_row, seat_number) 升序
        List<int[]> rowCols = candidates.stream()
                .map(s -> new int[]{s.getSeatRow(), s.getSeatNumber()})
                .collect(Collectors.toList());

        List<int[]> consecutive = findConsecutiveSeats(rowCols, count);
        if (consecutive.isEmpty()) {
            // 无连座，降级
            log.debug("【快速选座-DB】未找到连座，降级为随意选座");
            return candidates.subList(0, count);
        }

        // 按 row_col 反查对应的 Seat 实体
        Set<String> selectedKeys = consecutive.stream()
                .map(rc -> rc[0] + "_" + rc[1])
                .collect(Collectors.toSet());
        return candidates.stream()
                .filter(s -> selectedKeys.contains(s.getSeatRow() + "_" + s.getSeatNumber()))
                .collect(Collectors.toList());
    }

    /**
     * 连座查找算法：在有序 [row,col] 列表中找第一段满足 count 个连续座位号的组合。
     * 要求同一排（row 相同）且座位号连续（col 差值为 1）。
     *
     * @param sorted 已按 (row, col) 升序排列的座位列表
     * @param count  需要的连座数量
     * @return 找到则返回连座列表，否则返回空列表
     */
    private List<int[]> findConsecutiveSeats(List<int[]> sorted, int count) {
        // 按排号分组
        Map<Integer, List<Integer>> byRow = new TreeMap<>();
        for (int[] seat : sorted) {
            byRow.computeIfAbsent(seat[0], k -> new ArrayList<>()).add(seat[1]);
        }

        for (Map.Entry<Integer, List<Integer>> entry : byRow.entrySet()) {
            int row = entry.getKey();
            List<Integer> cols = entry.getValue(); // 已升序
            for (int i = 0; i <= cols.size() - count; i++) {
                boolean isConsecutive = true;
                for (int j = 1; j < count; j++) {
                    if (cols.get(i + j) != cols.get(i + j - 1) + 1) {
                        isConsecutive = false;
                        break;
                    }
                }
                if (isConsecutive) {
                    List<int[]> result = new ArrayList<>();
                    for (int j = 0; j < count; j++) {
                        result.add(new int[]{row, cols.get(i + j)});
                    }
                    log.debug("【连座找到】row={}, cols={}", row, result.stream()
                            .map(rc -> rc[1]).collect(Collectors.toList()));
                    return result;
                }
            }
        }
        return Collections.emptyList();
    }

    /**
     * 根据 (zone, row, col) 列表回查 DB，获取完整 Seat 实体（含 id、price、version）。
     * max 4 条，允许单条查询。
     */
    private List<Seat> fetchSeatEntitiesByRowCols(Long showEventId, String zone, List<int[]> rowCols) {
        List<Seat> result = new ArrayList<>();
        for (int[] rc : rowCols) {
            LambdaQueryWrapper<Seat> q = new LambdaQueryWrapper<>();
            q.eq(Seat::getShowEventId, showEventId)
             .eq(Seat::getSeatZone, zone)
             .eq(Seat::getSeatRow, rc[0])
             .eq(Seat::getSeatNumber, rc[1])
             .eq(Seat::getStatus, SeatStatus.AVAILABLE);
            Seat seat = seatMapper.selectOne(q);
            if (seat != null) {
                result.add(seat);
            }
        }
        return result;
    }

    // ------------------------------------------------------------------
    // 快速抢票 - 辅助方法
    // ------------------------------------------------------------------

    /**
     * 构建 Lua 脚本参数（快速抢票版本，showEventId 直接传入而非从 DTO 取）。
     */
    private List<String> buildQuickGrabLuaArgs(Long showEventId, List<Seat> seats,
                                                Long userId, String orderNo) {
        List<String> args = new ArrayList<>();
        args.add(showEventId.toString());
        args.add(String.valueOf(seats.size()));
        for (Seat seat : seats) {
            args.add(seat.getSeatZone());
            args.add(String.valueOf(seat.getSeatRow()));
            args.add(String.valueOf(seat.getSeatNumber()));
        }
        args.add(userId.toString());
        args.add(orderNo);
        log.info("【快速抢票-Lua参数】{}", args);
        return args;
    }

    /**
     * 构建订单创建事件（快速抢票版本）。
     * 将 {@link QuickGrabRequest.TicketUser} 转换为 {@link OrderCreateEvent.TicketUserInfo}。
     */
    private OrderCreateEvent buildOrderCreateEventForQuickGrab(TicketOrder order, List<Seat> seats,
                                                                QuickGrabRequest request,
                                                                ShowEvent showEvent) {
        List<OrderCreateEvent.SeatInfo> seatInfos = seats.stream()
                .map(seat -> OrderCreateEvent.SeatInfo.builder()
                        .seatId(seat.getId())
                        .seatCode(seat.getSeatCode())
                        .price(seat.getPrice())
                        .version(seat.getVersion())
                        .build())
                .collect(Collectors.toList());

        List<OrderCreateEvent.TicketUserInfo> ticketUserInfos = request.getTicketUsers().stream()
                .map(u -> OrderCreateEvent.TicketUserInfo.builder()
                        .contactName(u.getContactName())
                        .contactPhone(u.getContactPhone())
                        .contactIdCard(u.getContactIdCard())
                        .build())
                .collect(Collectors.toList());

        return OrderCreateEvent.builder()
                .orderId(order.getId())
                .orderNo(order.getOrderNo())
                .showEventId(order.getShowEventId())
                .userId(order.getUserId())
                .seatCount(order.getSeatCount())
                .totalAmount(order.getTotalAmount())
                .expireTime(order.getExpireTime())
                .contactName(order.getContactName())
                .contactPhone(order.getContactPhone())
                .contactIdCard(order.getContactIdCard())
                .seatIds(seats.stream().map(Seat::getId).collect(Collectors.toList()))
                .ticketUsers(ticketUserInfos)
                .seats(seatInfos)
                .showEventVersion(showEvent.getVersion())
                .createdAt(LocalDateTime.now())
                .build();
    }

}
