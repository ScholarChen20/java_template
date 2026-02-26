package com.example.yoyo_data.common.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 消息通知视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationVO {

    private Long id;
    private String type;
    private String title;
    private String content;
    private Boolean isRead;
    private Long relatedId;
    private String relatedType;
    private String actionUrl;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createdAt;
}
