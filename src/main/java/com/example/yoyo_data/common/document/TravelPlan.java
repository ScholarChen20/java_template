package com.example.yoyo_data.common.document;

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

    @Field("city")
    private String city;

    @Field("start_date")
    private String startDate;

    @Field("end_date")
    private String endDate;

    @Field("days")
    private List<Days> days;

    @Field("budget")
    private Budget budget;

    @Field("weather_info")
    private List<WeatherInfo> weather_info;

    @Field("overall_suggestions")
    private String overallSuggestions;

    @Field("status")
    private String status;

    @Field("created_at")
    private Date createdAt;

    @Field("updated_at")
    private Date updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Budget {
        @Field("total_attractions")
        private Double totalAttractions;

        @Field("total_hotels")
        private Double totalHotels;

        @Field("total_meals")
        private Double totalMeals;

        @Field("total_transportation")
        private Double totalTransportation;

        @Field("total")
        private Double total;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Days {
        /** 日期 */
        @Field("date")
        private String date;
        /** 天数索引 */
        @Field("day_index")
        private Integer dayIndex;
        /** 描述 */
        @Field("description")
        private String description;
        /** 交通 */
        @Field("transportation")
        private String transportation;
        /** 住宿 */
        @Field("accommodation")
        private String accommodation;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Hotel {
        @Field("name")
        private String name;

        @Field("address")
        private String address;

        @Field("location")
        private Location location;

        @Field("price_range")
        private String priceRange;

        @Field("rating")
        private String rating;

        @Field("distance")
        private String distance;

        @Field("type")
        private String type;

        @Field("estimated_cost")
        private Double estimatedCost;

    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Location {
        @Field("latitude")
        private Double latitude;
        @Field("longitude")
        private Double longitude;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Meals {
        @Field("name")
        private String name;
        @Field("price")
        private Double price;
        @Field("address")
        private String address;
        @Field("location")
        private Location location;
        @Field("description")
        private String description;
        @Field("estimated_cost")
        private Double estimatedCost;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Attractions {
        @Field("name")
        private String name;
        @Field("ticket_price")
        private Double ticketPrice;
        @Field("address")
        private String address;
        @Field("location")
        private Location location;
        @Field("description")
        private String description;
        @Field("visit_duration")
        private Integer visitDuration;
        @Field("category")
        private String category;
        @Field("rating")
        private String rating;
        @Field("poi_id")
        private String poiId;
        @Field("image_url")
        private String imageUrl;
        @Field("photos")
        private List<String> photos;

    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WeatherInfo {
        @Field("date")
        private String date;
        @Field("day_weather")
        private String dayWeather;
        @Field("night_weather")
        private String nightWeather;
        @Field("day_temp")
        private String dayTemp;
        @Field("night_temp")
        private String nightTemp;
        @Field("wind_direction")
        private String windDirection;
        @Field("wind_power")
        private String windPower;
    }
}
