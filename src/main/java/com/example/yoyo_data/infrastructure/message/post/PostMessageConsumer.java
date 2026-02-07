package com.example.yoyo_data.infrastructure.message.post;

import com.alibaba.fastjson.JSON;
import com.example.yoyo_data.common.Result;
import com.example.yoyo_data.common.dto.request.CreatePostRequest;
import com.example.yoyo_data.service.PostService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * 帖子消息消费者 - 用于处理帖子相关的Kafka消息
 * 主要处理帖子创建、更新等异步操作
 *
 * @author Template Framework
 * @version 1.0
 */
@Slf4j
@Component
public class PostMessageConsumer {

    @Autowired
    private PostService postService;

    /**
     * 处理帖子创建消息
     *
     * @param message 消息内容
     */
    @KafkaListener(topics = "posts-create", groupId = "post-consumer-group")
    public void handlePostCreateMessage(String message) {
        log.info("接收到帖子创建消息: {}", message);

        try {
            // 解析消息
            PostMessageEvent event = JSON.parseObject(message, PostMessageEvent.class);
            log.info("解析帖子创建事件: eventId={}, eventType={}, userId={}", 
                    event.getEventId(), event.getEventType(), event.getUserId());

            // 解析创建帖子请求数据
            CreatePostRequest request = JSON.parseObject(event.getData(), CreatePostRequest.class);
            log.info("解析帖子创建请求: title={}, category={}", request.getTitle(), request.getCategory());

            // 处理帖子创建
            Result<?> result = postService.createPost(event.getUserId(), request);

            if (result.getCode() == 200) {
                log.info("帖子创建处理成功: eventId={}, postTitle={}", 
                        event.getEventId(), request.getTitle());
                // 这里可以添加后续处理，比如更新帖子状态等
            } else {
                log.error("帖子创建处理失败: eventId={}, message={}", 
                        event.getEventId(), result.getMessage());
                // 这里可以添加失败处理逻辑
            }

        } catch (Exception e) {
            log.error("处理帖子创建消息异常: {}", message, e);
            // 这里可以添加异常处理逻辑，比如重试机制
        }
    }

    /**
     * 处理帖子更新消息
     *
     * @param message 消息内容
     */
    @KafkaListener(topics = "posts-update", groupId = "post-consumer-group")
    public void handlePostUpdateMessage(String message) {
        log.info("接收到帖子更新消息: {}", message);

        try {
            // 解析消息
            PostMessageEvent event = JSON.parseObject(message, PostMessageEvent.class);
            log.info("解析帖子更新事件: eventId={}, eventType={}, postId={}", 
                    event.getEventId(), event.getEventType(), event.getPostId());

            // 这里可以添加帖子更新的处理逻辑
            // 例如：解析更新请求数据，调用服务更新帖子等

            log.info("帖子更新处理完成: eventId={}, postId={}", 
                    event.getEventId(), event.getPostId());

        } catch (Exception e) {
            log.error("处理帖子更新消息异常: {}", message, e);
            // 这里可以添加异常处理逻辑
        }
    }

    /**
     * 处理帖子消息的错误情况
     *
     * @param message 消息内容
     */
    @KafkaListener(topics = "posts-error", groupId = "post-consumer-group")
    public void handlePostErrorMessage(String message) {
        log.warn("接收到帖子错误消息: {}", message);

        try {
            // 解析错误消息
            PostMessageEvent event = JSON.parseObject(message, PostMessageEvent.class);
            log.warn("解析帖子错误事件: eventId={}, errorMessage={}", 
                    event.getEventId(), event.getErrorMessage());

            // 这里可以添加错误处理逻辑，比如记录错误日志、发送告警等

        } catch (Exception e) {
            log.error("处理帖子错误消息异常: {}", message, e);
        }
    }
}
