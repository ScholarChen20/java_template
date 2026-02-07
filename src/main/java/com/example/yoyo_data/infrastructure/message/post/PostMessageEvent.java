package com.example.yoyo_data.infrastructure.message.post;

import com.example.yoyo_data.infrastructure.message.MessageEvent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 帖子消息事件 - 用于帖子相关的Kafka消息
 * 扩展自基础MessageEvent，添加帖子相关的字段
 *
 * @author Template Framework
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PostMessageEvent extends MessageEvent {

    private static final long serialVersionUID = 1L;

    /**
     * 帖子ID
     */
    private Long postId;

    /**
     * 帖子标题
     */
    private String postTitle;

    /**
     * 帖子状态
     */
    private String postStatus;

    /**
     * 帖子分类
     */
    private String category;

    /**
     * 帖子标签（JSON格式）
     */
    private String tags;

    /**
     * 封面图片URL
     */
    private String coverImage;

    /**
     * 构建帖子创建事件
     */
    public static PostMessageEvent buildCreateEvent(Long userId, String data, String title, String category) {
        PostMessageEvent event = new PostMessageEvent();
        event.setEventType("POST_CREATE");
        event.setSource("PostController");
        event.setTimestamp(LocalDateTime.now());
        event.setCreatedAt(LocalDateTime.now());
        event.setData(data);
        event.setPriority(5);
        event.setUserId(userId);
        event.setPostTitle(title);
        event.setPostStatus("PENDING");
        event.setCategory(category);
        return event;
    }

    /**
     * 构建帖子更新事件
     */
    public static PostMessageEvent buildUpdateEvent(Long userId, Long postId, String data, String title) {
        PostMessageEvent event = new PostMessageEvent();
        event.setEventType("POST_UPDATE");
        event.setSource("PostController");
        event.setTimestamp(LocalDateTime.now());
        event.setCreatedAt(LocalDateTime.now());
        event.setData(data);
        event.setPriority(5);
        event.setUserId(userId);
        event.setPostId(postId);
        event.setPostTitle(title);
        event.setPostStatus("PENDING");
        return event;
    }
}
