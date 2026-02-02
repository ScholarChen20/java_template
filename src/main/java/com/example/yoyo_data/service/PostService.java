package com.example.yoyo_data.service;

import com.example.yoyo_data.common.Result;

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
     * @param title 标题
     * @param content 内容
     * @param category 分类
     * @param tags 标签
     * @return 创建结果
     */
    Result<?> createPost(Long userId, String title, String content, String category, String tags);

    /**
     * 更新帖子
     *
     * @param postId 帖子ID
     * @param userId 用户ID
     * @param title 标题
     * @param content 内容
     * @param category 分类
     * @param tags 标签
     * @return 更新结果
     */
    Result<?> updatePost(Long postId, Long userId, String title, String content, String category, String tags);

    /**
     * 删除帖子
     *
     * @param postId 帖子ID
     * @param userId 用户ID
     * @return 删除结果
     */
    Result<?> deletePost(Long postId, Long userId);
}