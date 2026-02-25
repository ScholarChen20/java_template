package com.example.yoyo_data.common.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 用户登录请求DTO
 *
 * @author yoyo_data
 * @date 2026-02-25
 */
@Data
@ApiModel(description = "用户登录请求")
public class UserLoginRequest {

    @ApiModelProperty(value = "手机号", required = true, example = "13800138000")
    @NotBlank(message = "手机号不能为空")
    private String phone;

    @ApiModelProperty(value = "密码", required = true, example = "password123")
    @NotBlank(message = "密码不能为空")
    private String password;
}
