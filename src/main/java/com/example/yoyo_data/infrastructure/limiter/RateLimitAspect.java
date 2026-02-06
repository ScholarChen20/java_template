package com.example.yoyo_data.infrastructure.limiter;

import com.example.yoyo_data.common.exception.BusinessException;
import com.example.yoyo_data.util.ThreadLocalUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * 限流AOP切面 - 实现声明式限流
 * 根据@RateLimit注解的配置，对方法进行限流保护
 *
 * @author Template Framework
 * @version 1.0
 */
@Slf4j
@Aspect
@Component
public class RateLimitAspect {

    @Autowired
    private RedissonClient redissonClient;

    /**
     * 拦截所有标注了@RateLimit的方法
     *
     * @param pjp 切入点信息
     * @return 方法返回值
     * @throws Throwable 方法抛出的异常
     */
    @Around("@annotation(com.example.yoyo_data.infrastructure.limiter.RateLimit)")
    public Object rateLimit(ProceedingJoinPoint pjp) throws Throwable {
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Method method = signature.getMethod();
        RateLimit annotation = method.getAnnotation(RateLimit.class);

        if (annotation == null) {
            return pjp.proceed();
        }

        // 生成限流键
        String limitKey = generateLimitKey(annotation, pjp);

        // 获取限流器
        RRateLimiter rateLimiter = redissonClient.getRateLimiter(limitKey);

        // 尝试获取令牌
        boolean acquired = rateLimiter.tryAcquire();

        if (!acquired) {
            if (annotation.logEnabled()) {
                log.warn("限流触发: {}, 用户ID: {}, IP: {}",
                        limitKey, ThreadLocalUtils.getUserId(), ThreadLocalUtils.getIpAddress());
            }
            throw new BusinessException(429, annotation.message());
        }

        // 设置速率
//        rateLimiter.setRate(RateIntervalUnit.SECONDS, annotation.rate());

        try {
            return pjp.proceed();
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * 生成限流键
     * 简化实现，暂时不使用SpEL表达式
     *
     * @param annotation @RateLimit注解
     * @param pjp 切入点信息
     * @return 生成的限流键
     */
    private String generateLimitKey(RateLimit annotation, ProceedingJoinPoint pjp) {
        String keyTemplate = annotation.key();
        String className = pjp.getTarget().getClass().getSimpleName();
        String methodName = pjp.getSignature().getName();

        // 简化实现，直接使用类名和方法名作为限流键
        return String.format("ratelimit:%s:%s:%s", className, methodName, keyTemplate);
    }
}
