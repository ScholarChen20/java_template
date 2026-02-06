package com.example.yoyo_data.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.yoyo_data.common.Result;
import com.example.yoyo_data.common.pojo.Comment;
import com.example.yoyo_data.common.pojo.Post;
import com.example.yoyo_data.infrastructure.repository.CommentMapper;
import com.example.yoyo_data.infrastructure.repository.PostMapper;
import com.example.yoyo_data.service.CommentService;
import com.example.yoyo_data.util.jwt.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

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
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private PostMapper postMapper;

    /**
     * 创建评论
     *
     * @param postId 帖子ID
     * @param token  当前用户的token
     * @param params 创建评论参数，包含content、parent_id等
     * @return 创建结果
     */
    @Override
    public Result<Comment> createComment(Long postId, String token, Map<String, Object> params) {
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
        Long parentId = null;
        if (params.containsKey("parent_id") && params.get("parent_id") != null) {
            parentId = Long.valueOf(params.get("parent_id").toString());
            Comment parentComment = commentMapper.selectById(parentId);
            if (parentComment == null) {
                return Result.error("父评论不存在");
            }
        }

        Comment comment = Comment.builder()
                .postId(postId)
                .userId(userId)
                .parentId(parentId)
                .content((String) params.get("content"))
                .likeCount(0)
                .isDeleted(false)
                .build();

        commentMapper.insert(comment);

        // 更新帖子的评论数
        post.setCommentCount(post.getCommentCount() + 1);
        postMapper.updateById(post);

        return Result.success(comment);
    }

    /**
     * 获取评论列表
     *
     * @param postId 帖子ID
     * @param page   页码，默认 1
     * @param size   每页数量，默认 20
     * @param sort   排序方式，默认 created_at
     * @return 评论列表
     */
    @Override
    public Result<Page<Comment>> getCommentList(Long postId, Integer page, Integer size, String sort) {
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

        Page<Comment> result = commentMapper.selectPage(pageParam, queryWrapper);
        return Result.success(result);
    }

    /**
     * 删除评论
     *
     * @param id    评论ID
     * @param token 当前用户的token
     * @return 删除结果
     */
    @Override
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

        return Result.success(null);
    }
}
