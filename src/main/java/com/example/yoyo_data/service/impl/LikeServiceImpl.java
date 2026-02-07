package com.example.yoyo_data.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.yoyo_data.common.Result;
import com.example.yoyo_data.common.constant.ToggleType;
import com.example.yoyo_data.common.entity.Comment;
import com.example.yoyo_data.common.entity.Like;
import com.example.yoyo_data.common.entity.Post;
import com.example.yoyo_data.common.entity.Users;
import com.example.yoyo_data.common.vo.LikeListVO;
import com.example.yoyo_data.common.vo.LikeTopVO;
import com.example.yoyo_data.common.vo.LikeUserInfoVO;
import com.example.yoyo_data.common.vo.LikeToggleVO;
import com.example.yoyo_data.infrastructure.cache.RedisService;
import com.example.yoyo_data.infrastructure.message.KafkaProducerTemplate;
import com.example.yoyo_data.infrastructure.message.MessageEvent;
import com.example.yoyo_data.infrastructure.repository.CommentMapper;
import com.example.yoyo_data.infrastructure.repository.LikeMapper;
import com.example.yoyo_data.infrastructure.repository.PostMapper;
import com.example.yoyo_data.infrastructure.repository.UserMapper;
import com.example.yoyo_data.service.LikeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.example.yoyo_data.infrastructure.cache.CacheKeyManager.*;

/**
 * 点赞服务实现类
 * 实现真实的数据库+缓存+Kafka架构
 */
@Slf4j
@Service
public class LikeServiceImpl implements LikeService {

    @Autowired
    private RedisService redisService;

    @Autowired
    private LikeMapper likeMapper;

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private KafkaProducerTemplate kafkaProducerTemplate;

    private static final long CACHE_EXPIRE_TIME = 3600000L; // 1小时

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<LikeToggleVO> toggleLike(Long userId, Long targetId, String targetType) {
        try {
            // 1. 验证targetType
            if (!ToggleType.POST.equals(targetType) && !ToggleType.COMMENT.equals(targetType)) {
                return Result.error("无效的目标类型");
            }

            // 验证目标是否存在
            if (ToggleType.POST.equals(targetType)) {
                Post post = postMapper.selectById(targetId);
                if (post == null) {
                    return Result.error("帖子不存在");
                }
            } else {
                Comment comment = commentMapper.selectById(targetId);
                if (comment == null) {
                    return Result.error("评论不存在");
                }
            }

            // 查询是否已点赞
            LambdaQueryWrapper<Like> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Like::getUserId, userId)
                    .eq(Like::getTargetType, targetType)
                    .eq(Like::getTargetId, targetId);

            Like existingLike = likeMapper.selectOne(queryWrapper);
            boolean isLiked;
            int likeCount;

            if (existingLike != null) {
                // 已点赞，执行取消点赞
                likeMapper.deleteById(existingLike.getId());
                isLiked = false;

                // 更新目标的点赞数(减1)
                likeCount = updateTargetLikeCount(targetType, targetId, -1);

                log.info("取消点赞成功: userId={}, targetId={}, targetType={}", userId, targetId, targetType);

            } else {
                // 未点赞，执行点赞
                Like like = Like.builder()
                        .userId(userId)
                        .targetType(targetType)
                        .targetId(targetId)
                        .createdAt(LocalDateTime.now())
                        .build();

                likeMapper.insert(like);
                isLiked = true;

                // 更新目标的点赞数(加1)
                likeCount = updateTargetLikeCount(targetType, targetId, 1);

                log.info("点赞成功: userId={}, targetId={}, targetType={}", userId, targetId, targetType);
            }

            // 清除缓存
            clearLikeCache(userId, targetId, targetType);

            // 发送Kafka事件
            sendLikeEvent(userId, targetId, targetType, isLiked);

            LikeToggleVO result = LikeToggleVO.builder()
                    .userId(userId)
                    .targetId(targetId)
                    .targetType(targetType)
                    .isLiked(isLiked)
                    .likeCount(likeCount)
                    .build();

            return Result.success(result);

        } catch (Exception e) {
            log.error("点赞操作失败", e);
            return Result.error("点赞操作失败: " + e.getMessage());
        }
    }

    @Override
    public Result<LikeToggleVO> getLikeStatus(Long userId, Long targetId, String targetType) {
        try {
            // 尝试从Redis获取缓存
            String cacheKey = LIKE_STATUS_PREFIX + userId + ":" + targetType + ":" + targetId;
            String cachedStatus = redisService.stringGetString(cacheKey);

            boolean isLiked;
            if (cachedStatus != null) {
                isLiked = Boolean.parseBoolean(cachedStatus);
                log.debug("从Redis获取点赞状态: userId={}, targetId={}, targetType={}, isLiked={}",
                        userId, targetId, targetType, isLiked);
            } else {
                // 从数据库查询
                LambdaQueryWrapper<Like> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(Like::getUserId, userId)
                        .eq(Like::getTargetType, targetType)
                        .eq(Like::getTargetId, targetId);

                Like like = likeMapper.selectOne(queryWrapper);
                isLiked = (like != null);

                // 写入Redis缓存
                redisService.stringSetString(cacheKey, String.valueOf(isLiked), CACHE_EXPIRE_TIME);

                log.debug("从数据库获取点赞状态: userId={}, targetId={}, targetType={}, isLiked={}",
                        userId, targetId, targetType, isLiked);
            }

            LikeToggleVO result = LikeToggleVO.builder()
                    .userId(userId)
                    .targetId(targetId)
                    .targetType(targetType)
                    .isLiked(isLiked)
                    .build();

            return Result.success(result);

        } catch (Exception e) {
            log.error("获取点赞状态失败", e);
            return Result.error("获取点赞状态失败: " + e.getMessage());
        }
    }

    @Override
    public Result<LikeListVO> getLikeList(Long targetId, String targetType, Integer page, Integer size) {
        try {
            // 尝试从Redis获取缓存
            String cacheKey = LIKE_LIST_PREFIX + targetType + ":" + targetId + ":" + page + ":" + size;
            String cachedData = redisService.stringGetString(cacheKey);

            LikeListVO result;
            if (cachedData != null) {
                result = JSON.parseObject(cachedData, LikeListVO.class);
                log.debug("从Redis获取点赞列表: targetId={}, targetType={}, page={}, size={}",
                        targetId, targetType, page, size);
            } else {
                // 从数据库查询
                Page<Like> pageParam = new Page<>(page, size);
                LambdaQueryWrapper<Like> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(Like::getTargetType, targetType)
                        .eq(Like::getTargetId, targetId)
                        .orderByDesc(Like::getCreatedAt);

                Page<Like> likePage = likeMapper.selectPage(pageParam, queryWrapper);

                // 获取用户信息
                List<LikeUserInfoVO> likeList = likePage.getRecords().stream().map(like -> {
                    Users user = userMapper.selectById(like.getUserId());
                    return LikeUserInfoVO.builder()
                            .userId(like.getUserId())
                            .username(user != null ? user.getUserName() : "未知用户")
                            .avatarUrl(user != null ? user.getAvatarUrl() : null)
                            .likeTime(like.getCreatedAt())
                            .build();
                }).collect(Collectors.toList());

                result = LikeListVO.builder()
                        .likeList(likeList)
                        .total(likePage.getTotal())
                        .page(page)
                        .size(size)
                        .build();

                // 写入Redis缓存
                redisService.stringSetString(cacheKey, JSON.toJSONString(result), CACHE_EXPIRE_TIME);

                log.debug("从数据库获取点赞列表: targetId={}, targetType={}, page={}, size={}, total={}",
                        targetId, targetType, page, size, likePage.getTotal());
            }

            return Result.success(result);

        } catch (Exception e) {
            log.error("获取点赞列表失败", e);
            return Result.error("获取点赞列表失败: " + e.getMessage());
        }
    }

    @Override
    public Result<List<LikeTopVO>> getLikeRank(Integer top) {
        try {
            LambdaQueryWrapper<Post> queryWrapper = new LambdaQueryWrapper<>();
            // like_count 排序,取前top个值
            queryWrapper.orderByDesc(Post::getLikeCount);
            queryWrapper.last("LIMIT " + top);
            List<Post> postList = postMapper.selectList(queryWrapper);
            List<LikeTopVO> likeTopVOList = postList.stream().map(post -> LikeTopVO.builder()
                    .targetId(post.getId())
                    .targetType(ToggleType.POST)
                    .title(post.getTitle())
                    .content(post.getContent())
                    .mediaUrls(post.getMediaUrls())
                    .userId(post.getUserId())
                    .status(post.getStatus())
                    .likeCount(post.getLikeCount())
                    .createdAt(post.getCreatedAt())
                    .userName(userMapper.selectById(post.getUserId()).getUserName())
                    .build()).collect(Collectors.toList());
            log.debug("获取点赞排行榜: top={}", top);
            return Result.success(likeTopVOList);
        } catch (Exception e) {
            log.error("获取点赞排行榜失败", e);
            return Result.error("获取点赞排行榜失败: " + e.getMessage());
        }
    }

    /**
     * 更新目标的点赞数
     */
    private int updateTargetLikeCount(String targetType, Long targetId, int delta) {
        int newCount = 0;
        if (ToggleType.POST.equals(targetType)) {
            Post post = postMapper.selectById(targetId);
            if (post != null) {
                newCount = Math.max(0, post.getLikeCount() + delta);
                post.setLikeCount(newCount);
                postMapper.updateById(post);
            }
        } else if (ToggleType.COMMENT.equals(targetType)) {
            Comment comment = commentMapper.selectById(targetId);
            if (comment != null) {
                newCount = Math.max(0, comment.getLikeCount() + delta);
                comment.setLikeCount(newCount);
                commentMapper.updateById(comment);
            }
        }
        return newCount;
    }

    /**
     * 清除点赞相关缓存
     */
    private void clearLikeCache(Long userId, Long targetId, String targetType) {
        try {
            // 清除点赞状态缓存
            String statusKey = LIKE_STATUS_PREFIX + userId + ":" + targetType + ":" + targetId;
            redisService.delete(statusKey);

            // 清除点赞数量缓存
            String countKey = LIKE_COUNT_PREFIX + targetType + ":" + targetId;
            redisService.delete(countKey);

            // 清除点赞列表缓存
            Set<String> listKeys = redisService.keys(LIKE_LIST_PREFIX + targetType + ":" + targetId + ":*");
            if (listKeys != null && !listKeys.isEmpty()) {
                redisService.delete(listKeys);
            }

            log.debug("清除点赞缓存: userId={}, targetId={}, targetType={}", userId, targetId, targetType);

        } catch (Exception e) {
            log.error("清除点赞缓存失败: targetId={}, targetType={}", targetId, targetType, e);
        }
    }

    /**
     * 发送点赞事件到Kafka
     */
    private void sendLikeEvent(Long userId, Long targetId, String targetType, boolean isLiked) {
        try {
            Map<String, Object> eventData = new HashMap<>();
            eventData.put("userId", userId);
            eventData.put("targetId", targetId);
            eventData.put("targetType", targetType);
            eventData.put("isLiked", isLiked);
            eventData.put("timestamp", System.currentTimeMillis());

            MessageEvent event = MessageEvent.builder()
                    .eventType(isLiked ? "LIKE" : "UNLIKE")
                    .source("LikeService")
                    .userId(userId)
                    .data(JSON.toJSONString(eventData))
                    .timestamp(LocalDateTime.now())
                    .createdAt(LocalDateTime.now())
                    .priority(6)
                    .build();

            kafkaProducerTemplate.sendEvent("like-events", event);

            log.debug("发送点赞事件成功: userId={}, targetId={}, targetType={}, isLiked={}",
                    userId, targetId, targetType, isLiked);

        } catch (Exception e) {
            log.error("发送点赞事件失败: userId={}, targetId={}, targetType={}",
                    userId, targetId, targetType, e);
        }
    }
}
