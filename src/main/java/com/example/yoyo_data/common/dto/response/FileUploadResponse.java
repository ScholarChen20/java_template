package com.example.yoyo_data.common.dto.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 文件上传响应体
 */
@Data
@ApiModel(value = "FileUploadResponse", description = "文件上传响应体")
public class FileUploadResponse {

    @ApiModelProperty(value = "存储桶名称", required = true)
    private String bucketName;

    @ApiModelProperty(value = "对象名称", required = true)
    private String objectName;

    @ApiModelProperty(value = "文件大小（字节）", required = true)
    private Long fileSize;

    @ApiModelProperty(value = "文件类型", required = true)
    private String contentType;

    @ApiModelProperty(value = "文件URL", required = true)
    private String fileUrl;

    @ApiModelProperty(value = "预签名URL过期时间（秒）", required = false)
    private Integer expiry;
}
