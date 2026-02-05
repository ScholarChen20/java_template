package com.example.yoyo_data.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class CaptchaVO {
    private String uuid;
    private String img;
    private String username;
}
