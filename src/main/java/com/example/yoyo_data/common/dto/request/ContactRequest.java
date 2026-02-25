package com.example.yoyo_data.common.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * 常用观演人请求DTO
 *
 * @author yoyo_data
 * @date 2026-02-25
 */
@Data
@ApiModel(description = "添加常用观演人请求")
public class ContactRequest {

    @ApiModelProperty(value = "姓名", required = true, example = "张三")
    @NotBlank(message = "姓名不能为空")
    private String name;

    @ApiModelProperty(value = "手机号", required = true, example = "13800138000")
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @ApiModelProperty(value = "身份证号", required = true, example = "110101199001011234")
    @NotBlank(message = "身份证号不能为空")
    @Pattern(regexp = "^[1-9]\\d{5}(18|19|20)\\d{2}((0[1-9])|(1[0-2]))(([0-2][1-9])|10|20|30|31)\\d{3}[0-9Xx]$",
             message = "身份证号格式不正确")
    private String idCard;

    @ApiModelProperty(value = "是否默认", example = "false")
    private Boolean isDefault = false;
}
