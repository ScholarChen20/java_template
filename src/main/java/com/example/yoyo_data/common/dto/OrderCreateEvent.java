package com.example.yoyo_data.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单创建事件DTO
 * 用于 Kafka 消息传递，异步处理订单创建后的业务逻辑
 *
 * @author YoYo Data Team
 * @version 1.0
 * @since 2026-02-09
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreateEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 订单编号
     */
    private String orderNo;

    /**
     * 演出活动ID
     */
    private Long showEventId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 座位数量
     */
    private Integer seatCount;

    /**
     * 订单总金额
     */
    private BigDecimal totalAmount;

    /**
     * 订单过期时间
     */
    private LocalDateTime expireTime;

    /**
     * 主联系人姓名
     */
    private String contactName;

    /**
     * 主联系人手机
     */
    private String contactPhone;

    /**
     * 主联系人身份证
     */
    private String contactIdCard;

    /**
     * 座位ID列表
     */
    private List<Long> seatIds;

    /**
     * 观影人信息列表
     */
    private List<TicketUserInfo> ticketUsers;

    /**
     * 座位信息列表（用于日志记录）
     */
    private List<SeatInfo> seats;

    /**
     * 演出活动版本号（用于乐观锁）
     */
    private Integer showEventVersion;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 观影人信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TicketUserInfo implements Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * 观影人姓名
         */
        private String contactName;

        /**
         * 观影人手机
         */
        private String contactPhone;

        /**
         * 观影人身份证
         */
        private String contactIdCard;
    }

    /**
     * 座位信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SeatInfo implements Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * 座位ID
         */
        private Long seatId;

        /**
         * 座位编码
         */
        private String seatCode;

        /**
         * 座位价格
         */
        private BigDecimal price;

        /**
         * 座位版本号（用于CAS）
         */
        private Integer version;
    }
}
