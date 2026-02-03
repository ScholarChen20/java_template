package com.example.yoyo_data.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 对话会话文档
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "dialog_sessions")
public class DialogSession {

    @Id
    private String id;

    @Field("user_id")
    private Long userId;

    @Field("recipient_id")
    private Long recipientId;

    @Field("type")
    private String type;

    @Field("status")
    private String status;

    @Field("last_message")
    private String lastMessage;

    @Field("last_message_sender_id")
    private Long lastMessageSenderId;

    @Field("last_message_at")
    private Date lastMessageAt;

    @Field("unread_count")
    private Integer unreadCount;

    @Field("metadata")
    private Map<String, Object> metadata;

    @Field("messages")
    private List<Message> messages;

    @Field("created_at")
    private Date createdAt;

    @Field("updated_at")
    private Date updatedAt;

    @Field("is_archived")
    private Boolean isArchived;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Message {
        private String id;

        @Field("sender_id")
        private Long senderId;

        @Field("content")
        private String content;

        @Field("type")
        private String type;

        @Field("media_urls")
        private List<String> mediaUrls;

        @Field("metadata")
        private Map<String, Object> metadata;

        @Field("sent_at")
        private Date sentAt;

        @Field("status")
        private String status;
    }
}
