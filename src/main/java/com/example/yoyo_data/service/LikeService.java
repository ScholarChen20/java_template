package com.example.yoyo_data.service;

import com.example.yoyo_data.common.Result;
import java.util.Map;

/**
 * 点赞服务接口
 * 处理点赞相关业务逻辑
 */
public interface LikeService {

    /**
     * 点赞帖子
     *
     * @param postId 帖子ID
     * @param token  当前用户的token
     * @return 点赞结果
     */
    Result<Map<String, Object>> likePost(Long postId, String token);

    /**
     * 取消点赞帖子
     *
     * @param postId 帖子ID
     * @param token  当前用户的token
     * @return 取消点赞结果
     */
    Result<Map<String, Object>> unlikePost(Long postId, String token);

    /**
     * 点赞评论
     *
     * @param commentId 评论ID
     * @param token     当前用户的token
     * @return 点赞结果
     */
    Result<Map<String, Object>> likeComment(Long commentId, String token);

    /**
     * 取消点赞评论
     *
     * @param commentId 评论ID
     * @param token     当前用户的token
     * @return 取消点赞结果
     */
    Result<Map<String, Object>> unlikeComment(Long commentId, String token);

    /**
     * 获取用户的点赞列表
     *
     * @param userId 用户ID
     * @param page   页码，默认 1
     * @param size   每页数量，默认 10
     * @param type   类型，可选 post, comment
     * @return 点赞列表
     */
    Result<Map<String, Object>> getUserLikes(Long userId, Integer page, Integer size, String type);
}
