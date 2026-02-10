package com.example.yoyo_data.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 订单支付后置处理事件
 * 用于异步处理支付成功后的非核心业务：座位确认、演出统计、缓存清理
 *
 * @author YoYo Data Team
 * @version 1.0
 * @since 2026-02-10
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderPaidPostProcessEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 演出活动ID
     */
    private Long showEventId;

    /**
     * 座位数量
     */
    private Integer seatCount;

    /**
     * 座位ID列表
     */
    private List<Long> seatIds;

    /**
     * 重试次数（用于失败重试控制）
     */
    @Builder.Default
    private Integer retryCount = 0;

    /**
     * 最大重试次数
     */
    @Builder.Default
    private Integer maxRetryCount = 3;
}
