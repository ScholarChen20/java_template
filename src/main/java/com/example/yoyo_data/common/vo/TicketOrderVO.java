package com.example.yoyo_data.common.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketOrderVO {

    /**
     * 订单ID
     */
    private Long id;

    /**
     * 订单编号
     */
    private String orderNo;

    /**
     * 演出活动ID
     */
    private Long showEventId;

    /**
     * 演出名称
     */
    private String showName;

    /**
     * 场馆名称
     */
    private String venueName;

    /**
     * 演出时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime showTime;

    /**
     * 购买座位数量
     */
    private Integer seatCount;

    /**
     * 订单总金额
     */
    private BigDecimal totalAmount;

    /**
     * 状态：PENDING-待支付, PAID-已支付, CANCELLED-已取消, TIMEOUT-超时取消
     */
    private String status;

    /**
     * 支付方式：ALIPAY, WECHAT, CARD
     */
    private String payType;

    /**
     * 支付时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime payTime;

    /**
     * 订单过期时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime expireTime;

    /**
     * 联系人姓名
     */
    private String contactName;

    /**
     * 联系人手机
     */
    private String contactPhone;

    /**
     * 座位列表
     */
    private List<SeatVO> seats;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createdAt;
}
