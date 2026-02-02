package com.example.yoyo_data.controller;

import com.example.yoyo_data.common.Result;
import com.example.yoyo_data.service.SystemService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 系统控制器
 * 处理系统相关操作
 */
@Slf4j
@RestController
@RequestMapping("/api/system")
@Api(tags = "系统模块")
public class SystemController {

    @Autowired
    private SystemService systemService;

    /**
     * 获取系统状态
     *
     * @return 系统状态
     */
    @GetMapping("/status")
    @ApiOperation(value = "获取系统状态", notes = "获取系统的当前状态")
    public Result<Map<String, Object>> getSystemStatus() {
        return systemService.getSystemStatus();
    }

    /**
     * 获取系统版本
     *
     * @return 系统版本
     */
    @GetMapping("/version")
    @ApiOperation(value = "获取系统版本", notes = "获取系统的当前版本")
    public Result<Map<String, Object>> getSystemVersion() {
        return systemService.getSystemVersion();
    }

    /**
     * 生成验证码
     *
     * @param params 生成验证码参数，包含type、length等
     * @return 验证码信息
     */
    @PostMapping("/captcha")
    @ApiOperation(value = "生成验证码", notes = "生成新的验证码")
    public Result<Map<String, Object>> generateCaptcha(@ApiParam(value = "生成验证码参数") @RequestBody Map<String, Object> params) {
        return systemService.generateCaptcha(params);
    }

    /**
     * 验证验证码
     *
     * @param params 验证验证码参数，包含captcha_id、code等
     * @return 验证结果
     */
    @PostMapping("/captcha/verify")
    @ApiOperation(value = "验证验证码", notes = "验证验证码是否正确")
    public Result<Map<String, Object>> verifyCaptcha(@ApiParam(value = "验证验证码参数") @RequestBody Map<String, Object> params) {
        return systemService.verifyCaptcha(params);
    }

    /**
     * 获取系统配置
     *
     * @param key 配置键
     * @return 配置值
     */
    @GetMapping("/config/{key}")
    @ApiOperation(value = "获取系统配置", notes = "根据配置键获取系统配置值")
    public Result<Map<String, Object>> getSystemConfig(@ApiParam(value = "配置键") @PathVariable String key) {
        return systemService.getSystemConfig(key);
    }

    /**
     * 获取系统公告
     *
     * @param page 页码，默认 1
     * @param size 每页数量，默认 10
     * @return 系统公告列表
     */
    @GetMapping("/announcements")
    @ApiOperation(value = "获取系统公告", notes = "获取系统公告列表")
    public Result<Map<String, Object>> getSystemAnnouncements(
            @ApiParam(value = "页码", defaultValue = "1") @RequestParam(defaultValue = "1") Integer page,
            @ApiParam(value = "每页数量", defaultValue = "10") @RequestParam(defaultValue = "10") Integer size) {
        return systemService.getSystemAnnouncements(page, size);
    }
}
