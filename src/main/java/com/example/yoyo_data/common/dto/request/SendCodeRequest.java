package com.example.yoyo_data.common.dto.request;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

/**
 * 发送验证码请求DTO
 *
 * @author yoyo_data
 * @version 1.0
 */
@Data
public class SendCodeRequest {

    /**
     * 邮箱
     */
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    /**
     * 验证码类型：register-注册, reset_password-重置密码, verify_email-验证邮箱
     */
    @NotBlank(message = "验证码类型不能为空")
    private String type;
}
