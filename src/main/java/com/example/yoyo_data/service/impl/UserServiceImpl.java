package com.example.yoyo_data.service.impl;

//import cn.hutool.json.JSON;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.yoyo_data.infrastructure.cache.RedisService;
import com.example.yoyo_data.common.Result;
import com.example.yoyo_data.common.dto.JwtUserDTO;
import com.example.yoyo_data.common.dto.request.UpdateUserProfileRequest;
import com.example.yoyo_data.common.pojo.Follow;
import com.example.yoyo_data.common.pojo.UserProfile;
import com.example.yoyo_data.common.pojo.Users;
import com.example.yoyo_data.infrastructure.repository.FollowMapper;
import com.example.yoyo_data.infrastructure.repository.UserMapper;
import com.example.yoyo_data.infrastructure.repository.UserProfileMapper;
import com.example.yoyo_data.service.UserService;
import com.example.yoyo_data.util.jwt.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.yoyo_data.infrastructure.cache.CacheKeyManager.*;
import static com.example.yoyo_data.infrastructure.cache.CacheKeyManager.CacheTTL.*;

/**
 * 用户服务实现类
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, Users> implements UserService {
    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private RedisService redisService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserProfileMapper userProfileMapper;

    @Autowired
    private FollowMapper followMapper;


    @Override
    public Result<?> toggleFollow(Long userId, Long targetUserId) {
        try {
            // 检查目标用户是否存在
            if (userMapper.selectById(targetUserId) == null) {
                return Result.error("目标用户不存在");
            }

            // 检查是否已经关注
            Follow existingFollow = followMapper.selectOne(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Follow>()
                            .eq(Follow::getFollowerId, userId)
                            .eq(Follow::getFollowingId, targetUserId)
            );

            boolean isFollowing = existingFollow != null;
            if (isFollowing) {
                // 已关注，取消关注
                followMapper.delete(
                        new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Follow>()
                                .eq(Follow::getFollowerId, userId)
                                .eq(Follow::getFollowingId, targetUserId)
                );
            } else {
                // 未关注，添加关注
                Follow follow = new Follow();
                follow.setFollowerId(userId);
                follow.setFollowingId(targetUserId);
                follow.setCreatedAt(java.time.LocalDateTime.now());
                followMapper.insert(follow);
            }

            // 计算关注数和粉丝数
            int followCount = Math.toIntExact(followMapper.selectCount(
                    new LambdaQueryWrapper<Follow>()
                            .eq(Follow::getFollowerId, userId)
            ));

            int followerCount = Math.toIntExact(followMapper.selectCount(
                    new LambdaQueryWrapper<Follow>()
                            .eq(Follow::getFollowingId, targetUserId)
            ));

            Follow follow = new Follow();
            follow.setId(userId);
            follow.setFollowerId(userId);
            follow.setFollowingId(targetUserId);
            follow.setCreatedAt(LocalDateTime.now());

            log.info("{}关注成功: userId={}, targetUserId={}", isFollowing ? "取消" : "", userId, targetUserId);
            return Result.success(follow);

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
                            .eq(Follow::getFollowerId, userId)
                            .eq(Follow::getFollowingId, targetUserId)
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
            // 缓存键
            String cacheKey = USER_FOLLOWER_LIST_PREFIX + userId + ":" + page + ":" + size;

            // 尝试从缓存获取
            String cachedFollowList = redisService.stringGetString(cacheKey);
            if (cachedFollowList != null) {
                Map<String, Object> result = JSON.parseObject(cachedFollowList, Map.class);
                log.info("从缓存获取关注列表成功: userId={}, page={}, size={}", userId, page, size);
                return Result.success(result);
            }

            // 计算分页参数
            int offset = (page - 1) * size;

            // 从数据库获取关注列表
            List<Follow> follows = followMapper.selectList(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Follow>()
                            .eq(Follow::getFollowerId, userId)
                            .orderByDesc(Follow::getCreatedAt)
                            .last("LIMIT " + offset + ", " + size)
            );

            // 计算总数
            int total = Math.toIntExact(followMapper.selectCount(
                    new LambdaQueryWrapper<Follow>()
                            .eq(Follow::getFollowerId, userId)
            ));

            // 构建响应数据
            List<Map<String, Object>> followList = new ArrayList<>();
            for (Follow follow : follows) {
                Users followedUser = userMapper.selectById(follow.getFollowingId());
                if (followedUser != null) {
                    Map<String, Object> followInfo = new HashMap<>();
                    followInfo.put("id", followedUser.getId());
                    followInfo.put("username", followedUser.getUserName());
                    followInfo.put("avatarUrl", followedUser.getAvatarUrl());
                    followInfo.put("bio", followedUser.getBio());
                    followInfo.put("followedAt", follow.getCreatedAt());
                    followList.add(followInfo);
                }
            }

            Map<String, Object> result = new HashMap<>();
            result.put("followList", followList);
            result.put("total", total);
            result.put("page", page);
            result.put("size", size);

            // 存入缓存，设置过期时间为10分钟
            redisService.stringSetString(cacheKey, JSON.toJSONString(result), TWO_HOURS);

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
            // 缓存键
            String cacheKey = USER_FOLLOWER_LIST_PREFIX + userId + ":" + page + ":" + size;

            // 尝试从缓存获取
            String cachedFollowerList = redisService.stringGetString(cacheKey);
            if (cachedFollowerList != null) {
                Map<String, Object> result = JSON.parseObject(cachedFollowerList, Map.class);
                log.info("从缓存获取粉丝列表成功: userId={}, page={}, size={}", userId, page, size);
                return Result.success(result);
            }

            // 计算分页参数
            int offset = (page - 1) * size;

            // 从数据库获取粉丝列表
            List<Follow> follows = followMapper.selectList(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Follow>()
                            .eq(Follow::getFollowingId, userId)
                            .orderByDesc(Follow::getCreatedAt)
                            .last("LIMIT " + offset + ", " + size)
            );

            // 计算总数
            int total = Math.toIntExact(followMapper.selectCount(
                    new LambdaQueryWrapper<Follow>()
                            .eq(Follow::getFollowingId, userId)
            ));

            // 构建响应数据
            List<Map<String, Object>> followerList = new ArrayList<>();
            for (Follow follow : follows) {
                Users followerUser = userMapper.selectById(follow.getFollowerId());
                if (followerUser != null) {
                    Map<String, Object> followerInfo = new HashMap<>();
                    followerInfo.put("id", followerUser.getId());
                    followerInfo.put("username", followerUser.getUserName());
                    followerInfo.put("avatarUrl", followerUser.getAvatarUrl());
                    followerInfo.put("bio", followerUser.getBio());
                    followerInfo.put("followedAt", follow.getCreatedAt());
                    followerList.add(followerInfo);
                }
            }

            Map<String, Object> result = new HashMap<>();
            result.put("followerList", followerList);
            result.put("total", total);
            result.put("page", page);
            result.put("size", size);

            // 存入缓存，设置过期时间为60分钟
            redisService.stringSetString(cacheKey, JSON.toJSONString(result), ONE_HOUR);

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
            String cacheKey = USER_TOKEN_PREFIX + token;
            JwtUserDTO jwtUserDTO = redisService.getCacheObject(cacheKey);

            // 2. 获取id
            Long userId = jwtUserDTO.getId();

            // 3. 构建返回对象
            Users user = baseMapper.selectById(userId);
            log.info("获取当前用户信息成功: token={}", token);
            return Result.success(user);

        } catch (Exception e) {
            log.error("获取当前用户信息失败", e);
            return Result.error("获取当前用户信息失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public Result<Users> updateCurrentUser(String token, Users users) {
        try {
            // 1. 验证token的userId与当前用户id一致
            String cacheKey = USER_TOKEN_PREFIX + token;
            JwtUserDTO jwtUserDTO = redisService.getCacheObject(cacheKey);

            if (!jwtUserDTO.getId().equals(users.getId())) {
                return Result.error("用户id字段不一致");
            }

            // 2. 验证用户信息字段是否合理
            if (users.getUserName() == null || users.getUserName().isEmpty()) {
                return Result.error("用户名不能为空");
            }
            if (users.getEmail() == null || users.getEmail().isEmpty()) {
                return Result.error("邮箱不能为空");
            }

            // 3. 更新数据到数据库
            baseMapper.updateById(users);
            // 4. 删除原有的缓存
            redisService.delete(USER_TOKEN_PREFIX + token);

            // 5. 重新生成 token
            JwtUserDTO jwtUser = new JwtUserDTO();
            jwtUser.setAvatar(users.getAvatarUrl());
            jwtUser.setId(users.getId());
            jwtUser.setUsername(users.getUserName());
            jwtUser.setEmail(users.getEmail());
            jwtUser.setPhone(users.getPhone());
            String newToken = jwtUtils.generateToken(jwtUser);

            // 6. 缓存用户信息到Redis（7天过期）
            redisService.objectSetObject(cacheKey, jwtUser, SEVEN_DAYS);

            log.info("更新当前用户信息成功: token={}, username={}", newToken, users.getUserName());
            return Result.success(users);

        } catch (Exception e) {
            log.error("更新当前用户信息失败", e);
            return Result.error("更新当前用户信息失败: " + e.getMessage());
        }
    }

    @Override
    public Result<UserProfile> getCurrentUserProfile(String token) {
        try {
            // 1. 从redis中取出用户信息
            String cacheKey = USER_TOKEN_PREFIX + token;
            JwtUserDTO jwtUserDTO = redisService.getCacheObject(cacheKey);
            if (jwtUserDTO == null) {
                return Result.error("用户未登录或token已过期");
            }

            // 2. 解析用户id
            long userId = jwtUserDTO.getId();

            // 3. 缓存键
            String profileCacheKey = USER_PROFILE_PREFIX + userId;

            // 4. 尝试从缓存获取用户档案
            String userProfileStr = redisService.stringGetString(profileCacheKey);
            if (userProfileStr != null) {
                UserProfile userProfile = (UserProfile) JSON.parseObject(userProfileStr, UserProfile.class);
                log.info("从缓存获取用户档案成功: userId={}", userId);
                return Result.success(userProfile);
            }

            // 5. 缓存没有则从数据库获取用户档案
            UserProfile profile = userProfileMapper.selectOne(
                    new LambdaQueryWrapper<UserProfile>()
                            .eq(UserProfile::getUserId, userId)
            );

            // 6. 如果用户档案不存在，返回空结果
            if (profile == null) {
                log.info("用户档案不存在: userId={}", userId);
                return Result.success(null);
            }

            // 7. 存入缓存，设置过期时间为2小时
            redisService.stringSetString(profileCacheKey, JSON.toJSONString(profile), TWO_HOURS);

            log.info("获取当前用户个人资料成功: token={}, userId={}", token, userId);
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
            JwtUserDTO jwtUserDTO = redisService.getCacheObject(cacheKey);
            if (jwtUserDTO == null) {
                return Result.error("用户未登录或token已过期");
            }

            // 2. 解析用户信息
            long userId = jwtUserDTO.getId();

            // 3. 从数据库获取用户档案
            UserProfile profile = userProfileMapper.selectOne(
                    new LambdaQueryWrapper<UserProfile>()
                            .eq(UserProfile::getUserId, userId)
            );

            // 4. 如果用户档案不存在，创建新的
            if (profile == null) {
                profile = new UserProfile();
                profile.setUserId(userId);
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
            if (requestBody.getTravelPreferences() != null) {
                profile.setTravelPreferences(requestBody.getTravelPreferences());
            }
            if (requestBody.getVisitedCities() != null) {
                profile.setVisitedCities(requestBody.getVisitedCities());
            }
            if (requestBody.getTravelStats() != null) {
                profile.setTravelStats(requestBody.getTravelStats());
            }
            profile.setUpdatedAt(java.time.LocalDateTime.now());

            // 6. 保存到数据库
            String profileCacheKey = USER_PROFILE_PREFIX + userId;
            if (profile.getId() == null) {
                userProfileMapper.insert(profile);
            } else {
                LambdaQueryWrapper<UserProfile> lambdaQueryWrapper = new LambdaQueryWrapper<UserProfile>()
                        .eq(UserProfile::getId, profile.getId());
                userProfileMapper.update(profile, lambdaQueryWrapper);
                // 7. 删除缓存 --保证数据一致性
                redisService.delete(profileCacheKey);
            }


            log.info("更新当前用户个人资料成功: token={}, userId={}", token, userId);
            return Result.success(profile);

        } catch (Exception e) {
            log.error("更新当前用户个人资料失败", e);
            return Result.error("更新当前用户个人资料失败: " + e.getMessage());
        }
    }

    @Override
    public Result<Users> getUserById(Long id) {
        try {
            // 从数据库获取用户信息
            Users user = userMapper.selectById(id);
            if (user == null) {
                return Result.error("用户不存在");
            }

            log.info("根据ID获取用户成功: id={}", id);
            return Result.success(user);

        } catch (Exception e) {
            log.error("根据ID获取用户失败", e);
            return Result.error("根据ID获取用户失败: " + e.getMessage());
        }
    }

    @Override
    public boolean isActive(Long id) {
        return userMapper.isActive(id);
    }

    @Override
    public boolean activeUser(Long id) {
        return userMapper.activeUser(id);
    }
}