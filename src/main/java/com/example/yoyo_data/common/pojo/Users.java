/*
 * Copyright 2013-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.yoyo_data.pojo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;
import org.apache.http.annotation.Contract;

import javax.xml.soap.Text;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author <a href="mailto:chenxilzx1@gmail.com">theonefx</a>
 */
@Data
@Builder
@EqualsAndHashCode(callSuper = false)
@TableName("users")
public class Users implements Serializable {

    @TableField("id")
    private Integer id;

    @TableField("username")
    private String userName;

    @TableField("password_hash")
    private String password;

    @TableField("email")
    private String email;

    @TableField("phone")
    private String phone;

    @TableField("avatar_url")
    private String avatarUrl;

    @TableField("bio")
    private String bio;

    @TableField("role")
    private String role;

    @TableField("is_active")
    private Integer isActive;

    @TableField("is_verified")
    private Integer isVerified;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;

    @TableField("last_login_at")
    private LocalDateTime lastLoginAt;


}
