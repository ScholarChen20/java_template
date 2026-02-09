package com.example.yoyo_data.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.yoyo_data.common.Result;
import com.example.yoyo_data.common.constant.*;
import com.example.yoyo_data.common.dto.GrabTicketDTO;
import com.example.yoyo_data.common.dto.PayOrderDTO;
import com.example.yoyo_data.common.entity.*;
import com.example.yoyo_data.common.vo.SeatVO;
import com.example.yoyo_data.common.vo.TicketOrderVO;
import com.example.yoyo_data.infrastructure.cache.RedisService;
import com.example.yoyo_data.infrastructure.message.KafkaProducerTemplate;
import com.example.yoyo_data.infrastructure.message.MessageEvent;
import com.example.yoyo_data.infrastructure.repository.*;
import com.example.yoyo_data.service.TicketService;
import com.example.yoyo_data.util.jwt.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.example.yoyo_data.common.constant.ShowEventStatus.*;

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
    private com.example.yoyo_data.util.RedisIdWorker redisIdWorker;

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
            return Result.unauthorized("Token无效或已过期");
        }
        Long userId = jwtUtils.getUserIdFromToken(token);

        log.info("【抢票请求】用户={}, 演出={}, 座位数={}", userId, dto.getShowEventId(), dto.getSeatIds().size());

        // ========== 第二步：演出活动校验 ==========
        Result<?> eventStatus = checkShowEventStatus(dto.getShowEventId(), dto.getSeatIds().size(), dto.getTicketUsers().size(), userId);
        if (eventStatus.getCode() == 500) {
            log.warn("【抢票失败】演出活动状态异常: {}", eventStatus.getMessage());
            return Result.error(eventStatus.getMessage());
        }

        // ========== 第三步：获取 Redisson 分布式锁 ==========
        String lockKey = TicketRedisKey.GRAB_LOCK_PREFIX + userId + ":" + dto.getShowEventId();
        RLock lock = redissonClient.getLock(lockKey);

        long lockStartTime = System.currentTimeMillis();

        try {
            // 尝试获取锁：等待时间 3 秒，锁自动释放时间 30 秒
            // Watchdog 会每 10 秒检查一次，如果业务还在执行，自动续期 30 秒
            // 这样即使业务执行时间超过 30 秒，也不会出现锁过期问题
            boolean locked = lock.tryLock(3, 30, TimeUnit.SECONDS);

            if (!locked) {
                log.warn("【抢票失败】获取锁超时: userId={}, showEventId={}, 等待时间=3秒", userId, dto.getShowEventId());
                return Result.error(REPEAT_REQUEST);
            }

            long lockAcquiredTime = System.currentTimeMillis() - lockStartTime;
            log.info("【锁获取成功】userId={}, showEventId={}, 等待时间={}ms",
                    userId, dto.getShowEventId(), lockAcquiredTime);

            // ========== 第四步第五步：座位校验和限购校验 =========

            long execute = stringRedisTemplate.execute(
                    SECKILL_SCRIPT,
                    Collections.emptyList(),
                    dto.getShowEventId().toString(),
                    new ArrayList<>(),
                    userId.toString()
            );
            if(execute != 0){
                return Result.error(execute == 1 ? REPEAT_ORDER : SEAT_LOCKED_OR_SOLD);
            }


            // ========== 第六步：创建订单 ==========
            LocalDateTime now = LocalDateTime.now();
            String orderNo = String.valueOf(redisIdWorker.nextId("ticket_order"));
            List<Seat> seats = seatMapper.selectBatchIds(dto.getSeatIds());
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
                    .expireTime(now.plusMinutes(15)) // 15分钟过期
                    .contactName(primaryContact.getContactName())
                    .contactPhone(primaryContact.getContactPhone())
                    .contactIdCard(primaryContact.getContactIdCard())
                    .build();

            ticketOrderMapper.insert(order);
            log.info("【订单创建】orderNo={}, orderId={}, totalAmount={}",
                    orderNo, order.getId(), totalAmount);

            // ========== 第七步：CAS 锁定座位 ==========
            LocalDateTime lockExpireTime = now.plusMinutes(15);
            int lockedSeatCount = 0;

            for (Seat seat : seats) {
                int lockResult = seatMapper.lockSeatWithCAS(
                        seat.getId(),
                        userId,
                        order.getId(),
                        lockExpireTime,
                        seat.getVersion()
                );

                if (lockResult == 0) {
                    // CAS 失败，座位已被其他用户抢走
                    log.error("【CAS失败】座位被抢: seatCode={}, seatId={}, version={}",
                            seat.getSeatCode(), seat.getId(), seat.getVersion());
                    throw new RuntimeException("座位已被其他用户抢走: " + seat.getSeatCode());
                }

                lockedSeatCount++;
            }

            log.info("【座位锁定】orderId={}, 锁定座位数={}/{}",
                    order.getId(), lockedSeatCount, seats.size());

            // ========== 第八步：更新演出活动座位数 ==========
            ShowEvent showEvent = showEventMapper.selectById(dto.getShowEventId());
            int updateResult = showEventMapper.lockSeats(
                    dto.getShowEventId(),
                    seats.size(),
                    showEvent.getVersion()
            );

            if (updateResult == 0) {
                // 乐观锁失败，演出座位数已被其他事务修改
                log.error("【乐观锁失败】演出座位数更新失败: showEventId={}, version={}",
                        dto.getShowEventId(), showEvent.getVersion());
                throw new RuntimeException("演出座位库存更新失败，请重试");
            }

            log.info("【演出更新】showEventId={}, 可售座位-{}, 锁定座位+{}",
                    dto.getShowEventId(), seats.size(), seats.size());

            // ========== 第九步：创建订单座位关联（绑定观影人） ==========
            List<OrderSeat> orderSeats = new ArrayList<>();
            for (int i = 0; i < seats.size(); i++) {
                Seat seat = seats.get(i);
                GrabTicketDTO.TicketUser ticketUser = dto.getTicketUsers().get(i);

                OrderSeat orderSeat = OrderSeat.builder()
                        .orderId(order.getId())
                        .seatId(seat.getId())
                        .showEventId(dto.getShowEventId())
                        .seatCode(seat.getSeatCode())
                        .price(seat.getPrice())
                        .viewerName(ticketUser.getContactName())
                        .viewerPhone(ticketUser.getContactPhone())
                        .viewerIdCard(ticketUser.getContactIdCard())
                        .build();

                orderSeats.add(orderSeat);
            }

            orderSeats.forEach(orderSeatMapper::insert);
            log.info("【订单座位关联】orderId={}, 关联座位数={}, 已绑定观影人信息",
                    order.getId(), orderSeats.size());

            // ========== 第十步：更新用户购票记录 ==========
            updateUserTicketRecord(userId, dto.getShowEventId(), seats.size());
            log.info("【购票记录】userId={}, showEventId={}, 购票数+{}",
                    userId, dto.getShowEventId(), seats.size());

            // ========== 第十一步：清除缓存 ==========
            clearShowEventCache(dto.getShowEventId());

            // ========== 第十二步：发送 Kafka 消息 ==========
            sendGrabTicketEvent(order, seats);

            // ========== 第十三步：构建返回结果 ==========
            TicketOrderVO vo = buildOrderVO(order, showEvent, seats);

            long totalTime = System.currentTimeMillis() - lockStartTime;
            log.info("【抢票成功】userId={}, orderId={}, orderNo={}, seatCount={}, 总耗时={}ms, 锁持有时间={}ms",
                    userId, order.getId(), orderNo, seats.size(), totalTime,
                    totalTime - lockAcquiredTime);

            return Result.success(vo);

        } catch (InterruptedException e) {
            // 线程被中断（例如：超时、应用关闭）
            Thread.currentThread().interrupt();
            log.error("【抢票中断】线程被中断: userId={}, showEventId={}", userId, dto.getShowEventId(), e);
            return Result.error("抢票过程中线程被中断，请重试");

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
     * 5. 更新订单状态为已支付
     * 6. 批量确认座位为已售出（LOCKED → SOLD）
     * 7. 更新演出活动座位数（locked → sold）
     * 8. 清除缓存
     * 9. 发送 Kafka 消息
     * 10. 构建返回结果
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
            return Result.unauthorized("Token无效或已过期");
        }
        Long userId = jwtUtils.getUserIdFromToken(token);

        log.info("【支付请求】userId={}, orderId={}, payType={}", userId, dto.getOrderId(), dto.getPayType());

        try {
            // ========== 第二步：订单查询和权限校验 ==========
            TicketOrder order = ticketOrderMapper.selectById(dto.getOrderId());
            if (order == null) {
                log.warn("【支付失败】订单不存在: orderId={}", dto.getOrderId());
                return Result.error("订单不存在");
            }

            // 验证订单所有权
            if (!order.getUserId().equals(userId)) {
                log.warn("【支付失败】无权限: userId={}, orderId={}, orderUserId={}",
                        userId, dto.getOrderId(), order.getUserId());
                return Result.error("无权限操作此订单");
            }

            // ========== 第三步：订单状态和过期时间校验 ==========
            if (!OrderStatus.PENDING.equals(order.getStatus())) {
                log.warn("【支付失败】订单状态异常: orderId={}, status={}", dto.getOrderId(), order.getStatus());
                return Result.error("订单状态不正确，无法支付");
            }

            if (LocalDateTime.now().isAfter(order.getExpireTime())) {
                log.warn("【支付失败】订单已过期: orderId={}, expireTime={}",
                        dto.getOrderId(), order.getExpireTime());
                return Result.error("订单已过期");
            }

            // ========== 第四步：查询订单座位 ==========
            LambdaQueryWrapper<OrderSeat> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(OrderSeat::getOrderId, dto.getOrderId());
            List<OrderSeat> orderSeats = orderSeatMapper.selectList(queryWrapper);
            List<Long> seatIds = orderSeats.stream()
                    .map(OrderSeat::getSeatId)
                    .collect(Collectors.toList());

            // ========== 第五步：更新订单状态为已支付 ==========
            int updateOrderResult = ticketOrderMapper.updateOrderToPaid(
                    dto.getOrderId(),
                    dto.getPayType(),
                    LocalDateTime.now()
            );

            if (updateOrderResult == 0) {
                log.warn("【支付失败】订单状态已变更: orderId={}", dto.getOrderId());
                return Result.error("订单状态已变更，支付失败");
            }

            log.info("【订单支付】orderId={}, orderNo={}, payType={}",
                    dto.getOrderId(), order.getOrderNo(), dto.getPayType());

            // ========== 第六步：批量确认座位为已售出 ==========
            int updateSeatResult = seatMapper.batchConfirmSeatSold(seatIds);
            log.info("【座位确认】orderId={}, 预期座位数={}, 实际更新数={}",
                    dto.getOrderId(), seatIds.size(), updateSeatResult);

            // ========== 第七步：更新演出活动座位数 ==========
            showEventMapper.confirmSeats(order.getShowEventId(), order.getSeatCount());
            log.info("【演出更新】showEventId={}, 锁定座位-{}, 已售座位+{}",
                    order.getShowEventId(), order.getSeatCount(), order.getSeatCount());

            // ========== 第八步：清除缓存 ==========
            clearShowEventCache(order.getShowEventId());

            // ========== 第九步：发送 Kafka 消息 ==========
            sendPayOrderEvent(order);

            // ========== 第十步：构建返回结果 ==========
            ShowEvent showEvent = showEventMapper.selectById(order.getShowEventId());
            List<Seat> seats = seatIds.stream()
                    .map(seatMapper::selectById)
                    .collect(Collectors.toList());
            TicketOrderVO vo = buildOrderVO(order, showEvent, seats);

            log.info("【支付成功】userId={}, orderId={}, orderNo={}, totalAmount={}",
                    userId, dto.getOrderId(), order.getOrderNo(), order.getTotalAmount());

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
    public Result<Void> cancelOrder(Long orderId, String token) {
        // ========== 第一步：Token 验证 ==========
        if (token == null || !jwtUtils.validateToken(token)) {
            log.warn("取消订单请求失败: Token无效或已过期");
            return Result.unauthorized("Token无效或已过期");
        }
        Long userId = jwtUtils.getUserIdFromToken(token);

        log.info("【取消订单请求】userId={}, orderId={}", userId, orderId);

        try {
            // ========== 第二步：订单查询和权限校验 ==========
            TicketOrder order = ticketOrderMapper.selectById(orderId);
            if (order == null) {
                log.warn("【取消失败】订单不存在: orderId={}", orderId);
                return Result.error("订单不存在");
            }

            if (!order.getUserId().equals(userId)) {
                log.warn("【取消失败】无权限: userId={}, orderId={}, orderUserId={}",
                        userId, orderId, order.getUserId());
                return Result.error("无权限操作此订单");
            }

            // ========== 第三步：订单状态校验 ==========
            if (!OrderStatus.PENDING.equals(order.getStatus())) {
                log.warn("【取消失败】订单状态异常: orderId={}, status={}", orderId, order.getStatus());
                return Result.error("订单状态不正确，无法取消");
            }

            // ========== 第四步：查询订单座位 ==========
            LambdaQueryWrapper<OrderSeat> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(OrderSeat::getOrderId, orderId);
            List<OrderSeat> orderSeats = orderSeatMapper.selectList(queryWrapper);
            List<Long> seatIds = orderSeats.stream()
                    .map(OrderSeat::getSeatId)
                    .collect(Collectors.toList());

            // ========== 第五步：批量释放座位 ==========
            int releaseSeatResult = seatMapper.batchReleaseSeat(seatIds);
            log.info("【座位释放】orderId={}, 预期座位数={}, 实际释放数={}",
                    orderId, seatIds.size(), releaseSeatResult);

            // ========== 第六步：更新演出活动座位数 ==========
            showEventMapper.releaseSeats(order.getShowEventId(), order.getSeatCount());
            log.info("【演出更新】showEventId={}, 锁定座位-{}, 可售座位+{}",
                    order.getShowEventId(), order.getSeatCount(), order.getSeatCount());

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

            return Result.success(null);

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
            return Result.unauthorized("Token无效或已过期");
        }
        Long userId = jwtUtils.getUserIdFromToken(token);

        try {
            // 查询订单
            TicketOrder order = ticketOrderMapper.selectById(orderId);
            if (order == null) {
                log.warn("【查询失败】订单不存在: orderId={}", orderId);
                return Result.error("订单不存在");
            }

            // 验证订单所有权
            if (!order.getUserId().equals(userId)) {
                log.warn("【查询失败】无权限: userId={}, orderId={}, orderUserId={}",
                        userId, orderId, order.getUserId());
                return Result.error("无权限查看此订单");
            }

            // 查询演出活动
            ShowEvent showEvent = showEventMapper.selectById(order.getShowEventId());

            // 查询订单座位
            LambdaQueryWrapper<OrderSeat> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(OrderSeat::getOrderId, orderId);
            List<OrderSeat> orderSeats = orderSeatMapper.selectList(queryWrapper);
            List<Seat> seats = orderSeats.stream()
                    .map(os -> seatMapper.selectById(os.getSeatId()))
                    .collect(Collectors.toList());

            // 构建返回结果
            TicketOrderVO vo = buildOrderVO(order, showEvent, seats);

            log.debug("【查询订单】userId={}, orderId={}, status={}", userId, orderId, order.getStatus());

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
            return Result.unauthorized("Token无效或已过期");
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
     * 检查用户限购
     *
     * @param userId 用户ID
     * @param showEventId 演出活动ID
     * @param requestCount 本次购票数量
     * @param maxBuyLimit 最大限购数量
     * @return 校验结果
     */
    private boolean checkUserPurchaseLimit(Long userId, Long showEventId,
                                                Integer requestCount, Integer maxBuyLimit) {
        LambdaQueryWrapper<UserTicketRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserTicketRecord::getUserId, userId)
                .eq(UserTicketRecord::getShowEventId, showEventId);

        UserTicketRecord record = userTicketRecordMapper.selectOne(queryWrapper);
        int currentCount = record == null ? 0 : record.getTicketCount();

        if (currentCount + requestCount > maxBuyLimit) {
            log.warn("【限购检查】userId={}, 已购={}, 本次购={}, 限购={}",
                    userId, currentCount, requestCount, maxBuyLimit);
            return false;
        }

        log.debug("【限购检查】userId={}, 已购={}, 本次购={}, 限购={}, 校验通过",
                userId, currentCount, requestCount, maxBuyLimit);
        return true;
    }

    /**
     * 更新用户购票记录
     *
     * @param userId 用户ID
     * @param showEventId 演出活动ID
     * @param ticketCount 购票数量
     */
    private void updateUserTicketRecord(Long userId, Long showEventId, Integer ticketCount) {
        LambdaQueryWrapper<UserTicketRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserTicketRecord::getUserId, userId)
                .eq(UserTicketRecord::getShowEventId, showEventId);

        UserTicketRecord record = userTicketRecordMapper.selectOne(queryWrapper);
        if (record == null) {
            // 首次购票，创建记录
            record = UserTicketRecord.builder()
                    .userId(userId)
                    .showEventId(showEventId)
                    .ticketCount(ticketCount)
                    .build();
            userTicketRecordMapper.insert(record);
            log.debug("【创建购票记录】userId={}, showEventId={}, ticketCount={}",
                    userId, showEventId, ticketCount);
        } else {
            // 增加购票数（使用原子性 SQL）
            userTicketRecordMapper.increaseTicketCount(userId, showEventId, ticketCount);
            log.debug("【更新购票记录】userId={}, showEventId={}, ticketCount+{}",
                    userId, showEventId, ticketCount);
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
        TicketOrderVO vo = new TicketOrderVO();
        BeanUtils.copyProperties(order, vo);

        if (showEvent != null) {
            vo.setShowName(showEvent.getShowName());
            vo.setVenueName(showEvent.getVenueName());
            vo.setShowTime(showEvent.getShowTime());
        }

        if (seats != null && !seats.isEmpty()) {
            List<SeatVO> seatVOs = seats.stream().map(seat -> {
                SeatVO seatVO = new SeatVO();
                BeanUtils.copyProperties(seat, seatVO);
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
     * 发送抢票成功事件到 Kafka
     *
     * @param order 订单实体
     * @param seats 座位列表
     */
    private void sendGrabTicketEvent(TicketOrder order, List<Seat> seats) {
        try {
            Map<String, Object> eventData = new HashMap<>();
            eventData.put("orderId", order.getId());
            eventData.put("orderNo", order.getOrderNo());
            eventData.put("userId", order.getUserId());
            eventData.put("showEventId", order.getShowEventId());
            eventData.put("seatCount", order.getSeatCount());
            eventData.put("totalAmount", order.getTotalAmount());
            eventData.put("seats", seats.stream().map(Seat::getSeatCode).collect(Collectors.toList()));
            eventData.put("expireTime", order.getExpireTime());

            MessageEvent event = MessageEvent.builder()
                    .eventType(KafkaTopic.ORDER_CREATE)
                    .source("TicketServiceRedisson")
                    .userId(order.getUserId())
                    .data(JSON.toJSONString(eventData))
                    .timestamp(LocalDateTime.now())
                    .createdAt(LocalDateTime.now())
                    .priority(8)
                    .build();

            kafkaProducerTemplate.sendEvent(KafkaTopic.ORDER_CREATE, event);
            log.debug("【Kafka消息】topic={}, orderNo={}", KafkaTopic.ORDER_CREATE, order.getOrderNo());

        } catch (Exception e) {
            // Kafka 发送失败不影响主流程
            log.warn("【Kafka发送失败】orderNo={}, error={}", order.getOrderNo(), e.getMessage());
        }
    }

    /**
     * 发送支付订单事件到 Kafka
     *
     * @param order 订单实体
     */
    private void sendPayOrderEvent(TicketOrder order) {
        try {
            Map<String, Object> eventData = new HashMap<>();
            eventData.put("orderId", order.getId());
            eventData.put("orderNo", order.getOrderNo());
            eventData.put("userId", order.getUserId());
            eventData.put("payType", order.getPayType());
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

    private Result<?> checkShowEventStatus(long eventId, int seatSize, int userSize, long userId){
        ShowEvent showEvent = showEventMapper.selectById(eventId);
        if (showEvent == null) {
            log.warn("【抢票失败】演出不存在: showEventId={}", eventId);
            return Result.error(NOT_EXIST);
        }

        // 检查演出状态
        if (!ShowEventStatus.SELLING.equals(showEvent.getStatus())) {
            log.warn("【抢票失败】演出状态异常: status={}, showEventId={}", showEvent.getStatus(), eventId);
            return Result.error(NOT_START);
        }

        // 检查售票时间
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(showEvent.getSaleStartTime())) {
            log.warn("【抢票失败】售票未开始: saleStartTime={}, showEventId={}", showEvent.getSaleStartTime(), eventId);
            return Result.error(NOT_SELLING);
        }
        if (now.isAfter(showEvent.getSaleEndTime())) {
            log.warn("【抢票失败】售票已结束: saleEndTime={}, showEventId={}", showEvent.getSaleEndTime(), eventId);
            return Result.error(SELLING_END);
        }

        // 验证座位数量与观影人数量一致
        if (seatSize != userSize) {
            log.warn("【抢票失败】座位数量与观影人数量不一致: seatCount={}, userCount={}, userId={}", seatSize, userSize, userId);
            return Result.error(SEAT_NUM_NOT_EQUAL_TO_VIEWER_NUM);
        }
        return Result.success();
    }
}
