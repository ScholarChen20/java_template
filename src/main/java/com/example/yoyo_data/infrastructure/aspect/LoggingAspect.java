package com.example.yoyo_data.infrastructure.aspect;

import com.example.yoyo_data.infrastructure.utils.DateTimeUtils;
import com.example.yoyo_data.infrastructure.utils.ThreadLocalUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.UUID;

/**
 * 日志AOP切面 - 记录方法调用前后的日志信息
 * 包括方法名、参数、返回值、执行时间等
 *
 * @author Template Framework
 * @version 1.0
 */
@Slf4j
@Aspect
@Component
public class LoggingAspect {

    /**
     * 对所有Controller层的public方法进行日志记录
     *
     * @param pjp 切入点信息
     * @return 方法返回值
     * @throws Throwable 方法抛出的异常
     */
    @Around("execution(public * com.example.yoyo_data.controller.*.*(..))")
    public Object logController(ProceedingJoinPoint pjp) throws Throwable {
        return logExecution(pjp, "Controller");
    }

    /**
     * 对所有Service层的public方法进行日志记录
     *
     * @param pjp 切入点信息
     * @return 方法返回值
     * @throws Throwable 方法抛出的异常
     */
    @Around("execution(public * com.example.yoyo_data.service.*.*(..))")
    public Object logService(ProceedingJoinPoint pjp) throws Throwable {
        return logExecution(pjp, "Service");
    }

    /**
     * 对所有Mapper层的public方法进行日志记录
     *
     * @param pjp 切入点信息
     * @return 方法返回值
     * @throws Throwable 方法抛出的异常
     */
    @Around("execution(public * com.example.yoyo_data.mapper.*.*(..))")
    public Object logMapper(ProceedingJoinPoint pjp) throws Throwable {
        return logExecution(pjp, "Mapper");
    }

    /**
     * 通用的执行日志记录方法
     *
     * @param pjp 切入点信息
     * @param layerName 层名称（用于日志显示）
     * @return 方法返回值
     * @throws Throwable 方法抛出的异常
     */
    private Object logExecution(ProceedingJoinPoint pjp, String layerName) throws Throwable {
        // 生成或获取请求ID
        String requestId = ThreadLocalUtils.getRequestId();
        if (requestId == null) {
            requestId = UUID.randomUUID().toString();
            ThreadLocalUtils.setRequestId(requestId);
        }

        String className = pjp.getTarget().getClass().getSimpleName();
        String methodName = pjp.getSignature().getName();
        Object[] args = pjp.getArgs();

        // 记录方法调用
        log.info("[{}] [RequestID: {}] {}.{}() 开始执行, 参数: {}",
                layerName, requestId, className, methodName,
                formatArgs(args));

        long startTime = System.currentTimeMillis();
        try {
            Object result = pjp.proceed();
            long duration = System.currentTimeMillis() - startTime;

            // 记录方法返回
            log.info("[{}] [RequestID: {}] {}.{}() 执行完成, 耗时: {}ms, 返回值: {}",
                    layerName, requestId, className, methodName,
                    duration, formatResult(result));

            return result;
        } catch (Throwable e) {
            long duration = System.currentTimeMillis() - startTime;

            // 记录方法异常
            log.error("[{}] [RequestID: {}] {}.{}() 执行失败, 耗时: {}ms, 异常: {}",
                    layerName, requestId, className, methodName,
                    duration, e.getMessage(), e);

            throw e;
        }
    }

    /**
     * 格式化参数用于日志显示
     *
     * @param args 方法参数数组
     * @return 格式化后的参数字符串
     */
    private String formatArgs(Object[] args) {
        if (args == null || args.length == 0) {
            return "[]";
        }

        // 限制参数显示长度，避免日志过大
        if (args.length > 5) {
            return "args length: " + args.length;
        }

        return Arrays.toString(args);
    }

    /**
     * 格式化返回值用于日志显示
     *
     * @param result 返回值
     * @return 格式化后的返回值字符串
     */
    private String formatResult(Object result) {
        if (result == null) {
            return "null";
        }

        String resultStr = result.toString();
        // 限制返回值显示长度，避免日志过大
        if (resultStr.length() > 200) {
            return resultStr.substring(0, 200) + "...";
        }

        return resultStr;
    }
}
