package com.example.yoyo_data.common.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * 用户注册请求DTO
 *
 * @author yoyo_data
 * @date 2026-02-25
 */
@Data
@ApiModel(description = "用户注册请求")
public class UserRegisterRequest {

    @ApiModelProperty(value = "手机号", required = true, example = "13800138000")
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @ApiModelProperty(value = "密码", required = true, example = "password123")
    @NotBlank(message = "密码不能为空")
    @Pattern(regexp = "^.{6,20}$", message = "密码长度必须在6-20位之间")
    private String password;

    @ApiModelProperty(value = "验证码", required = true, example = "123456")
    @NotBlank(message = "验证码不能为空")
    private String verifyCode;

    @ApiModelProperty(value = "昵称", example = "张三")
    private String nickname;
}
