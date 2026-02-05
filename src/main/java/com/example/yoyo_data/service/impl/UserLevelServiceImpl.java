package com.example.yoyo_data.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.yoyo_data.common.Enum.UserLevel;
import com.example.yoyo_data.common.pojo.Follow;
import com.example.yoyo_data.common.pojo.UserLevelInfo;
import com.example.yoyo_data.common.pojo.Users;
import com.example.yoyo_data.infrastructure.repository.FollowMapper;
import com.example.yoyo_data.infrastructure.repository.UserMapper;
import com.example.yoyo_data.service.UserLevelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class UserLevelServiceImpl implements UserLevelService {
    
    @Autowired
    private FollowMapper followMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    private static final String FOLLOWER_COUNT_CACHE_KEY = "user:follower:count:";
    private static final String USER_LEVEL_CACHE_KEY = "user:level:";
    private static final long CACHE_EXPIRE_HOURS = 1;
    
    @Override
    public int getFollowerCount(Long userId) {
        try {
            String cacheKey = FOLLOWER_COUNT_CACHE_KEY + userId;
            
            // 尝试从Redis缓存获取
            Integer cachedCount = (Integer) redisTemplate.opsForValue().get(cacheKey);
            if (cachedCount != null) {
                return cachedCount;
            }
            
            // 从数据库查询粉丝数
            QueryWrapper<Follow> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("following_id", userId);
            Long count = followMapper.selectCount(queryWrapper);
            
            int followerCount = count != null ? count.intValue() : 0;
            
            // 存入缓存
            redisTemplate.opsForValue().set(cacheKey, followerCount, CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
            
            log.info("获取用户粉丝数成功，用户ID: {}, 粉丝数: {}", userId, followerCount);
            return followerCount;
        } catch (Exception e) {
            log.error("获取用户粉丝数失败，用户ID: {}", userId, e);
            return 0;
        }
    }
    
    @Override
    public UserLevel getUserLevel(Long userId) {
        try {
            int followerCount = getFollowerCount(userId);
            return UserLevel.fromFollowerCount(followerCount);
        } catch (Exception e) {
            log.error("获取用户等级失败，用户ID: {}", userId, e);
            return UserLevel.LEVEL_C;
        }
    }
    
    @Override
    public UserLevelInfo getUserLevelInfo(Long userId) {
        try {
            Users user = userMapper.selectById(userId);
            if (user == null) {
                log.warn("用户不存在，用户ID: {}", userId);
                return null;
            }
            
            UserLevel level = getUserLevel(userId);
            int followerCount = getFollowerCount(userId);
            
            UserLevelInfo info = UserLevelInfo.builder()
                    .userName(user.getUserName())
                    .userId(userId)
                    .level(level)
                    .followerCount(followerCount)
                    .levelName(level.getName())
                    .build();
            
            log.info("获取用户等级信息成功，用户ID: {}, 用户名: {}, 等级: {}, 粉丝数: {}", 
                    userId, user.getUserName(), level.getName(), followerCount);
            
            return info;
        } catch (Exception e) {
            log.error("获取用户等级信息失败，用户ID: {}", userId, e);
            return null;
        }
    }
}
