package com.example.yoyo_data.service.impl;

import com.example.yoyo_data.common.Result;
import com.example.yoyo_data.service.UserService;
import com.example.yoyo_data.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户服务实现类
 * 实现用户信息相关业务逻辑
 */
@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 获取当前用户信息
     *
     * @param token 当前用户的token
     * @return 当前用户信息
     */
    @Override
    public Result<Map<String, Object>> getCurrentUser(String token) {
        // 模拟获取用户信息逻辑
        // 实际项目中需要：
        // 1. 验证token
        // 2. 从token中获取用户ID
        // 3. 根据用户ID查询用户信息
        // 4. 构建返回结果
        
        if (token == null || !jwtUtils.validateToken(token)) {
            return Result.unauthorized("Token无效或已过期");
        }

        // 从token中获取用户名
        String username = jwtUtils.getUsernameFromToken(token);
        Long userId = jwtUtils.getUserIdFromToken(token);

        // 模拟用户信息
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", userId);
        userInfo.put("username", username);
        userInfo.put("email", username + "@example.com");
        userInfo.put("phone", "13800138000");
        userInfo.put("avatar_url", "https://example.com/avatar.jpg");
        userInfo.put("bio", "旅行爱好者");
        userInfo.put("role", "user");
        userInfo.put("is_active", true);
        userInfo.put("is_verified", true);
        userInfo.put("created_at", new Date());
        userInfo.put("last_login_at", new Date());

        return Result.success(userInfo);
    }

    /**
     * 更新当前用户信息
     *
     * @param token 当前用户的token
     * @param params 更新参数，包含phone、avatar_url、bio等
     * @return 更新结果
     */
    @Override
    public Result<Map<String, Object>> updateCurrentUser(String token, Map<String, Object> params) {
        // 模拟更新用户信息逻辑
        // 实际项目中需要：
        // 1. 验证token
        // 2. 从token中获取用户ID
        // 3. 验证参数
        // 4. 更新用户信息
        // 5. 构建返回结果
        
        if (token == null || !jwtUtils.validateToken(token)) {
            return Result.unauthorized("Token无效或已过期");
        }

        // 从token中获取用户名
        String username = jwtUtils.getUsernameFromToken(token);
        Long userId = jwtUtils.getUserIdFromToken(token);

        // 模拟更新成功
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", userId);
        userInfo.put("username", username);
        userInfo.put("email", username + "@example.com");
        userInfo.put("phone", params.getOrDefault("phone", "13800138000"));
        userInfo.put("avatar_url", params.getOrDefault("avatar_url", "https://example.com/avatar.jpg"));
        userInfo.put("bio", params.getOrDefault("bio", "旅行爱好者"));

        return Result.success(userInfo);
    }

    /**
     * 获取当前用户档案
     *
     * @param token 当前用户的token
     * @return 当前用户档案
     */
    @Override
    public Result<Map<String, Object>> getCurrentUserProfile(String token) {
        // 模拟获取用户档案逻辑
        // 实际项目中需要：
        // 1. 验证token
        // 2. 从token中获取用户ID
        // 3. 根据用户ID查询用户档案
        // 4. 构建返回结果
        
        if (token == null || !jwtUtils.validateToken(token)) {
            return Result.unauthorized("Token无效或已过期");
        }

        // 从token中获取用户ID
        Long userId = jwtUtils.getUserIdFromToken(token);

        // 模拟用户档案
        Map<String, Object> profile = new HashMap<>();
        profile.put("id", 1L);
        profile.put("user_id", userId);
        profile.put("full_name", "张三");
        profile.put("gender", "male");
        profile.put("birth_date", "1990-01-01");
        profile.put("location", "北京市");
        
        // 旅行偏好
        String[] travelPreferences = {"自然风光", "历史文化", "美食"};
        profile.put("travel_preferences", travelPreferences);
        
        // 已访问城市
        String[] visitedCities = {"北京", "上海", "杭州"};
        profile.put("visited_cities", visitedCities);
        
        // 旅行统计
        Map<String, Object> travelStats = new HashMap<>();
        travelStats.put("total_trips", 10);
        travelStats.put("total_distance", 5000);
        travelStats.put("favorite_country", "中国");
        profile.put("travel_stats", travelStats);

        return Result.success(profile);
    }

    /**
     * 更新当前用户档案
     *
     * @param token 当前用户的token
     * @param params 更新参数，包含full_name、gender、birth_date、location、travel_preferences、visited_cities等
     * @return 更新结果
     */
    @Override
    public Result<Map<String, Object>> updateCurrentUserProfile(String token, Map<String, Object> params) {
        // 模拟更新用户档案逻辑
        // 实际项目中需要：
        // 1. 验证token
        // 2. 从token中获取用户ID
        // 3. 验证参数
        // 4. 更新用户档案
        // 5. 构建返回结果
        
        if (token == null || !jwtUtils.validateToken(token)) {
            return Result.unauthorized("Token无效或已过期");
        }

        // 从token中获取用户ID
        Long userId = jwtUtils.getUserIdFromToken(token);

        // 模拟更新成功
        Map<String, Object> profile = new HashMap<>();
        profile.put("id", 1L);
        profile.put("user_id", userId);
        profile.put("full_name", params.getOrDefault("full_name", "张三"));
        profile.put("gender", params.getOrDefault("gender", "male"));
        profile.put("birth_date", params.getOrDefault("birth_date", "1990-01-01"));
        profile.put("location", params.getOrDefault("location", "北京市"));
        profile.put("travel_preferences", params.getOrDefault("travel_preferences", new String[]{"自然风光", "历史文化", "美食"}));
        profile.put("visited_cities", params.getOrDefault("visited_cities", new String[]{"北京", "上海", "杭州"}));

        return Result.success(profile);
    }

    /**
     * 获取其他用户信息
     *
     * @param id 用户ID
     * @return 用户信息
     */
    @Override
    public Result<Map<String, Object>> getUserById(Long id) {
        // 模拟获取用户信息逻辑
        // 实际项目中需要：
        // 1. 验证用户ID
        // 2. 根据用户ID查询用户信息
        // 3. 构建返回结果（只返回公开信息）
        
        // 模拟用户信息
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", id);
        userInfo.put("username", "otheruser" + id);
        userInfo.put("avatar_url", "https://example.com/avatar" + id + ".jpg");
        userInfo.put("bio", "旅行博主");
        userInfo.put("location", "上海市");
        
        // 旅行偏好
        String[] travelPreferences = {"自然风光", "摄影"};
        userInfo.put("travel_preferences", travelPreferences);
        
        userInfo.put("created_at", new Date());

        return Result.success(userInfo);
    }
}
