package com.example.yoyo_data.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@TableName("reports")
public class Report implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("reporter_id")
    private Long reporterId;

    @TableField("reported_type")
    private String reportedType;

    @TableField("reported_id")
    private Long reportedId;

    @TableField("reason_type")
    private String reasonType;

    @TableField("reason")
    private String reason;

    @TableField("evidence_urls")
    private String evidenceUrls;

    @TableField("status")
    private String status;

    @TableField("handler_id")
    private Long handlerId;

    @TableField("handle_result")
    private String handleResult;

    @TableField("handle_note")
    private String handleNote;

    @TableField("handled_at")
    private LocalDateTime handledAt;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
