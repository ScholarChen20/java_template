package com.example.yoyo_data.service;

import com.example.yoyo_data.common.Result;
import java.util.Map;

/**
 * 认证服务接口
 * 处理用户登录、注册、刷新token等认证相关业务逻辑
 */
public interface AuthService {

    /**
     * 用户注册
     *
     * @param params 注册参数，包含username、email、password、phone
     * @return 注册结果
     */
    Result<Map<String, Object>> register(Map<String, Object> params);

    /**
     * 用户登录
     *
     * @param params 登录参数，包含username、password
     * @return 登录结果，包含token、用户信息、过期时间
     */
    Result<Map<String, Object>> login(Map<String, Object> params);

    /**
     * 刷新Token
     *
     * @param token 当前token
     * @return 刷新结果，包含新token和过期时间
     */
    Result<Map<String, Object>> refreshToken(String token);

    /**
     * 用户登出
     *
     * @param token 当前token
     * @return 登出结果
     */
    Result<Void> logout(String token);

    /**
     * 发送验证码
     *
     * @param params 发送验证码参数，包含email、type
     * @return 发送结果
     */
    Result<Map<String, Object>> sendCode(Map<String, Object> params);

    /**
     * 验证邮箱
     *
     * @param params 验证邮箱参数，包含email、code
     * @return 验证结果
     */
    Result<Map<String, Object>> verifyEmail(Map<String, Object> params);
}
