package com.example.yoyo_data.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 演出评价表实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@TableName("tb_show_review")
public class ShowReview implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("show_event_id")
    private Long showEventId;

    @TableField("user_id")
    private Long userId;

    @TableField("order_id")
    private Long orderId;

    /** 评分 1-5 */
    @TableField("rating")
    private Integer rating;

    @TableField("content")
    private String content;

    /** 标签（JSON数组字符串） */
    @TableField("tags")
    private String tags;

    /** 图片URL列表（JSON数组字符串） */
    @TableField("images")
    private String images;

    @TableField("seat_info")
    private String seatInfo;

    @TableField("like_count")
    private Integer likeCount;

    @TableField("is_verified_purchase")
    private Boolean isVerifiedPurchase;

    @TableField("is_deleted")
    private Boolean isDeleted;

    @TableField("created_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updatedAt;
}
