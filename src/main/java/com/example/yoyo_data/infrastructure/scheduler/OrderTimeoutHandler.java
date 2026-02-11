package com.example.yoyo_data.infrastructure.scheduler;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.yoyo_data.common.constant.KafkaTopic;
import com.example.yoyo_data.common.constant.OrderStatus;
import com.example.yoyo_data.common.constant.TicketRedisKey;
import com.example.yoyo_data.common.entity.OrderSeat;
import com.example.yoyo_data.common.entity.TicketOrder;
import com.example.yoyo_data.infrastructure.cache.RedisService;
import com.example.yoyo_data.infrastructure.message.KafkaProducerTemplate;
import com.example.yoyo_data.infrastructure.message.MessageEvent;
import com.example.yoyo_data.infrastructure.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RDelayedQueue;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 订单超时处理器 - Redisson RDelayedQueue 方案
 *
 * 技术特点：
 * 1. 使用 Redisson RDelayedQueue 实现延迟队列
 * 2. 基于 Redis ZSet，自动处理延迟消息
 * 3. 不需要定时任务扫描，消息到期自动推送到阻塞队列
 * 4. 分布式环境下只有一个实例能消费到消息（天然防止重复处理）
 * 5. 消费者线程阻塞等待，实时性高
 *
 * 工作原理：
 * 1. 订单创建时，将 orderId 加入延迟队列，延迟时间为 15 分钟
 * 2. 延迟时间到期后，消息自动从 RDelayedQueue 转移到 RBlockingQueue
 * 3. 消费者线程从 RBlockingQueue 阻塞获取消息
 * 4. 检查订单状态，如果是 PENDING，则取消订单并释放座位
 * 5. 发送订单超时事件到 Kafka（通知用户、记录日志）
 *
 * @author YoYo Data Team
 * @version 2.0 (Redisson Enhanced)
 * @since 2026-02-10
 */
@Slf4j
@Component
public class OrderTimeoutHandler implements CommandLineRunner {

    @Autowired
    private RedissonClient redissonClient;

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

    @Autowired
    private RedisService redisService;

    /**
     * 阻塞队列（接收延迟队列到期的消息）
     */
    private RBlockingQueue<Long> blockingQueue;

    /**
     * 延迟队列（用于添加延迟消息）
     */
    private RDelayedQueue<Long> delayedQueue;

    /**
     * 应用启动时初始化队列并启动消费者线程
     */
    @Override
    public void run(String... args) {
        try {
            log.info("【订单超时处理器】初始化 Redisson 延迟队列");

            // 初始化阻塞队列
            blockingQueue = redissonClient.getBlockingQueue(TicketRedisKey.ORDER_TIMEOUT_QUEUE);

            // 初始化延迟队列（基于阻塞队列）
            delayedQueue = redissonClient.getDelayedQueue(blockingQueue);

            // 启动消费者线程
            Thread consumerThread = new Thread(this::consumeExpiredOrders, "order-timeout-consumer");
            consumerThread.setDaemon(true); // 设置为守护线程，应用关闭时自动退出
            consumerThread.start();

            log.info("【订单超时处理器】启动成功，消费者线程已启动");

        } catch (Exception e) {
            log.error("【订单超时处理器】初始化失败", e);
        }
    }

    /**
     * 消费者线程：不断从阻塞队列获取超时订单
     * 该方法会一直阻塞等待，直到有消息到达
     */
    private void consumeExpiredOrders() {
        log.info("【订单超时消费者】开始监听延迟队列");

        while (!Thread.currentThread().isInterrupted()) {
            try {
                // 阻塞等待，直到有订单超时（take 会一直阻塞，直到有元素）
                Long orderId = blockingQueue.take();

                log.info("【延迟队列】收到超时订单: orderId={}", orderId);

                // 处理超时订单
                handleExpiredOrder(orderId);

            } catch (InterruptedException e) {
                log.warn("【订单超时消费者】线程被中断，退出监听");
                Thread.currentThread().interrupt();
                break;

            } catch (Exception e) {
                log.error("【订单超时消费者】处理异常: {}", e.getMessage(), e);
                // 继续监听，不中断消费者线程
            }
        }

        log.info("【订单超时消费者】停止监听");
    }

    /**
     * 添加订单到延迟队列
     * 在订单创建时调用
     *
     * @param orderId 订单ID
     * @param delayMinutes 延迟时间（分钟）
     */
    public void addOrderToDelayQueue(Long orderId, int delayMinutes) {
        try {
            // 将订单ID加入延迟队列，15分钟后自动推送到阻塞队列
            delayedQueue.offer(orderId, delayMinutes, TimeUnit.MINUTES);

            log.info("【延迟队列】订单已加入: orderId={}, delayMinutes={}", orderId, delayMinutes);

        } catch (Exception e) {
            log.error("【延迟队列】添加订单失败: orderId={}, error={}", orderId, e.getMessage(), e);
        }
    }

    /**
     * 从延迟队列移除订单
     * 在订单支付成功时调用，防止订单被错误取消
     *
     * @param orderId 订单ID
     * @return 是否移除成功
     */
    public boolean removeOrderFromDelayQueue(Long orderId) {
        try {
            // 从延迟队列中移除订单（支付成功后不需要超时处理）
            boolean removed = delayedQueue.remove(orderId);

            if (removed) {
                log.info("【延迟队列】订单已移除: orderId={} (原因: 订单已支付)", orderId);
            } else {
                log.warn("【延迟队列】订单移除失败: orderId={} (可能已超时或不存在)", orderId);
            }

            return removed;

        } catch (Exception e) {
            log.error("【延迟队列】移除订单失败: orderId={}, error={}", orderId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 处理单个超时订单
     *
     * @param orderId 订单ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void handleExpiredOrder(Long orderId) {
        log.info("【订单超时处理】开始处理: orderId={}", orderId);

        try {
            // 1. 查询订单
            TicketOrder order = ticketOrderMapper.selectById(orderId);
            if (order == null) {
                log.warn("【订单超时处理】订单不存在: orderId={}", orderId);
                return;
            }

            // 2. 检查订单状态（只处理待支付的订单）
            if (!OrderStatus.PENDING.equals(order.getStatus())) {
                log.info("【订单超时处理】订单状态非待支付，无需处理: orderId={}, status={}",
                        orderId, order.getStatus());
                return;
            }

            // 3. 检查是否真的超时（双重校验）
            LocalDateTime now = LocalDateTime.now();
            if (now.isBefore(order.getExpireTime())) {
                log.warn("【订单超时处理】订单尚未超时，延迟队列可能有误: orderId={}, expireTime={}, currentTime={}",
                        orderId, order.getExpireTime(), now);
                return;
            }

            log.warn("【订单超时】orderId={}, orderNo={}, expireTime={}, 开始自动取消",
                    orderId, order.getOrderNo(), order.getExpireTime());

            // 4. 取消订单
            cancelExpiredOrder(order);

            // 5. 发送订单超时事件到 Kafka
            sendOrderTimeoutEvent(order);

            log.info("【订单超时处理完成】orderId={}, orderNo={}", orderId, order.getOrderNo());

        } catch (Exception e) {
            log.error("【订单超时处理异常】orderId={}, error={}", orderId, e.getMessage(), e);
            throw new RuntimeException("订单超时处理失败", e);
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

        // 2. 批量释放座位（LOCKED → AVAILABLE）
        if (!seatIds.isEmpty()) {
            int releaseSeatResult = seatMapper.batchReleaseSeat(seatIds);
            log.info("【座位释放】orderId={}, 预期座位数={}, 实际释放数={}",
                    order.getId(), seatIds.size(), releaseSeatResult);
        }

        // 3. 更新演出活动座位数（locked → available）
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
            eventData.put("totalAmount", order.getTotalAmount());
            eventData.put("expireTime", order.getExpireTime());
            eventData.put("timeoutTime", LocalDateTime.now());

            MessageEvent event = MessageEvent.builder()
                    .eventType(KafkaTopic.ORDER_TIMEOUT)
                    .source("OrderTimeoutHandler")
                    .userId(order.getUserId())
                    .data(JSON.toJSONString(eventData))
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
     * 获取延迟队列大小（用于监控）
     *
     * @return 队列中待处理的订单数量
     */
    public int getDelayQueueSize() {
        try {
            return delayedQueue.size();
        } catch (Exception e) {
            log.error("【延迟队列】获取队列大小失败: {}", e.getMessage());
            return -1;
        }
    }

    /**
     * 获取阻塞队列大小（用于监控）
     *
     * @return 阻塞队列中待消费的订单数量
     */
    public int getBlockingQueueSize() {
        try {
            return blockingQueue.size();
        } catch (Exception e) {
            log.error("【阻塞队列】获取队列大小失败: {}", e.getMessage());
            return -1;
        }
    }
}
