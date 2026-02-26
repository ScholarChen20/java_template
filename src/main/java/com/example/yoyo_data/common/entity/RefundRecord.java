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
 * 退款记录表实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@TableName("tb_refund_record")
public class RefundRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("refund_no")
    private String refundNo;

    @TableField("order_id")
    private Long orderId;

    @TableField("order_no")
    private String orderNo;

    @TableField("user_id")
    private Long userId;

    @TableField("show_event_id")
    private Long showEventId;

    @TableField("original_amount")
    private BigDecimal originalAmount;

    @TableField("handling_fee")
    private BigDecimal handlingFee;

    @TableField("refund_amount")
    private BigDecimal refundAmount;

    /** 退款原因枚举 */
    @TableField("reason")
    private String reason;

    @TableField("remark")
    private String remark;

    /** 退款状态：PENDING/APPROVED/REFUNDED/REJECTED */
    @TableField("refund_status")
    private String refundStatus;

    @TableField("pay_type")
    private String payType;

    @TableField("transaction_id")
    private String transactionId;

    @TableField("refund_txn_id")
    private String refundTxnId;

    @TableField("refund_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime refundTime;

    @TableField("apply_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime applyTime;

    @TableField("updated_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updatedAt;
}
