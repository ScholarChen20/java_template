package com.example.yoyo_data.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 票务订单表实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@TableName(value = "tb_ticket_order")
public class TicketOrder implements Serializable {

    private static final long serialVersionUID = 1L; // 避免主键冲突

    /**
     * 订单ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 订单编号
     */
    @TableField("order_no")
    private String orderNo;

    /**
     * 演出活动ID
     */
    @TableField("show_event_id")
    private Long showEventId;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 购买座位数量
     */
    @TableField("seat_count")
    private Integer seatCount;

    /**
     * 订单总金额
     */
    @TableField("total_amount")
    private BigDecimal totalAmount;

    /**
     * 状态：PENDING-待支付, PAID-已支付, CANCELLED-已取消, TIMEOUT-超时取消
     */
    @TableField("status")
    private String status;

    /**
     * 支付方式：ALIPAY, WECHAT, CARD
     */
    @TableField("pay_type")
    private String payType;

    /**
     * 第三方支付交易流水号
     */
    @TableField("transaction_id")
    private String transactionId;

    /**
     * 支付时间
     */
    @TableField("pay_time")
    private LocalDateTime payTime;

    /**
     * 订单过期时间
     */
    @TableField("expire_time")
    private LocalDateTime expireTime;

    /**
     * 联系人姓名
     */
    @TableField("contact_name")
    private String contactName;

    /**
     * 联系人手机
     */
    @TableField("contact_phone")
    private String contactPhone;

    /**
     * 联系人身份证
     */
    @TableField("contact_id_card")
    private String contactIdCard;

    /**
     * 订单二维码URL（用于入场验证）
     */
    @TableField("qr_code_url")
    private String qrCodeUrl;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 座位区域（冗余字段）
     */
    @TableField("seat_zone")
    private String seatZone;

    /**
     * 退款记录ID
     */
    @TableField("refund_id")
    private Long refundId;

    /**
     * 创建时间
     */
    @TableField("created_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss" ,timezone = "GMT+8")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField("updated_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss" ,timezone = "GMT+8")
    private LocalDateTime updatedAt;
}
