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
 * 退款记录视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundRecordVO {

    private Long refundId;
    private Long orderId;
    private String orderNo;
    private String showName;
    private BigDecimal originalAmount;
    private BigDecimal handlingFee;
    private BigDecimal refundAmount;
    private String refundStatus;
    private String payType;
    private String transactionId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime applyTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime refundTime;

    private List<TimelineItem> timeline;
}
