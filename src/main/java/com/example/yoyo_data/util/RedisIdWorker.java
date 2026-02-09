package com.example.yoyo_data.util;

import com.example.yoyo_data.infrastructure.cache.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Component
public class RedisIdWorker {
    /** 开始时间戳 */
    private static final long BEGIN_TIMESTAMP = 1640995200L;
    /** 序列号位数 */
    private static final long COUNT_BITS = 32;

    @Autowired
    private RedisService redisService;

    /**
     * 生成唯一id
     * 介绍以下函数的作用
     * 1. 生成时间戳
     * 2. 生成序列号
     * 3. 拼接并返回
     * @param keyPrefix
     * @return
     */
    public long nextId(String keyPrefix) {
        LocalDateTime now = LocalDateTime.now();
        long nowSecond = now.toEpochSecond(ZoneOffset.UTC);
        long timestamp = nowSecond - BEGIN_TIMESTAMP;

        // 2. 生成序列号
        String date = now.format(DateTimeFormatter.ofPattern("yyyy:MM:dd"));
        // 3. 获取当前日期
        long count = redisService.stringIncrementLongString("icr:" + keyPrefix + ":" + date, 1L);

        // 4. 拼接并返回
        return timestamp << COUNT_BITS | count;
    }
}
