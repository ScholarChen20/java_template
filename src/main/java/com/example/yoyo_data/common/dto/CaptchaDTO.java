package com.example.yoyo_data.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CaptchaDTO {
    /**
     * 编号id
     */
    private String uuid;
    /**
     * 验证码图片
     */
    private String img;
}
