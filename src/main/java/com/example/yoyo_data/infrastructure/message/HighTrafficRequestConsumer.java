package com.example.yoyo_data.infrastructure.message;

import com.example.yoyo_data.infrastructure.service.HighTrafficRequestService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 高并发请求Kafka消费者
 * 用于处理来自Kafka队列的高并发请求消息
 *
 * @author Template Framework
 * @version 1.0
 */
@Slf4j
@Component
public class HighTrafficRequestConsumer extends KafkaConsumerTemplate {

    @Autowired
    private HighTrafficRequestService highTrafficRequestService;

    /**
     * 消费高并发请求消息
     *
     * @param record 消息记录
     * @param acknowledgment 确认机制
     */
    @KafkaListener(
            topics = "high-traffic-requests",
            groupId = "high-traffic-consumer-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeHighTrafficRequest(ConsumerRecord<String, String> record, Acknowledgment acknowledgment) {
        String message = record.value();
        int partition = record.partition();
        long offset = record.offset();
        String topic = record.topic();

        log.info("接收到高并发请求消息: topic={}, partition={}, offset={}, message={}",
                topic, partition, offset, message);

        try {
            // 验证消息完整性
            if (!validateMessage(message)) {
                log.warn("消息不完整，跳过处理: topic={}, partition={}, offset={}",
                        topic, partition, offset);
                acknowledgment.acknowledge();
                return;
            }

            // 解析事件消息
            MessageEvent event = parseEvent(message);
            if (event == null) {
                log.warn("事件消息解析失败，跳过处理: topic={}, partition={}, offset={}",
                        topic, partition, offset);
                acknowledgment.acknowledge();
                return;
            }

            // 验证事件完整性
            if (!validateEvent(event)) {
                log.warn("事件不完整，跳过处理: topic={}, partition={}, offset={}, eventId={}",
                        topic, partition, offset, event.getEventId());
                acknowledgment.acknowledge();
                return;
            }

            // 记录处理开始时间
            LocalDateTime startTime = LocalDateTime.now();

            // 处理事件
            boolean processed = highTrafficRequestService.processRequest(event);

            // 记录处理结束时间
            LocalDateTime endTime = LocalDateTime.now();
            long processingTime = Duration.between(startTime, endTime).toMillis();

            if (processed) {
                log.info("高并发请求处理成功: eventId={}, requestId={}, processingTime={}ms",
                        event.getEventId(), event.getRequestId(), processingTime);
            } else {
                log.warn("高并发请求处理失败: eventId={}, requestId={}, processingTime={}ms",
                        event.getEventId(), event.getRequestId(), processingTime);
            }

            // 确认消息消费完成
            acknowledgment.acknowledge();

        } catch (Exception e) {
            // 处理消费异常
            handleException(topic, partition, message, e);
            
            // 确认消息（避免消息重复消费）
            acknowledgment.acknowledge();
        }
    }

    /**
     * 消费重试队列中的消息
     *
     * @param record 消息记录
     * @param acknowledgment 确认机制
     */
    @KafkaListener(
            topics = "high-traffic-requests-retry",
            groupId = "high-traffic-retry-consumer-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeRetryHighTrafficRequest(ConsumerRecord<String, String> record, Acknowledgment acknowledgment) {
        String message = record.value();
        int partition = record.partition();
        long offset = record.offset();
        String topic = record.topic();

        log.info("接收到重试高并发请求消息: topic={}, partition={}, offset={}, message={}",
                topic, partition, offset, message);

        try {
            // 解析事件消息
            MessageEvent event = parseEvent(message);
            if (event == null) {
                log.warn("重试事件消息解析失败，跳过处理: topic={}, partition={}, offset={}",
                        topic, partition, offset);
                acknowledgment.acknowledge();
                return;
            }

            // 检查是否需要重试
            if (!event.shouldRetry()) {
                log.info("事件不需要重试，跳过处理: eventId={}, retryCount={}, maxRetries={}",
                        event.getEventId(), event.getRetryCount(), event.getMaxRetries());
                acknowledgment.acknowledge();
                return;
            }

            // 增加重试次数
            event.incrementRetryCount();

            // 处理事件
            boolean processed = highTrafficRequestService.processRequest(event);

            if (processed) {
                log.info("重试高并发请求处理成功: eventId={}, requestId={}, retryCount={}",
                        event.getEventId(), event.getRequestId(), event.getRetryCount());
            } else {
                log.warn("重试高并发请求处理失败: eventId={}, requestId={}, retryCount={}",
                        event.getEventId(), event.getRequestId(), event.getRetryCount());
            }

            // 确认消息消费完成
            acknowledgment.acknowledge();

        } catch (Exception e) {
            // 处理消费异常
            handleException(topic, partition, message, e);
            
            // 确认消息
            acknowledgment.acknowledge();
        }
    }

    /**
     * 消费死信队列中的消息
     *
     * @param record 消息记录
     * @param acknowledgment 确认机制
     */
    @KafkaListener(
            topics = "high-traffic-requests-dlq",
            groupId = "high-traffic-dlq-consumer-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeDeadLetterHighTrafficRequest(ConsumerRecord<String, String> record, Acknowledgment acknowledgment) {
        String message = record.value();
        int partition = record.partition();
        long offset = record.offset();
        String topic = record.topic();

        log.warn("接收到死信队列高并发请求消息: topic={}, partition={}, offset={}, message={}",
                topic, partition, offset, message);

        try {
            // 解析事件消息
            MessageEvent event = parseEvent(message);
            if (event == null) {
                log.error("死信队列事件消息解析失败: topic={}, partition={}, offset={}",
                        topic, partition, offset);
                acknowledgment.acknowledge();
                return;
            }

            // 记录死信消息
            log.error("死信队列消息详情: eventId={}, eventType={}, requestId={}, errorMessage={}, retryCount={}, maxRetries={}",
                    event.getEventId(), event.getEventType(), event.getRequestId(),
                    event.getErrorMessage(), event.getRetryCount(), event.getMaxRetries());

            // 可以在这里添加死信消息处理逻辑，例如：
            // 1. 存储到数据库
            // 2. 发送告警通知
            // 3. 人工处理

            // 确认消息消费完成
            acknowledgment.acknowledge();

        } catch (Exception e) {
            // 处理消费异常
            handleException(topic, partition, message, e);
            
            // 确认消息
            acknowledgment.acknowledge();
        }
    }
}