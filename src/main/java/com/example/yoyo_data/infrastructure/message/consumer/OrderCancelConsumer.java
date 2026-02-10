package com.example.yoyo_data.infrastructure.message.consumer;

import com.alibaba.fastjson.JSON;
import com.example.yoyo_data.infrastructure.message.MessageEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.example.yoyo_data.common.constant.KafkaTopic.ORDER_CANCEL;

/**
 * 订单取消事件消费者
 * 处理订单取消后的后续业务：用户通知、优惠券退还、数据统计、行为分析等
 *
 * @author YoYo Data Team
 * @version 1.0
 * @since 2026-02-10
 */
@Slf4j
@Component
public class OrderCancelConsumer {

    /**
     * 消费订单取消事件
     * 订单已经在 cancelOrder 方法中完成了取消操作，这里只处理后续业务
     *
     * @param message 消息内容
     */
    @KafkaListener(topics = ORDER_CANCEL, groupId = "order-cancel-consumer-group")
    public void handleOrderCancelEvent(String message) {
        log.info("【订单取消事件】收到消息: {}", message);

        try {
            // 解析消息
            MessageEvent messageEvent = JSON.parseObject(message, MessageEvent.class);
            Map<String, Object> eventData = JSON.parseObject(messageEvent.getData(), Map.class);

            Long orderId = Long.valueOf(eventData.get("orderId").toString());
            String orderNo = eventData.get("orderNo").toString();
            Long userId = Long.valueOf(eventData.get("userId").toString());
            Integer seatCount = Integer.valueOf(eventData.get("seatCount").toString());

            log.info("【订单取消事件-处理开始】orderId={}, orderNo={}, userId={}, seatCount={}",
                    orderId, orderNo, userId, seatCount);

            // ========== 业务处理 ==========

            // 1. 发送订单取消通知
            sendCancelNotification(userId, orderNo, eventData);

            // 2. 记录订单取消日志（用于数据分析）
            recordCancelLog(orderId, orderNo, userId, eventData);

            // 3. 更新用户行为分析（取消率分析）
            updateUserBehaviorAnalysis(userId, orderId);

            log.info("【订单取消事件-处理成功】orderId={}, orderNo={}", orderId, orderNo);

        } catch (Exception e) {
            log.error("【订单取消事件-处理失败】message={}, error={}",
                    message, e.getMessage(), e);
            // 不抛出异常，避免 Kafka 重试（订单取消已完成，后续业务失败不影响核心流程）
        }
    }

    /**
     * 发送订单取消通知
     *
     * @param userId 用户ID
     * @param orderNo 订单号
     * @param eventData 事件数据
     */
    private void sendCancelNotification(Long userId, String orderNo, Map<String, Object> eventData) {
        try {
            // TODO: 调用通知服务发送短信/推送
            log.info("【取消通知】userId={}, orderNo={}, 通知内容: 您的订单{}已取消，座位已释放",
                    userId, orderNo, orderNo);

            // 示例：发送短信
            // String phone = userMapper.selectById(userId).getPhone();
            // smsService.sendSms(phone, "您的订单" + orderNo + "已取消，座位已释放。如需购票请重新下单。");

            // 示例：发送推送
            // pushService.sendPush(userId, "订单取消", "您的订单" + orderNo + "已取消，座位已释放");

        } catch (Exception e) {
            log.warn("【取消通知失败】userId={}, orderNo={}, error={}",
                    userId, orderNo, e.getMessage());
        }
    }

    /**
     * 退还优惠券（如果使用了）
     *
     * @param userId 用户ID
     * @param orderId 订单ID
     * @param eventData 事件数据
     */
    private void refundCoupon(Long userId, Long orderId, Map<String, Object> eventData) {
        try {
            // TODO: 查询订单是否使用了优惠券，如果使用了则退还
            log.debug("【优惠券检查】检查订单是否使用了优惠券: userId={}, orderId={}", userId, orderId);

            // 示例：查询订单优惠券
            // OrderCoupon orderCoupon = orderCouponMapper.selectByOrderId(orderId);
            // if (orderCoupon != null) {
            //     // 退还优惠券
            //     couponMapper.refundCoupon(userId, orderCoupon.getCouponId());
            //     log.info("【优惠券退还】userId={}, orderId={}, couponId={}",
            //             userId, orderId, orderCoupon.getCouponId());
            // }

        } catch (Exception e) {
            log.warn("【优惠券退还失败】userId={}, orderId={}, error={}",
                    userId, orderId, e.getMessage());
        }
    }

    /**
     * 记录订单取消日志（用于数据分析）
     *
     * @param orderId 订单ID
     * @param orderNo 订单号
     * @param userId 用户ID
     * @param eventData 事件数据
     */
    private void recordCancelLog(Long orderId, String orderNo, Long userId, Map<String, Object> eventData) {
        try {
            // TODO: 写入取消日志表或发送到数据分析系统
            log.info("【取消日志】orderId={}, orderNo={}, userId={}, seatCount={}",
                    orderId, orderNo, userId, eventData.get("seatCount"));

            // 示例：写入取消日志表
            // orderCancelLogMapper.insert(OrderCancelLog.builder()
            //         .orderId(orderId)
            //         .orderNo(orderNo)
            //         .userId(userId)
            //         .cancelReason("USER_CANCEL") // 用户主动取消
            //         .cancelTime(LocalDateTime.now())
            //         .build());

            // 示例：发送到数据分析系统
            // dataAnalysisProducer.send("order-cancel-analysis", eventData);

        } catch (Exception e) {
            log.warn("【取消日志记录失败】orderId={}, error={}", orderId, e.getMessage());
        }
    }

    /**
     * 更新用户行为分析（取消率分析）
     *
     * @param userId 用户ID
     * @param orderId 订单ID
     */
    private void updateUserBehaviorAnalysis(Long userId, Long orderId) {
        try {
            // TODO: 更新用户行为分析数据
            log.debug("【行为分析】用户取消订单: userId={}, orderId={}", userId, orderId);

            // 示例：更新用户统计表
            // userStatisticsMapper.incrementCancelCount(userId);

            // 示例：计算用户取消率
            // int totalOrders = orderMapper.countByUserId(userId);
            // int cancelCount = orderMapper.countCancelByUserId(userId);
            // double cancelRate = (double) cancelCount / totalOrders;
            // userStatisticsMapper.updateCancelRate(userId, cancelRate);

            // 示例：标记高取消率用户
            // if (cancelRate > 0.5) {
            //     userTagMapper.addTag(userId, "高取消率用户");
            // }

        } catch (Exception e) {
            log.warn("【行为分析更新失败】userId={}, error={}", userId, e.getMessage());
        }
    }
}
