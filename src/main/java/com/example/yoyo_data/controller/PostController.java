package com.example.yoyo_data.controller;

import com.alibaba.fastjson.JSON;
import com.example.yoyo_data.common.Result;
import com.example.yoyo_data.common.dto.request.CreatePostRequest;
import com.example.yoyo_data.common.dto.request.UpdatePostRequest;
import com.example.yoyo_data.infrastructure.message.KafkaProducerTemplate;
import com.example.yoyo_data.infrastructure.message.post.PostMessageEvent;
import com.example.yoyo_data.service.PostService;
import com.example.yoyo_data.util.jwt.JwtUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * 帖子模块控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/posts")
@Api(tags = "帖子模块", description = "帖子的创建、查询、更新、删除等操作")
public class PostController {

    @Autowired
    private PostService postService;

    @Autowired
    private JwtUtils  jwtUtils;

    /**
     * 获取帖子列表
     *
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
     * 获取帖子详情
     *
     * @param postId 帖子ID
     * @return 帖子详情
     */
    @GetMapping("/detail/{postId}")
    @ApiOperation(value = "获取帖子详情", notes = "获取帖子的详细信息")
    public Result<?> getPostDetail(
            @ApiParam(value = "帖子ID", required = true) @PathVariable("postId") Long postId
    ) {
        log.info("获取帖子详情: postId={}", postId);
        return postService.getPostDetail(postId);
    }

    /**
     * 创建帖子
     *
     * @param request 创建帖子请求体
     * @param requestHttp 请求对象，用于获取当前用户信息
     * @return 创建结果
     */
    @PostMapping("/create")
    @ApiOperation(value = "创建帖子", notes = "创建新帖子")
    public Result<?> createPost(
            @ApiParam(value = "创建帖子请求体", required = true) @RequestBody CreatePostRequest request,
            HttpServletRequest requestHttp
    ) {
        log.info("创建帖子: title={}, category={}, tags={}", request.getTitle(), request.getCategory(), request.getTags());
        String token = jwtUtils.getToken(requestHttp);
        try {
            return postService.sendCreatePostMsg(request, token);
        } catch (Exception e) {
            log.error("处理帖子创建请求异常: title={}", request.getTitle(), e);
            return Result.error("处理请求失败，请稍后重试");
        }
    }

    /**
     * 更新帖子
     *
     * @param postId 帖子ID
     * @param request 更新帖子请求体
     * @param requestHttp 请求对象，用于获取当前用户信息
     * @return 更新结果
     */
    @PutMapping("/update/{postId}")
    @ApiOperation(value = "更新帖子", notes = "更新帖子信息")
    public Result<?> updatePost(
            @ApiParam(value = "帖子ID", required = true) @PathVariable("postId") Long postId,
            @ApiParam(value = "更新帖子请求体", required = true) @RequestBody UpdatePostRequest request,
            HttpServletRequest requestHttp
    ) {
        log.info("更新帖子: postId={}, title={}, category={}, tags={}", postId, request.getTitle(), request.getCategory(), request.getTags());
        // 这里需要从请求中获取当前用户ID，暂时硬编码为1
        Long userId = 1L;
        return postService.updatePost(postId, userId, request);
    }

    /**
     * 删除帖子
     *
     * @param postId 帖子ID
     * @param requestHttp 请求对象，用于获取当前用户信息
     * @return 删除结果
     */
    @DeleteMapping("/delete/{postId}")
    @ApiOperation(value = "删除帖子", notes = "删除帖子")
    public Result<?> deletePost(
            @ApiParam(value = "帖子ID", required = true) @PathVariable("postId") Long postId,
            HttpServletRequest requestHttp
    ) {
        log.info("删除帖子: postId={}", postId);
        // 这里需要从请求中获取当前用户ID，暂时硬编码为1
        Long userId = 1L;
        return postService.deletePost(postId, userId);
    }
}