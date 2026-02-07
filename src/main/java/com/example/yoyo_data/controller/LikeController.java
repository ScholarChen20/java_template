package com.example.yoyo_data.controller;

import com.example.yoyo_data.common.Result;
import com.example.yoyo_data.common.vo.LikeToggleVO;
import com.example.yoyo_data.common.vo.LikeListVO;
import com.example.yoyo_data.common.dto.LikeToggleDTO;
import com.example.yoyo_data.common.vo.LikeTopVO;
import com.example.yoyo_data.service.LikeService;
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
 * 点赞模块控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/likes")
@Api(tags = "点赞模块", description = "点赞、取消点赞等操作")
public class LikeController {

    @Autowired
    private LikeService likeService;

    @Autowired
    private JwtUtils jwtUtils;

    /**
     * 点赞/取消点赞
     */
    @PostMapping("/toggle")
    @ApiOperation(value = "点赞/取消点赞", notes = "对帖子或评论进行点赞/取消点赞操作")
    public Result<LikeToggleVO> toggleLike(@RequestBody LikeToggleDTO likeToggleDTO,
            HttpServletRequest request
    ) {
        log.info("点赞/取消点赞: targetId={}, targetType={}", likeToggleDTO.getTargetId(), likeToggleDTO.getTargetType());
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        if (token == null || !jwtUtils.validateToken(token)) {
            return Result.unauthorized("未登录或token已过期");
        }

        Long userId = jwtUtils.getUserIdFromToken(token);
        return likeService.toggleLike(userId, likeToggleDTO.getTargetId(), likeToggleDTO.getTargetType());
    }

    /**
     * 获取点赞状态
     */
    @GetMapping("/status")
    @ApiOperation(value = "获取点赞状态", notes = "获取用户对帖子或评论的点赞状态")
    public Result<LikeToggleVO> getLikeStatus(
            @ApiParam(value = "目标ID", required = true) @RequestParam("targetId") Long targetId,
            @ApiParam(value = "目标类型", required = true) @RequestParam("targetType") String targetType,
            HttpServletRequest request
    ) {
        log.info("获取点赞状态: targetId={}, targetType={}", targetId, targetType);

        // 从请求头获取token
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        if (token == null || !jwtUtils.validateToken(token)) {
            return Result.unauthorized("未登录或token已过期");
        }

        Long userId = jwtUtils.getUserIdFromToken(token);
        return likeService.getLikeStatus(userId, targetId, targetType);
    }

    /**
     * 获取点赞列表（公开接口）
     */
    @GetMapping("/list")
    @ApiOperation(value = "获取点赞列表", notes = "获取帖子或评论的点赞列表")
    public Result<LikeListVO> getLikeList(
            @ApiParam(value = "目标ID", required = true) @RequestParam("targetId") Long targetId,
            @ApiParam(value = "目标类型", required = true) @RequestParam("targetType") String targetType,
            @ApiParam(value = "页码", required = false, defaultValue = "1") @RequestParam(value = "page", defaultValue = "1") Integer page,
            @ApiParam(value = "每页大小", required = false, defaultValue = "10") @RequestParam(value = "size", defaultValue = "10") Integer size
    ) {
        log.info("获取点赞列表: targetId={}, targetType={}, page={}, size={}", targetId, targetType, page, size);
        return likeService.getLikeList(targetId, targetType, page, size);
    }
    /**
     * 获取点赞数top10的帖子
     */
    @GetMapping("/rank")
    @ApiOperation(value = "获取点赞数排行榜的评论", notes = "获取点赞数排行榜的评论")
    public Result<List<LikeTopVO>> getLikeRank(@ApiParam(value = "top10", required = false, defaultValue = "10")
                                             @RequestParam(value = "top", defaultValue = "10")Integer top) {
        log.info("获取点赞数排行榜的评论: top={}", top);
        return likeService.getLikeRank(top);
    }
}
