package com.example.yoyo_data.common.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 演出评价汇总视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewSummaryVO {

    private Double avgRating;
    private Integer totalCount;
    /** 评分分布 key=1-5, value=数量 */
    private Map<Integer, Integer> ratingDistribution;
    private List<String> hotTags;
}
