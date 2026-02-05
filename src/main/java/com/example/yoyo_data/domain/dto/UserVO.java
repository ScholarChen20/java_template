package com.example.yoyo_data.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户信息VO
 *
 * @author yoyo_data
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像URL
     */
    private String avatar;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 个人简介
     */
    private String bio;

    /**
     * 角色
     */
    private String role;

    /**
     * 状态：1-正常，0-禁用
     */
    private Integer status;

    /**
     * 是否激活
     */
    private Boolean isActive;

    /**
     * 是否已验证邮箱
     */
    private Boolean isVerified;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginAt;

    /**
     * 角色列表（扩展字段）
     */
    private List<String> roles;

    /**
     * 权限列表（扩展字段）
     */
    private List<String> permissions;

    /**
     * 备注
     */
    private String remark;
}
