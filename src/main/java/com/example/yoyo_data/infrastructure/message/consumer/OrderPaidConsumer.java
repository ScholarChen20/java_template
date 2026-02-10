package com.example.yoyo_data.infrastructure.message.consumer;

import com.alibaba.fastjson.JSON;
import com.example.yoyo_data.infrastructure.message.MessageEvent;
import com.example.yoyo_data.infrastructure.qrcode.QRCodeService;
import com.example.yoyo_data.infrastructure.repository.TicketOrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.example.yoyo_data.common.constant.KafkaTopic.ORDER_PAID;

/**
 * 订单支付成功事件消费者
 * 处理订单支付成功后的后续业务：生成电子票、用户通知、积分发放、数据统计等
 *
 * @author YoYo Data Team
 * @version 1.0
 * @since 2026-02-10
 */
@Slf4j
@Component
public class OrderPaidConsumer {

    @Autowired
    private QRCodeService qrCodeService;

    @Autowired
    private TicketOrderMapper ticketOrderMapper;

    /**
     * 消费订单支付成功事件
     * 订单已经在 payOrder 方法中完成了支付操作，这里只处理后续业务
     *
     * @param message 消息内容
     */
    @KafkaListener(topics = ORDER_PAID, groupId = "order-paid-consumer-group")
    public void handleOrderPaidEvent(String message) {
        log.info("【订单支付成功事件】收到消息: {}", message);

        try {
            // 解析消息
            MessageEvent messageEvent = JSON.parseObject(message, MessageEvent.class);
            Map<String, Object> eventData = JSON.parseObject(messageEvent.getData(), Map.class);

            Long orderId = Long.valueOf(eventData.get("orderId").toString());
            String orderNo = eventData.get("orderNo").toString();
            Long userId = Long.valueOf(eventData.get("userId").toString());
            String payType = eventData.get("payType").toString();

            log.info("【订单支付成功事件-处理开始】orderId={}, orderNo={}, userId={}, payType={}",
                    orderId, orderNo, userId, payType);

            // ========== 业务处理 ==========

            // 1. 生成电子票/二维码
            generateElectronicTicket(orderId, orderNo, userId, eventData);

            // 2. 发送支付成功通知（短信/推送/邮件）
            sendPaymentSuccessNotification(userId, orderNo, eventData);

            // 3. 发放用户积分
            grantUserPoints(userId, orderId, eventData);

            // 4. 记录支付成功日志（用于数据分析）
            recordPaymentLog(orderId, orderNo, userId, payType, eventData);

            // 5. 更新用户行为分析（支付成功率、偏好分析）
            updateUserBehaviorAnalysis(userId, orderId, payType);

            log.info("【订单支付成功事件-处理成功】orderId={}, orderNo={}", orderId, orderNo);

        } catch (Exception e) {
            log.error("【订单支付成功事件-处理失败】message={}, error={}",
                    message, e.getMessage(), e);
            // 不抛出异常，避免 Kafka 重试（支付已完成，后续业务失败不影响核心流程）
        }
    }

    /**
     * 生成电子票/二维码
     *
     * @param orderId 订单ID
     * @param orderNo 订单号
     * @param userId 用户ID
     * @param eventData 事件数据
     */
    private void generateElectronicTicket(Long orderId, String orderNo, Long userId, Map<String, Object> eventData) {
        try {
            log.info("【电子票生成】开始生成: orderId={}, orderNo={}, userId={}", orderId, orderNo, userId);

            // 1. 检查是否已生成过二维码（幂等性检查）
            // TODO: 可以添加查询数据库检查 qr_code_url 是否已有值
             if (ticketOrderMapper.selectById(orderId).getQrCodeUrl() != null) {
                 log.warn("【电子票已生成】orderId={}, 跳过重复生成", orderId);
                 return;
             }

            // 2. 调用二维码生成服务
            // 二维码内容：订单号（用于扫码验证）
            String qrCodeUrl = qrCodeService.generateOrderQRCode(orderNo);

            if (qrCodeUrl == null || qrCodeUrl.isEmpty()) {
                log.error("【电子票生成失败】二维码生成失败: orderId={}, orderNo={}", orderId, orderNo);
                return;
            }

            // 3. 更新订单表，保存二维码URL
            int updateResult = ticketOrderMapper.updateQRCodeUrl(orderId, qrCodeUrl);

            if (updateResult > 0) {
                log.info("【电子票生成成功】orderId={}, orderNo={}, qrCodeUrl={}", orderId, orderNo, qrCodeUrl);
            } else {
                log.error("【电子票生成失败】数据库更新失败: orderId={}, orderNo={}", orderId, orderNo);
            }

            // 4. 生成电子票PDF（可选，如果需要PDF格式的电子票）
            // TODO: 调用PDF生成服务
            // byte[] ticketPdf = ticketPDFService.generateTicketPDF(orderId);
            // String pdfUrl = ossService.uploadTicketPDF(orderNo, ticketPdf);
            // ticketOrderMapper.updatePDFUrl(orderId, pdfUrl);

        } catch (Exception e) {
            log.error("【电子票生成异常】orderId={}, orderNo={}, error={}", orderId, orderNo, e.getMessage(), e);
        }
    }

    /**
     * 发送支付成功通知
     *
     * @param userId 用户ID
     * @param orderNo 订单号
     * @param eventData 事件数据
     */
    private void sendPaymentSuccessNotification(Long userId, String orderNo, Map<String, Object> eventData) {
        try {
            // TODO: 调用通知服务发送短信/推送/邮件
            log.info("【支付成功通知】userId={}, orderNo={}, 通知内容: 您的订单{}支付成功，请凭电子票入场",
                    userId, orderNo, orderNo);

            // 示例：发送短信
            // String phone = userMapper.selectById(userId).getPhone();
            // smsService.sendSms(phone, "您的订单" + orderNo + "支付成功，请凭电子票入场。演出时间：" + showTime);

            // 示例：发送推送
            // pushService.sendPush(userId, "支付成功", "您的订单" + orderNo + "支付成功，电子票已生成");

            // 示例：发送邮件（包含电子票附件）
            // String email = userMapper.selectById(userId).getEmail();
            // emailService.sendTicketEmail(email, orderNo, ticketPdfUrl);

        } catch (Exception e) {
            log.warn("【支付成功通知失败】userId={}, orderNo={}, error={}",
                    userId, orderNo, e.getMessage());
        }
    }

    /**
     * 发放用户积分
     *
     * @param userId 用户ID
     * @param orderId 订单ID
     * @param eventData 事件数据
     */
    private void grantUserPoints(Long userId, Long orderId, Map<String, Object> eventData) {
        try {
            // TODO: 计算并发放积分
            Object totalAmountObj = eventData.get("totalAmount");
            if (totalAmountObj != null) {
                double totalAmount = Double.parseDouble(totalAmountObj.toString());
                int points = (int) (totalAmount / 10); // 每10元1积分

                log.info("【积分发放】userId={}, orderId={}, totalAmount={}, points={}",
                        userId, orderId, totalAmount, points);

                // 示例：更新用户积分
                // userPointsMapper.increasePoints(userId, points, orderId);
            }

        } catch (Exception e) {
            log.warn("【积分发放失败】userId={}, orderId={}, error={}",
                    userId, orderId, e.getMessage());
        }
    }

    /**
     * 记录支付成功日志（用于数据分析）
     *
     * @param orderId 订单ID
     * @param orderNo 订单号
     * @param userId 用户ID
     * @param payType 支付方式
     * @param eventData 事件数据
     */
    private void recordPaymentLog(Long orderId, String orderNo, Long userId, String payType, Map<String, Object> eventData) {
        try {
            // TODO: 写入支付日志表或发送到数据分析系统
            log.info("【支付日志】orderId={}, orderNo={}, userId={}, payType={}, totalAmount={}, payTime={}",
                    orderId, orderNo, userId, payType,
                    eventData.get("totalAmount"),
                    eventData.get("payTime"));

            // 示例：写入支付日志表
            // paymentLogMapper.insert(PaymentLog.builder()
            //         .orderId(orderId)
            //         .orderNo(orderNo)
            //         .userId(userId)
            //         .payType(payType)
            //         .totalAmount(new BigDecimal(eventData.get("totalAmount").toString()))
            //         .payTime(LocalDateTime.parse(eventData.get("payTime").toString()))
            //         .build());

            // 示例：发送到数据分析系统（Kafka/Flink）
            // dataAnalysisProducer.send("payment-analysis", eventData);

        } catch (Exception e) {
            log.warn("【支付日志记录失败】orderId={}, error={}", orderId, e.getMessage());
        }
    }

    /**
     * 更新用户行为分析（支付成功率、偏好分析）
     *
     * @param userId 用户ID
     * @param orderId 订单ID
     * @param payType 支付方式
     */
    private void updateUserBehaviorAnalysis(Long userId, Long orderId, String payType) {
        try {
            // TODO: 更新用户行为分析数据
            log.debug("【行为分析】用户支付成功: userId={}, orderId={}, payType={}", userId, orderId, payType);

            // 示例：更新用户统计表
            // userStatisticsMapper.incrementPaymentCount(userId);
            // userStatisticsMapper.updatePreferredPayType(userId, payType);

            // 示例：更新用户标签
            // if (isHighValueUser(userId)) {
            //     userTagMapper.addTag(userId, "高价值用户");
            // }

        } catch (Exception e) {
            log.warn("【行为分析更新失败】userId={}, error={}", userId, e.getMessage());
        }
    }
}
