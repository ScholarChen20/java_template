package com.example.yoyo_data.controller;

import com.example.yoyo_data.domain.dto.request.HighTrafficRequest;
import com.example.yoyo_data.domain.dto.response.HighTrafficResponse;
import com.example.yoyo_data.common.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 高并发接口性能测试
 * 用于测试高并发请求接口的性能和削峰效果
 *
 * @author Template Framework
 * @version 1.0
 */
@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class HighTrafficControllerPerformanceTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private String baseUrl;

    @BeforeEach
    public void setUp() {
        baseUrl = "http://localhost:" + port + "/api/high-traffic";
        log.info("测试基础URL: {}", baseUrl);
    }

    /**
     * 测试单个请求
     */
    @Test
    public void testSingleRequest() {
        log.info("开始测试单个高并发请求");

        // 构建请求
        HighTrafficRequest request = buildTestRequest();
        ResponseEntity<HighTrafficResponse> response = sendRequest(request);

        log.info("单个请求测试结果: statusCode={}, body={}",
                response.getStatusCode(), response.getBody());

        assert response.getStatusCode().is2xxSuccessful();
        assert response.getBody() != null;
        assert response.getBody().getCode() != 200;
    }

    /**
     * 测试并发请求（10个线程）
     */
    @Test
    public void testConcurrentRequests() throws InterruptedException {
        int threadCount = 10;
        int requestsPerThread = 10;
        int totalRequests = threadCount * requestsPerThread;

        log.info("开始测试并发请求: 线程数={}, 每线程请求数={}, 总请求数={}",
                threadCount, requestsPerThread, totalRequests);

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(totalRequests);
        List<Long> responseTimes = new ArrayList<>();
        List<Integer> successCount = new ArrayList<>();
        successCount.add(0);

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < threadCount; i++) {
            final int threadIndex = i;
            executorService.submit(() -> {
                for (int j = 0; j < requestsPerThread; j++) {
                    try {
                        HighTrafficRequest request = buildTestRequest();
                        request.setRequestType("TEST_REQUEST_" + threadIndex + "_" + j);

                        long requestStartTime = System.currentTimeMillis();
                        ResponseEntity<HighTrafficResponse> response = sendRequest(request);
                        long requestEndTime = System.currentTimeMillis();
                        long responseTime = requestEndTime - requestStartTime;

                        responseTimes.add(responseTime);

                        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                            synchronized (successCount) {
                                successCount.set(0, successCount.get(0) + 1);
                            }
                            log.info("线程{}请求{}成功, 响应时间: {}ms, 状态码: {}",
                                    threadIndex, j, responseTime, response.getStatusCode());
                        } else {
                            log.warn("线程{}请求{}失败, 响应时间: {}ms, 状态码: {}",
                                    threadIndex, j, responseTime, response.getStatusCode());
                        }

                    } catch (Exception e) {
                        log.error("线程{}请求{}异常: {}", threadIndex, j, e.getMessage(), e);
                    } finally {
                        latch.countDown();
                    }
                }
            });
        }

        // 等待所有请求完成
        latch.await(60, TimeUnit.SECONDS);
        executorService.shutdown();
        executorService.awaitTermination(60, TimeUnit.SECONDS);

        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;

        // 计算统计信息
        double averageResponseTime = responseTimes.stream().mapToLong(Long::longValue).average().orElse(0);
        long maxResponseTime = responseTimes.stream().mapToLong(Long::longValue).max().orElse(0);
        long minResponseTime = responseTimes.stream().mapToLong(Long::longValue).min().orElse(0);
        int actualSuccessCount = successCount.get(0);
        double successRate = (double) actualSuccessCount / totalRequests * 100;

        log.info("并发请求测试结果:");
        log.info("总请求数: {}", totalRequests);
        log.info("成功请求数: {}", actualSuccessCount);
        log.info("成功率: {:.2f}%", successRate);
        log.info("总耗时: {}ms", totalTime);
        log.info("平均响应时间: {:.2f}ms", averageResponseTime);
        log.info("最大响应时间: {}ms", maxResponseTime);
        log.info("最小响应时间: {}ms", minResponseTime);
        log.info("QPS: {:.2f}", (double) totalRequests / totalTime * 1000);

        // 验证测试结果
        assert actualSuccessCount > 0;
        assert successRate > 0;
    }

    /**
     * 测试批量请求
     */
    @Test
    public void testBatchRequests() {
        log.info("开始测试批量高并发请求");

        // 构建批量请求
        List<HighTrafficRequest> requests = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            HighTrafficRequest request = buildTestRequest();
            request.setRequestType("BATCH_TEST_REQUEST_" + i);
            requests.add(request);
        }

        // 发送批量请求
        ResponseEntity<Result> response = sendBatchRequest(requests);

        log.info("批量请求测试结果: statusCode={}, body={}",
                response.getStatusCode(), response.getBody());

        assert response.getStatusCode().is2xxSuccessful();
        assert response.getBody() != null;
        assert response.getBody().getCode() == 200;
    }

    /**
     * 构建测试请求
     */
    private HighTrafficRequest buildTestRequest() {
        return HighTrafficRequest.builder()
                .requestId(UUID.randomUUID().toString())
                .requestType("TEST_REQUEST")
                .userId(1001L)
                .businessData(buildTestBusinessData())
                .priority(5)
                .timestamp(System.currentTimeMillis())
                .clientIp("192.168.1.100")
                .clientId("test-client-001")
                .operation("CREATE")
                .extraParams("{\"test\":\"value\"}")
                .build();
    }

    /**
     * 构建测试业务数据
     */
    private String buildTestBusinessData() {
        return "{\"productId\": 1001,\"quantity\": 1,\"price\": 99.99,\"currency\": \"CNY\",\"shippingAddress\": {\"name\": \"测试用户\",\"phone\": \"13800138000\",\"address\": \"北京市朝阳区测试地址\"},\"paymentMethod\": \"ALIPAY\",\"remark\": \"测试订单\",\"items\": [{\"skuId\": 2001,\"name\": \"测试商品\",\"quantity\": 1,\"price\": 99.99}]}";
    }

    /**
     * 发送请求
     */
    private ResponseEntity<HighTrafficResponse> sendRequest(HighTrafficRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<HighTrafficRequest> entity = new HttpEntity<>(request, headers);

        return restTemplate.postForEntity(
                baseUrl + "/process",
                entity,
                HighTrafficResponse.class
        );
    }

    /**
     * 发送批量请求
     */
    private ResponseEntity<Result> sendBatchRequest(List<HighTrafficRequest> requests) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<List<HighTrafficRequest>> entity = new HttpEntity<>(requests, headers);

        return restTemplate.postForEntity(
                baseUrl + "/batch-process",
                entity,
                Result.class
        );
    }
}