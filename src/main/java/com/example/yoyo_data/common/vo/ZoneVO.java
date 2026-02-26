package com.example.yoyo_data.common.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 座位区域视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ZoneVO {

    private String zone;
    private String zoneName;
    private String description;
    private BigDecimal price;
    private Integer totalSeats;
    private Integer availableSeats;
    private String soldRate;
    private String seatMapImageUrl;
}
