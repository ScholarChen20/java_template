package com.example.yoyo_data.domain.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 更新用户档案请求体
 */
@Data
@ApiModel(value = "UpdateUserProfileRequest", description = "更新用户档案请求体")
public class UpdateUserProfileRequest {

    @ApiModelProperty(value = "用户ID", required = false)
    private Long userId;

    @ApiModelProperty(value = "真实姓名", required = false)
    @Size(max = 50, message = "真实姓名长度不能超过50个字符")
    private String fullName;

    @ApiModelProperty(value = "性别", required = false, allowableValues = "男,女,未知")
    private String gender;

    @ApiModelProperty(value = "出生日期", required = false, example = "1990-01-01")
    private String birthDate;

    @ApiModelProperty(value = "所在地", required = false)
    @Size(max = 100, message = "所在地长度不能超过100个字符")
    private String location;

    @ApiModelProperty(value = "个人简介", required = false)
    @Size(max = 500, message = "个人简介长度不能超过500个字符")
    private String bio;

    @ApiModelProperty(value = "头像URL", required = false)
    private String avatarUrl;

    @ApiModelProperty(value = "个人网站", required = false)
    private String website;

    @ApiModelProperty(value = "职业", required = false)
    @Size(max = 100, message = "职业长度不能超过100个字符")
    private String occupation;

    @ApiModelProperty(value = "教育背景", required = false)
    @Size(max = 100, message = "教育背景长度不能超过100个字符")
    private String education;

    @ApiModelProperty(value = "旅行偏好", required = false)
    private java.util.List<String> travelPreferences;

    @ApiModelProperty(value = "已访问城市", required = false)
    private java.util.List<String> visitedCities;
}
