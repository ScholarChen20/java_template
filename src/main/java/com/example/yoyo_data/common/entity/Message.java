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
@TableName("messages")
public class Message implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("sender_id")
    private Long senderId;

    @TableField("receiver_id")
    private Long receiverId;

    @TableField("content")
    private String content;

    @TableField("message_type")
    private String messageType;

    @TableField("is_read")
    private Boolean isRead;

    @TableField("read_at")
    private LocalDateTime readAt;

    @TableField("is_deleted_by_sender")
    private Boolean isDeletedBySender;

    @TableField("is_deleted_by_receiver")
    private Boolean isDeletedByReceiver;

    @TableField("extra_data")
    private String extraData;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
