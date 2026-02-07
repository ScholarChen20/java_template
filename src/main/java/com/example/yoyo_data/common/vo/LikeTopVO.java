package com.example.yoyo_data.common.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LikeTopVO {
    /**
     * 目标ID（帖子或评论）
     */
    private Long targetId;
    /**
     * 目标类型（post或comment）
     */
    private String targetType;
    /**
     * 帖子主题
     */
    private String title;
    /**
     * 帖子内容
     */
    private String content;
    /**
     * 帖子图像
     */
    private List<String> mediaUrls;
    /**
     * 帖子标签
     */
    private List<String> tags;
    /**
     * 帖子作者
     */
    private String userName;
    /**
     * 帖子作者ID
     */
    private Long userId;
    /**
     * 帖子状态
     */
    private String status;
    /**
     * 当前点赞总数
     */
    private Integer likeCount;
    /**
     * 帖子创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
