package com.example.yoyo_data.infrastructure.scheduler;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.yoyo_data.common.constant.KafkaTopic;
import com.example.yoyo_data.common.constant.OrderStatus;
import com.example.yoyo_data.common.constant.TicketRedisKey;
import com.example.yoyo_data.common.entity.OrderSeat;
import com.example.yoyo_data.common.entity.TicketOrder;
import com.example.yoyo_data.infrastructure.cache.RedisService;
import com.example.yoyo_data.infrastructure.message.KafkaProducerTemplate;
import com.example.yoyo_data.infrastructure.message.MessageEvent;
import com.example.yoyo_data.infrastructure.repository.OrderSeatMapper;
import com.example.yoyo_data.infrastructure.repository.SeatMapper;
import com.example.yoyo_data.infrastructure.repository.ShowEventMapper;
import com.example.yoyo_data.infrastructure.repository.TicketOrderMapper;
import com.example.yoyo_data.infrastructure.repository.UserTicketRecordMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 订单超时处理器（旧方案 - 已废弃）
 * 使用 Redis ZSet 延迟队列 + 定时任务扫描超时订单
 *
 * ⚠️ 已废弃：请使用 OrderTimeoutHandler（Redisson RDelayedQueue 方案）
 *
 * 问题：
 * 1. 需要定时任务扫描，实时性差
 * 2. 没有分布式锁，多实例会重复处理同一订单
 * 3. 需要手动管理 ZSet，容易出错
 *
 * @deprecated 使用 OrderTimeoutHandler 替代
 * @see com.example.yoyo_data.infrastructure.scheduler.OrderTimeoutHandler
 * @author YoYo Data Team
 * @version 1.0
 * @since 2026-02-09
 */
@Slf4j
// @Component // 已禁用：使用 OrderTimeoutHandler 替代
@Deprecated
public class OrderTimeoutScheduler {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RedisService redisService;

    @Autowired
    private TicketOrderMapper ticketOrderMapper;

    @Autowired
    private OrderSeatMapper orderSeatMapper;

    @Autowired
    private SeatMapper seatMapper;

    @Autowired
    private ShowEventMapper showEventMapper;

    @Autowired
    private UserTicketRecordMapper userTicketRecordMapper;

    @Autowired
    private KafkaProducerTemplate kafkaProducerTemplate;

    /**
     * 定时扫描延迟队列，处理超时订单
     * 每30秒执行一次
     * @deprecated 已废弃
     */
    // @Scheduled(fixedDelay = 1800000, initialDelay = 5000) // 已禁用：使用 OrderTimeoutHandler 替代
    @Deprecated
    public void scanExpiredOrders() {
        try {
            String delayQueueKey = TicketRedisKey.ORDER_DELAY_QUEUE;
            long currentTimestamp = System.currentTimeMillis() / 1000; // 当前时间戳（秒）

            // 从 ZSet 中获取 score <= 当前时间戳的订单（已过期）
            Set<String> expiredOrderIds = stringRedisTemplate.opsForZSet()
                    .rangeByScore(delayQueueKey, 0, currentTimestamp);

            if (expiredOrderIds == null || expiredOrderIds.isEmpty()) {
                log.debug("【延迟队列扫描】当前无超时订单");
                return;
            }

            log.info("【延迟队列扫描】发现 {} 个可能超时的订单", expiredOrderIds.size());

            // 批量处理超时订单
            for (String orderIdStr : expiredOrderIds) {
                try {
                    Long orderId = Long.parseLong(orderIdStr);

                    // 处理单个超时订单
                    boolean processed = handleExpiredOrder(orderId);

                    if (processed) {
                        // 处理成功，从延迟队列中移除
                        stringRedisTemplate.opsForZSet().remove(delayQueueKey, orderIdStr);
                        log.info("【订单超时处理成功】orderId={}, 已从延迟队列移除", orderId);
                    }

                } catch (Exception e) {
                    log.error("【订单超时处理失败】orderId={}, error={}",
                            orderIdStr, e.getMessage(), e);
                    // 处理失败，保留在队列中，下次继续尝试
                }
            }

        } catch (Exception e) {
            log.error("【延迟队列扫描异常】error={}", e.getMessage(), e);
        }
    }

    /**
     * 处理单个超时订单
     *
     * @param orderId 订单ID
     * @return 是否处理成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean handleExpiredOrder(Long orderId) {
        log.info("【订单超时处理】开始处理订单: orderId={}", orderId);

        try {
            // 1. 查询订单
            TicketOrder order = ticketOrderMapper.selectById(orderId);
            if (order == null) {
                log.warn("【订单超时处理】订单不存在: orderId={}", orderId);
                return true; // 订单不存在，视为已处理
            }

            // 2. 检查订单状态
            if (!OrderStatus.PENDING.equals(order.getStatus())) {
                log.info("【订单超时处理】订单状态非待支付，无需处理: orderId={}, status={}",
                        orderId, order.getStatus());
                return true; // 订单已支付或已取消，无需处理
            }

            // 3. 检查是否真的超时
            LocalDateTime now = LocalDateTime.now();
            if (now.isBefore(order.getExpireTime())) {
                log.warn("【订单超时处理】订单尚未超时，可能时间不准确: orderId={}, expireTime={}",
                        orderId, order.getExpireTime());
                return false; // 未超时，不处理
            }

            log.warn("【订单超时】orderId={}, orderNo={}, expireTime={}, 开始自动取消",
                    orderId, order.getOrderNo(), order.getExpireTime());

            // 4. 取消订单
            cancelExpiredOrder(order);

            // 5. 发送订单超时事件到 Kafka
            sendOrderTimeoutEvent(order);

            log.info("【订单超时处理完成】orderId={}, orderNo={}", orderId, order.getOrderNo());
            return true;

        } catch (Exception e) {
            log.error("【订单超时处理异常】orderId={}, error={}", orderId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 取消超时订单
     *
     * @param order 订单实体
     */
    private void cancelExpiredOrder(TicketOrder order) {
        // 1. 查询订单座位
        LambdaQueryWrapper<OrderSeat> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderSeat::getOrderId, order.getId());
        List<OrderSeat> orderSeats = orderSeatMapper.selectList(queryWrapper);
        List<Long> seatIds = orderSeats.stream()
                .map(OrderSeat::getSeatId)
                .collect(Collectors.toList());

        log.info("【订单取消】orderId={}, 需释放座位数={}", order.getId(), seatIds.size());

        // 2. 批量释放座位
        if (!seatIds.isEmpty()) {
            int releaseSeatResult = seatMapper.batchReleaseSeat(seatIds);
            log.info("【座位释放】orderId={}, 预期座位数={}, 实际释放数={}",
                    order.getId(), seatIds.size(), releaseSeatResult);
        }

        // 3. 更新演出活动座位数
        showEventMapper.releaseSeats(order.getShowEventId(), order.getSeatCount());
        log.info("【演出更新】showEventId={}, 锁定座位-{}, 可售座位+{}",
                order.getShowEventId(), order.getSeatCount(), order.getSeatCount());

        // 4. 更新订单状态为超时取消
        ticketOrderMapper.updateOrderToTimeout(order.getId());
        log.info("【订单状态更新】orderId={}, orderNo={}, 状态: PENDING -> TIMEOUT",
                order.getId(), order.getOrderNo());

        // 5. 减少用户购票记录
        userTicketRecordMapper.decreaseTicketCount(
                order.getUserId(),
                order.getShowEventId(),
                order.getSeatCount()
        );
        log.info("【购票记录】userId={}, showEventId={}, 购票数-{}",
                order.getUserId(), order.getShowEventId(), order.getSeatCount());

        // 6. 清除缓存
        clearShowEventCache(order.getShowEventId());
    }

    /**
     * 发送订单超时事件到 Kafka
     *
     * @param order 订单实体
     */
    private void sendOrderTimeoutEvent(TicketOrder order) {
        try {
            Map<String, Object> eventData = new HashMap<>();
            eventData.put("orderId", order.getId());
            eventData.put("orderNo", order.getOrderNo());
            eventData.put("userId", order.getUserId());
            eventData.put("showEventId", order.getShowEventId());
            eventData.put("seatCount", order.getSeatCount());
            eventData.put("expireTime", order.getExpireTime());
            eventData.put("timeoutTime", LocalDateTime.now());

            MessageEvent event = MessageEvent.builder()
                    .eventType(KafkaTopic.ORDER_TIMEOUT)
                    .source("OrderTimeoutScheduler")
                    .userId(order.getUserId())
                    .data(com.alibaba.fastjson.JSON.toJSONString(eventData))
                    .timestamp(LocalDateTime.now())
                    .createdAt(LocalDateTime.now())
                    .priority(8)
                    .build();

            kafkaProducerTemplate.sendEvent(KafkaTopic.ORDER_TIMEOUT, event);
            log.debug("【Kafka消息】topic={}, orderNo={}", KafkaTopic.ORDER_TIMEOUT, order.getOrderNo());

        } catch (Exception e) {
            log.warn("【Kafka发送失败】orderNo={}, error={}", order.getOrderNo(), e.getMessage());
        }
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
            log.warn("【缓存清除失败】showEventId={}, error={}", showEventId, e.getMessage());
        }
    }

    /**
     * 手动触发单个订单超时检查（用于测试或手动处理）
     *
     * @param orderId 订单ID
     * @return 处理结果
     */
    public String manualCheckOrder(Long orderId) {
        log.info("【手动触发】订单超时检查: orderId={}", orderId);
        boolean result = handleExpiredOrder(orderId);

        if (result) {
            // 从延迟队列中移除
            stringRedisTemplate.opsForZSet().remove(
                    TicketRedisKey.ORDER_DELAY_QUEUE,
                    orderId.toString()
            );
            return "订单处理成功";
        } else {
            return "订单处理失败";
        }
    }
}
