package com.example.yoyo_data.common.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 发表演出评价请求DTO
 */
@Data
@ApiModel(description = "发表演出评价请求")
public class PostReviewRequest {

    @ApiModelProperty(value = "订单ID", required = true, example = "123456789")
    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    @ApiModelProperty(value = "评分（1-5）", required = true, example = "5")
    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分最低1分")
    @Max(value = 5, message = "评分最高5分")
    private Integer rating;

    @ApiModelProperty(value = "评价内容", example = "演出非常精彩！")
    private String content;

    @ApiModelProperty(value = "标签列表", example = "[\"舞台效果好\",\"歌手互动多\"]")
    private List<String> tags;

    @ApiModelProperty(value = "图片URL列表")
    private List<String> images;
}
