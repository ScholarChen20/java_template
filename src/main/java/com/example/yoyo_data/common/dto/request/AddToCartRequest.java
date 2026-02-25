package com.example.yoyo_data.common.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 添加到购物车请求DTO
 *
 * @author yoyo_data
 * @date 2026-02-25
 */
@Data
@ApiModel(description = "添加到购物车请求")
public class AddToCartRequest {

    @ApiModelProperty(value = "演出活动ID", required = true, example = "1")
    @NotNull(message = "演出活动ID不能为空")
    private Long showEventId;

    @ApiModelProperty(value = "座位ID列表", required = true, example = "[1, 2]")
    @NotEmpty(message = "座位ID列表不能为空")
    private List<Long> seatIds;
}
