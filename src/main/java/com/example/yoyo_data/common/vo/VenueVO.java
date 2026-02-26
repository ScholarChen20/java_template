package com.example.yoyo_data.common.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 场馆视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VenueVO {

    private Long id;
    private String name;
    private String city;
    private String district;
    private String address;
    private Integer capacity;
    private String logoUrl;
    private String panoramaUrl;
    private String seatMapUrl;
    private Map<String, Object> transportInfo;
    private List<String> amenities;
    private Integer upcomingShowCount;
    private List<SimpleShowVO> upcomingShows;
}
