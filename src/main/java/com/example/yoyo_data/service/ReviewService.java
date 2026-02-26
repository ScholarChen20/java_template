package com.example.yoyo_data.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.yoyo_data.common.Result;
import com.example.yoyo_data.common.dto.request.PostReviewRequest;
import com.example.yoyo_data.common.vo.ReviewVO;

import java.util.List;
import java.util.Map;

/**
 * 演出评价服务接口
 */
public interface ReviewService {

    Result<Map<String, Object>> getReviews(Long showEventId, String sort, Integer rating,
                                           Integer page, Integer size, String token);

    Result<ReviewVO> postReview(Long showEventId, PostReviewRequest req, String token);

    Result<Page<ReviewVO>> getMyReviews(String token, Integer page, Integer size);

    Result<String> likeReview(Long reviewId, String token);

    Result<String> cancelLikeReview(Long reviewId, String token);

    Result<List<String>> getReviewTags(Long showEventId);
}
