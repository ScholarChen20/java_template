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
 * 旅行计划事件消费者
 * 处理旅行计划相关的Kafka事件
 */
@Slf4j
@Component
public class TravelPlanEventConsumer extends KafkaConsumerTemplate {

    @Autowired
    private RedisService redisService;

    private static final String TRAVEL_PLAN_CACHE_PREFIX = "travel_plan:";
    private static final String TRAVEL_PLAN_LIST_CACHE_PREFIX = "travel_plan:list:";

    /**
     * 监听旅行计划创建事件
     */
    @KafkaListener(topics = KafkaTopic.TRAVEL_PLAN_CREATED, groupId = "travel-plan-group")
    public void handleTravelPlanCreated(ConsumerRecord<String, String> record) {
        try {
            String message = record.value();
            log.info("接收到旅行计划创建事件: topic={}, partition={}, offset={}",
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

            // 处理业务逻辑
            // 1. 可以发送通知给用户
            log.info("旅行计划创建成功通知: planId={}, userId={}", planId, userId);

            // 2. 可以进行数据聚合或统计
            // 例如:更新用户的旅行计划统计数据

            // 3. 可以触发其他业务流程
            // 例如:推荐相关的旅行攻略

            consumeEvent(event);

        } catch (Exception e) {
            log.error("处理旅行计划创建事件失败", e);
            handleException(record.topic(), record.partition(), record.value(), e);
        }
    }

    /**
     * 监听旅行计划更新事件
     */
    @KafkaListener(topics = KafkaTopic.TRAVEL_PLAN_UPDATED, groupId = "travel-plan-group")
    public void handleTravelPlanUpdated(ConsumerRecord<String, String> record) {
        try {
            String message = record.value();
            log.info("接收到旅行计划更新事件: topic={}, partition={}, offset={}",
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

            // 处理缓存同步
            syncCacheAfterUpdate(planId, userId);

            // 发送更新通知
            log.info("旅行计划更新成功: planId={}, userId={}", planId, userId);

            consumeEvent(event);

        } catch (Exception e) {
            log.error("处理旅行计划更新事件失败", e);
            handleException(record.topic(), record.partition(), record.value(), e);
        }
    }

    /**
     * 监听旅行计划删除事件
     */
    @KafkaListener(topics = KafkaTopic.TRAVEL_PLAN_DELETED, groupId = "travel-plan-group")
    public void handleTravelPlanDeleted(ConsumerRecord<String, String> record) {
        try {
            String message = record.value();
            log.info("接收到旅行计划删除事件: topic={}, partition={}, offset={}",
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

            // 清理缓存
            clearCache(planId, userId);

            // 清理相关数据
            log.info("旅行计划删除成功,清理相关数据: planId={}, userId={}", planId, userId);

            consumeEvent(event);

        } catch (Exception e) {
            log.error("处理旅行计划删除事件失败", e);
            handleException(record.topic(), record.partition(), record.value(), e);
        }
    }

    /**
     * 同步缓存(更新后)
     */
    private void syncCacheAfterUpdate(String planId, Long userId) {
        try {
            // 清除详情缓存
            String cacheKey = TRAVEL_PLAN_CACHE_PREFIX + planId;
            redisService.delete(cacheKey);

            // 清除列表缓存
            clearListCache(userId);

            log.info("旅行计划缓存同步完成: planId={}, userId={}", planId, userId);

        } catch (Exception e) {
            log.error("旅行计划缓存同步失败: planId={}", planId, e);
        }
    }

    /**
     * 清理缓存
     */
    private void clearCache(String planId, Long userId) {
        try {
            // 清除详情缓存
            String cacheKey = TRAVEL_PLAN_CACHE_PREFIX + planId;
            redisService.delete(cacheKey);

            // 清除列表缓存
            clearListCache(userId);

            log.info("旅行计划缓存清理完成: planId={}, userId={}", planId, userId);

        } catch (Exception e) {
            log.error("旅行计划缓存清理失败: planId={}", planId, e);
        }
    }

    /**
     * 清除列表缓存
     */
    private void clearListCache(Long userId) {
        try {
            java.util.Set<String> keys = redisService.keys(TRAVEL_PLAN_LIST_CACHE_PREFIX + userId + ":*");
            if (keys != null && !keys.isEmpty()) {
                redisService.delete(keys);
            }
        } catch (Exception e) {
            log.error("清除旅行计划列表缓存失败: userId={}", userId, e);
        }
    }
}
