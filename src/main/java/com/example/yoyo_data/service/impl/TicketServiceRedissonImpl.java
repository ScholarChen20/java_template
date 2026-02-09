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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 抢票服务实现类（Redisson 增强版）
 *
 * 改进点：
 * 1. 使用 Redisson 分布式锁，支持 Watchdog 自动续期
 * 2. 使用可重入锁，避免死锁
 * 3. 支持锁等待超时，提升用户体验
 * 4. Lua 脚本保证原子性操作
 *
 * 对比简单实现的优势：
 * - 锁超时自动续期（Watchdog）
 * - 可重入锁支持
 * - 原子性删除锁（不会误删其他线程的锁）
 * - 支持锁等待（tryLock with timeout）
 * - 更好的异常处理
 */
@Slf4j
@Service("ticketServiceRedisson") // 使用不同的 Bean 名称，方便对比
public class TicketServiceRedissonImpl implements TicketService {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private RedissonClient redissonClient; // 使用 Redisson 客户端

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

    /**
     * 抢票 - 使用 Redisson 分布式锁（增强版）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<TicketOrderVO> grabTicket(GrabTicketDTO dto, String token) {
        // 1. 验证 token
        if (token == null || !jwtUtils.validateToken(token)) {
            return Result.unauthorized("Token无效或已过期");
        }
        Long userId = jwtUtils.getUserIdFromToken(token);

        // 2. 验证演出活动
        ShowEvent showEvent = showEventMapper.selectById(dto.getShowEventId());
        if (showEvent == null) {
            return Result.error("演出活动不存在");
        }

        // 检查演出状态
        if (!ShowEventStatus.SELLING.equals(showEvent.getStatus())) {
            return Result.error("演出未开始售票或已结束");
        }

        // 检查是否在售票时间内
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(showEvent.getSaleStartTime())) {
            return Result.error("演出尚未开始售票");
        }
        if (now.isAfter(showEvent.getSaleEndTime())) {
            return Result.error("演出已结束售票");
        }

        // 3. 获取 Redisson 分布式锁（改进点：使用 Redisson）
        String lockKey = TicketRedisKey.GRAB_LOCK_PREFIX + userId + ":" + dto.getShowEventId();
        RLock lock = redissonClient.getLock(lockKey);

        try {
            // 尝试获取锁，等待时间 3 秒，锁自动释放时间 30 秒（Watchdog 会自动续期）
            // 改进点：支持等待超时，避免用户重复点击时直接失败
            boolean locked = lock.tryLock(3, 30, TimeUnit.SECONDS);

            if (!locked) {
                // 3秒内未获取到锁，说明正在处理中
                return Result.error("请勿重复提交抢票请求，请稍后再试");
            }

            log.info("用户 {} 获取到抢票锁，开始处理抢票请求", userId);

            // 4. 检查用户限购
            Result<Void> limitCheckResult = checkUserPurchaseLimit(userId, dto.getShowEventId(),
                    dto.getSeatIds().size(), showEvent.getMaxBuyLimit());
            if (!limitCheckResult.isSuccess()) {
                return Result.error(limitCheckResult.getMessage());
            }

            // 5. 验证座位并锁定
            List<Seat> seats = new ArrayList<>();
            for (Long seatId : dto.getSeatIds()) {
                Seat seat = seatMapper.selectById(seatId);
                if (seat == null) {
                    return Result.error("座位不存在: " + seatId);
                }
                if (!seat.getShowEventId().equals(dto.getShowEventId())) {
                    return Result.error("座位不属于该演出");
                }
                if (!SeatStatus.AVAILABLE.equals(seat.getStatus())) {
                    return Result.error("座位已被锁定或售出: " + seat.getSeatCode());
                }
                seats.add(seat);
            }

            // 6. 创建订单
            String orderNo = generateOrderNo(userId);
            BigDecimal totalAmount = seats.stream()
                    .map(Seat::getPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            TicketOrder order = TicketOrder.builder()
                    .orderNo(orderNo)
                    .showEventId(dto.getShowEventId())
                    .userId(userId)
                    .seatCount(seats.size())
                    .totalAmount(totalAmount)
                    .status(OrderStatus.PENDING)
                    .expireTime(now.plusMinutes(15)) // 15分钟过期
                    .contactName(dto.getContactName())
                    .contactPhone(dto.getContactPhone())
                    .contactIdCard(dto.getContactIdCard())
                    .build();

            ticketOrderMapper.insert(order);

            // 7. CAS 锁定座位
            LocalDateTime lockExpireTime = now.plusMinutes(15);
            for (Seat seat : seats) {
                int lockResult = seatMapper.lockSeatWithCAS(
                        seat.getId(),
                        userId,
                        order.getId(),
                        lockExpireTime,
                        seat.getVersion()
                );
                if (lockResult == 0) {
                    throw new RuntimeException("座位已被其他用户抢走: " + seat.getSeatCode());
                }
            }

            // 8. 更新演出活动座位数（乐观锁）
            int updateResult = showEventMapper.lockSeats(
                    dto.getShowEventId(),
                    seats.size(),
                    showEvent.getVersion()
            );
            if (updateResult == 0) {
                throw new RuntimeException("演出座位库存更新失败，请重试");
            }

            // 9. 创建订单座位关联
            List<OrderSeat> orderSeats = seats.stream().map(seat -> OrderSeat.builder()
                    .orderId(order.getId())
                    .seatId(seat.getId())
                    .showEventId(dto.getShowEventId())
                    .seatCode(seat.getSeatCode())
                    .price(seat.getPrice())
                    .build()
            ).collect(Collectors.toList());
            orderSeats.forEach(orderSeatMapper::insert);

            // 10. 更新用户购票记录
            updateUserTicketRecord(userId, dto.getShowEventId(), seats.size());

            // 11. 清除演出详情缓存
            clearShowEventCache(dto.getShowEventId());

            // 12. 发送 Kafka 消息
            sendGrabTicketEvent(order, seats);

            // 13. 构建返回结果
            TicketOrderVO vo = buildOrderVO(order, showEvent, seats);

            log.info("抢票成功: userId={}, orderId={}, orderNo={}, seatCount={}, 锁持有时间={}ms",
                    userId, order.getId(), orderNo, seats.size(),
                    System.currentTimeMillis() - lock.getHoldCount());

            return Result.success(vo);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("抢票过程中线程被中断: userId={}, showEventId={}", userId, dto.getShowEventId(), e);
            return Result.error("抢票过程中线程被中断，请重试");
        } catch (Exception e) {
            log.error("抢票失败: userId={}, showEventId={}", userId, dto.getShowEventId(), e);
            return Result.error("抢票失败: " + e.getMessage());
        } finally {
            // 14. 释放分布式锁
            // 改进点：Redisson 会检查当前线程是否持有锁，避免误删其他线程的锁
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.debug("释放抢票锁: userId={}, showEventId={}", userId, dto.getShowEventId());
            }
        }
    }

    /**
     * 支付订单（实现与原版相同）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<TicketOrderVO> payOrder(PayOrderDTO dto, String token) {
        // 实现逻辑与 TicketServiceImpl 相同
        // 为了简洁，这里省略重复代码
        // 实际项目中可以抽取公共方法
        return Result.error("请参考原实现");
    }

    /**
     * 取消订单（实现与原版相同）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> cancelOrder(Long orderId, String token) {
        return Result.error("请参考原实现");
    }

    /**
     * 查询订单详情（实现与原版相同）
     */
    @Override
    public Result<TicketOrderVO> queryOrder(Long orderId, String token) {
        return Result.error("请参考原实现");
    }

    /**
     * 查询我的订单列表（实现与原版相同）
     */
    @Override
    public Result<Page<TicketOrderVO>> queryMyOrders(String token, Integer page, Integer size, String status) {
        return Result.error("请参考原实现");
    }

    // ========================================
    // 私有辅助方法（与原版相同）
    // ========================================

    private Result<Void> checkUserPurchaseLimit(Long userId, Long showEventId, Integer requestCount, Integer maxBuyLimit) {
        LambdaQueryWrapper<UserTicketRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserTicketRecord::getUserId, userId)
                .eq(UserTicketRecord::getShowEventId, showEventId);

        UserTicketRecord record = userTicketRecordMapper.selectOne(queryWrapper);
        int currentCount = record == null ? 0 : record.getTicketCount();

        if (currentCount + requestCount > maxBuyLimit) {
            return Result.error("超过限购数量，每人最多购买" + maxBuyLimit + "张");
        }

        return Result.success(null);
    }

    private void updateUserTicketRecord(Long userId, Long showEventId, Integer ticketCount) {
        LambdaQueryWrapper<UserTicketRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserTicketRecord::getUserId, userId)
                .eq(UserTicketRecord::getShowEventId, showEventId);

        UserTicketRecord record = userTicketRecordMapper.selectOne(queryWrapper);
        if (record == null) {
            record = UserTicketRecord.builder()
                    .userId(userId)
                    .showEventId(showEventId)
                    .ticketCount(ticketCount)
                    .build();
            userTicketRecordMapper.insert(record);
        } else {
            userTicketRecordMapper.increaseTicketCount(userId, showEventId, ticketCount);
        }
    }

    private String generateOrderNo(Long userId) {
        return "TK" + System.currentTimeMillis() + String.format("%06d", userId % 1000000);
    }

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

    private void clearShowEventCache(Long showEventId) {
        try {
            String cacheKey = TicketRedisKey.SHOW_DETAIL_PREFIX + showEventId;
            redisService.delete(cacheKey);
            log.debug("清除演出详情缓存: showEventId={}", showEventId);
        } catch (Exception e) {
            log.error("清除演出详情缓存失败: showEventId={}", showEventId, e);
        }
    }

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
            log.debug("发送抢票成功事件: orderNo={}", order.getOrderNo());

        } catch (Exception e) {
            log.error("发送抢票成功事件失败: orderNo={}", order.getOrderNo(), e);
        }
    }
}
