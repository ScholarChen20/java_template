package com.example.yoyo_data.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 演出活动表实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@TableName(value = "tb_show_event")
public class ShowEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 活动ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 演出名称
     */
    @TableField("show_name")
    private String showName;

    /**
     * 演出类型：CONCERT-演唱会, MOVIE-电影, DRAMA-话剧
     */
    @TableField("show_type")
    private String showType;

    /**
     * 场馆ID
     */
    @TableField("venue_id")
    private Long venueId;

    /**
     * 场馆名称
     */
    @TableField("venue_name")
    private String venueName;

    /**
     * 城市
     */
    @TableField("city")
    private String city;

    /**
     * 演出分类：CONCERT/DRAMA/MUSICAL/SPORTS/COMEDY/CHILDREN
     */
    @TableField("category")
    private String category;

    /**
     * 演出时间
     */
    @TableField("show_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss" ,timezone = "GMT+8")
    private LocalDateTime showTime;

    /**
     * 开票时间
     */
    @TableField("sale_start_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss" ,timezone = "GMT+8")
    private LocalDateTime saleStartTime;

    /**
     * 结束售票时间
     */
    @TableField("sale_end_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss" ,timezone = "GMT+8")
    private LocalDateTime saleEndTime;

    /**
     * 总座位数
     */
    @TableField("total_seats")
    private Integer totalSeats;

    /**
     * 可售座位数
     */
    @TableField("available_seats")
    private Integer availableSeats;

    /**
     * 锁定座位数
     */
    @TableField("locked_seats")
    private Integer lockedSeats;

    /**
     * 已售座位数
     */
    @TableField("sold_seats")
    private Integer soldSeats;

    /**
     * 每人限购数量
     */
    @TableField("max_buy_limit")
    private Integer maxBuyLimit;

    /**
     * 海报图片URL
     */
    @TableField("poster_url")
    private String posterUrl;

    /**
     * 演出介绍
     */
    @TableField("description")
    private String description;

    /**
     * 想看人数
     */
    @TableField("wish_count")
    private Integer wishCount;

    /**
     * 热度评分
     */
    @TableField("hot_score")
    private Integer hotScore;

    /**
     * 状态：PENDING-待开票, SELLING-售票中, SOLD_OUT-售罄, ENDED-已结束
     */
    @TableField("status")
    private String status;

    /**
     * 乐观锁版本号
     */
    @TableField("version")
    private Integer version;

    /**
     * 创建时间
     */
    @TableField("created_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss" ,timezone = "GMT+8")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField("updated_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss" ,timezone = "GMT+8")
    private LocalDateTime updatedAt;
}
