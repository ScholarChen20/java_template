package com.example.yoyo_data.common.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Token响应VO
 *
 * @author yoyo_data
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenResponse {

    /**
     * JWT Token
     */
    private String token;

    /**
     * Token过期时间
     */
    private Date expiresAt;
}
