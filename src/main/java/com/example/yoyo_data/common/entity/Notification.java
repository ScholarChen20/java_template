package com.example.yoyo_data.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@TableName("notifications")
public class Notification implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("type")
    private String type;

    @TableField("title")
    private String title;

    @TableField("content")
    private String content;

    @TableField("related_type")
    private String relatedType;

    @TableField("related_id")
    private Long relatedId;

    @TableField("is_read")
    private Boolean isRead;

    @TableField("read_at")
    private LocalDateTime readAt;

    @TableField("action_url")
    private String actionUrl;

    @TableField("extra_data")
    private String extraData;

    @TableField("created_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss" ,timezone = "GMT+8")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss" ,timezone = "GMT+8")
    private LocalDateTime updatedAt;
}
