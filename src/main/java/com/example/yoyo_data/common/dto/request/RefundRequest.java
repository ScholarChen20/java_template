package com.example.yoyo_data.common.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 申请退款请求DTO
 *
 * @author yoyo_data
 * @date 2026-02-25
 */
@Data
@ApiModel(description = "申请退款请求")
public class RefundRequest {

    @ApiModelProperty(value = "退款原因", required = true, example = "行程变更")
    @NotBlank(message = "退款原因不能为空")
    private String reason;

    @ApiModelProperty(value = "退款说明", example = "临时有事无法参加")
    private String description;
}
