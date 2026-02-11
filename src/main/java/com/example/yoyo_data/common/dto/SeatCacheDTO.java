package com.example.yoyo_data.common.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SeatCacheDTO {
    /**
     * 座位区域：VIP, A, B, C
     */
    private String seatZone;

    /**
     * 排号_座位号
     */
    private String seatKey;

    /**
     * 座位状态：'AVAILABLE' COMMENT '状态：AVAILABLE-可售, LOCKED-已锁定, SOLD-已售出',
     */
    private String Status;
}
