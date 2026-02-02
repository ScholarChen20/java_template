package com.example.yoyo_data.service;

import com.example.yoyo_data.common.Result;
import java.util.Map;

/**
 * 系统服务接口
 * 处理系统相关业务逻辑
 */
public interface SystemService {

    /**
     * 获取系统状态
     *
     * @return 系统状态
     */
    Result<Map<String, Object>> getSystemStatus();

    /**
     * 获取系统版本
     *
     * @return 系统版本
     */
    Result<Map<String, Object>> getSystemVersion();

    /**
     * 生成验证码
     *
     * @param params 生成验证码参数，包含type、length等
     * @return 验证码信息
     */
    Result<Map<String, Object>> generateCaptcha(Map<String, Object> params);

    /**
     * 验证验证码
     *
     * @param params 验证验证码参数，包含captcha_id、code等
     * @return 验证结果
     */
    Result<Map<String, Object>> verifyCaptcha(Map<String, Object> params);

    /**
     * 获取系统配置
     *
     * @param key 配置键
     * @return 配置值
     */
    Result<Map<String, Object>> getSystemConfig(String key);

    /**
     * 获取系统公告
     *
     * @param page 页码，默认 1
     * @param size 每页数量，默认 10
     * @return 系统公告列表
     */
    Result<Map<String, Object>> getSystemAnnouncements(Integer page, Integer size);
}
