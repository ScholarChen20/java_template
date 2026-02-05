package com.example.yoyo_data.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 对话会话DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DialogSessionDTO {

    private String id;
    private Long userId;
    private Long recipientId;
    private String type;
    private String status;
    private String lastMessage;
    private Long lastMessageSenderId;
    private Date lastMessageAt;
    private Integer unreadCount;
    private Map<String, Object> metadata;
    private List<MessageDTO> messages;
    private Date createdAt;
    private Date updatedAt;
    private Boolean isArchived;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MessageDTO {
        private String id;
        private Long senderId;
        private String content;
        private String type;
        private List<String> mediaUrls;
        private Map<String, Object> metadata;
        private Date sentAt;
        private String status;
    }
}
