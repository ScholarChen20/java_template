package com.example.yoyo_data.common.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 用户登录响应VO
 *
 * @author yoyo_data
 * @date 2026-02-25
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "用户登录响应")
public class UserLoginVO {

    @ApiModelProperty(value = "用户ID")
    private Long userId;

    @ApiModelProperty(value = "手机号")
    private String phone;

    @ApiModelProperty(value = "昵称")
    private String nickname;

    @ApiModelProperty(value = "头像URL")
    private String avatar;

    @ApiModelProperty(value = "JWT Token")
    private String token;

    @ApiModelProperty(value = "Token过期时间")
    private String tokenExpireTime;

    @ApiModelProperty(value = "是否实名认证")
    private Boolean isRealNameVerified;

    @ApiModelProperty(value = "会员等级")
    private String memberLevel;

    @ApiModelProperty(value = "积分")
    private Integer points;
}
