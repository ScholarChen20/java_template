package com.example.yoyo_data.common.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 帖子表实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@TableName(value = "posts", autoResultMap = true)
public class Post implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("title")
    private String title;

    @TableField("content")
    private String content;

    @TableField(value = "media_urls", typeHandler = JacksonTypeHandler.class)
    private List<String> mediaUrls;

    @TableField(value = "tags", typeHandler = JacksonTypeHandler.class)
    private List<String> tags;

    @TableField("location")
    private String location;

    @TableField("trip_plan_id")
    private String tripPlanId;

    @TableField("status")
    private String status;

    @TableField("view_count")
    private Integer viewCount;

    @TableField("like_count")
    private Integer likeCount;

    @TableField("comment_count")
    private Integer commentCount;

    @TableField("share_count")
    private Integer shareCount;

    @TableField("is_moderated")
    private Boolean isModerated;

    @TableField("moderation_status")
    private String moderationStatus;

    @TableField("moderation_reason")
    private String moderationReason;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;

    @TableField("published_at")
    private LocalDateTime publishedAt;
}
