package com.example.yoyo_data.infrastructure.message.consumer;

import com.alibaba.fastjson.JSON;
import com.example.yoyo_data.common.dto.OrderPaidPostProcessEvent;
import com.example.yoyo_data.infrastructure.cache.RedisService;
import com.example.yoyo_data.infrastructure.repository.SeatMapper;
import com.example.yoyo_data.infrastructure.repository.ShowEventMapper;
import com.example.yoyo_data.infrastructure.scheduler.OrderTimeoutHandler;
import com.example.yoyo_data.common.constant.TicketRedisKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import static com.example.yoyo_data.common.constant.KafkaTopic.ORDER_PAID_POST_PROCESS;

/**
 * 订单支付后置处理消费者
 * 异步处理支付成功后的座位确认、演出统计、缓存清理等操作
 *
 * 职责：
 * 1. 从延迟队列移除订单（防止超时取消）
 * 2. 确认座位为已售出（LOCKED → SOLD）
 * 3. 更新演出活动座位统计（locked → sold）
 * 4. 清除演出详情缓存
 *
 * 补偿机制：
 * - 支持重试（最多3次）
 * - 失败后发送到死信队列
 * - 定时任务定期修复数据不一致
 *
 * @author YoYo Data Team
 * @version 1.0
 * @since 2026-02-10
 */
@Slf4j
//@Component
@Deprecated
public class OrderPaidPostProcessConsumer {

    @Autowired
    private OrderTimeoutHandler orderTimeoutHandler;

    @Autowired
    private SeatMapper seatMapper;

    @Autowired
    private ShowEventMapper showEventMapper;

    @Autowired
    private RedisService redisService;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    /**
     * 消费订单支付后置处理事件
     *
     * @param message 消息内容
     */
    @KafkaListener(topics = ORDER_PAID_POST_PROCESS, groupId = "order-paid-post-process-group")
    @Transactional(rollbackFor = Exception.class)
    public void handleOrderPaidPostProcess(String message) {
        long startTime = System.currentTimeMillis();
        log.info("【订单支付后置处理】收到消息: {}", message);

        OrderPaidPostProcessEvent event = null;

        try {
            // 解析消息
            event = JSON.parseObject(message, OrderPaidPostProcessEvent.class);

            log.info("【订单支付后置处理-开始】orderId={}, orderNo={}, showEventId={}, seatCount={}",
                    event.getOrderId(), event.getOrderNo(), event.getShowEventId(), event.getSeatCount());

            // ========== 第7步：从延迟队列移除订单 ==========
            removeOrderFromDelayQueue(event);

            // ========== 第8步：批量确认座位为已售出 ==========
            confirmSeatsAsSold(event);

            // ========== 第9步：更新演出活动座位统计 ==========
            updateShowEventStats(event);

            // ========== 第10步：清除缓存 ==========
            clearShowEventCache(event);

            long totalTime = System.currentTimeMillis() - startTime;
            log.info("【订单支付后置处理-成功】orderId={}, orderNo={}, 耗时={}ms",
                    event.getOrderId(), event.getOrderNo(), totalTime);

        } catch (Exception e) {
            log.error("【订单支付后置处理-失败】message={}, error={}",
                    message, e.getMessage(), e);

            // 重试机制
            if (event != null && event.getRetryCount() < event.getMaxRetryCount()) {
                retryPostProcess(event);
            } else {
                // 达到最大重试次数，发送到死信队列
                sendToDeadLetterQueue(message, e);
            }

            // 抛出异常，让Kafka重新投递（如果配置了重试）
            throw new RuntimeException("订单支付后置处理失败: " + e.getMessage(), e);
        }
    }

    /**
     * 第7步：从延迟队列移除订单
     * 防止已支付的订单被误判为超时
     *
     * @param event 事件数据
     */
    private void removeOrderFromDelayQueue(OrderPaidPostProcessEvent event) {
        try {
            boolean removed = orderTimeoutHandler.removeOrderFromDelayQueue(event.getOrderId());

            if (removed) {
                log.info("【延迟队列-移除成功】orderId={}, orderNo={} (原因: 订单已支付)",
                        event.getOrderId(), event.getOrderNo());
            } else {
                log.warn("【延迟队列-移除失败】orderId={}, orderNo={} (可能已被处理或不存在)",
                        event.getOrderId(), event.getOrderNo());
            }

        } catch (Exception e) {
            log.error("【延迟队列-移除异常】orderId={}, orderNo={}, error={}",
                    event.getOrderId(), event.getOrderNo(), e.getMessage(), e);
            throw e; // 抛出异常，触发重试
        }
    }

    /**
     * 第8步：批量确认座位为已售出
     * 将座位状态从 LOCKED 更新为 SOLD
     *
     * @param event 事件数据
     */
    private void confirmSeatsAsSold(OrderPaidPostProcessEvent event) {
        try {
            int updateSeatResult = seatMapper.batchConfirmSeatSold(event.getSeatIds());

            if (updateSeatResult == event.getSeatIds().size()) {
                log.info("【座位确认-成功】orderId={}, 预期座位数={}, 实际更新数={}",
                        event.getOrderId(), event.getSeatIds().size(), updateSeatResult);
            } else {
                log.warn("【座位确认-部分成功】orderId={}, 预期座位数={}, 实际更新数={} (可能部分座位已被确认)",
                        event.getOrderId(), event.getSeatIds().size(), updateSeatResult);
            }

        } catch (Exception e) {
            log.error("【座位确认-失败】orderId={}, seatIds={}, error={}",
                    event.getOrderId(), event.getSeatIds(), e.getMessage(), e);
            throw e; // 抛出异常，触发重试
        }
    }

    /**
     * 第9步：更新演出活动座位统计
     * 将锁定座位数-N，已售座位数+N
     *
     * @param event 事件数据
     */
    private void updateShowEventStats(OrderPaidPostProcessEvent event) {
        try {
            int updateResult = showEventMapper.confirmSeats(event.getShowEventId(), event.getSeatCount());

            if (updateResult > 0) {
                log.info("【演出统计-更新成功】showEventId={}, 锁定座位-{}, 已售座位+{}",
                        event.getShowEventId(), event.getSeatCount(), event.getSeatCount());
            } else {
                log.warn("【演出统计-更新失败】showEventId={}, seatCount={} (可能演出不存在或已被更新)",
                        event.getShowEventId(), event.getSeatCount());
            }

        } catch (Exception e) {
            log.error("【演出统计-更新异常】showEventId={}, seatCount={}, error={}",
                    event.getShowEventId(), event.getSeatCount(), e.getMessage(), e);
            throw e; // 抛出异常，触发重试
        }
    }

    /**
     * 第10步：清除演出详情缓存
     * 保证缓存数据一致性
     *
     * @param event 事件数据
     */
    private void clearShowEventCache(OrderPaidPostProcessEvent event) {
        try {
            String cacheKey = TicketRedisKey.SHOW_DETAIL_PREFIX + event.getShowEventId();
            redisService.delete(cacheKey);
            log.info("【缓存清除-成功】showEventId={}, cacheKey={}",
                    event.getShowEventId(), cacheKey);

        } catch (Exception e) {
            // 缓存清除失败不影响主流程，不抛出异常
            log.warn("【缓存清除-失败】showEventId={}, error={}",
                    event.getShowEventId(), e.getMessage());
        }
    }

    /**
     * 重试后置处理
     *
     * @param event 事件数据
     */
    private void retryPostProcess(OrderPaidPostProcessEvent event) {
        try {
            event.setRetryCount(event.getRetryCount() + 1);

            // 延迟重试：第1次1秒，第2次5秒，第3次30秒
            long delayMs = event.getRetryCount() == 1 ? 1000L :
                          event.getRetryCount() == 2 ? 5000L : 30000L;

            Thread.sleep(delayMs);

            String retryMessage = JSON.toJSONString(event);
            kafkaTemplate.send(ORDER_PAID_POST_PROCESS, retryMessage);

            log.warn("【后置处理-重试】orderId={}, retryCount={}/{}, delayMs={}",
                    event.getOrderId(), event.getRetryCount(), event.getMaxRetryCount(), delayMs);

        } catch (Exception e) {
            log.error("【后置处理-重试失败】orderId={}, error={}",
                    event.getOrderId(), e.getMessage(), e);
        }
    }

    /**
     * 发送到死信队列
     *
     * @param message 原始消息
     * @param error 错误信息
     */
    private void sendToDeadLetterQueue(String message, Exception error) {
        try {
            String deadLetterTopic = ORDER_PAID_POST_PROCESS + "-dead-letter";
            kafkaTemplate.send(deadLetterTopic, message);

            log.error("【发送死信队列】topic={}, message={}, error={}",
                    deadLetterTopic, message, error.getMessage());

            // TODO: 发送告警通知运维人员

        } catch (Exception e) {
            log.error("【死信队列-发送失败】message={}, error={}",
                    message, e.getMessage(), e);
        }
    }
}
