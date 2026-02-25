package com.example.yoyo_data.common.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 购物车响应VO
 *
 * @author yoyo_data
 * @date 2026-02-25
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "购物车信息")
public class CartVO {

    @ApiModelProperty(value = "购物车项列表")
    private List<CartItemVO> items;

    @ApiModelProperty(value = "购物车项数量")
    private Integer totalItems;

    @ApiModelProperty(value = "总金额")
    private BigDecimal totalAmount;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CartItemVO {
        @ApiModelProperty(value = "购物车项ID")
        private Long id;

        @ApiModelProperty(value = "演出活动ID")
        private Long showEventId;

        @ApiModelProperty(value = "演出名称")
        private String showName;

        @ApiModelProperty(value = "演出时间")
        private String showTime;

        @ApiModelProperty(value = "座位信息列表")
        private List<SeatInfo> seats;

        @ApiModelProperty(value = "小计金额")
        private BigDecimal totalAmount;

        @ApiModelProperty(value = "添加时间")
        private String addedAt;

        @ApiModelProperty(value = "过期时间")
        private String expireAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SeatInfo {
        @ApiModelProperty(value = "座位ID")
        private Long seatId;

        @ApiModelProperty(value = "座位编码")
        private String seatCode;

        @ApiModelProperty(value = "价格")
        private BigDecimal price;
    }
}
