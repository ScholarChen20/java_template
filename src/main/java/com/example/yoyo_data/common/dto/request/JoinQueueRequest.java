package com.example.yoyo_data.common.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 加入排队请求DTO
 *
 * @author yoyo_data
 * @date 2026-02-25
 */
@Data
@ApiModel(description = "加入排队请求")
public class JoinQueueRequest {

    @ApiModelProperty(value = "演出活动ID", required = true, example = "1")
    @NotNull(message = "演出活动ID不能为空")
    private Long showEventId;
}
