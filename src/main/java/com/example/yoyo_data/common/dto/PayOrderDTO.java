package com.example.yoyo_data.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * 支付订单请求对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayOrderDTO {

    /**
     * 订单ID
     */
    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    /**
     * 支付方式：ALIPAY, WECHAT, CARD
     */
    @NotNull(message = "支付方式不能为空")
    private String payType;
}
