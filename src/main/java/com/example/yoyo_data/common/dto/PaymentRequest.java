package com.example.yoyo_data.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 支付请求DTO
 *
 * @author YoYo Data Team
 * @version 1.0
 * @since 2026-02-10
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {

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
     * 支付金额
     */
    private BigDecimal amount;

    /**
     * 支付方式：WECHAT-微信支付, ALIPAY-支付宝支付, CARD-银行卡支付
     */
    private String payType;

    /**
     * 支付描述
     */
    private String description;

    /**
     * 用户IP地址
     */
    private String clientIp;

    /**
     * 回调通知URL
     */
    private String notifyUrl;
}
