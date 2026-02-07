package com.example.yoyo_data.controller;

import com.example.yoyo_data.domain.dto.request.HighTrafficRequest;
import com.example.yoyo_data.domain.dto.response.HighTrafficResponse;
import com.example.yoyo_data.infrastructure.message.KafkaProducerTemplate;
import com.example.yoyo_data.infrastructure.message.MessageEvent;
import com.example.yoyo_data.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 高并发请求控制器
 * 处理高流量请求，使用Kafka队列实现异步削峰
 *
 * @author Template Framework
 * @version 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/high-traffic")
public class HighTrafficController {

    @Autowired
    private KafkaProducerTemplate kafkaProducerTemplate;

    /**
     * 高并发请求处理接口
     *
     * @param request 高并发请求数据
     * @param httpRequest HttpServletRequest
     * @return 处理结果
     */
    @PostMapping("/process")
    public Result<HighTrafficResponse> processHighTrafficRequest(
            @Valid @RequestBody HighTrafficRequest request,
            HttpServletRequest httpRequest) {
        
        // 生成请求ID（如果没有提供）
        String requestId = request.getRequestId();
        if (requestId == null || requestId.trim().isEmpty()) {
            requestId = UUID.randomUUID().toString();
            request.setRequestId(requestId);
        }

        // 设置客户端IP
        String clientIp = getClientIp(httpRequest);
        request.setClientIp(clientIp);

        // 设置时间戳
        if (request.getTimestamp() == 0) {
            request.setTimestamp(System.currentTimeMillis());
        }

        try {
            // 构建事件对象
            MessageEvent event = MessageEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .eventType("HIGH_TRAFFIC_REQUEST")
                    .source("HighTrafficController")
                    .timestamp(LocalDateTime.now())
                    .createdAt(LocalDateTime.now())
                    .data(request.getBusinessData())
                    .priority(request.getPriority())
                    .userId(request.getUserId())
                    .requestId(requestId)
                    .build();

            // 发送到Kafka队列
            boolean sent = kafkaProducerTemplate.sendEvent("high-traffic-requests", event);

            if (sent) {
                log.info("高并发请求已成功发送到队列: requestId={}, requestType={}, userId={}",
                        requestId, request.getRequestType(), request.getUserId());

                // 返回成功响应
                HighTrafficResponse response = HighTrafficResponse.success(requestId, event.getEventId());
                return Result.success(response);
            } else {
                log.error("高并发请求发送到队列失败: requestId={}, requestType={}, userId={}",
                        requestId, request.getRequestType(), request.getUserId());

                // 返回失败响应
                HighTrafficResponse response = HighTrafficResponse.failure(requestId, "请求发送到队列失败");
                return Result.error("请求发送失败");
            }

        } catch (Exception e) {
            log.error("处理高并发请求异常: requestId={}, requestType={}, userId={}",
                    requestId, request.getRequestType(), request.getUserId(), e);

            // 返回失败响应
            HighTrafficResponse response = HighTrafficResponse.failure(requestId, "处理请求时发生异常: " + e.getMessage());
            return Result.error(500, "处理请求失败");
        }
    }

    /**
     * 批量处理高并发请求
     *
     * @param requests 高并发请求列表
     * @param httpRequest HttpServletRequest
     * @return 处理结果
     */
    @PostMapping("/batch-process")
    public Result<String> batchProcessHighTrafficRequests(
            @Valid @RequestBody java.util.List<HighTrafficRequest> requests,
            HttpServletRequest httpRequest) {
        
        if (requests == null || requests.isEmpty()) {
            return Result.error(400, "请求列表不能为空");
        }

        if (requests.size() > 1000) {
            return Result.error(400, "批量请求数量不能超过1000个");
        }

        int successCount = 0;
        int failureCount = 0;

        for (HighTrafficRequest request : requests) {
            // 生成请求ID（如果没有提供）
            String requestId = request.getRequestId();
            if (requestId == null || requestId.trim().isEmpty()) {
                requestId = UUID.randomUUID().toString();
                request.setRequestId(requestId);
            }

            // 设置客户端IP
            if (request.getClientIp() == null) {
                request.setClientIp(getClientIp(httpRequest));
            }

            // 设置时间戳
            if (request.getTimestamp() == 0) {
                request.setTimestamp(System.currentTimeMillis());
            }

            try {
                // 构建事件对象
                MessageEvent event = MessageEvent.builder()
                        .eventId(UUID.randomUUID().toString())
                        .eventType("HIGH_TRAFFIC_REQUEST_BATCH")
                        .source("HighTrafficController")
                        .timestamp(LocalDateTime.now())
                        .createdAt(LocalDateTime.now())
                        .data(request.getBusinessData())
                        .priority(request.getPriority())
                        .userId(request.getUserId())
                        .requestId(requestId)
                        .build();

                // 发送到Kafka队列
                boolean sent = kafkaProducerTemplate.sendEvent("high-traffic-requests", event);

                if (sent) {
                    successCount++;
                } else {
                    failureCount++;
                }

            } catch (Exception e) {
                log.error("处理批量高并发请求异常: requestId={}", requestId, e);
                failureCount++;
            }
        }

        log.info("批量处理完成: 总请求数={}, 成功数={}, 失败数={}",
                requests.size(), successCount, failureCount);

        return Result.success("批量处理完成: 总请求数=" + requests.size() + ", 成功数=" + successCount + ", 失败数=" + failureCount);
    }

    /**
     * 获取客户端IP地址
     *
     * @param request HttpServletRequest
     * @return 客户端IP地址
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}