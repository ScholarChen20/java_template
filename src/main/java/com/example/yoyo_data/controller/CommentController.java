package com.example.yoyo_data.controller;

import com.example.yoyo_data.common.Result;
import com.example.yoyo_data.common.dto.CommentDTO;
import com.example.yoyo_data.service.CommentService;
import com.example.yoyo_data.util.jwt.JwtUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 评论模块控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/comments")
@Api(tags = "评论模块", description = "评论的创建、查询、删除等操作")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private JwtUtils jwtUtils;

    /**
     * 获取评论列表
     */
    @GetMapping("/list")
    @ApiOperation(value = "获取评论列表", notes = "获取指定帖子的评论列表")
    public Result<?> getCommentList(
            @ApiParam(value = "帖子ID", required = true) @RequestParam("postId") Long postId,
            @ApiParam(value = "页码", required = false, defaultValue = "1") @RequestParam(value = "page", defaultValue = "1") Integer page,
            @ApiParam(value = "每页大小", required = false, defaultValue = "20") @RequestParam(value = "size", defaultValue = "20") Integer size,
            @ApiParam(value = "排序方式", required = false, defaultValue = "created_at") @RequestParam(value = "sort", required = false) String sort
    ) {
        log.info("获取评论列表: postId={}, page={}, size={}, sort={}", postId, page, size, sort);
        return commentService.getCommentList(postId, page, size, sort);
    }

    /**
     * 创建评论
     */
    @PostMapping("")
    @ApiOperation(value = "创建评论", notes = "创建新评论")
    public Result<?> createComment(@RequestBody CommentDTO commentDTO, HttpServletRequest request) {
        log.info("创建评论: postId={}, content={}, parentId={}", commentDTO.getPostId(), commentDTO.getContent(), commentDTO.getParentId());
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        if (token == null || !jwtUtils.validateToken(token)) {
            return Result.unauthorized("未登录或token已过期");
        }

        return commentService.createComment(commentDTO.getPostId(), token, commentDTO.getContent(), commentDTO.getParentId());
    }

    /**
     * 删除评论
     */
    @DeleteMapping("/{commentId}")
    @ApiOperation(value = "删除评论", notes = "删除指定评论")
    public Result<?> deleteComment(
            @ApiParam(value = "评论ID", required = true) @PathVariable("commentId") Long commentId,
            HttpServletRequest request
    ) {
        log.info("删除评论: commentId={}", commentId);
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        if (token == null || !jwtUtils.validateToken(token)) {
            return Result.unauthorized("未登录或token已过期");
        }

        return commentService.deleteComment(commentId, token);
    }
}
