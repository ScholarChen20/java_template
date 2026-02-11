package com.example.yoyo_data.service;

import com.example.yoyo_data.common.Result;
import com.example.yoyo_data.common.dto.PaymentRequest;
import com.example.yoyo_data.common.dto.PaymentResponse;

/**
 * 第三方支付服务接口
 * 支持微信支付、支付宝支付等
 *
 * @author YoYo Data Team
 * @version 1.0
 * @since 2026-02-10
 */
public interface PaymentService {

    /**
     * 执行支付
     *
     * @param request 支付请求
     * @return 支付结果
     */
    Result<PaymentResponse> executePayment(PaymentRequest request);

    /**
     * 查询支付状态
     *
     * @param transactionId 交易流水号
     * @return 支付状态
     */
    Result<PaymentResponse> queryPaymentStatus(String transactionId);

    /**
     * 退款
     *
     * @param transactionId 交易流水号
     * @param refundAmount 退款金额
     * @param refundReason 退款原因
     * @return 退款结果
     */
    Result<PaymentResponse> refund(String transactionId, Double refundAmount, String refundReason);
}
