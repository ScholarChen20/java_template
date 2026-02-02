package com.example.yoyo_data.common.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 发送验证码响应VO
 *
 * @author yoyo_data
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendCodeResponse {

    /**
     * 邮箱
     */
    private String email;

    /**
     * 验证码有效期（秒）
     */
    private Integer expiresIn;

    /**
     * 发送时间
     */
    private Long timestamp;
}
