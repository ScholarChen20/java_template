package com.example.yoyo_data.common.pojo;

import com.example.yoyo_data.common.enums.UserLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户等级信息
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserLevelInfo {
    private Long userId;
    private String userName;
    private UserLevel level;
    private int followerCount;
    private String levelName;
}