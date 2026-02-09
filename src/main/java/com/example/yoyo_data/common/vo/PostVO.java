package com.example.yoyo_data.common.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
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
public class PostVO {
    private Long id;

    private Long userId;

    private String nickName;

    private String title;

    private String content;

    private List<String> mediaUrls;

    private List<String> tags;

    private String location;

    private String tripPlanId;

    private String tripPlanName;

    private String category;

    private String status;

    private Integer viewCount;

    private Integer likeCount;

    private Integer commentCount;

    private Integer shareCount;

    private Boolean isModerated;

    private String moderationStatus;

    private String moderationReason;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss" ,timezone = "GMT+8")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss" ,timezone = "GMT+8")
    private LocalDateTime updatedAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss" ,timezone = "GMT+8")
    private LocalDateTime publishedAt;
}
