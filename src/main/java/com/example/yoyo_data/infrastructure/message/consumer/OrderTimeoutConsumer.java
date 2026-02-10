package com.example.yoyo_data.infrastructure.message.consumer;

import com.alibaba.fastjson.JSON;
import com.example.yoyo_data.infrastructure.message.MessageEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.example.yoyo_data.common.constant.KafkaTopic.ORDER_TIMEOUT;

/**
 * 订单超时事件消费者
 * 处理订单超时后的后续业务：通知用户、记录日志、数据分析等
 *
 * @author YoYo Data Team
 * @version 1.0
 * @since 2026-02-10
 */
@Slf4j
@Component
public class OrderTimeoutConsumer {

    /**
     * 消费订单超时事件
     * 订单已经在延迟队列处理器中完成了取消操作，这里只处理后续业务
     *
     * @param message 消息内容
     */
    @KafkaListener(topics = ORDER_TIMEOUT, groupId = "order-timeout-consumer-group")
    public void handleOrderTimeoutEvent(String message) {
        log.info("【订单超时事件】收到消息: {}", message);

        try {
            // 解析消息
            MessageEvent messageEvent = JSON.parseObject(message, MessageEvent.class);
            Map<String, Object> eventData = JSON.parseObject(messageEvent.getData(), Map.class);

            Long orderId = Long.valueOf(eventData.get("orderId").toString());
            String orderNo = eventData.get("orderNo").toString();
            Long userId = Long.valueOf(eventData.get("userId").toString());

            log.info("【订单超时事件-处理开始】orderId={}, orderNo={}, userId={}",
                    orderId, orderNo, userId);

            // ========== 业务处理 ==========

            // 1. 发送用户通知（短信/推送/邮件）
            sendUserNotification(userId, orderNo);

            // 2. 记录订单超时日志（用于数据分析）
            recordTimeoutLog(orderId, orderNo, userId, eventData);

            // 3. 更新用户行为分析（可选）
            updateUserBehaviorAnalysis(userId, orderId);

            log.info("【订单超时事件-处理成功】orderId={}, orderNo={}", orderId, orderNo);

        } catch (Exception e) {
            log.error("【订单超时事件-处理失败】message={}, error={}",
                    message, e.getMessage(), e);
            // 不抛出异常，避免 Kafka 重试（订单取消已完成，通知失败不影响核心流程）
        }
    }

    /**
     * 发送用户通知
     *
     * @param userId 用户ID
     * @param orderNo 订单号
     */
    private void sendUserNotification(Long userId, String orderNo) {
        try {
            // TODO: 调用通知服务发送短信/推送
            log.info("【用户通知】订单超时未支付: userId={}, orderNo={}, 通知内容: 您的订单{}已超时取消，座位已释放",
                    userId, orderNo, orderNo);

            // 示例：调用短信服务
            // smsService.sendSms(userId, "您的订单" + orderNo + "已超时取消，座位已释放，如需购票请重新下单");

            // 示例：调用推送服务
            // pushService.sendPush(userId, "订单超时", "您的订单" + orderNo + "已超时取消");

        } catch (Exception e) {
            log.warn("【用户通知失败】userId={}, orderNo={}, error={}",
                    userId, orderNo, e.getMessage());
        }
    }

    /**
     * 记录订单超时日志（用于数据分析）
     *
     * @param orderId 订单ID
     * @param orderNo 订单号
     * @param userId 用户ID
     * @param eventData 事件数据
     */
    private void recordTimeoutLog(Long orderId, String orderNo, Long userId, Map<String, Object> eventData) {
        try {
            // TODO: 写入日志表或发送到数据分析系统
            log.info("【超时日志】orderId={}, orderNo={}, userId={}, showEventId={}, seatCount={}, expireTime={}",
                    orderId, orderNo, userId,
                    eventData.get("showEventId"),
                    eventData.get("seatCount"),
                    eventData.get("expireTime"));

            // 示例：写入超时日志表
//             orderTimeoutLogMapper.insert(OrderTimeoutLog.builder()
//                     .orderId(orderId)
//                     .orderNo(orderNo)
//                     .userId(userId)
//                     .timeoutReason("PAYMENT_TIMEOUT")
//                     .build());

        } catch (Exception e) {
            log.warn("【超时日志记录失败】orderId={}, error={}", orderId, e.getMessage());
        }
    }

    /**
     * 更新用户行为分析（可选）
     *
     * @param userId 用户ID
     * @param orderId 订单ID
     */
    private void updateUserBehaviorAnalysis(Long userId, Long orderId) {
        try {
            // TODO: 更新用户行为分析数据
            // 例如：记录用户下单但未支付的次数，用于用户画像分析
            log.debug("【行为分析】用户下单未支付: userId={}, orderId={}", userId, orderId);

            // 示例：更新用户统计表
            // userStatisticsMapper.incrementTimeoutCount(userId);

        } catch (Exception e) {
            log.warn("【行为分析更新失败】userId={}, error={}", userId, e.getMessage());
        }
    }
}
