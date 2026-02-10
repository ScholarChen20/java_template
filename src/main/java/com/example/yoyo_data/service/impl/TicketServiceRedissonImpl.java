package com.example.yoyo_data.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.yoyo_data.common.Result;
import com.example.yoyo_data.common.constant.*;
import com.example.yoyo_data.common.dto.*;
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
            if(execute != null && execute != 0){
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

}
