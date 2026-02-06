package com.example.yoyo_data.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.yoyo_data.common.Result;
import com.example.yoyo_data.common.dto.request.UpdateUserProfileRequest;
import com.example.yoyo_data.common.pojo.UserProfile;
import com.example.yoyo_data.common.pojo.Users;

/**
 * 用户服务接口
 * 处理用户信息相关业务逻辑
 */
public interface UserService extends IService<Users> {


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
     * @return 更新结果
     */
    Result<Users> updateCurrentUser(String token, Users users);

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
     * @param requestBody 更新参数，包含full_name、gender、birth_date、location、travel_preferences、visited_cities等
     * @return 更新结果
     */
    Result<UserProfile> updateCurrentUserProfile(String token, UpdateUserProfileRequest requestBody);

    /**
     * 获取其他用户信息
     *
     * @param id 用户ID
     * @return 用户信息
     */
    Result<Users> getUserById(Long id);

    /**
     *  获取其他用户档案
     * @param id
     * @return
     */
    boolean isActive(Long id);
    /**
     * 激活用户
     * @param id
     * @return
     */
    boolean activeUser(Long id);
}
