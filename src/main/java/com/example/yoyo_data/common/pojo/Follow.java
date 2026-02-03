package com.example.yoyo_data.common.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 关注表实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@TableName("follows")
public class Follow implements Serializable {
    /**
     * 关注id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     * 关注者id
     */
    @TableField("follower_id")
    private Long followerId;
    /**
     * 被关注者id
     */
    @TableField("following_id")
    private Long followingId;
    /**
     * 创建时间
     */
    @TableField("created_at")
    private LocalDateTime createdAt;
}
