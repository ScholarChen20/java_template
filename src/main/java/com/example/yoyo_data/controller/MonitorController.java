package com.example.yoyo_data.controller;

import com.example.yoyo_data.common.Result;
import com.example.yoyo_data.infrastructure.scheduler.OrderTimeoutHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/monitor")
@Slf4j
public class MonitorController {
    @Autowired
    private OrderTimeoutHandler orderTimeoutHandler;

    /**
     * 获取延迟队列大小--调试使用
     * @return
     */
    @GetMapping("/delay-queue-size")
    public Result<Map<String, Integer>> getDelayQueueSize() {
        int size = orderTimeoutHandler.getDelayQueueSize();
        int blockingQueueSize = orderTimeoutHandler.getBlockingQueueSize();
        log.info("【获取延迟队列大小】delayQueueSize={}, blockingQueueSize={}", size, blockingQueueSize);
        Map<String,  Integer> resultMap = new HashMap<>();
        resultMap.put("delayQueueSize", size);
        resultMap.put("blockingQueueSize", blockingQueueSize);
        return Result.success(resultMap);
    }
}
