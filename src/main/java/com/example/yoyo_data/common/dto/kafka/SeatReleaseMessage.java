package com.example.yoyo_data.common.dto.kafka;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 座位释放消息
 *
 * @description 订单取消或超时时，通知释放座位
 * @author yoyo_data
 * @date 2026-02-25
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeatReleaseMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 演出活动ID
     */
    private Long showEventId;

    /**
     * 需要释放的座位ID列表
     */
    private List<Long> seatIds;

    /**
     * 释放原因: TIMEOUT-超时, CANCEL-主动取消, REFUND-退款
     */
    private String releaseReason;

    /**
     * 时间戳
     */
    private Long timestamp;
}
