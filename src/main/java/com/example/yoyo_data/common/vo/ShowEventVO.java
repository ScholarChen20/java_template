package com.example.yoyo_data.common.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 演出活动视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShowEventVO {

    /**
     * 活动ID
     */
    private Long id;

    /**
     * 演出名称
     */
    private String showName;

    /**
     * 演出类型：CONCERT-演唱会, MOVIE-电影, DRAMA-话剧
     */
    private String showType;

    /**
     * 场馆名称
     */
    private String venueName;

    /**
     * 演出时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime showTime;

    /**
     * 开票时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime saleStartTime;

    /**
     * 结束售票时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime saleEndTime;

    /**
     * 总座位数
     */
    private Integer totalSeats;

    /**
     * 可售座位数
     */
    private Integer availableSeats;

    /**
     * 已售座位数
     */
    private Integer soldSeats;

    /**
     * 每人限购数量
     */
    private Integer maxBuyLimit;

    /**
     * 海报图片URL
     */
    private String posterUrl;

    /**
     * 演出介绍
     */
    private String description;

    /**
     * 状态：PENDING-待开票, SELLING-售票中, SOLD_OUT-售罄, ENDED-已结束
     */
    private String status;

    /**
     * 最低票价
     */
    private BigDecimal minPrice;

    /**
     * 最高票价
     */
    private BigDecimal maxPrice;
}
