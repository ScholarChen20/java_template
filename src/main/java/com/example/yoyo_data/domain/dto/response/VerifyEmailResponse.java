package com.example.yoyo_data.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 验证邮箱响应VO
 *
 * @author yoyo_data
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerifyEmailResponse {

    /**
     * 邮箱
     */
    private String email;

    /**
     * 是否已验证
     */
    private Boolean isVerified;

    /**
     * 验证时间
     */
    private Long timestamp;
}
