package com.example.yoyo_data.service;

import com.example.yoyo_data.common.Result;
import java.util.Map;

/**
 * 用户服务接口
 * 处理用户信息相关业务逻辑
 */
public interface UserService {

    /**
     * 获取当前用户信息
     *
     * @param token 当前用户的token
     * @return 当前用户信息
     */
    Result<Map<String, Object>> getCurrentUser(String token);

    /**
     * 更新当前用户信息
     *
     * @param token 当前用户的token
     * @param params 更新参数，包含phone、avatar_url、bio等
     * @return 更新结果
     */
    Result<Map<String, Object>> updateCurrentUser(String token, Map<String, Object> params);

    /**
     * 获取当前用户档案
     *
     * @param token 当前用户的token
     * @return 当前用户档案
     */
    Result<Map<String, Object>> getCurrentUserProfile(String token);

    /**
     * 更新当前用户档案
     *
     * @param token 当前用户的token
     * @param params 更新参数，包含full_name、gender、birth_date、location、travel_preferences、visited_cities等
     * @return 更新结果
     */
    Result<Map<String, Object>> updateCurrentUserProfile(String token, Map<String, Object> params);

    /**
     * 获取其他用户信息
     *
     * @param id 用户ID
     * @return 用户信息
     */
    Result<Map<String, Object>> getUserById(Long id);
}
