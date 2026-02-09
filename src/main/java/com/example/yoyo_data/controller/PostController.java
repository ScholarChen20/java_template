package com.example.yoyo_data.controller;

import cn.hutool.core.date.DateTime;
import com.example.yoyo_data.common.Result;
import com.example.yoyo_data.common.dto.PostDTO;
import com.example.yoyo_data.service.PostService;
import com.example.yoyo_data.util.jwt.JwtUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 帖子模块控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/posts")
@Api(tags = "帖子模块", description = "帖子的创建、查询、更新、删除等操作")
public class PostController {
    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private PostService postService;

    /**
     * 获取帖子列表
     * @param page 页码
     * @param size 每页大小
     * @param category 分类
     * @return 帖子列表
     */
    @GetMapping("/list")
    @ApiOperation(value = "获取帖子列表", notes = "获取帖子列表，支持分页和分类筛选")
    public Result<?> getPostList(
            @ApiParam(value = "页码", required = false, defaultValue = "1") @RequestParam(value = "page", defaultValue = "1") Integer page,
            @ApiParam(value = "每页大小", required = false, defaultValue = "10") @RequestParam(value = "size", defaultValue = "10") Integer size,
            @ApiParam(value = "分类", required = false) @RequestParam(value = "category", required = false) String category
    ) {
        log.info("获取帖子列表: page={}, size={}, category={}", page, size, category);
        return postService.getPostList(page, size, category);
    }
    /**
     * 获取帖子列表,模糊查询（查询条件：用户id，标题，内容，发布时间）
     */
    @GetMapping("/page")
    @ApiOperation(value = "模糊查询帖子列表", notes = "模糊查询帖子列表")
    public Result<?> getPostListByCondition(
            @ApiParam(value = "页码", required = false, defaultValue = "1") @RequestParam(value = "page", defaultValue = "1") Integer page,
            @ApiParam(value = "每页大小", required = false, defaultValue = "10") @RequestParam(value = "size", defaultValue = "10") Integer size,
            @ApiParam(value = "用户id", required = false) @RequestParam(value = "userId", required = false) Long userId,
            @ApiParam(value = "标题", required = false) @RequestParam(value = "title", required = false) String title,
            @ApiParam(value = "内容", required = false) @RequestParam(value = "content", required = false) String content,
            @ApiParam(value = "开始时间", required = false) @RequestParam(value = "beginTime", required = false) DateTime beginTime,
            @ApiParam(value = "结束时间", required = false) @RequestParam(value = "endTime", required = false) DateTime endTime
    ) {
        log.info("模糊查询帖子列表: page={}, size={}, userId={}, title={}, content={}, beginTime={}, endTime:{}",
                page, size, userId, title, content, beginTime, endTime);
        return postService.getPostListByCondition(page, size, userId, title, content, beginTime, endTime);
    }

    /**
     * 获取帖子详情
     *
     * @param postId 帖子ID
     * @return 帖子详情
     */
    @GetMapping("/{postId}")
    @ApiOperation(value = "获取帖子详情", notes = "获取帖子的详细信息")
    public Result<?> getPostDetail(@ApiParam(value = "帖子ID", required = true) @PathVariable("postId") Long postId) {
        log.info("获取帖子详情: postId={}", postId);
        return postService.getPostDetail(postId);
    }

    /**
     * 创建帖子
     * @param postDTO 创建帖子请求体
     * @param requestHttp 请求对象，用于获取当前用户信息
     * @return 创建结果
     */
    @PostMapping("")
    @ApiOperation(value = "创建帖子", notes = "创建新帖子")
    public Result<?> createPost(
            @ApiParam(value = "创建帖子请求体", required = true) @RequestBody PostDTO postDTO,
            HttpServletRequest requestHttp
    ) {
        log.info("创建帖子: title={}, category={}, tags={}", postDTO.getTitle(), postDTO.getCategory(), postDTO.getTags());
        String token = requestHttp.getHeader("Authorization");
        if (token == null) {
            return Result.unauthorized("未登录或token已过期");
        }
        // 先移除 Bearer 前缀，再验证
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        if (!jwtUtils.validateToken(token)) {
            return Result.unauthorized("未登录或token已过期");
        }
        Long userId = jwtUtils.getUserIdFromToken(token);
        return postService.createPost(userId, postDTO);
    }

    /**
     * 更新帖子
     *
     * @param postId 帖子ID
     * @param postDTO 更新帖子请求体
     * @param requestHttp 请求对象，用于获取当前用户信息
     * @return 更新结果
     */
    @PutMapping("/{postId}")
    @ApiOperation(value = "更新帖子", notes = "更新帖子信息")
    public Result<?> updatePost(
            @ApiParam(value = "帖子ID", required = true) @PathVariable("postId") Long postId,
            @ApiParam(value = "更新帖子请求体", required = true) @RequestBody PostDTO postDTO,
            HttpServletRequest requestHttp
    ) {
        log.info("更新帖子: postId={}, title={}, category={}, tags={}", postId, postDTO.getTitle(), postDTO.getCategory(), postDTO.getTags());
        String token = requestHttp.getHeader("Authorization");
        if (token == null) {
            return Result.unauthorized("未登录或token已过期");
        }
        // 先移除 Bearer 前缀，再验证
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        if (!jwtUtils.validateToken(token)) {
            return Result.unauthorized("未登录或token已过期");
        }
        Long userId = jwtUtils.getUserIdFromToken(token);
        return postService.updatePost(postId, userId, postDTO);
    }

    /**
     * 删除帖子
     *
     * @param postId 帖子ID
     * @param requestHttp 请求对象，用于获取当前用户信息
     * @return 删除结果
     */
    @DeleteMapping("/{postId}")
    @ApiOperation(value = "删除帖子", notes = "删除帖子")
    public Result<?> deletePost(
            @ApiParam(value = "帖子ID", required = true) @PathVariable("postId") Long postId,
            HttpServletRequest requestHttp
    ) {
        log.info("删除帖子: postId={}", postId);
        String token = requestHttp.getHeader("Authorization");
        if (token == null) {
            return Result.unauthorized("未登录或token已过期");
        }
        // 先移除 Bearer 前缀，再验证
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        if (!jwtUtils.validateToken(token)) {
            return Result.unauthorized("未登录或token已过期");
        }
        Long userId = jwtUtils.getUserIdFromToken(token);
        return postService.deletePost(postId, userId);
    }

    /**
     * 批量删除帖子
     */
    @DeleteMapping("")
    @ApiOperation(value = "批量删除帖子", notes = "批量删除帖子")
    public Result<?> deletePosts(
            @ApiParam(value = "帖子ID列表", required = true) @RequestBody List<Long> postIds,
            HttpServletRequest requestHttp
    ){
        log.info("批量删除帖子: postIds={}", postIds);
        String token = requestHttp.getHeader("Authorization");
        if (token == null){
            return Result.unauthorized("未登录或token已过期");
        }
        // 先移除 Bearer 前缀，再验证
        if (token.startsWith("Bearer ")){
            token = token.substring(7);
        }
        if (!jwtUtils.validateToken(token)){
            return Result.unauthorized("未登录或token已过期");
        }
        Long userId = jwtUtils.getUserIdFromToken(token);
        return postService.deletePosts(postIds, userId);
    }

    /**
     * 获取帖子点赞数topN
     */
    @GetMapping("/topN")
    @ApiOperation(value = "获取帖子点赞数topN", notes = "获取帖子点赞数topN")
    public Result<?> getPostLikeTopN(
            @ApiParam(value = "topN", required = true) @RequestParam("topN") Integer topN
    ) {
        log.info("获取帖子点赞数topN: topN={}", topN);
        return postService.getPostLikeTopN(topN);
    }

}