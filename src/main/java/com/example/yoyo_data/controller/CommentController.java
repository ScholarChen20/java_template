package com.example.yoyo_data.controller;

import com.example.yoyo_data.common.Result;
import com.example.yoyo_data.service.CommentService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 评论控制器
 * 处理评论相关操作
 */
@Slf4j
@RestController
@RequestMapping("/api")
@Api(tags = "评论管理")
public class CommentController {

    @Autowired
    private CommentService commentService;

    /**
     * 创建评论
     *
     * @param postId 帖子ID
     * @param params 创建评论参数，包含content、parent_id等
     * @param request 请求对象，用于获取当前用户信息
     * @return 创建结果
     */
    @PostMapping("/posts/{postId}/comments")
    @ApiOperation(value = "创建评论", notes = "为帖子创建评论")
    public Result<Map<String, Object>> createComment(
            @ApiParam(value = "帖子ID") @PathVariable Long postId,
            @ApiParam(value = "创建评论参数") @RequestBody Map<String, Object> params,
            HttpServletRequest request) {
        // 从请求头获取token
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return commentService.createComment(postId, token, params);
    }

    /**
     * 获取评论列表
     *
     * @param postId 帖子ID
     * @param page 页码，默认 1
     * @param size 每页数量，默认 20
     * @param sort 排序方式，默认 created_at
     * @return 评论列表
     */
    @GetMapping("/posts/{postId}/comments")
    @ApiOperation(value = "获取评论列表", notes = "获取帖子的评论列表")
    public Result<Map<String, Object>> getCommentList(
            @ApiParam(value = "帖子ID") @PathVariable Long postId,
            @ApiParam(value = "页码", defaultValue = "1") @RequestParam(defaultValue = "1") Integer page,
            @ApiParam(value = "每页数量", defaultValue = "20") @RequestParam(defaultValue = "20") Integer size,
            @ApiParam(value = "排序方式") @RequestParam(required = false) String sort) {
        return commentService.getCommentList(postId, page, size, sort);
    }

    /**
     * 删除评论
     *
     * @param id 评论ID
     * @param request 请求对象，用于获取当前用户信息
     * @return 删除结果
     */
    @DeleteMapping("/comments/{id}")
    @ApiOperation(value = "删除评论", notes = "删除评论")
    public Result<Map<String, Object>> deleteComment(
            @ApiParam(value = "评论ID") @PathVariable Long id,
            HttpServletRequest request) {
        // 从请求头获取token
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return commentService.deleteComment(id, token);
    }
}
