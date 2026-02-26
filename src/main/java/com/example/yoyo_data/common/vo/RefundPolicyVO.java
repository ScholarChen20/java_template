package com.example.yoyo_data.common.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 退票政策视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundPolicyVO {

    private Long showEventId;
    private String showName;
    private Boolean allowRefund;
    private String refundDeadline;
    private BigDecimal handlingFeeRate;
    private String description;
}
