package com.example.yoyo_data.service;

import cn.hutool.core.date.DateTime;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.yoyo_data.common.Result;
import com.example.yoyo_data.common.dto.PostDTO;
import com.example.yoyo_data.common.entity.Post;

import java.util.List;

/**
 * 帖子服务接口
 */
public interface PostService extends IService<Post> {
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
     * @param postDTO 创建帖子请求体
     * @return 创建结果
     */
    Result<?> createPost(Long userId, PostDTO postDTO);

    /**
     * 更新帖子
     *
     * @param postId 帖子ID
     * @param userId 用户id
     * @param postDTO 帖子DTO
     * @return 更新结果
     */
    Result<?> updatePost(Long postId, Long userId, PostDTO postDTO);

    /**
     * 删除帖子
     *
     * @param postId 帖子ID
     * @param userId 用户id
     * @return 删除结果
     */
    Result<?> deletePost(Long postId, Long userId);

    Result<?> getPostListByCondition(Integer page, Integer size, Long userId, String title, String content, DateTime publishTime, DateTime endTime);

    Result<?> deletePosts(List<Long> postIds, Long userId);

    Result<?> getPostLikeTopN(Integer topN);
}