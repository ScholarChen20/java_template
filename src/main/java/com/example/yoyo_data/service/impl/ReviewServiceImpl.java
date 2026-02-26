package com.example.yoyo_data.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.yoyo_data.common.Result;
import com.example.yoyo_data.common.constant.OrderStatus;
import com.example.yoyo_data.common.dto.request.PostReviewRequest;
import com.example.yoyo_data.common.entity.Like;
import com.example.yoyo_data.common.entity.ShowReview;
import com.example.yoyo_data.common.entity.TicketOrder;
import com.example.yoyo_data.common.entity.Users;
import com.example.yoyo_data.common.vo.ReviewSummaryVO;
import com.example.yoyo_data.common.vo.ReviewVO;
import com.example.yoyo_data.infrastructure.repository.LikeMapper;
import com.example.yoyo_data.infrastructure.repository.ShowReviewMapper;
import com.example.yoyo_data.infrastructure.repository.TicketOrderMapper;
import com.example.yoyo_data.infrastructure.repository.UserMapper;
import com.example.yoyo_data.service.ReviewService;
import com.example.yoyo_data.util.jwt.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 演出评价服务实现类
 */
@Slf4j
@Service
public class ReviewServiceImpl implements ReviewService {

    private static final String REVIEW_LIKE_TYPE = "REVIEW";

    @Autowired
    private ShowReviewMapper showReviewMapper;

    @Autowired
    private TicketOrderMapper ticketOrderMapper;

    @Autowired
    private LikeMapper likeMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    public Result<Map<String, Object>> getReviews(Long showEventId, String sort, Integer rating,
                                                   Integer page, Integer size, String token) {
        Long currentUserId = getUserId(token);

        // 汇总
        ReviewSummaryVO summary = buildSummary(showEventId);

        // 分页查询
        LambdaQueryWrapper<ShowReview> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShowReview::getShowEventId, showEventId)
                .eq(ShowReview::getIsDeleted, false);
        if (rating != null && rating > 0) {
            wrapper.eq(ShowReview::getRating, rating);
        }
        if ("hot".equals(sort)) {
            wrapper.orderByDesc(ShowReview::getLikeCount);
        } else {
            wrapper.orderByDesc(ShowReview::getCreatedAt);
        }

        Page<ShowReview> reviewPage = showReviewMapper.selectPage(new Page<>(page, size), wrapper);
        List<ReviewVO> voList = reviewPage.getRecords().stream()
                .map(r -> toVO(r, currentUserId))
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("summary", summary);
        result.put("total", reviewPage.getTotal());
        result.put("current", reviewPage.getCurrent());
        result.put("size", reviewPage.getSize());
        result.put("records", voList);

        return Result.success(result);
    }

    @Override
    @Transactional
    public Result<ReviewVO> postReview(Long showEventId, PostReviewRequest req, String token) {
        Long userId = getUserId(token);
        if (userId == null) return Result.unauthorized("请先登录");

        // 校验订单归属
        TicketOrder order = ticketOrderMapper.selectById(req.getOrderId());
        if (order == null || !order.getUserId().equals(userId)) {
            return Result.error(400, "订单不存在或不属于当前用户");
        }
        if (!order.getShowEventId().equals(showEventId)) {
            return Result.error(400, "订单与演出不匹配");
        }
        if (!OrderStatus.PAID.equals(order.getStatus()) && !OrderStatus.USED.equals(order.getStatus())) {
            return Result.error(400, "只有已支付的订单才能评价");
        }

        // 检查是否已评价
        LambdaQueryWrapper<ShowReview> existWrapper = new LambdaQueryWrapper<>();
        existWrapper.eq(ShowReview::getOrderId, req.getOrderId());
        if (showReviewMapper.selectOne(existWrapper) != null) {
            return Result.error(400, "该订单已评价");
        }

        ShowReview review = ShowReview.builder()
                .showEventId(showEventId)
                .userId(userId)
                .orderId(req.getOrderId())
                .rating(req.getRating())
                .content(req.getContent())
                .tags(req.getTags() != null ? JSON.toJSONString(req.getTags()) : null)
                .images(req.getImages() != null ? JSON.toJSONString(req.getImages()) : null)
                .likeCount(0)
                .isVerifiedPurchase(true)
                .isDeleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        showReviewMapper.insert(review);
        return Result.success(toVO(review, userId));
    }

    @Override
    public Result<Page<ReviewVO>> getMyReviews(String token, Integer page, Integer size) {
        Long userId = getUserId(token);
        if (userId == null) return Result.unauthorized("请先登录");

        LambdaQueryWrapper<ShowReview> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShowReview::getUserId, userId)
                .eq(ShowReview::getIsDeleted, false)
                .orderByDesc(ShowReview::getCreatedAt);

        Page<ShowReview> reviewPage = showReviewMapper.selectPage(new Page<>(page, size), wrapper);
        Page<ReviewVO> voPage = new Page<>(reviewPage.getCurrent(), reviewPage.getSize(), reviewPage.getTotal());
        voPage.setRecords(reviewPage.getRecords().stream()
                .map(r -> toVO(r, userId))
                .collect(Collectors.toList()));

        return Result.success(voPage);
    }

    @Override
    @Transactional
    public Result<String> likeReview(Long reviewId, String token) {
        Long userId = getUserId(token);
        if (userId == null) return Result.unauthorized("请先登录");

        ShowReview review = showReviewMapper.selectById(reviewId);
        if (review == null || Boolean.TRUE.equals(review.getIsDeleted())) {
            return Result.error(404, "评价不存在");
        }

        LambdaQueryWrapper<Like> existWrapper = new LambdaQueryWrapper<>();
        existWrapper.eq(Like::getUserId, userId)
                .eq(Like::getTargetType, REVIEW_LIKE_TYPE)
                .eq(Like::getTargetId, reviewId);
        if (likeMapper.selectOne(existWrapper) != null) {
            return Result.error(400, "已点赞");
        }

        Like like = Like.builder()
                .userId(userId)
                .targetType(REVIEW_LIKE_TYPE)
                .targetId(reviewId)
                .createdAt(LocalDateTime.now())
                .build();
        likeMapper.insert(like);

        LambdaUpdateWrapper<ShowReview> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ShowReview::getId, reviewId)
                .setSql("like_count = like_count + 1");
        showReviewMapper.update(null, updateWrapper);

        return Result.success("点赞成功");
    }

    @Override
    @Transactional
    public Result<String> cancelLikeReview(Long reviewId, String token) {
        Long userId = getUserId(token);
        if (userId == null) return Result.unauthorized("请先登录");

        LambdaQueryWrapper<Like> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Like::getUserId, userId)
                .eq(Like::getTargetType, REVIEW_LIKE_TYPE)
                .eq(Like::getTargetId, reviewId);
        int deleted = likeMapper.delete(wrapper);

        if (deleted > 0) {
            LambdaUpdateWrapper<ShowReview> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(ShowReview::getId, reviewId)
                    .setSql("like_count = GREATEST(like_count - 1, 0)");
            showReviewMapper.update(null, updateWrapper);
        }

        return Result.success("取消点赞成功");
    }

    @Override
    public Result<List<String>> getReviewTags(Long showEventId) {
        List<String> tagJsonList = showReviewMapper.selectTagsByShowEventId(showEventId);
        Map<String, Integer> tagCount = new HashMap<>();
        for (String tagsJson : tagJsonList) {
            if (!StringUtils.hasText(tagsJson)) continue;
            try {
                List<String> tags = JSON.parseArray(tagsJson, String.class);
                for (String tag : tags) {
                    tagCount.merge(tag, 1, Integer::sum);
                }
            } catch (Exception ignored) {}
        }
        List<String> hotTags = tagCount.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(10)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        return Result.success(hotTags);
    }

    private ReviewSummaryVO buildSummary(Long showEventId) {
        LambdaQueryWrapper<ShowReview> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShowReview::getShowEventId, showEventId).eq(ShowReview::getIsDeleted, false);
        List<ShowReview> reviews = showReviewMapper.selectList(wrapper);

        if (reviews.isEmpty()) {
            return ReviewSummaryVO.builder()
                    .avgRating(0.0)
                    .totalCount(0)
                    .ratingDistribution(new HashMap<>())
                    .hotTags(Collections.emptyList())
                    .build();
        }

        double avgRating = reviews.stream().mapToInt(ShowReview::getRating).average().orElse(0.0);
        Map<Integer, Integer> distribution = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            final int r = i;
            distribution.put(r, (int) reviews.stream().filter(rev -> rev.getRating() == r).count());
        }

        return ReviewSummaryVO.builder()
                .avgRating(Math.round(avgRating * 10.0) / 10.0)
                .totalCount(reviews.size())
                .ratingDistribution(distribution)
                .hotTags(Collections.emptyList())
                .build();
    }

    private ReviewVO toVO(ShowReview review, Long currentUserId) {
        List<String> tags = null;
        List<String> images = null;
        try {
            if (StringUtils.hasText(review.getTags())) {
                tags = JSON.parseArray(review.getTags(), String.class);
            }
            if (StringUtils.hasText(review.getImages())) {
                images = JSON.parseArray(review.getImages(), String.class);
            }
        } catch (Exception ignored) {}

        boolean isLiked = false;
        if (currentUserId != null) {
            LambdaQueryWrapper<Like> likeWrapper = new LambdaQueryWrapper<>();
            likeWrapper.eq(Like::getUserId, currentUserId)
                    .eq(Like::getTargetType, REVIEW_LIKE_TYPE)
                    .eq(Like::getTargetId, review.getId());
            isLiked = likeMapper.selectOne(likeWrapper) != null;
        }

        // 获取用户信息
        String userName = null;
        String avatarUrl = null;
        try {
            Users user = userMapper.selectById(review.getUserId());
            if (user != null) {
                userName = user.getUserName();
                avatarUrl = user.getAvatarUrl();
            }
        } catch (Exception ignored) {}

        return ReviewVO.builder()
                .id(review.getId())
                .userId(review.getUserId())
                .userName(userName)
                .avatarUrl(avatarUrl)
                .rating(review.getRating())
                .content(review.getContent())
                .tags(tags)
                .images(images)
                .seatInfo(review.getSeatInfo())
                .isVerifiedPurchase(review.getIsVerifiedPurchase())
                .likeCount(review.getLikeCount())
                .isLiked(isLiked)
                .createdAt(review.getCreatedAt())
                .build();
    }

    private Long getUserId(String token) {
        if (!StringUtils.hasText(token)) return null;
        if (!Boolean.TRUE.equals(jwtUtils.validateToken(token))) return null;
        return jwtUtils.getUserIdFromToken(token);
    }
}
