package com.example.yoyo_data.data.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 消息事件基类 - 所有消息事件的父类
 * 用于Kafka消息队列中的事件传输
 *
 * @author Template Framework
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 事件ID（唯一标识）
     */
    private String eventId;

    /**
     * 事件类型
     */
    private String eventType;

    /**
     * 事件来源
     */
    private String source;

    /**
     * 事件发生时间
     */
    private LocalDateTime timestamp;

    /**
     * 事件创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 事件数据（JSON格式）
     */
    private String data;

    /**
     * 优先级（0-10，默认5）
     */
    private int priority = 5;

    /**
     * 重试次数
     */
    private int retryCount = 0;

    /**
     * 最大重试次数
     */
    private int maxRetries = 3;

    /**
     * 是否已处理
     */
    private boolean processed = false;

    /**
     * 处理结果
     */
    private String result;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 用户ID（事件关联的用户）
     */
    private Long userId;

    /**
     * 请求ID（链接请求和事件）
     */
    private String requestId;

    /**
     * 检查事件是否需要重试
     *
     * @return true: 需要重试, false: 不需要
     */
    public boolean shouldRetry() {
        return !processed && retryCount < maxRetries;
    }

    /**
     * 增加重试次数
     */
    public void incrementRetryCount() {
        this.retryCount++;
    }

    /**
     * 标记为已处理成功
     *
     * @param result 处理结果
     */
    public void markAsProcessed(String result) {
        this.processed = true;
        this.result = result;
        this.errorMessage = null;
    }

    /**
     * 标记为处理失败
     *
     * @param errorMessage 错误信息
     */
    public void markAsFailed(String errorMessage) {
        this.processed = false;
        this.errorMessage = errorMessage;
    }
}
