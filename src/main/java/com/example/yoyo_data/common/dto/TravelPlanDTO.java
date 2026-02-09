package com.example.yoyo_data.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

/**
 * 旅行计划DTO - 严格对齐 TravelPlan 实体字段
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

    private String city;

    private String startDate;

    private String endDate;

    private List<DaysDTO> days;

    private BudgetDTO budget;

    private List<WeatherInfoDTO> weatherInfo;

    private String overallSuggestions;

    private String status;

    private Date createdAt;

    private Date updatedAt;

    /**
     * 每日行程DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DaysDTO {
        /** 日期 */
        private String date;
        /** 天数索引 */
        private Integer dayIndex;
        /** 描述 */
        private String description;
        /** 交通 */
        private String transportation;
        /** 住宿 */
        private String accommodation;
    }

    /**
     * 预算DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BudgetDTO {
        private Double totalAttractions;
        private Double totalHotels;
        private Double totalMeals;
        private Double totalTransportation;
        private Double total;
    }

    /**
     * 位置DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LocationDTO {
        private Double latitude;
        private Double longitude;
    }

    /**
     * 酒店DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HotelDTO {
        private String name;
        private String address;
        private LocationDTO location;
        private String priceRange;
        private String rating;
        private String distance;
        private String type;
        private Double estimatedCost;
    }

    /**
     * 餐饮DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MealsDTO {
        private String name;
        private Double price;
        private String address;
        private LocationDTO location;
        private String description;
        private Double estimatedCost;
    }

    /**
     * 景点DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AttractionsDTO {
        private String name;
        private Double ticketPrice;
        private String address;
        private LocationDTO location;
        private String description;
        private Integer visitDuration;
        private String category;
        private String rating;
        private String poiId;
        private String imageUrl;
        private List<String> photos;
    }

    /**
     * 天气信息DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WeatherInfoDTO {
        private String date;
        private String dayWeather;
        private String nightWeather;
        private String dayTemp;
        private String nightTemp;
        private String windDirection;
        private String windPower;
    }
}
