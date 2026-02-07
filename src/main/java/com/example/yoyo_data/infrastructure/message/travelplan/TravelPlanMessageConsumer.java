package com.example.yoyo_data.infrastructure.message.travelplan;

import com.alibaba.fastjson.JSON;
import com.example.yoyo_data.common.Result;
import com.example.yoyo_data.service.TravelPlanService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 旅行计划消息消费者 - 用于处理旅行计划相关的Kafka消息
 * 主要处理旅行计划创建、更新等异步操作
 *
 * @author Template Framework
 * @version 1.0
 */
@Slf4j
@Component
public class TravelPlanMessageConsumer {

    @Autowired
    private TravelPlanService travelPlanService;

    /**
     * 处理旅行计划创建消息
     *
     * @param message 消息内容
     */
    @KafkaListener(topics = "travel-plans-create", groupId = "travel-plan-consumer-group")
    public void handleTravelPlanCreateMessage(String message) {
        log.info("接收到旅行计划创建消息: {}", message);

        try {
            // 解析消息
            TravelPlanMessageEvent event = JSON.parseObject(message, TravelPlanMessageEvent.class);
            log.info("解析旅行计划创建事件: eventId={}, eventType={}, userId={}", 
                    event.getEventId(), event.getEventType(), event.getUserId());

            // 解析创建旅行计划请求数据
            Map<String, Object> requestData = JSON.parseObject(event.getData(), Map.class);
            String title = (String) requestData.get("title");
            String description = (String) requestData.get("description");
            String destination = (String) requestData.get("destination");
            String startDate = (String) requestData.get("startDate");
            String endDate = (String) requestData.get("endDate");

            log.info("解析旅行计划创建请求: title={}, destination={}, startDate={}, endDate={}", 
                    title, destination, startDate, endDate);

            // 处理旅行计划创建
            Result<?> result = travelPlanService.createTravelPlan(
                    event.getUserId(), title, description, destination, startDate, endDate);

            if (result.getCode() == 200) {
                log.info("旅行计划创建处理成功: eventId={}, planTitle={}", 
                        event.getEventId(), title);
                // 这里可以添加后续处理，比如更新旅行计划状态等
            } else {
                log.error("旅行计划创建处理失败: eventId={}, message={}", 
                        event.getEventId(), result.getMessage());
                // 这里可以添加失败处理逻辑
            }

        } catch (Exception e) {
            log.error("处理旅行计划创建消息异常: {}", message, e);
            // 这里可以添加异常处理逻辑，比如重试机制
        }
    }

    /**
     * 处理旅行计划更新消息
     *
     * @param message 消息内容
     */
    @KafkaListener(topics = "travel-plans-update", groupId = "travel-plan-consumer-group")
    public void handleTravelPlanUpdateMessage(String message) {
        log.info("接收到旅行计划更新消息: {}", message);

        try {
            // 解析消息
            TravelPlanMessageEvent event = JSON.parseObject(message, TravelPlanMessageEvent.class);
            log.info("解析旅行计划更新事件: eventId={}, eventType={}, planId={}", 
                    event.getEventId(), event.getEventType(), event.getPlanId());

            // 解析更新旅行计划请求数据
            Map<String, Object> requestData = JSON.parseObject(event.getData(), Map.class);
            String title = (String) requestData.get("title");
            String description = (String) requestData.get("description");
            String destination = (String) requestData.get("destination");
            String startDate = (String) requestData.get("startDate");
            String endDate = (String) requestData.get("endDate");

            // 处理旅行计划更新
            Result<?> result = travelPlanService.updateTravelPlan(
                    Long.parseLong(event.getPlanId()), event.getUserId(), 
                    title, description, destination, startDate, endDate);

            if (result.getCode() == 200) {
                log.info("旅行计划更新处理成功: eventId={}, planId={}", 
                        event.getEventId(), event.getPlanId());
            } else {
                log.error("旅行计划更新处理失败: eventId={}, message={}", 
                        event.getEventId(), result.getMessage());
            }

        } catch (Exception e) {
            log.error("处理旅行计划更新消息异常: {}", message, e);
            // 这里可以添加异常处理逻辑
        }
    }

    /**
     * 处理旅行计划消息的错误情况
     *
     * @param message 消息内容
     */
    @KafkaListener(topics = "travel-plans-error", groupId = "travel-plan-consumer-group")
    public void handleTravelPlanErrorMessage(String message) {
        log.warn("接收到旅行计划错误消息: {}", message);

        try {
            // 解析错误消息
            TravelPlanMessageEvent event = JSON.parseObject(message, TravelPlanMessageEvent.class);
            log.warn("解析旅行计划错误事件: eventId={}, errorMessage={}", 
                    event.getEventId(), event.getErrorMessage());

            // 这里可以添加错误处理逻辑，比如记录错误日志、发送告警等

        } catch (Exception e) {
            log.error("处理旅行计划错误消息异常: {}", message, e);
        }
    }
}
