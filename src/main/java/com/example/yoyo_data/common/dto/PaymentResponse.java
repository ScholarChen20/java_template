package com.example.yoyo_data.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付响应DTO
 *
 * @author YoYo Data Team
 * @version 1.0
 * @since 2026-02-10
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {

    /**
     * 是否支付成功
     */
    private Boolean success;

    /**
     * 交易流水号（第三方支付平台生成）
     */
    private String transactionId;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 支付金额
     */
    private BigDecimal amount;

    /**
     * 支付方式
     */
    private String payType;

    /**
     * 支付状态：SUCCESS-成功, FAILED-失败, PROCESSING-处理中
     */
    private String status;

    /**
     * 支付时间
     */
    private LocalDateTime payTime;

    /**
     * 错误码
     */
    private String errorCode;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 第三方平台响应原文
     */
    private String rawResponse;
}
