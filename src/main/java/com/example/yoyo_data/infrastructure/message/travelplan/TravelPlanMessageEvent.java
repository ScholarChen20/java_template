package com.example.yoyo_data.infrastructure.message.travelplan;

import com.example.yoyo_data.infrastructure.message.MessageEvent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 旅行计划消息事件 - 用于旅行计划相关的Kafka消息
 * 扩展自基础MessageEvent，添加旅行计划相关的字段
 *
 * @author Template Framework
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TravelPlanMessageEvent extends MessageEvent {

    private static final long serialVersionUID = 1L;

    /**
     * 旅行计划ID
     */
    private String planId;

    /**
     * 旅行计划标题
     */
    private String planTitle;

    /**
     * 旅行计划状态
     */
    private String planStatus;

    /**
     * 目的地
     */
    private String destination;

    /**
     * 开始日期
     */
    private String startDate;

    /**
     * 结束日期
     */
    private String endDate;

    /**
     * 旅行天数
     */
    private Integer days;

    /**
     * 构建旅行计划创建事件
     */
    public static TravelPlanMessageEvent buildCreateEvent(Long userId, String data, String title, String destination) {
        TravelPlanMessageEvent event = new TravelPlanMessageEvent();
        event.setEventType("TRAVEL_PLAN_CREATE");
        event.setSource("TravelPlanController");
        event.setTimestamp(LocalDateTime.now());
        event.setCreatedAt(LocalDateTime.now());
        event.setData(data);
        event.setPriority(5);
        event.setUserId(userId);
        event.setPlanTitle(title);
        event.setPlanStatus("PENDING");
        event.setDestination(destination);
        return event;
    }

    /**
     * 构建旅行计划更新事件
     */
    public static TravelPlanMessageEvent buildUpdateEvent(Long userId, String planId, String data, String title) {
        TravelPlanMessageEvent event = new TravelPlanMessageEvent();
        event.setEventType("TRAVEL_PLAN_UPDATE");
        event.setSource("TravelPlanController");
        event.setTimestamp(LocalDateTime.now());
        event.setCreatedAt(LocalDateTime.now());
        event.setData(data);
        event.setPriority(5);
        event.setUserId(userId);
        event.setPlanId(planId);
        event.setPlanTitle(title);
        event.setPlanStatus("PENDING");
        return event;
    }
}
