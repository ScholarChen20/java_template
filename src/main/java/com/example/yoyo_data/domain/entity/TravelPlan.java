package com.example.yoyo_data.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 旅行计划文档
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "travel_plans")
public class TravelPlan {

    @Id
    private String id;

    @Field("user_id")
    private Long userId;

    @Field("title")
    private String title;

    @Field("description")
    private String description;

    @Field("destination")
    private String destination;

    @Field("start_date")
    private String startDate;

    @Field("end_date")
    private String endDate;

    @Field("days")
    private Integer days;

    @Field("daily_itinerary")
    private List<DailyItinerary> dailyItinerary;

    @Field("budget")
    private Double budget;

    @Field("status")
    private String status;

    @Field("metadata")
    private Map<String, Object> metadata;

    @Field("created_at")
    private Date createdAt;

    @Field("updated_at")
    private Date updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyItinerary {
        private Integer day;
        private String date;
        private List<String> activities;
        private Map<String, Object> metadata;
    }
}
