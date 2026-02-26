package com.example.yoyo_data.common.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 观演人视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ViewerVO {

    private Long id;
    private String name;
    /** 脱敏身份证号，如 110101****1234 */
    private String idCardMasked;
    private String phone;
    private Boolean isDefault;
    private Boolean isVerified;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createdAt;
}
