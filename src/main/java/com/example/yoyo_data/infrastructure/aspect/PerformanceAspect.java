package com.example.yoyo_data.infrastructure.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * 性能监控AOP切面 - 监控方法执行性能
 * 记录执行时间过长的方法，用于性能优化
 *
 * @author Template Framework
 * @version 1.0
 */
@Slf4j
@Aspect
@Component
public class PerformanceAspect {

    /**
     * 性能警告阈值（毫秒）
     */
    private static final long WARN_THRESHOLD = 1000;

    /**
     * 性能错误阈值（毫秒）
     */
    private static final long ERROR_THRESHOLD = 5000;

    /**
     * 监控Service层方法性能
     *
     * @param pjp 切入点信息
     * @return 方法返回值
     * @throws Throwable 方法抛出的异常
     */
    @Around("execution(public * com.example.yoyo_data.service.*.*(..))")
    public Object monitorServicePerformance(ProceedingJoinPoint pjp) throws Throwable {
        return monitorPerformance(pjp, "Service");
    }

    /**
     * 监控Controller层方法性能
     *
     * @param pjp 切入点信息
     * @return 方法返回值
     * @throws Throwable 方法抛出的异常
     */
    @Around("execution(public * com.example.yoyo_data.controller.*.*(..))")
    public Object monitorControllerPerformance(ProceedingJoinPoint pjp) throws Throwable {
        return monitorPerformance(pjp, "Controller");
    }

    /**
     * 监控Mapper层方法性能
     *
     * @param pjp 切入点信息
     * @return 方法返回值
     * @throws Throwable 方法抛出的异常
     */
    @Around("execution(public * com.example.yoyo_data.mapper.*.*(..))")
    public Object monitorMapperPerformance(ProceedingJoinPoint pjp) throws Throwable {
        return monitorPerformance(pjp, "Mapper");
    }

    /**
     * 通用的性能监控方法
     *
     * @param pjp 切入点信息
     * @param layerName 层名称
     * @return 方法返回值
     * @throws Throwable 方法抛出的异常
     */
    private Object monitorPerformance(ProceedingJoinPoint pjp, String layerName) throws Throwable {
        String className = pjp.getTarget().getClass().getSimpleName();
        String methodName = pjp.getSignature().getName();

        long startTime = System.currentTimeMillis();
        try {
            Object result = pjp.proceed();
            return result;
        } finally {
            long duration = System.currentTimeMillis() - startTime;

            // 根据执行时间记录不同级别的日志
            if (duration >= ERROR_THRESHOLD) {
                log.error("[{}] {}.{}() 执行时间过长: {}ms (超过错误阈值 {}ms)",
                        layerName, className, methodName, duration, ERROR_THRESHOLD);
            } else if (duration >= WARN_THRESHOLD) {
                log.warn("[{}] {}.{}() 执行时间过长: {}ms (超过警告阈值 {}ms)",
                        layerName, className, methodName, duration, WARN_THRESHOLD);
            } else if (duration > 100) {
                log.debug("[{}] {}.{}() 执行耗时: {}ms",
                        layerName, className, methodName, duration);
            }
        }
    }
}
