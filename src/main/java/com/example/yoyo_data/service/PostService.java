package com.example.yoyo_data.service;

import com.example.yoyo_data.common.Result;
import com.example.yoyo_data.common.dto.request.CreatePostRequest;
import com.example.yoyo_data.common.dto.request.UpdatePostRequest;

import javax.servlet.http.HttpServletRequest;

/**
 * 帖子服务接口
 */
public interface PostService {
    /**
     * 获取帖子列表
     *
     * @param page 页码
     * @param size 每页大小
     * @param category 分类
     * @return 帖子列表
     */
    Result<?> getPostList(Integer page, Integer size, String category);

    /**
     * 获取帖子详情
     *
     * @param postId 帖子ID
     * @return 帖子详情
     */
    Result<?> getPostDetail(Long postId);

    /**
     * 创建帖子
     *
     * @param userId 用户ID
     * @param request 创建帖子请求体
     * @return 创建结果
     */
    Result<?> createPost(Long userId, CreatePostRequest request);

    /**
     * 更新帖子
     *
     * @param postId 帖子ID
     * @param userId 用户ID
     * @param request 更新帖子请求体
     * @return 更新结果
     */
    Result<?> updatePost(Long postId, Long userId, UpdatePostRequest request);

    /**
     * 删除帖子
     *
     * @param postId 帖子ID
     * @param userId 用户ID
     * @return 删除结果
     */
    Result<?> deletePost(Long postId, Long userId);

    Result<?>  sendCreatePostMsg(CreatePostRequest request, String  token);
}