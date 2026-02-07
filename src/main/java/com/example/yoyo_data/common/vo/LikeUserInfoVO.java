package com.example.yoyo_data.common.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 点赞用户信息DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LikeUserInfoVO {
    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 头像URL
     */
    private String avatarUrl;

    /**
     * 点赞时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime likeTime;
}
