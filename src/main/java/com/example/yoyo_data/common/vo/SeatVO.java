package com.example.yoyo_data.common.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 座位视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatVO {

    /**
     * 座位ID
     */
    private Long id;

    /**
     * 演出活动ID
     */
    private Long showEventId;

    /**
     * 座位区域：VIP, A, B, C
     */
    private String seatZone;

    /**
     * 排号
     */
    private Integer seatRow;

    /**
     * 座位号
     */
    private Integer seatNumber;

    /**
     * 座位编码：如VIP-1-5
     */
    private String seatCode;

    /**
     * 票价
     */
    private BigDecimal price;

    /**
     * 状态：AVAILABLE-可售, LOCKED-已锁定, SOLD-已售出
     */
    private String status;
}
