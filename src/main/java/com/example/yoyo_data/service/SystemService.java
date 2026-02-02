package com.example.yoyo_data.service;

import com.example.yoyo_data.common.Result;

/**
 * 系统服务接口
 */
public interface SystemService {
    /**
     * 获取系统信息
     *
     * @return 系统信息
     */
    Result<?> getSystemInfo();

    /**
     * 获取系统状态
     *
     * @return 系统状态
     */
    Result<?> getSystemStatus();

    /**
     * 清理系统缓存
     *
     * @return 清理结果
     */
    Result<?> cleanSystemCache();

    /**
     * 重启系统服务
     *
     * @param serviceName 服务名称
     * @return 重启结果
     */
    Result<?> restartSystemService(String serviceName);
}