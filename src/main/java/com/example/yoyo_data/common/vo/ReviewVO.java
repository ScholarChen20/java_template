package com.example.yoyo_data.common.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 演出评价视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewVO {

    private Long id;
    private Long userId;
    private String userName;
    private String avatarUrl;
    private Integer rating;
    private String content;
    private List<String> tags;
    private List<String> images;
    private String seatInfo;
    private Boolean isVerifiedPurchase;
    private Integer likeCount;
    private Boolean isLiked;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createdAt;
}
