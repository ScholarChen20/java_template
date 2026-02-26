package com.example.yoyo_data.common.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 开票提醒视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShowRemindVO {

    private Long showEventId;
    private String showName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime saleStartTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime remindTime;

    private Integer remindCount;
    private Boolean isReminded;
    private String remindStatus;
}
