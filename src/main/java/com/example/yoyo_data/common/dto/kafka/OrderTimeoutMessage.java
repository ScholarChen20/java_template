package com.example.yoyo_data.common.dto.kafka;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 订单超时消息
 *
 * @description 用于延迟队列处理订单超时
 * @author yoyo_data
 * @date 2026-02-25
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderTimeoutMessage implements Serializable {
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
     * 创建时间戳
     */
    private Long createTime;

    /**
     * 过期时间戳
     */
    private Long expireTime;
}
