package com.example.yoyo_data.controller;

import com.example.yoyo_data.common.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 系统模块控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/system")
@Api(tags = "系统模块", description = "系统信息、状态等操作")
public class SystemController {

    /**
     * 获取系统信息
     *
     * @return 系统信息
     */
    @GetMapping("/info")
    @ApiOperation(value = "获取系统信息", notes = "获取系统的基本信息")
    public Result<?> getSystemInfo() {
        log.info("获取系统信息");
        
        // 模拟数据
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("systemName", "YoYo Data System");
        result.put("version", "1.0.0");
        result.put("description", "高并发单体项目架构示例");
        result.put("author", "YoYo Data Team");
        result.put("email", "contact@yoyodata.com");
        result.put("website", "https://www.yoyodata.com");
        result.put("timestamp", System.currentTimeMillis());
        
        return Result.success(result);
    }

    /**
     * 获取系统状态
     *
     * @return 系统状态
     */
    @GetMapping("/status")
    @ApiOperation(value = "获取系统状态", notes = "获取系统的运行状态")
    public Result<?> getSystemStatus() {
        log.info("获取系统状态");
        
        // 模拟数据
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("status", "healthy");
        result.put("cpuUsage", 25.5);
        result.put("memoryUsage", 60.2);
        result.put("diskUsage", 45.8);
        result.put("networkIn", 1024);
        result.put("networkOut", 512);
        result.put("onlineUsers", 1000);
        result.put("activeConnections", 500);
        result.put("timestamp", System.currentTimeMillis());
        
        return Result.success(result);
    }

    /**
     * 清理系统缓存
     *
     * @return 清理结果
     */
    @PostMapping("/clean-cache")
    @ApiOperation(value = "清理系统缓存", notes = "清理系统的缓存数据")
    public Result<?> cleanSystemCache() {
        log.info("清理系统缓存");
        
        // 模拟数据
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("status", "success");
        result.put("message", "系统缓存清理成功");
        result.put("clearedCacheSize", "1024MB");
        result.put("timestamp", System.currentTimeMillis());
        
        return Result.success(result);
    }

    /**
     * 重启系统服务
     *
     * @param serviceName 服务名称
     * @return 重启结果
     */
    @PostMapping("/restart-service")
    @ApiOperation(value = "重启系统服务", notes = "重启指定的系统服务")
    public Result<?> restartSystemService(
            @ApiParam(value = "服务名称", required = true) @RequestParam("serviceName") String serviceName
    ) {
        log.info("重启系统服务: serviceName={}", serviceName);
        
        // 模拟数据
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("status", "success");
        result.put("message", "系统服务重启成功");
        result.put("serviceName", serviceName);
        result.put("timestamp", System.currentTimeMillis());
        
        return Result.success(result);
    }
}