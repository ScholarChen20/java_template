package com.example.yoyo_data.common.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 演出评价视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShowRatingVO {
    /**
     * 评价数量
     */
    private Integer cnt;
    /**
     * 评价分数
     */
    private Integer rating;
}
