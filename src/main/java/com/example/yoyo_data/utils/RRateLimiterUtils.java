package com.example.yoyo_data.utils;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RedissonClient;

import org.redisson.api.RateType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RRateLimiterUtils {

    @Autowired
    private RedissonClient redissonClient;

    // 限流配置：每秒最多10个请求
    private static final long RATE_LIMIT_RATE = 20;
    private static final long RATE_LIMIT_INTERVAL = 1;
    private static final RateIntervalUnit RATE_LIMIT_UNIT = RateIntervalUnit.SECONDS;

    // Redis key前缀
    private static final String RATE_LIMITER_KEY_PREFIX = "rate_limiter:";

    // 最大等待时间（毫秒），设置为-1表示无限等待
    private static final long MAX_WAIT_TIME = 1L;

    /**
     * 初始化限流器对象
     * @param key
     * @return
     */
    public RRateLimiter getRateLimiter(String key) {
        // 初始化一个限流器对象
        String rateLimiterKey = RATE_LIMITER_KEY_PREFIX + key;
        RRateLimiter rateLimiter = redissonClient.getRateLimiter(rateLimiterKey);
        rateLimiter.trySetRate(RateType.OVERALL, RATE_LIMIT_RATE, RATE_LIMIT_INTERVAL, RATE_LIMIT_UNIT);
        return rateLimiter;
    }
    /**
     * 堵塞式获取令牌桶
     */
    public void acquire(String accountId) {
        RRateLimiter rateLimiter = getRateLimiter(accountId);
        long startTime = System.currentTimeMillis();
        try{
            rateLimiter.acquire(1);
            long waitTime = System.currentTimeMillis() - startTime;
            System.out.println("获取令牌成功，耗时：" +  waitTime + "ms");
            if(waitTime > 1000){
                log.info("获取令牌耗时过长，请检查是否被限流了！");
            }
        }catch(Exception e){
            log.error("获取令牌失败：{}", e.getMessage());
        }
    }
    /**
     * 非堵塞式获取令牌桶
     * @return true表示获取成功，false表示获取失败
     */
    public boolean tryAcquire(String accountId) {
        RRateLimiter rateLimiter = getRateLimiter(accountId);
        return rateLimiter.tryAcquire(MAX_WAIT_TIME);
    }
}
