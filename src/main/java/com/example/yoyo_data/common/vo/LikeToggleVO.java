package com.example.yoyo_data.common.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LikeToggleVO {
    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 目标ID（帖子或评论）
     */
    private Long targetId;

    /**
     * 目标类型（post或comment）
     */
    private String targetType;

    /**
     * 是否已点赞
     */
    private Boolean isLiked;

    /**
     * 当前点赞总数
     */
    private Integer likeCount;
}
