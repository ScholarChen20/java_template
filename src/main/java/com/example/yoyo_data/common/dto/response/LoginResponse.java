package com.example.yoyo_data.common.dto.response;

import com.example.yoyo_data.common.vo.UserVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 登录响应VO
 *
 * @author yoyo_data
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    /**
     * JWT Token
     */
    private String token;

    /**
     * 用户信息
     */
    private UserVO user;

    /**
     * Token过期时间
     */
    private Date expiresAt;
}
