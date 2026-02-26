package com.example.yoyo_data.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.yoyo_data.common.Result;
import com.example.yoyo_data.common.dto.request.PostReviewRequest;
import com.example.yoyo_data.common.vo.ReviewVO;
import com.example.yoyo_data.service.ReviewService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 演出评价控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/ticket")
@Api(tags = "演出评价", description = "演出评价的查询、发布、点赞等操作")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @GetMapping("/shows/{showEventId}/reviews")
    @ApiOperation(value = "获取演出评价列表", notes = "分页查询演出评价（含汇总数据）")
    public Result<Map<String, Object>> getReviews(
            @ApiParam(value = "演出活动ID", required = true) @PathVariable Long showEventId,
            @ApiParam(value = "排序方式：latest-最新, hot-最热") @RequestParam(defaultValue = "latest") String sort,
            @ApiParam(value = "评分筛选（1-5，0或不传=全部）") @RequestParam(defaultValue = "0") Integer rating,
            @ApiParam(value = "页码", defaultValue = "1") @RequestParam(defaultValue = "1") Integer page,
            @ApiParam(value = "每页大小", defaultValue = "10") @RequestParam(defaultValue = "10") Integer size,
            HttpServletRequest request
    ) {
        String token = extractToken(request);
        return reviewService.getReviews(showEventId, sort, rating, page, size, token);
    }

    @PostMapping("/shows/{showEventId}/reviews")
    @ApiOperation(value = "发表演出评价", notes = "需持有已支付订单。需要JWT认证")
    public Result<ReviewVO> postReview(
            @ApiParam(value = "演出活动ID", required = true) @PathVariable Long showEventId,
            @Valid @RequestBody PostReviewRequest req,
            HttpServletRequest request
    ) {
        String token = extractToken(request);
        log.info("发表评价: showEventId={}, orderId={}, rating={}", showEventId, req.getOrderId(), req.getRating());
        return reviewService.postReview(showEventId, req, token);
    }

    @GetMapping("/my-reviews")
    @ApiOperation(value = "查询我的评价", notes = "分页查询当前用户发布的所有评价。需要JWT认证")
    public Result<Page<ReviewVO>> getMyReviews(
            @ApiParam(value = "页码", defaultValue = "1") @RequestParam(defaultValue = "1") Integer page,
            @ApiParam(value = "每页大小", defaultValue = "10") @RequestParam(defaultValue = "10") Integer size,
            HttpServletRequest request
    ) {
        String token = extractToken(request);
        return reviewService.getMyReviews(token, page, size);
    }

    @PostMapping("/reviews/{reviewId}/like")
    @ApiOperation(value = "点赞评价", notes = "需要JWT认证")
    public Result<String> likeReview(
            @ApiParam(value = "评价ID", required = true) @PathVariable Long reviewId,
            HttpServletRequest request
    ) {
        String token = extractToken(request);
        return reviewService.likeReview(reviewId, token);
    }

    @DeleteMapping("/reviews/{reviewId}/like")
    @ApiOperation(value = "取消点赞评价", notes = "需要JWT认证")
    public Result<String> cancelLikeReview(
            @ApiParam(value = "评价ID", required = true) @PathVariable Long reviewId,
            HttpServletRequest request
    ) {
        String token = extractToken(request);
        return reviewService.cancelLikeReview(reviewId, token);
    }

    @GetMapping("/shows/{showEventId}/review-tags")
    @ApiOperation(value = "获取评价热门标签", notes = "统计并返回该演出的热门评价标签")
    public Result<List<String>> getReviewTags(
            @ApiParam(value = "演出活动ID", required = true) @PathVariable Long showEventId
    ) {
        return reviewService.getReviewTags(showEventId);
    }

    private String extractToken(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return token;
    }
}
