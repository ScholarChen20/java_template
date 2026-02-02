package com.example.yoyo_data.controller;

import com.example.yoyo_data.common.Result;
import com.example.yoyo_data.service.PostService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 帖子控制器
 * 处理帖子相关操作
 */
@Slf4j
@RestController
@RequestMapping("/api/posts")
@Api(tags = "帖子管理")
public class PostController {

    @Autowired
    private PostService postService;

    /**
     * 创建帖子
     *
     * @param params 创建帖子参数，包含title、content、media_urls、tags、location、status等
     * @param request 请求对象，用于获取当前用户信息
     * @return 创建结果
     */
    @PostMapping
    @ApiOperation(value = "创建帖子", notes = "创建新帖子")
    public Result<Map<String, Object>> createPost(@ApiParam(value = "创建帖子参数") @RequestBody Map<String, Object> params, HttpServletRequest request) {
        // 从请求头获取token
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return postService.createPost(token, params);
    }

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
    @GetMapping
    @ApiOperation(value = "获取帖子列表", notes = "获取帖子列表，支持分页和筛选")
    public Result<Map<String, Object>> getPostList(
            @ApiParam(value = "页码", defaultValue = "1") @RequestParam(defaultValue = "1") Integer page,
            @ApiParam(value = "每页数量", defaultValue = "10") @RequestParam(defaultValue = "10") Integer size,
            @ApiParam(value = "状态") @RequestParam(required = false) String status,
            @ApiParam(value = "标签") @RequestParam(required = false) String tag,
            @ApiParam(value = "位置") @RequestParam(required = false) String location,
            @ApiParam(value = "排序方式") @RequestParam(required = false) String sort) {
        return postService.getPostList(page, size, status, tag, location, sort);
    }

    /**
     * 获取帖子详情
     *
     * @param id 帖子ID
     * @param request 请求对象，用于获取当前用户信息
     * @return 帖子详情
     */
    @GetMapping("/{id}")
    @ApiOperation(value = "获取帖子详情", notes = "根据帖子ID获取帖子详情")
    public Result<Map<String, Object>> getPostDetail(@ApiParam(value = "帖子ID") @PathVariable Long id, HttpServletRequest request) {
        // 从请求头获取token
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return postService.getPostDetail(id, token);
    }

    /**
     * 更新帖子
     *
     * @param id     帖子ID
     * @param params 更新参数，包含title、content、tags等
     * @param request 请求对象，用于获取当前用户信息
     * @return 更新结果
     */
    @PutMapping("/{id}")
    @ApiOperation(value = "更新帖子", notes = "更新帖子信息")
    public Result<Map<String, Object>> updatePost(@ApiParam(value = "帖子ID") @PathVariable Long id, @ApiParam(value = "更新参数") @RequestBody Map<String, Object> params, HttpServletRequest request) {
        // 从请求头获取token
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return postService.updatePost(id, token, params);
    }

    /**
     * 删除帖子
     *
     * @param id 帖子ID
     * @param request 请求对象，用于获取当前用户信息
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除帖子", notes = "删除帖子")
    public Result<Map<String, Object>> deletePost(@ApiParam(value = "帖子ID") @PathVariable Long id, HttpServletRequest request) {
        // 从请求头获取token
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return postService.deletePost(id, token);
    }
}
