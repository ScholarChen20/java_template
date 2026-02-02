package com.example.yoyo_data.infrastructure.limiter;

import java.lang.annotation.*;

/**
 * 限流注解 - 用于声明式限流
 * 在方法上添加此注解可以实现对该方法的限流保护
 *
 * 使用示例：
 * @RateLimit(key = "user:{userId}", rate = 10, interval = 60)
 * public Result<?> getUserInfo(@PathVariable Long userId) { ... }
 *
 * @author Template Framework
 * @version 1.0
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {

    /**
     * 限流键 - 可使用SpEL表达式
     * 支持的变量：
     * - {userId}: 用户ID（从ThreadLocal获取）
     * - {username}: 用户名（从ThreadLocal获取）
     * - {ipAddress}: IP地址（从ThreadLocal获取）
     * - {requestId}: 请求ID（从ThreadLocal获取）
     * - {method}: 方法名称
     * - {arg0}, {arg1}...: 方法参数
     */
    String key();

    /**
     * 限流速率 - 单位时间内允许的最大请求数
     * 默认值: 10
     */
    int rate() default 10;

    /**
     * 时间间隔 - 单位秒
     * 默认值: 60 (1分钟)
     */
    int interval() default 60;

    /**
     * 限流消息 - 限流触发时返回的错误信息
     * 默认值: "请求过于频繁，请稍后再试"
     */
    String message() default "请求过于频繁，请稍后再试";

    /**
     * 是否记录限流日志
     * 默认值: true
     */
    boolean logEnabled() default true;
}
