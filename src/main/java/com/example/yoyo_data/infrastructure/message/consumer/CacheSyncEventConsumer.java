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
 * 缓存同步事件消费者
 * 处理缓存失效和数据同步相关的Kafka事件
 */
@Slf4j
@Component
public class CacheSyncEventConsumer extends KafkaConsumerTemplate {

    @Autowired
    private RedisService redisService;

    /**
     * 监听缓存失效通知
     */
    @KafkaListener(topics = KafkaTopic.CACHE_INVALIDATION, groupId = "cache-sync-group")
    public void handleCacheInvalidation(ConsumerRecord<String, String> record) {
        try {
            String message = record.value();
            log.info("接收到缓存失效通知: topic={}, partition={}, offset={}",
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
            String cacheKey = (String) eventData.get("cacheKey");
            String cachePattern = (String) eventData.get("cachePattern");

            // 处理缓存失效
            if (cacheKey != null) {
                // 删除单个缓存key
                redisService.delete(cacheKey);
                log.info("删除缓存key成功: {}", cacheKey);
            } else if (cachePattern != null) {
                // 删除匹配模式的缓存key
                java.util.Set<String> keys = redisService.keys(cachePattern);
                if (keys != null && !keys.isEmpty()) {
                    redisService.delete(keys);
                    log.info("批量删除缓存key成功: pattern={}, count={}", cachePattern, keys.size());
                }
            }

            consumeEvent(event);

        } catch (Exception e) {
            log.error("处理缓存失效通知失败", e);
            handleException(record.topic(), record.partition(), record.value(), e);
        }
    }

    /**
     * 监听数据同步事件
     */
    @KafkaListener(topics = KafkaTopic.DATA_SYNC, groupId = "cache-sync-group")
    public void handleDataSync(ConsumerRecord<String, String> record) {
        try {
            String message = record.value();
            log.info("接收到数据同步事件: topic={}, partition={}, offset={}",
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
            String dataType = (String) eventData.get("dataType");
            String operation = (String) eventData.get("operation");
            Object data = eventData.get("data");

            // 处理数据同步
            log.info("数据同步处理: dataType={}, operation={}", dataType, operation);

            switch (operation) {
                case "CREATE":
                    handleDataCreate(dataType, data);
                    break;
                case "UPDATE":
                    handleDataUpdate(dataType, data);
                    break;
                case "DELETE":
                    handleDataDelete(dataType, data);
                    break;
                default:
                    log.warn("未知的数据同步操作: {}", operation);
            }

            consumeEvent(event);

        } catch (Exception e) {
            log.error("处理数据同步事件失败", e);
            handleException(record.topic(), record.partition(), record.value(), e);
        }
    }

    /**
     * 监听旅行计划缓存同步
     */
    @KafkaListener(topics = KafkaTopic.TRAVEL_PLAN_CACHE_SYNC, groupId = "cache-sync-group")
    public void handleTravelPlanCacheSync(ConsumerRecord<String, String> record) {
        try {
            String message = record.value();
            log.info("接收到旅行计划缓存同步事件: topic={}, partition={}, offset={}",
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
            String planId = (String) eventData.get("planId");
            Long userId = ((Number) eventData.get("userId")).longValue();

            // 清除旅行计划相关缓存
            String planCacheKey = "travel_plan:" + planId;
            redisService.delete(planCacheKey);

            String listCachePattern = "travel_plan:list:" + userId + ":*";
            java.util.Set<String> keys = redisService.keys(listCachePattern);
            if (keys != null && !keys.isEmpty()) {
                redisService.delete(keys);
            }

            log.info("旅行计划缓存同步完成: planId={}, userId={}", planId, userId);

            consumeEvent(event);

        } catch (Exception e) {
            log.error("处理旅行计划缓存同步事件失败", e);
            handleException(record.topic(), record.partition(), record.value(), e);
        }
    }

    /**
     * 监听对话缓存同步
     */
    @KafkaListener(topics = KafkaTopic.DIALOG_CACHE_SYNC, groupId = "cache-sync-group")
    public void handleDialogCacheSync(ConsumerRecord<String, String> record) {
        try {
            String message = record.value();
            log.info("接收到对话缓存同步事件: topic={}, partition={}, offset={}",
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

            // 清除对话相关缓存
            String dialogCacheKey = "dialog:" + dialogId;
            redisService.delete(dialogCacheKey);

            String listCachePattern = "dialog:list:" + userId + ":*";
            java.util.Set<String> keys = redisService.keys(listCachePattern);
            if (keys != null && !keys.isEmpty()) {
                redisService.delete(keys);
            }

            log.info("对话缓存同步完成: dialogId={}, userId={}", dialogId, userId);

            consumeEvent(event);

        } catch (Exception e) {
            log.error("处理对话缓存同步事件失败", e);
            handleException(record.topic(), record.partition(), record.value(), e);
        }
    }

    /**
     * 处理数据创建
     */
    private void handleDataCreate(String dataType, Object data) {
        log.info("处理数据创建: dataType={}, data={}", dataType, data);
        // 实现具体的数据创建逻辑
    }

    /**
     * 处理数据更新
     */
    private void handleDataUpdate(String dataType, Object data) {
        log.info("处理数据更新: dataType={}, data={}", dataType, data);
        // 实现具体的数据更新逻辑
    }

    /**
     * 处理数据删除
     */
    private void handleDataDelete(String dataType, Object data) {
        log.info("处理数据删除: dataType={}, data={}", dataType, data);
        // 实现具体的数据删除逻辑
    }
}
