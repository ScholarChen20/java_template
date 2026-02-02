package com.example.yoyo_data.data.message;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Kafka消费者模板 - 用于监听和处理来自Kafka的消息
 * 提供消息消费的基础框架和最佳实践
 *
 * 使用示例：
 * @KafkaListener(topics = "your-topic", groupId = "your-group")
 * public void consume(ConsumerRecord<String, String> record) {
 *     String message = record.value();
 *     // 处理消息
 * }
 *
 * @author Template Framework
 * @version 1.0
 */
@Slf4j
@Component
public class KafkaConsumerTemplate {

    /**
     * 消费通用消息（子类应重写此方法）
     *
     * @param message 消息内容
     */
    protected void consumeMessage(String message) {
        log.info("消费消息: {}", message);
    }

    /**
     * 消费事件消息（子类应重写此方法）
     *
     * @param event 事件对象
     */
    protected void consumeEvent(MessageEvent event) {
        log.info("消费事件: eventId={}, eventType={}", event.getEventId(), event.getEventType());
    }

    /**
     * 处理消费异常
     *
     * @param topic 主题
     * @param partition 分区
     * @param message 消息
     * @param exception 异常
     */
    protected void handleException(String topic, int partition, String message, Exception exception) {
        log.error("消费消息异常: topic={}, partition={}, message={}, exception={}",
                topic, partition, message, exception.getMessage(), exception);
    }

    /**
     * 解析JSON格式的事件消息
     *
     * @param message 消息内容
     * @return 事件对象，如果解析失败返回null
     */
    protected MessageEvent parseEvent(String message) {
        try {
            return JSON.parseObject(message, MessageEvent.class);
        } catch (Exception e) {
            log.error("事件消息解析失败: message={}", message, e);
            return null;
        }
    }

    /**
     * 解析JSON格式的对象
     *
     * @param message 消息内容
     * @param clazz 目标类型
     * @param <T> 泛型类型
     * @return 解析后的对象，如果解析失败返回null
     */
    protected <T> T parseObject(String message, Class<T> clazz) {
        try {
            return JSON.parseObject(message, clazz);
        } catch (Exception e) {
            log.error("对象消息解析失败: message={}, targetType={}", message, clazz.getSimpleName(), e);
            return null;
        }
    }

    /**
     * 创建一个重试策略
     * 用于处理消费失败时的重试逻辑
     *
     * @param retryCount 当前重试次数
     * @param maxRetries 最大重试次数
     * @return true: 应该重试, false: 不应该重试
     */
    protected boolean shouldRetry(int retryCount, int maxRetries) {
        return retryCount < maxRetries;
    }

    /**
     * 计算重试延迟时间（毫秒）
     * 使用指数退避策略：delay = baseDelay * 2^retryCount
     *
     * @param retryCount 当前重试次数（从0开始）
     * @param baseDelay 基础延迟时间（毫秒）
     * @return 延迟时间（毫秒）
     */
    protected long calculateRetryDelay(int retryCount, long baseDelay) {
        return baseDelay * (long) Math.pow(2, Math.min(retryCount, 5)); // 最多延迟baseDelay * 32倍
    }

    /**
     * 验证消息的完整性
     *
     * @param message 消息内容
     * @return true: 消息完整有效, false: 消息无效
     */
    protected boolean validateMessage(String message) {
        if (message == null || message.trim().isEmpty()) {
            log.warn("消息为空");
            return false;
        }
        return true;
    }

    /**
     * 验证事件的完整性
     *
     * @param event 事件对象
     * @return true: 事件完整有效, false: 事件无效
     */
    protected boolean validateEvent(MessageEvent event) {
        if (event == null) {
            log.warn("事件对象为空");
            return false;
        }
        if (event.getEventId() == null || event.getEventId().trim().isEmpty()) {
            log.warn("事件ID为空");
            return false;
        }
        if (event.getEventType() == null || event.getEventType().trim().isEmpty()) {
            log.warn("事件类型为空");
            return false;
        }
        return true;
    }
}
