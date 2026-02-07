package com.example.yoyo_data.domain.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 高并发请求响应DTO
 * 用于返回高流量请求的处理结果
 *
 * @author Template Framework
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HighTrafficResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 响应码
     */
    private int code;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 请求ID
     */
    private String requestId;

    /**
     * 任务ID（异步处理的任务标识）
     */
    private String taskId;

    /**
     * 处理状态
     */
    private String status;

    /**
     * 预计处理时间（毫秒）
     */
    private long estimatedProcessingTime;

    /**
     * 提交时间
     */
    private LocalDateTime submittedAt;

    /**
     * 响应时间
     */
    private LocalDateTime responseAt;

    /**
     * 数据版本
     */
    private String version;

    /**
     * 服务端标识
     */
    private String serverId;

    /**
     * 队列位置（可选）
     */
    private Integer queuePosition;

    /**
     * 处理结果（异步处理完成后通过回调获取）
     */
    private String processingResult;

    /**
     * 错误信息（如果有）
     */
    private String errorMessage;

    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 重试次数
     */
    private int retryCount;

    /**
     * 最大重试次数
     */
    private int maxRetries;

    /**
     * 扩展信息
     */
    private String extraInfo;

    /**
     * 创建成功响应
     *
     * @param requestId 请求ID
     * @param taskId 任务ID
     * @return 成功响应
     */
    public static HighTrafficResponse success(String requestId, String taskId) {
        return HighTrafficResponse.builder()
                .code(200)
                .message("请求已成功提交，正在异步处理")
                .requestId(requestId)
                .taskId(taskId)
                .status("PENDING")
                .estimatedProcessingTime(1000)
                .submittedAt(LocalDateTime.now())
                .responseAt(LocalDateTime.now())
                .version("1.0")
                .success(true)
                .build();
    }

    /**
     * 创建失败响应
     *
     * @param requestId 请求ID
     * @param errorMessage 错误信息
     * @return 失败响应
     */
    public static HighTrafficResponse failure(String requestId, String errorMessage) {
        return HighTrafficResponse.builder()
                .code(500)
                .message("请求提交失败")
                .requestId(requestId)
                .status("FAILED")
                .submittedAt(LocalDateTime.now())
                .responseAt(LocalDateTime.now())
                .errorMessage(errorMessage)
                .success(false)
                .build();
    }
}