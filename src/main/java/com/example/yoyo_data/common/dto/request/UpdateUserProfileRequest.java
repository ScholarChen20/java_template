package com.example.yoyo_data.common.dto.request;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.Map;

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

    @ApiModelProperty(value = "旅行偏好", required = false)
    private java.util.List<String> travelPreferences;

    @ApiModelProperty(value = "已访问城市", required = false)
    private java.util.List<String> visitedCities;

    @ApiModelProperty(value = "旅行统计", required = false)
    private Map<String, Object> travelStats;


}
