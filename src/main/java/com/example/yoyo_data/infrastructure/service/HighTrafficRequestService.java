package com.example.yoyo_data.infrastructure.service;

import com.example.yoyo_data.infrastructure.message.MessageEvent;

/**
 * 高并发请求服务接口
 * 用于处理高并发请求的业务逻辑
 *
 * @author Template Framework
 * @version 1.0
 */
public interface HighTrafficRequestService {

    /**
     * 处理高并发请求
     *
     * @param event 消息事件
     * @return 是否处理成功
     */
    boolean processRequest(MessageEvent event);

    /**
     * 处理批量高并发请求
     *
     * @param events 消息事件列表
     * @return 处理成功的数量
     */
    int processBatchRequests(java.util.List<MessageEvent> events);

    /**
     * 处理重试请求
     *
     * @param event 消息事件
     * @param retryCount 当前重试次数
     * @return 是否处理成功
     */
    boolean processRetryRequest(MessageEvent event, int retryCount);

    /**
     * 处理死信消息
     *
     * @param event 消息事件
     * @param errorMessage 错误信息
     * @return 是否处理成功
     */
    boolean processDeadLetterMessage(MessageEvent event, String errorMessage);

    /**
     * 验证请求数据
     *
     * @param event 消息事件
     * @return 是否验证通过
     */
    boolean validateRequest(MessageEvent event);

    /**
     * 获取请求处理状态
     *
     * @param requestId 请求ID
     * @return 处理状态
     */
    String getRequestStatus(String requestId);

    /**
     * 获取请求处理结果
     *
     * @param requestId 请求ID
     * @return 处理结果
     */
    String getRequestResult(String requestId);

    /**
     * 取消请求处理
     *
     * @param requestId 请求ID
     * @return 是否取消成功
     */
    boolean cancelRequest(String requestId);

    /**
     * 暂停请求处理
     *
     * @param requestId 请求ID
     * @return 是否暂停成功
     */
    boolean pauseRequest(String requestId);

    /**
     * 恢复请求处理
     *
     * @param requestId 请求ID
     * @return 是否恢复成功
     */
    boolean resumeRequest(String requestId);

    /**
     * 获取处理统计信息
     *
     * @return 统计信息
     */
    java.util.Map<String, Object> getProcessingStats();

    /**
     * 清理过期请求
     *
     * @param expirationTime 过期时间（毫秒）
     * @return 清理的请求数量
     */
    int cleanupExpiredRequests(long expirationTime);
}