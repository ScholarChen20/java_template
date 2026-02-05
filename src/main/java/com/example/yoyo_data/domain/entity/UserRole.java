package com.example.yoyo_data.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.io.Serializable;

/**
 * 用户角色关联表实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@TableName("user_roles")
public class UserRole implements Serializable {

    @TableField("user_id")
    private Long userId;

    @TableField("role_id")
    private Integer roleId;
}
