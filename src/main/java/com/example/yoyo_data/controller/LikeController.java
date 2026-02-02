package com.example.yoyo_data.controller;

import com.example.yoyo_data.common.Result;
import com.example.yoyo_data.service.LikeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 点赞控制器
 * 处理点赞相关操作
 */
@Slf4j
@RestController
@RequestMapping("/api")
@Api(tags = "点赞管理")
public class LikeController {

    @Autowired
    private LikeService likeService;

    /**
     * 点赞帖子
     *
     * @param postId 帖子ID
     * @param request 请求对象，用于获取当前用户信息
     * @return 点赞结果
     */
    @PostMapping("/posts/{postId}/like")
    @ApiOperation(value = "点赞帖子", notes = "为帖子点赞")
    public Result<Map<String, Object>> likePost(
            @ApiParam(value = "帖子ID") @PathVariable Long postId,
            HttpServletRequest request) {
        // 从请求头获取token
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return likeService.likePost(postId, token);
    }

    /**
     * 取消点赞帖子
     *
     * @param postId 帖子ID
     * @param request 请求对象，用于获取当前用户信息
     * @return 取消点赞结果
     */
    @DeleteMapping("/posts/{postId}/like")
    @ApiOperation(value = "取消点赞帖子", notes = "取消对帖子的点赞")
    public Result<Map<String, Object>> unlikePost(
            @ApiParam(value = "帖子ID") @PathVariable Long postId,
            HttpServletRequest request) {
        // 从请求头获取token
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return likeService.unlikePost(postId, token);
    }

    /**
     * 点赞评论
     *
     * @param commentId 评论ID
     * @param request 请求对象，用于获取当前用户信息
     * @return 点赞结果
     */
    @PostMapping("/comments/{commentId}/like")
    @ApiOperation(value = "点赞评论", notes = "为评论点赞")
    public Result<Map<String, Object>> likeComment(
            @ApiParam(value = "评论ID") @PathVariable Long commentId,
            HttpServletRequest request) {
        // 从请求头获取token
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return likeService.likeComment(commentId, token);
    }

    /**
     * 取消点赞评论
     *
     * @param commentId 评论ID
     * @param request 请求对象，用于获取当前用户信息
     * @return 取消点赞结果
     */
    @DeleteMapping("/comments/{commentId}/like")
    @ApiOperation(value = "取消点赞评论", notes = "取消对评论的点赞")
    public Result<Map<String, Object>> unlikeComment(
            @ApiParam(value = "评论ID") @PathVariable Long commentId,
            HttpServletRequest request) {
        // 从请求头获取token
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return likeService.unlikeComment(commentId, token);
    }

    /**
     * 获取用户的点赞列表
     *
     * @param userId 用户ID
     * @param page 页码，默认 1
     * @param size 每页数量，默认 10
     * @param type 类型，可选 post, comment
     * @return 点赞列表
     */
    @GetMapping("/users/{userId}/likes")
    @ApiOperation(value = "获取用户的点赞列表", notes = "获取用户的点赞记录")
    public Result<Map<String, Object>> getUserLikes(
            @ApiParam(value = "用户ID") @PathVariable Long userId,
            @ApiParam(value = "页码", defaultValue = "1") @RequestParam(defaultValue = "1") Integer page,
            @ApiParam(value = "每页数量", defaultValue = "10") @RequestParam(defaultValue = "10") Integer size,
            @ApiParam(value = "类型") @RequestParam(required = false) String type) {
        return likeService.getUserLikes(userId, page, size, type);
    }
}
