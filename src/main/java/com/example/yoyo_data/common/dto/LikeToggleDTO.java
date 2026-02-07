package com.example.yoyo_data.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 点赞/取消点赞响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LikeToggleDTO {
    /**
     * 目标ID-帖子id
     */
    private Long targetId;
    /**
     * 目标类型-post或者comment
     */
    private String targetType;
}
