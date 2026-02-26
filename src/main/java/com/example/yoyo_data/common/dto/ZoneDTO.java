package com.example.yoyo_data.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 座位区域 数据传输对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ZoneDTO {
    /**
     * 座位区域
     */
    private String seatZone;
    /**
     * 座位总数
     */
    private Integer totalSeats;
    /**
     * 可用座位数
     */
    private Integer availableSeats;
    /**
     * 最低价格
     */
    private BigDecimal minPrice;
}
