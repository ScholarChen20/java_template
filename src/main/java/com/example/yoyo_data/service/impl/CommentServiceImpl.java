package com.example.yoyo_data.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.yoyo_data.common.Result;
import com.example.yoyo_data.common.constant.KafkaTopic;
import com.example.yoyo_data.common.entity.Comment;
import com.example.yoyo_data.common.entity.Like;
import com.example.yoyo_data.common.entity.Post;
import com.example.yoyo_data.common.vo.CommentVO;
import com.example.yoyo_data.infrastructure.cache.RedisService;
import com.example.yoyo_data.infrastructure.message.KafkaProducerTemplate;
import com.example.yoyo_data.infrastructure.message.MessageEvent;
import com.example.yoyo_data.infrastructure.repository.CommentMapper;
import com.example.yoyo_data.infrastructure.repository.PostMapper;
import com.example.yoyo_data.infrastructure.repository.UserMapper;
import com.example.yoyo_data.infrastructure.repository.UserProfileMapper;
import com.example.yoyo_data.service.CommentService;
import com.example.yoyo_data.util.jwt.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 评论服务实现类
 * 实现评论相关业务逻辑
 */
@Slf4j
@Service
public class CommentServiceImpl implements CommentService {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private RedisService redisService;

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private KafkaProducerTemplate kafkaProducerTemplate;

    private static final String COMMENT_LIST_CACHE_PREFIX = "comment:list:";
    private static final long CACHE_EXPIRE_TIME = 1800000L; // 30分钟
    @Autowired
    private UserProfileMapper userProfileMapper;

    /**
     * 创建评论
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Comment> createComment(Long postId, String token, String content, Long parentId) {
        if (token == null || !jwtUtils.validateToken(token)) {
            return Result.unauthorized("Token无效或已过期");
        }
        Long userId = jwtUtils.getUserIdFromToken(token);

        // 验证帖子是否存在
        Post post = postMapper.selectById(postId);
        if (post == null) {
            return Result.error("帖子不存在");
        }

        // 如果有父评论ID，验证父评论是否存在
        if (parentId != null){
            Comment parentComment = commentMapper.selectById(parentId);
            if (parentComment == null) {
                return Result.error("父评论不存在");
            }
        }

        Comment comment = Comment.builder()
                .postId(postId)
                .userId(userId)
                .parentId(parentId)
                .content(content)
                .likeCount(0)
                .isDeleted(false)
                .build();

        // 清除评论列表缓存
        clearCommentListCache(postId);
        // 发送Kafka事件
        sendCommentCreateEvent(KafkaTopic.COMMENT_CREATE, comment, "评论创建成功");

        log.info("创建评论成功: commentId={}, postId={}, userId={}", comment.getId(), postId, userId);

        return Result.success(comment);
    }

    /**
     * 获取评论列表
     */
    @Override
    public Result<Page<CommentVO>> getCommentList(Long postId, Integer page, Integer size, String sort) {
        try {
            // 尝试从Redis获取缓存
            String cacheKey = COMMENT_LIST_CACHE_PREFIX + postId + ":" + page + ":" + size + ":" + sort;
            String cachedData = redisService.stringGetString(cacheKey);

            Page<CommentVO> commentVOPage;
            if (cachedData != null) {
                commentVOPage = JSON.parseObject(cachedData, Page.class);
                log.debug("从Redis获取评论列表: postId={}, page={}, size={}", postId, page, size);
            } else {
                // 从数据库查询
                Page<Comment> pageParam = new Page<>(page, size);
                LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(Comment::getPostId, postId)
                        .eq(Comment::getIsDeleted, false);

                // 排序
                if ("like_count".equals(sort)) {
                    queryWrapper.orderByDesc(Comment::getLikeCount);
                } else {
                    queryWrapper.orderByDesc(Comment::getCreatedAt);
                }

                Page<Comment> commentPage = commentMapper.selectPage(pageParam, queryWrapper);

                // 构建Page<CommentVO>
                commentVOPage = new Page<>(pageParam.getCurrent(), pageParam.getSize(), pageParam.getTotal());
                List<CommentVO> commentVOStream = commentPage.getRecords().stream().map(
                        comment -> CommentVO.builder()
                                .id(comment.getId())
                                .userId(comment.getUserId())
                                .content(comment.getContent())
                                .likeCount(comment.getLikeCount())
                                .isDeleted(comment.getIsDeleted())
                                .createdAt(comment.getCreatedAt())
                                .updatedAt(comment.getUpdatedAt())
                                .nickname(userProfileMapper.selectById(comment.getUserId()).getFullName())
                                .avatar(userMapper.selectById(comment.getUserId()).getAvatarUrl())
                                .parentId(comment.getParentId())
                                .build()
                ).collect(Collectors.toList());

                commentVOPage.setRecords(commentVOStream);
                // 写入Redis缓存
                redisService.stringSetString(cacheKey, JSON.toJSONString(commentVOPage), CACHE_EXPIRE_TIME);
                log.debug("从数据库获取评论列表: postId={}, page={}, size={}, total={}",
                        postId, page, size, commentVOPage.getTotal());
            }

            return Result.success(commentVOPage);

        } catch (Exception e) {
            log.error("获取评论列表失败", e);
            return Result.error("获取评论列表失败: " + e.getMessage());
        }
    }

    /**
     * 删除评论
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> deleteComment(Long id, String token) {
        if (token == null || !jwtUtils.validateToken(token)) {
            return Result.unauthorized("Token无效或已过期");
        }

        Long userId = jwtUtils.getUserIdFromToken(token);
        Comment comment = commentMapper.selectById(id);

        if (comment == null) {
            return Result.error("评论不存在");
        }

        if (!comment.getUserId().equals(userId)) {
            return Result.error("无权限删除此评论");
        }

        // 软删除：标记为已删除
        comment.setIsDeleted(true);
        commentMapper.updateById(comment);

        // 更新帖子的评论数
        Post post = postMapper.selectById(comment.getPostId());
        if (post != null) {
            post.setCommentCount(Math.max(0, post.getCommentCount() - 1));
            postMapper.updateById(post);
        }

        // 清除评论列表缓存
        clearCommentListCache(comment.getPostId());

        // 发送Kafka事件
        sendCommentEvent(KafkaTopic.COMMENT_DELETE, comment.getPostId(), userId, id, "评论删除成功");

        log.info("删除评论成功: commentId={}, postId={}, userId={}", id, comment.getPostId(), userId);

        return Result.success(null);
    }

    /**
     * 清除评论列表缓存
     */
    private void clearCommentListCache(Long postId) {
        try {
            Set<String> keys = redisService.keys(COMMENT_LIST_CACHE_PREFIX + postId + ":*");
            if (keys != null && !keys.isEmpty()) {
                redisService.delete(keys);
                log.debug("清除评论列表缓存: postId={}, count={}", postId, keys.size());
            }
        } catch (Exception e) {
            log.error("清除评论列表缓存失败: postId={}", postId, e);
        }
    }

    /**
     * 发送评论事件到Kafka
     */
    private void sendCommentEvent(String eventType, Long postId, Long userId, Long commentId, String message) {
        try {
            Map<String, Object> eventData = new HashMap<>();
            eventData.put("postId", postId);
            eventData.put("userId", userId);
            eventData.put("commentId", commentId);
            eventData.put("message", message);
            eventData.put("timestamp", System.currentTimeMillis());

            MessageEvent event = MessageEvent.builder()
                    .eventType(eventType)
                    .source("CommentService")
                    .userId(userId)
                    .data(JSON.toJSONString(eventData))
                    .timestamp(LocalDateTime.now())
                    .createdAt(LocalDateTime.now())
                    .priority(6)
                    .build();

            kafkaProducerTemplate.sendEvent(eventType, event);

            log.debug("发送评论事件成功: eventType={}, commentId={}, postId={}", eventType, commentId, postId);

        } catch (Exception e) {
            log.error("发送评论事件失败: eventType={}, commentId={}", eventType, commentId, e);
        }
    }

    /**
     *  发送创建评论事件到Kafka
     * @param eventType
     * @param comment
     * @param message
     */
    private void sendCommentCreateEvent(String eventType, Comment  comment, String message) {
        try {
            Map<String, Object> eventData = new HashMap<>();
            eventData.put("postId", comment.getPostId());
            eventData.put("userId", comment.getUserId());
            eventData.put("commentObj",  JSON.toJSONString(comment));
            eventData.put("message", message);
            eventData.put("timestamp", System.currentTimeMillis());
            MessageEvent event = MessageEvent.builder()
                    .eventType(eventType)
                    .source("CommentService")
                    .userId(comment.getUserId())
                    .data(JSON.toJSONString(eventData))
                    .timestamp(LocalDateTime.now())
                    .createdAt(LocalDateTime.now())
                    .priority(6)
                    .build();

            kafkaProducerTemplate.sendEvent(eventType, event);
        } catch (Exception e) {
            log.error("发送评论事件失败: eventType={}, commentId={}", eventType, comment.getId(), e);
        }

    }
}
