package com.example.yoyo_data.infrastructure.message.consumer;

import com.alibaba.fastjson.JSON;
import com.example.yoyo_data.common.constant.KafkaTopic;
import com.example.yoyo_data.infrastructure.cache.RedisService;
import com.example.yoyo_data.infrastructure.message.KafkaConsumerTemplate;
import com.example.yoyo_data.infrastructure.message.MessageEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 对话事件消费者
 * 处理对话相关的Kafka事件
 */
@Slf4j
@Component
public class DialogEventConsumer extends KafkaConsumerTemplate {

    @Autowired
    private RedisService redisService;

    private static final String DIALOG_CACHE_PREFIX = "dialog:";
    private static final String DIALOG_LIST_CACHE_PREFIX = "dialog:list:";
    private static final String UNREAD_COUNT_PREFIX = "dialog:unread:";

    /**
     * 监听对话创建事件
     */
    @KafkaListener(topics = KafkaTopic.DIALOG_CREATED, groupId = "dialog-group")
    public void handleDialogCreated(ConsumerRecord<String, String> record) {
        try {
            String message = record.value();
            log.info("接收到对话创建事件: topic={}, partition={}, offset={}",
                    record.topic(), record.partition(), record.offset());

            if (!validateMessage(message)) {
                return;
            }

            MessageEvent event = parseEvent(message);
            if (!validateEvent(event)) {
                return;
            }

            // 解析事件数据
            Map<String, Object> eventData = JSON.parseObject(event.getData(), Map.class);
            String dialogId = (String) eventData.get("dialogId");
            Long userId = ((Number) eventData.get("userId")).longValue();
            Long recipientId = ((Number) eventData.get("recipientId")).longValue();

            // 处理业务逻辑
            // 1. 发送通知给双方用户
            log.info("对话创建成功通知: dialogId={}, userId={}, recipientId={}", dialogId, userId, recipientId);

            // 2. 初始化未读消息计数
            initUnreadCount(dialogId, userId, recipientId);

            consumeEvent(event);

        } catch (Exception e) {
            log.error("处理对话创建事件失败", e);
            handleException(record.topic(), record.partition(), record.value(), e);
        }
    }

    /**
     * 监听消息发送事件
     */
    @KafkaListener(topics = KafkaTopic.MESSAGE_SENT, groupId = "dialog-group")
    public void handleMessageSent(ConsumerRecord<String, String> record) {
        try {
            String message = record.value();
            log.info("接收到消息发送事件: topic={}, partition={}, offset={}",
                    record.topic(), record.partition(), record.offset());

            if (!validateMessage(message)) {
                return;
            }

            MessageEvent event = parseEvent(message);
            if (!validateEvent(event)) {
                return;
            }

            // 解析事件数据
            Map<String, Object> eventData = JSON.parseObject(event.getData(), Map.class);
            String dialogId = (String) eventData.get("dialogId");
            String messageId = (String) eventData.get("messageId");
            Long senderId = ((Number) eventData.get("senderId")).longValue();
            Long recipientId = ((Number) eventData.get("recipientId")).longValue();
            String content = (String) eventData.get("content");

            // 处理业务逻辑
            // 1. 发送实时通知给接收者
            sendNotificationToRecipient(recipientId, senderId, content);

            // 2. 更新未读消息计数
            incrementUnreadCount(dialogId, recipientId);

            // 3. 清除对话缓存
            clearDialogCache(dialogId, senderId, recipientId);

            // 4. 记录消息统计
            log.info("消息发送成功: dialogId={}, messageId={}, senderId={}, recipientId={}",
                    dialogId, messageId, senderId, recipientId);

            consumeEvent(event);

        } catch (Exception e) {
            log.error("处理消息发送事件失败", e);
            handleException(record.topic(), record.partition(), record.value(), e);
        }
    }

    /**
     * 监听消息状态更新事件
     */
    @KafkaListener(topics = KafkaTopic.MESSAGE_STATUS_UPDATED, groupId = "dialog-group")
    public void handleMessageStatusUpdated(ConsumerRecord<String, String> record) {
        try {
            String message = record.value();
            log.info("接收到消息状态更新事件: topic={}, partition={}, offset={}",
                    record.topic(), record.partition(), record.offset());

            if (!validateMessage(message)) {
                return;
            }

            MessageEvent event = parseEvent(message);
            if (!validateEvent(event)) {
                return;
            }

            // 解析事件数据
            Map<String, Object> eventData = JSON.parseObject(event.getData(), Map.class);
            String dialogId = (String) eventData.get("dialogId");
            Long userId = ((Number) eventData.get("userId")).longValue();

            // 处理业务逻辑
            // 1. 清除未读消息计数
            clearUnreadCount(dialogId, userId);

            // 2. 清除对话缓存
            clearDialogCacheByUser(dialogId, userId);

            log.info("消息状态更新成功: dialogId={}, userId={}", dialogId, userId);

            consumeEvent(event);

        } catch (Exception e) {
            log.error("处理消息状态更新事件失败", e);
            handleException(record.topic(), record.partition(), record.value(), e);
        }
    }

    /**
     * 监听对话归档事件
     */
    @KafkaListener(topics = KafkaTopic.DIALOG_ARCHIVED, groupId = "dialog-group")
    public void handleDialogArchived(ConsumerRecord<String, String> record) {
        try {
            String message = record.value();
            log.info("接收到对话归档事件: topic={}, partition={}, offset={}",
                    record.topic(), record.partition(), record.offset());

            if (!validateMessage(message)) {
                return;
            }

            MessageEvent event = parseEvent(message);
            if (!validateEvent(event)) {
                return;
            }

            // 解析事件数据
            Map<String, Object> eventData = JSON.parseObject(event.getData(), Map.class);
            String dialogId = (String) eventData.get("dialogId");
            Long userId = ((Number) eventData.get("userId")).longValue();

            // 处理业务逻辑
            // 1. 清除对话缓存
            clearDialogCacheByUser(dialogId, userId);

            log.info("对话归档成功: dialogId={}, userId={}", dialogId, userId);

            consumeEvent(event);

        } catch (Exception e) {
            log.error("处理对话归档事件失败", e);
            handleException(record.topic(), record.partition(), record.value(), e);
        }
    }

    /**
     * 初始化未读消息计数
     */
    private void initUnreadCount(String dialogId, Long userId, Long recipientId) {
        try {
            String userUnreadKey = UNREAD_COUNT_PREFIX + dialogId + ":" + userId;
            String recipientUnreadKey = UNREAD_COUNT_PREFIX + dialogId + ":" + recipientId;

            redisService.stringSetString(userUnreadKey, "0", 86400000L); // 24小时
            redisService.stringSetString(recipientUnreadKey, "0", 86400000L); // 24小时

            log.debug("初始化未读消息计数: dialogId={}", dialogId);

        } catch (Exception e) {
            log.error("初始化未读消息计数失败: dialogId={}", dialogId, e);
        }
    }

    /**
     * 增加未读消息计数
     */
    private void incrementUnreadCount(String dialogId, Long recipientId) {
        try {
            String unreadKey = UNREAD_COUNT_PREFIX + dialogId + ":" + recipientId;
            redisService.increment(unreadKey);
            log.debug("增加未读消息计数: dialogId={}, recipientId={}", dialogId, recipientId);

        } catch (Exception e) {
            log.error("增加未读消息计数失败: dialogId={}", dialogId, e);
        }
    }

    /**
     * 清除未读消息计数
     */
    private void clearUnreadCount(String dialogId, Long userId) {
        try {
            String unreadKey = UNREAD_COUNT_PREFIX + dialogId + ":" + userId;
            redisService.stringSetString(unreadKey, "0", 86400000L); // 24小时
            log.debug("清除未读消息计数: dialogId={}, userId={}", dialogId, userId);

        } catch (Exception e) {
            log.error("清除未读消息计数失败: dialogId={}", dialogId, e);
        }
    }

    /**
     * 发送通知给接收者
     */
    private void sendNotificationToRecipient(Long recipientId, Long senderId, String content) {
        try {
            // 实际应该通过WebSocket或推送服务发送实时通知
            // 这里仅记录日志
            log.info("发送消息通知: recipientId={}, senderId={}, content={}", recipientId, senderId, content);

            // 可以将通知事件发送到通知主题
            // kafkaProducerTemplate.sendEvent(KafkaTopic.USER_NOTIFICATION, notificationEvent);

        } catch (Exception e) {
            log.error("发送通知失败: recipientId={}", recipientId, e);
        }
    }

    /**
     * 清除对话缓存
     */
    private void clearDialogCache(String dialogId, Long senderId, Long recipientId) {
        try {
            // 清除对话详情缓存
            String cacheKey = DIALOG_CACHE_PREFIX + dialogId;
            redisService.delete(cacheKey);

            // 清除双方的对话列表缓存
            clearListCache(senderId);
            clearListCache(recipientId);

            log.debug("清除对话缓存: dialogId={}", dialogId);

        } catch (Exception e) {
            log.error("清除对话缓存失败: dialogId={}", dialogId, e);
        }
    }

    /**
     * 清除对话缓存(按用户)
     */
    private void clearDialogCacheByUser(String dialogId, Long userId) {
        try {
            // 清除对话详情缓存
            String cacheKey = DIALOG_CACHE_PREFIX + dialogId;
            redisService.delete(cacheKey);

            // 清除用户的对话列表缓存
            clearListCache(userId);

            log.debug("清除对话缓存: dialogId={}, userId={}", dialogId, userId);

        } catch (Exception e) {
            log.error("清除对话缓存失败: dialogId={}", dialogId, e);
        }
    }

    /**
     * 清除列表缓存
     */
    private void clearListCache(Long userId) {
        try {
            java.util.Set<String> keys = redisService.keys(DIALOG_LIST_CACHE_PREFIX + userId + ":*");
            if (keys != null && !keys.isEmpty()) {
                redisService.delete(keys);
            }
        } catch (Exception e) {
            log.error("清除对话列表缓存失败: userId={}", userId, e);
        }
    }
}
