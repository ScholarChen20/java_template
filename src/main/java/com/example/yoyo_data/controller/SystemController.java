package com.example.yoyo_data.controller;

import com.example.yoyo_data.common.Result;
import com.example.yoyo_data.service.SystemService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 系统模块控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/system")
@Api(tags = "系统模块", description = "系统信息、状态等操作")
public class SystemController {

    @Autowired
    private SystemService systemService;

    /**
     * 获取系统信息
     */
    @GetMapping("/info")
    @ApiOperation(value = "获取系统信息", notes = "获取系统的基本信息")
    public Result<?> getSystemInfo() {
        log.info("获取系统信息");
        return systemService.getSystemInfo();
    }

    /**
     * 获取系统状态
     */
    @GetMapping("/status")
    @ApiOperation(value = "获取系统状态", notes = "获取系统的运行状态")
    public Result<?> getSystemStatus() {
        log.info("获取系统状态");
        return systemService.getSystemStatus();
    }

    /**
     * 清理系统缓存
     */
    @PostMapping("/clean-cache")
    @ApiOperation(value = "清理系统缓存", notes = "清理系统的缓存数据")
    public Result<?> cleanSystemCache() {
        log.info("清理系统缓存");
        return systemService.cleanSystemCache();
    }

    /**
     * 重启系统服务
     */
    @PostMapping("/restart-service")
    @ApiOperation(value = "重启系统服务", notes = "重启指定的系统服务")
    public Result<?> restartSystemService(
            @ApiParam(value = "服务名称", required = true) @RequestParam("serviceName") String serviceName
    ) {
        log.info("重启系统服务: serviceName={}", serviceName);
        return systemService.restartSystemService(serviceName);
    }
}
