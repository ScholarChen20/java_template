package com.example.yoyo_data.service;

import com.example.yoyo_data.common.Result;
import com.example.yoyo_data.common.dto.request.*;
import com.example.yoyo_data.common.dto.response.*;

/**
 * 认证服务接口
 */
public interface AuthService {
    /**
     * 用户登录
     *
     * @param request 登录请求
     * @return 登录结果，包含用户信息和token
     */
    Result<LoginResponse> login(LoginRequest request);

    /**
     * 用户注册
     *
     * @param request 注册请求
     * @return 注册结果，包含用户信息
     */
    Result<?> register(RegisterRequest request);

    /**
     * 刷新Token
     *
     * @param token 当前token
     * @return 刷新结果，包含新token和过期时间
     */
    Result<TokenResponse> refreshToken(String token);

    /**
     * 发送验证码
     *
     * @param request 发送验证码请求
     * @return 发送结果
     */
    Result<SendCodeResponse> sendCode(SendCodeRequest request);

    /**
     * 验证邮箱
     *
     * @param request 验证邮箱请求
     * @return 验证结果
     */
    Result<VerifyEmailResponse> verifyEmail(VerifyEmailRequest request);
    /**
     * 登出
     */
    Result<Void> logout(String token);
}