package com.example.yoyo_data.common.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 帖子标签关联表实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@TableName("post_tags")
public class PostTag implements Serializable {

    @TableField("post_id")
    private Long postId;

    @TableField("tag_id")
    private Integer tagId;

    @TableField("created_at")
    private LocalDateTime createdAt;
}
