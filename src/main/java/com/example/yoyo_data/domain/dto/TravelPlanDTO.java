package com.example.yoyo_data.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 旅行计划DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TravelPlanDTO {

    private String id;
    private Long userId;
    private String title;
    private String description;
    private String destination;
    private String startDate;
    private String endDate;
    private Integer days;
    private List<DailyItineraryDTO> dailyItinerary;
    private Double budget;
    private String status;
    private Map<String, Object> metadata;
    private Date createdAt;
    private Date updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyItineraryDTO {
        private Integer day;
        private String date;
        private List<String> activities;
        private Map<String, Object> metadata;
    }
}
