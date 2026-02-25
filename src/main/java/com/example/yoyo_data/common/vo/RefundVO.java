package com.example.yoyo_data.common.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 退款响应VO
 *
 * @author yoyo_data
 * @date 2026-02-25
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "退款信息")
public class RefundVO {

    @ApiModelProperty(value = "退款ID")
    private String refundId;

    @ApiModelProperty(value = "订单ID")
    private Long orderId;

    @ApiModelProperty(value = "退款金额")
    private BigDecimal refundAmount;

    @ApiModelProperty(value = "退款手续费")
    private BigDecimal refundFee;

    @ApiModelProperty(value = "实际退款金额")
    private BigDecimal actualRefundAmount;

    @ApiModelProperty(value = "退款状态: PROCESSING-处理中, SUCCESS-成功, FAILED-失败")
    private String status;

    @ApiModelProperty(value = "预计到账时间")
    private String estimatedTime;

    @ApiModelProperty(value = "退款原因")
    private String reason;
}
