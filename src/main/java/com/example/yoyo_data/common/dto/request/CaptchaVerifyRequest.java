package com.example.yoyo_data.common.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 验证码校验请求DTO
 */
@Data
@ApiModel(description = "验证码校验请求")
public class CaptchaVerifyRequest {

    @ApiModelProperty(value = "用户名", required = true, example = "zhangsan")
    @NotBlank(message = "用户名不能为空")
    private String username;

    @ApiModelProperty(value = "验证码UUID", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
    @NotBlank(message = "验证码UUID不能为空")
    private String uuid;

    @ApiModelProperty(value = "验证码内容", required = true, example = "A1B2")
    @NotBlank(message = "验证码不能为空")
    private String captcha;
}
