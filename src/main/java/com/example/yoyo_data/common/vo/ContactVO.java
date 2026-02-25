package com.example.yoyo_data.common.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 常用观演人响应VO
 *
 * @author yoyo_data
 * @date 2026-02-25
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "常用观演人信息")
public class ContactVO {

    @ApiModelProperty(value = "常用观演人ID")
    private Long id;

    @ApiModelProperty(value = "姓名")
    private String name;

    @ApiModelProperty(value = "手机号")
    private String phone;

    @ApiModelProperty(value = "身份证号（脱敏）")
    private String idCard;

    @ApiModelProperty(value = "是否默认")
    private Boolean isDefault;
}
