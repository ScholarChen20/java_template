package com.example.yoyo_data.service.impl;

import com.example.yoyo_data.common.document.HotNewsDetail;
import com.example.yoyo_data.service.HotNewsCacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.example.yoyo_data.infrastructure.cache.CacheKeyManager.HOT_NEWS_STREAM_KEY_PREFIX;
import static com.example.yoyo_data.infrastructure.cache.CacheKeyManager.HOT_NEWS_ZSET_KEY_PREFIX;

@Slf4j
@Service
public class HotNewsCacheServiceImpl implements HotNewsCacheService {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    

    
    /**
     * 生成热点数据ZSET的Redis键
     * @param type 热点类型
     * @return Redis键
     */
    private String getHotNewsZSetKey(String type) {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return HOT_NEWS_ZSET_KEY_PREFIX + type + ":" + date;
    }
    
    /**
     * 生成热点数据Stream的Redis键
     * @param type 热点类型
     * @return Redis键
     */
    public String getHotNewsStreamKey(String type) {
        return HOT_NEWS_STREAM_KEY_PREFIX + type;
    }
    
    @Override
    public void saveHotNewsToRedisZSet(String type, List<HotNewsDetail> hotNewsDetails) {
        try {
            String key = getHotNewsZSetKey(type);
            
            // 清除旧数据
            redisTemplate.delete(key);
            
            // 存入ZSET，score为hot值，member为序列化的HotNewsDetail对象
            for (HotNewsDetail detail : hotNewsDetails) {
                try {
                    // 将hot字符串转换为double类型
                    double hotValue = Double.parseDouble(detail.getHot());
                    redisTemplate.opsForZSet().add(key, detail, hotValue);
                } catch (NumberFormatException e) {
                    // 如果转换失败，使用默认值0
                    log.warn("转换hot值失败，使用默认值0，标题: {}", detail.getTitle());
                    redisTemplate.opsForZSet().add(key, detail, 0);
                }
            }
            
            log.info("保存热点数据到Redis ZSET成功，类型: {}, 数据条数: {}, 键: {}", type, hotNewsDetails.size(), key);
        } catch (Exception e) {
            log.error("保存热点数据到Redis ZSET失败", e);
        }
    }
    
    @Override
    public List<HotNewsDetail> getHotNewsFromRedisZSet(String type, int count) {
        try {
            String key = getHotNewsZSetKey(type);
            
            // 从ZSET中获取数据，按分数倒序排列（热点值越高排名越前）
            Set<Object> members = redisTemplate.opsForZSet().reverseRange(key, 0, count - 1);
            
            if (members != null && !members.isEmpty()) {
                List<HotNewsDetail> result = new ArrayList<>();
                for (Object member : members) {
                    if (member instanceof HotNewsDetail) {
                        result.add((HotNewsDetail) member);
                    }
                }
                log.info("从Redis ZSET获取热点数据成功，类型: {}, 获取条数: {}, 键: {}", type, result.size(), key);
                return result;
            }
            
            log.info("Redis ZSET中无热点数据，类型: {}, 键: {}", type, key);
            return new ArrayList<>();
        } catch (Exception e) {
            log.error("从Redis ZSET获取热点数据失败", e);
            return new ArrayList<>();
        }
    }
    
    @Override
    public boolean hasHotNewsCache(String type) {
        try {
            String key = getHotNewsZSetKey(type);
            boolean exists = redisTemplate.hasKey(key);
            log.info("检查热点数据缓存是否存在，类型: {}, 键: {}, 结果: {}", type, key, exists);
            return exists;
        } catch (Exception e) {
            log.error("检查热点数据缓存失败", e);
            return false;
        }
    }
    
    @Override
    public void clearHotNewsCache(String type) {
        try {
            String key = getHotNewsZSetKey(type);
            redisTemplate.delete(key);
            log.info("清除热点数据缓存成功，类型: {}, 键: {}", type, key);
        } catch (Exception e) {
            log.error("清除热点数据缓存失败", e);
        }
    }
}
