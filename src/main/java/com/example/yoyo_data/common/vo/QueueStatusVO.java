package com.example.yoyo_data.common.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 排队状态响应VO
 *
 * @author yoyo_data
 * @date 2026-02-25
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "排队状态信息")
public class QueueStatusVO {

    @ApiModelProperty(value = "排队ID")
    private String queueId;

    @ApiModelProperty(value = "当前位置")
    private Integer position;

    @ApiModelProperty(value = "预计等待时间（秒）")
    private Integer estimatedWaitTime;

    @ApiModelProperty(value = "状态: WAITING-等待中, READY-可购买, EXPIRED-已过期")
    private String status;

    @ApiModelProperty(value = "是否可以购买")
    private Boolean canBuy;

    @ApiModelProperty(value = "提示信息")
    private String message;
}
