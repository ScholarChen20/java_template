package com.example.yoyo_data.service;

import com.example.yoyo_data.common.Result;
import java.util.Map;

/**
 * 帖子服务接口
 * 处理帖子相关业务逻辑
 */
public interface PostService {

    /**
     * 创建帖子
     *
     * @param token  当前用户的token
     * @param params 创建帖子参数，包含title、content、media_urls、tags、location、status等
     * @return 创建结果
     */
    Result<Map<String, Object>> createPost(String token, Map<String, Object> params);

    /**
     * 获取帖子列表
     *
     * @param page     页码，默认 1
     * @param size     每页数量，默认 10
     * @param status   状态，可选 published, draft
     * @param tag      标签筛选
     * @param location 位置筛选
     * @param sort     排序方式，默认 created_at
     * @return 帖子列表
     */
    Result<Map<String, Object>> getPostList(Integer page, Integer size, String status, String tag, String location, String sort);

    /**
     * 获取帖子详情
     *
     * @param id    帖子ID
     * @param token 当前用户的token（可选）
     * @return 帖子详情
     */
    Result<Map<String, Object>> getPostDetail(Long id, String token);

    /**
     * 更新帖子
     *
     * @param id     帖子ID
     * @param token  当前用户的token
     * @param params 更新参数，包含title、content、tags等
     * @return 更新结果
     */
    Result<Map<String, Object>> updatePost(Long id, String token, Map<String, Object> params);

    /**
     * 删除帖子
     *
     * @param id    帖子ID
     * @param token 当前用户的token
     * @return 删除结果
     */
    Result<Map<String, Object>> deletePost(Long id, String token);
}
