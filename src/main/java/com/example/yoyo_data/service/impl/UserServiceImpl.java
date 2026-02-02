package com.example.yoyo_data.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.yoyo_data.cache.RedisService;
import com.example.yoyo_data.common.Result;
import com.example.yoyo_data.common.dto.request.UpdateUserProfileRequest;
import com.example.yoyo_data.common.pojo.Follow;
import com.example.yoyo_data.common.pojo.UserProfile;
import com.example.yoyo_data.common.pojo.Users;
import com.example.yoyo_data.mapper.FollowMapper;
import com.example.yoyo_data.mapper.UserMapper;
import com.example.yoyo_data.mapper.UserProfileMapper;
import com.example.yoyo_data.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.yoyo_data.data.cache.CacheKeyManager.USER_TOKEN_PREFIX;

/**
 * 用户服务实现类
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, Users> implements UserService {

    @Autowired
    private RedisService redisService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserProfileMapper userProfileMapper;

    @Autowired
    private FollowMapper followMapper;

    @Override
    public Result<?> getUserInfo(Long userId) {
        try {
            // 从数据库获取用户信息
            Users user = userMapper.selectById(userId);
            if (user == null) {
                return Result.error("用户不存在");
            }
            

            log.info("获取用户信息成功: userId={}", userId);
            return Result.success(user);

        } catch (Exception e) {
            log.error("获取用户信息失败", e);
            return Result.error("获取用户信息失败: " + e.getMessage());
        }
    }

    @Override
    public Result<?> updateUserInfo(Long userId, String username, String bio, String avatarUrl) {
        try {
            // 模拟更新用户信息
            Map<String, Object> updatedInfo = new HashMap<>();
            updatedInfo.put("id", userId);
            updatedInfo.put("username", username);
            updatedInfo.put("bio", bio);
            updatedInfo.put("avatarUrl", avatarUrl);
            updatedInfo.put("updatedAt", System.currentTimeMillis());

            log.info("更新用户信息成功: userId={}, username={}", userId, username);
            return Result.success(updatedInfo);

        } catch (Exception e) {
            log.error("更新用户信息失败", e);
            return Result.error("更新用户信息失败: " + e.getMessage());
        }
    }

    @Override
    public Result<?> toggleFollow(Long userId, Long targetUserId) {
        try {
            // 检查目标用户是否存在
            if (userMapper.selectById(targetUserId) == null) {
                return Result.error("目标用户不存在");
            }

            // 检查是否已经关注
            Follow follow = new Follow();
            follow.setUserId(userId);
            follow.setTargetUserId(targetUserId);
            follow.setCreatedAt(java.time.LocalDateTime.now());

            Follow existingFollow = followMapper.selectOne(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Follow>()
                            .eq(Follow::getUserId, userId)
                            .eq(Follow::getTargetUserId, targetUserId)
            );

            boolean isFollowing = existingFollow != null;
            if (isFollowing) {
                // 已关注，取消关注
                followMapper.delete(
                        new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Follow>()
                                .eq(Follow::getUserId, userId)
                                .eq(Follow::getTargetUserId, targetUserId)
                );
            } else {
                // 未关注，添加关注
                followMapper.insert(follow);
            }

            // 计算关注数和粉丝数
            int followCount = followMapper.selectCount(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Follow>()
                            .eq(Follow::getUserId, userId)
            );

            int followerCount = followMapper.selectCount(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Follow>()
                            .eq(Follow::getTargetUserId, targetUserId)
            );

            Map<String, Object> result = new HashMap<>();
            result.put("userId", userId);
            result.put("targetUserId", targetUserId);
            result.put("isFollowing", !isFollowing);
            result.put("followCount", followCount);
            result.put("followerCount", followerCount);

            log.info("{}关注成功: userId={}, targetUserId={}", isFollowing ? "取消" : "", userId, targetUserId);
            return Result.success(result);

        } catch (Exception e) {
            log.error("关注操作失败", e);
            return Result.error("关注操作失败: " + e.getMessage());
        }
    }

    @Override
    public Result<?> getFollowStatus(Long userId, Long targetUserId) {
        try {
            // 检查是否已经关注
            Follow existingFollow = followMapper.selectOne(new LambdaQueryWrapper<Follow>()
                            .eq(Follow::getUserId, userId)
                            .eq(Follow::getTargetUserId, targetUserId)
            );

            boolean isFollowing = existingFollow != null;
            
            Map<String, Object> result = new HashMap<>();
            result.put("userId", userId);
            result.put("targetUserId", targetUserId);
            result.put("isFollowing", isFollowing);

            log.info("获取关注状态成功: userId={}, targetUserId={}, isFollowing={}", userId, targetUserId, isFollowing);
            return Result.success(result);

        } catch (Exception e) {
            log.error("获取关注状态失败", e);
            return Result.error("获取关注状态失败: " + e.getMessage());
        }
    }

    @Override
    public Result<?> getFollowList(Long userId, Integer page, Integer size) {
        try {
            // 模拟数据
            List<Map<String, Object>> followList = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                Map<String, Object> follow = new HashMap<>();
                follow.put("id", (long) (i + 1));
                follow.put("username", "用户" + (i + 1));
                follow.put("avatarUrl", "https://example.com/avatar" + (i + 1) + ".jpg");
                follow.put("bio", "这是用户" + (i + 1) + "的个人简介。");
                follow.put("followedAt", System.currentTimeMillis() - i * 86400000);
                followList.add(follow);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("followList", followList);
            result.put("total", 100);
            result.put("page", page);
            result.put("size", size);

            log.info("获取关注列表成功: userId={}, page={}, size={}", userId, page, size);
            return Result.success(result);

        } catch (Exception e) {
            log.error("获取关注列表失败", e);
            return Result.error("获取关注列表失败: " + e.getMessage());
        }
    }

    @Override
    public Result<?> getFollowerList(Long userId, Integer page, Integer size) {
        try {
            // 模拟数据
            List<Map<String, Object>> followerList = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                Map<String, Object> follower = new HashMap<>();
                follower.put("id", (long) (i + 101));
                follower.put("username", "粉丝" + (i + 1));
                follower.put("avatarUrl", "https://example.com/avatar" + (i + 101) + ".jpg");
                follower.put("bio", "这是粉丝" + (i + 1) + "的个人简介。");
                follower.put("followedAt", System.currentTimeMillis() - i * 86400000);
                followerList.add(follower);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("followerList", followerList);
            result.put("total", 200);
            result.put("page", page);
            result.put("size", size);

            log.info("获取粉丝列表成功: userId={}, page={}, size={}", userId, page, size);
            return Result.success(result);

        } catch (Exception e) {
            log.error("获取粉丝列表失败", e);
            return Result.error("获取粉丝列表失败: " + e.getMessage());
        }
    }

    @Override
    public Result<Users> getCurrentUser(String token) {
        try {
            // 1. 从redis中取出用户信息
            String cacheKey = USER_TOKEN_PREFIX+ token;
            String userStr  = redisService.stringGetString(cacheKey);

            // 2. 解析用户信息
            Users user = JSON.parseObject(userStr, Users.class);
            log.info("获取当前用户信息成功: token={}", token);
            return Result.success(user);

        } catch (Exception e) {
            log.error("获取当前用户信息失败", e);
            return Result.error("获取当前用户信息失败: " + e.getMessage());
        }
    }

    @Override
    public Result<Users> updateCurrentUser(String token, Users users) {
        try {
            // 1. 验证用户信息字段是否合理
            if (users == null || users.getId() == null) {
                return Result.error("用户信息字段不能为空");
            }
            if (users.getUserName() == null || users.getUserName().isEmpty()) {
                return Result.error("用户名不能为空");
            }
            if (users.getEmail() == null || users.getEmail().isEmpty()) {
                return Result.error("邮箱不能为空");
            }

            // 2. 更新数据到数据库
            baseMapper.updateById(users);


            log.info("更新当前用户信息成功: token={}, username={}", token, users.getUserName());
            return Result.success(users);

        } catch (Exception e) {
            log.error("更新当前用户信息失败", e);
            return Result.error("更新当前用户信息失败: " + e.getMessage());
        }
    }

    @Override
    public Result<UserProfile> getCurrentUserProfile(String token) {
        try {
            // 模拟当前用户个人资料
            UserProfile profile = UserProfile.builder()
                    .id(1L)
                    .userId(1L)
                    .gender("男")
                    .birthDate(LocalDate.of(1990, 1, 1))
                    .location("北京市")
                    .build();

            log.info("获取当前用户个人资料成功: token={}", token);
            return Result.success(profile);

        } catch (Exception e) {
            log.error("获取当前用户个人资料失败", e);
            return Result.error("获取当前用户个人资料失败: " + e.getMessage());
        }
    }

    @Override
    public Result<UserProfile> updateCurrentUserProfile(String token, UpdateUserProfileRequest requestBody) {
        try {
            // 1. 从redis中取出用户信息
            String cacheKey = USER_TOKEN_PREFIX + token;
            String userStr = redisService.stringGetString(cacheKey);
            if (userStr == null) {
                return Result.error("用户未登录或token已过期");
            }

            // 2. 解析用户信息
            Users user = JSON.parseObject(userStr, Users.class);
            if (user == null) {
                return Result.error("无法获取用户信息");
            }

            // 3. 从数据库获取用户档案
            UserProfile profile = userProfileMapper.selectOne(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserProfile>()
                            .eq(UserProfile::getUserId, user.getId())
            );

            // 4. 如果用户档案不存在，创建新的
            if (profile == null) {
                profile = new UserProfile();
                profile.setUserId(user.getId());
                profile.setCreatedAt(java.time.LocalDateTime.now());
            }

            // 5. 更新用户档案信息
            if (requestBody.getFullName() != null) {
                profile.setFullName(requestBody.getFullName());
            }
            if (requestBody.getGender() != null) {
                profile.setGender(requestBody.getGender());
            }
            if (requestBody.getBirthDate() != null) {
                try {
                    profile.setBirthDate(LocalDate.parse(requestBody.getBirthDate()));
                } catch (Exception e) {
                    log.error("出生日期格式错误", e);
                }
            }
            if (requestBody.getLocation() != null) {
                profile.setLocation(requestBody.getLocation());
            }
            if (requestBody.getBio() != null) {
                profile.setBio(requestBody.getBio());
            }
            if (requestBody.getAvatarUrl() != null) {
                profile.setAvatarUrl(requestBody.getAvatarUrl());
            }
            if (requestBody.getWebsite() != null) {
                profile.setWebsite(requestBody.getWebsite());
            }
            if (requestBody.getOccupation() != null) {
                profile.setOccupation(requestBody.getOccupation());
            }
            if (requestBody.getEducation() != null) {
                profile.setEducation(requestBody.getEducation());
            }
            if (requestBody.getTravelPreferences() != null) {
                profile.setTravelPreferences(requestBody.getTravelPreferences());
            }
            if (requestBody.getVisitedCities() != null) {
                profile.setVisitedCities(requestBody.getVisitedCities());
            }
            profile.setUpdatedAt(java.time.LocalDateTime.now());

            // 6. 保存到数据库
            if (profile.getId() == null) {
                userProfileMapper.insert(profile);
            } else {
                userProfileMapper.updateById(profile);
            }

            log.info("更新当前用户个人资料成功: token={}, userId={}", token, user.getId());
            return Result.success(profile);

        } catch (Exception e) {
            log.error("更新当前用户个人资料失败", e);
            return Result.error("更新当前用户个人资料失败: " + e.getMessage());
        }
    }

    @Override
    public Result<Users> getUserById(Long id) {
        try {
            // 模拟根据ID获取用户
            Users user = Users.builder()
                    .id(id)
                    .userName("用户" + id)
                    .email("user" + id + "@example.com")
                    .phone("1380013800" + (id % 10))
                    .avatarUrl("https://example.com/avatar" + id + ".jpg")
                    .bio("这是用户" + id + "的个人简介。")
                    .role("ROLE_USER")
                    .isActive(true)
                    .isVerified(true)
                    .build();

            log.info("根据ID获取用户成功: id={}", id);
            return Result.success(user);

        } catch (Exception e) {
            log.error("根据ID获取用户失败", e);
            return Result.error("根据ID获取用户失败: " + e.getMessage());
        }
    }
}