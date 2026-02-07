package com.example.yoyo_data.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.yoyo_data.common.Result;
import com.example.yoyo_data.common.entity.Comment;
import com.example.yoyo_data.common.vo.CommentVO;

/**
 * 评论服务接口
 * 处理评论相关业务逻辑
 */
public interface CommentService {

    /**
     * 创建评论
     *
     * @param postId 帖子ID
     * @param token  当前用户的token
     * @return 创建结果
     */
    Result<Comment> createComment(Long postId, String token, String content, Long parentId);

    /**
     * 获取评论列表
     *
     * @param postId 帖子ID
     * @param page   页码，默认 1
     * @param size   每页数量，默认 20
     * @param sort   排序方式，默认 created_at
     * @return 评论列表
     */
    Result<Page<CommentVO>> getCommentList(Long postId, Integer page, Integer size, String sort);

    /**
     * 删除评论
     *
     * @param id    评论ID
     * @param token 当前用户的token
     * @return 删除结果
     */
    Result<Void> deleteComment(Long id, String token);
}
