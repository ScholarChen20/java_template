package com.example.yoyo_data.common.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Pattern;

/**
 * 更新观演人请求DTO（只允许修改手机号和默认标记）
 */
@Data
@ApiModel(description = "更新观演人请求")
public class UpdateViewerRequest {

    @ApiModelProperty(value = "手机号", example = "13800138000")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @ApiModelProperty(value = "是否默认", example = "true")
    private Boolean isDefault;
}
