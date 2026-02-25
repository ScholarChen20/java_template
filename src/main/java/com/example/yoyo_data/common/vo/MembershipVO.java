package com.example.yoyo_data.common.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 会员信息响应VO
 *
 * @author yoyo_data
 * @date 2026-02-25
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "会员信息响应")
public class MembershipVO {

    @ApiModelProperty(value = "用户ID")
    private Long userId;

    @ApiModelProperty(value = "会员等级")
    private String memberLevel;

    @ApiModelProperty(value = "积分")
    private Integer points;

    @ApiModelProperty(value = "会员权益列表")
    private List<String> benefits;

    @ApiModelProperty(value = "升级到下一等级所需积分")
    private Integer nextLevelPoints;

    @ApiModelProperty(value = "会员有效期")
    private String validUntil;
}
