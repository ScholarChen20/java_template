package com.example.yoyo_data.common.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 实名认证结果VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "实名认证结果")
public class RealNameVerifyVO {

    @ApiModelProperty(value = "是否已认证")
    private Boolean verified;

    @ApiModelProperty(value = "认证的真实姓名（脱敏）", example = "张*")
    private String maskedName;

    @ApiModelProperty(value = "认证时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime verifiedAt;

    @ApiModelProperty(value = "提示信息")
    private String message;
}
