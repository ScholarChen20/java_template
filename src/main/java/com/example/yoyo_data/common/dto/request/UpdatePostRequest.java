package com.example.yoyo_data.common.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * 更新帖子请求体
 */
@Data
@ApiModel(value = "UpdatePostRequest", description = "更新帖子请求体")
public class UpdatePostRequest {

    @ApiModelProperty(value = "帖子ID", required = true)
    private Long postId;

    @ApiModelProperty(value = "帖子标题", required = true)
    @NotBlank(message = "帖子标题不能为空")
    @Size(max = 100, message = "帖子标题长度不能超过100个字符")
    private String title;

    @ApiModelProperty(value = "帖子内容", required = true)
    @NotBlank(message = "帖子内容不能为空")
    private String content;

    @ApiModelProperty(value = "帖子分类", required = true)
    @NotBlank(message = "帖子分类不能为空")
    @Size(max = 50, message = "帖子分类长度不能超过50个字符")
    private String category;

    @ApiModelProperty(value = "帖子标签", required = false)
    private List<String> tags;

    @ApiModelProperty(value = "帖子封面图片URL", required = false)
    private String coverImage;
}
