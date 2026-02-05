package com.example.yoyo_data.infrastructure.message;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Kafka生产者模板 - 用于发送消息到Kafka
 * 提供简化的消息发送接口
 *
 * @author Template Framework
 * @version 1.0
 */
@Slf4j
@Component
public class KafkaProducerTemplate {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    /**
     * 发送消息到指定主题
     *
     * @param topic 主题名称
     * @param message 消息内容
     * @return 是否发送成功
     */
    public boolean sendMessage(String topic, String message) {
        try {
            kafkaTemplate.send(topic, message);
            log.info("消息发送成功: topic={}, message={}", topic, message);
            return true;
        } catch (Exception e) {
            log.error("消息发送失败: topic={}, message={}", topic, message, e);
            return false;
        }
    }

    /**
     * 发送消息到指定主题和分区
     *
     * @param topic 主题名称
     * @param partition 分区号
     * @param key 消息键
     * @param message 消息内容
     * @return 是否发送成功
     */
    public boolean sendMessage(String topic, int partition, String key, String message) {
        try {
            kafkaTemplate.send(topic, partition, key, message);
            log.info("消息发送成功: topic={}, partition={}, key={}, message={}",
                    topic, partition, key, message);
            return true;
        } catch (Exception e) {
            log.error("消息发送失败: topic={}, partition={}, key={}, message={}",
                    topic, partition, key, message, e);
            return false;
        }
    }

    /**
     * 发送事件消息
     *
     * @param topic 主题名称
     * @param event 事件对象
     * @return 是否发送成功
     */
    public boolean sendEvent(String topic, MessageEvent event) {
        try {
            if (event.getEventId() == null) {
                event.setEventId(UUID.randomUUID().toString());
            }
            String message = JSON.toJSONString(event);
            kafkaTemplate.send(topic, event.getEventId(), message);
            log.info("事件消息发送成功: topic={}, eventId={}, eventType={}",
                    topic, event.getEventId(), event.getEventType());
            return true;
        } catch (Exception e) {
            log.error("事件消息发送失败: topic={}, eventId={}",
                    topic, event.getEventId(), e);
            return false;
        }
    }

    /**
     * 发送事件消息到指定分区
     *
     * @param topic 主题名称
     * @param partition 分区号
     * @param event 事件对象
     * @return 是否发送成功
     */
    public boolean sendEvent(String topic, int partition, MessageEvent event) {
        try {
            if (event.getEventId() == null) {
                event.setEventId(UUID.randomUUID().toString());
            }
            String message = JSON.toJSONString(event);
            kafkaTemplate.send(topic, partition, event.getEventId(), message);
            log.info("事件消息发送成功: topic={}, partition={}, eventId={}, eventType={}",
                    topic, partition, event.getEventId(), event.getEventType());
            return true;
        } catch (Exception e) {
            log.error("事件消息发送失败: topic={}, partition={}, eventId={}",
                    topic, partition, event.getEventId(), e);
            return false;
        }
    }

    /**
     * 发送对象消息（自动序列化为JSON）
     *
     * @param topic 主题名称
     * @param key 消息键
     * @param object 对象
     * @return 是否发送成功
     */
    public boolean sendObject(String topic, String key, Object object) {
        try {
            String message = JSON.toJSONString(object);
            kafkaTemplate.send(topic, key, message);
            log.info("对象消息发送成功: topic={}, key={}, objectType={}",
                    topic, key, object.getClass().getSimpleName());
            return true;
        } catch (Exception e) {
            log.error("对象消息发送失败: topic={}, key={}", topic, key, e);
            return false;
        }
    }

    /**
     * 获取KafkaTemplate实例（如需直接使用）
     *
     * @return KafkaTemplate实例
     */
    public KafkaTemplate<String, String> getKafkaTemplate() {
        return kafkaTemplate;
    }
}
