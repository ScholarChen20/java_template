package com.example.yoyo_data.service;

import com.example.yoyo_data.common.Result;
import com.example.yoyo_data.common.pojo.UserProfile;
import com.example.yoyo_data.common.pojo.Users;

import java.util.Map;

/**
 * 用户服务接口
 * 处理用户信息相关业务逻辑
 */
public interface UserService {

    Result<?> getUserInfo(Long userId);

    Result<?> updateUserInfo(Long userId, String username, String bio, String avatarUrl);

    Result<?> toggleFollow(Long userId, Long targetUserId);

    Result<?> getFollowStatus(Long userId, Long targetUserId);

    Result<?> getFollowList(Long userId, Integer page, Integer size);

    Result<?> getFollowerList(Long userId, Integer page, Integer size);

    /**
     * 获取当前用户信息
     *
     * @param token 当前用户的token
     * @return 当前用户信息
     */
    Result<Users> getCurrentUser(String token);

    /**
     * 更新当前用户信息
     *
     * @param token 当前用户的token
     * @param params 更新参数，包含phone、avatar_url、bio等
     * @return 更新结果
     */
    Result<Users> updateCurrentUser(String token, Map<String, Object> params);

    /**
     * 获取当前用户档案
     *
     * @param token 当前用户的token
     * @return 当前用户档案
     */
    Result<UserProfile> getCurrentUserProfile(String token);

    /**
     * 更新当前用户档案
     *
     * @param token 当前用户的token
     * @param params 更新参数，包含full_name、gender、birth_date、location、travel_preferences、visited_cities等
     * @return 更新结果
     */
    Result<UserProfile> updateCurrentUserProfile(String token, Map<String, Object> params);

    /**
     * 获取其他用户信息
     *
     * @param id 用户ID
     * @return 用户信息
     */
    Result<Users> getUserById(Long id);
}
