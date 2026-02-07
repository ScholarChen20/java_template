package com.example.yoyo_data.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 查询座位请求对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuerySeatDTO {

    /**
     * 演出活动ID
     */
    private Long showEventId;

    /**
     * 座位区域（可选）
     */
    private String seatZone;

    /**
     * 座位状态（可选）：AVAILABLE-可售, LOCKED-已锁定, SOLD-已售出
     */
    private String status;

    /**
     * 页码
     */
    private Integer page = 1;

    /**
     * 每页大小
     */
    private Integer size = 20;
}
