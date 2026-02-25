package com.example.yoyo_data.common.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * 实名认证请求DTO
 *
 * @author yoyo_data
 * @date 2026-02-25
 */
@Data
@ApiModel(description = "实名认证请求")
public class RealNameVerifyRequest {

    @ApiModelProperty(value = "真实姓名", required = true, example = "张三")
    @NotBlank(message = "真实姓名不能为空")
    @Pattern(regexp = "^[\\u4e00-\\u9fa5]{2,50}$", message = "真实姓名格式不正确")
    private String realName;

    @ApiModelProperty(value = "身份证号", required = true, example = "110101199001011234")
    @NotBlank(message = "身份证号不能为空")
    @Pattern(regexp = "^[1-9]\\d{5}(18|19|20)\\d{2}((0[1-9])|(1[0-2]))(([0-2][1-9])|10|20|30|31)\\d{3}[0-9Xx]$",
             message = "身份证号格式不正确")
    private String idCard;
}
