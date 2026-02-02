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

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("follower_id")
    private Long followerId;

    @TableField("following_id")
    private Long followingId;

    @TableField("created_at")
    private LocalDateTime createdAt;
}
