package com.example.yoyo_data.service.impl;

//import cn.hutool.json.JSON;
import com.alibaba.fastjson.JSON;
import com.example.yoyo_data.common.enums.UserLevel;
import com.example.yoyo_data.common.document.HotNewsDetail;
import com.example.yoyo_data.common.dto.DistributionMetrics;
import com.example.yoyo_data.common.dto.DistributionResult;
import com.example.yoyo_data.common.dto.DistributionStatus;
import com.example.yoyo_data.infrastructure.cache.RedisService;
import com.example.yoyo_data.service.DataDistributionService;
import com.example.yoyo_data.service.HotNewsCacheService;
import com.example.yoyo_data.service.UserLevelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamReadOptions;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.example.yoyo_data.infrastructure.cache.CacheKeyManager.*;
import static com.example.yoyo_data.infrastructure.cache.CacheKeyManager.CacheTTL.ONE_DAY;
import static com.example.yoyo_data.infrastructure.cache.CacheKeyManager.CacheTTL.ONE_HOUR;

@Slf4j
@Service
public class DataDistributionServiceImpl implements DataDistributionService {
    
    @Autowired
    private HotNewsCacheService hotNewsCacheService;
    
    @Autowired
    private UserLevelService userLevelService;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final long LOCK_EXPIRE_SECONDS = 60;

    @Autowired
    private RedisService redisService;

    @Override
    public DistributionResult distributeHotNews(String type, List<HotNewsDetail> hotNewsDetails) {
        try {
            String lockKey = DISTRIBUTION_LOCK_KEY + type;
            
            // 尝试获取分布式锁，防止重复分发
            Boolean locked = redisService.setIfAbsent(lockKey, "1", LOCK_EXPIRE_SECONDS, TimeUnit.SECONDS);
            if (locked == null || !locked) {
                log.warn("分发任务正在进行中，跳过本次分发，类型: {}", type);
                return new DistributionResult(false, type, 0, new HashMap<>(), "分发任务正在进行中");
            }
            
            try {
                long startTime = System.currentTimeMillis();
                
                // 记录分发状态
                DistributionStatus status = new DistributionStatus(type, true, System.currentTimeMillis(), 
                        hotNewsDetails.size(), 0);
                redisService.stringSetString(DISTRIBUTION_STATUS_KEY + type, JSON.toJSONString(status), ONE_HOUR);
                
                // 按等级分发数据
                Map<UserLevel, Integer> levelCounts = new HashMap<>();
                for (UserLevel level : UserLevel.values()) {
                    int count = distributeToLevel(type, hotNewsDetails, level);
                    levelCounts.put(level, count);
                }
                
                // 更新分发状态
                status.setDistributing(false);
                status.setProcessedCount(hotNewsDetails.size());
                status.setPendingCount(0);
                redisService.stringSetString(DISTRIBUTION_STATUS_KEY + type, JSON.toJSONString(status), ONE_HOUR);
                
                // 更新分发指标
                updateDistributionMetrics(type, hotNewsDetails.size(), levelCounts, System.currentTimeMillis() - startTime);
                
                log.info("热点数据分发成功，类型: {}, 总数: {}, 等级分布: {}", 
                        type, hotNewsDetails.size(), levelCounts);
                
                return new DistributionResult(true, type, hotNewsDetails.size(), levelCounts, "分发成功");
            } finally {
                // 释放锁
                redisService.delete(lockKey);
            }
        } catch (Exception e) {
            log.error("分发热点数据失败，类型: {}", type, e);
            return new DistributionResult(false, type, 0, new HashMap<>(), "分发失败: " + e.getMessage());
        }
    }
    
    private int distributeToLevel(String type, List<HotNewsDetail> hotNewsDetails, UserLevel level) {
        try {
            String streamKey = hotNewsCacheService.getHotNewsStreamKey(type) + ":" + level.getName();
            
            // 清除旧的Stream数据
            redisService.delete(streamKey);
            
            // 根据等级确定分发数量
            int distributeCount = calculateDistributeCount(hotNewsDetails.size(), level);
            
            // 发布数据到等级专属Stream
            for (int i = 0; i < distributeCount && i < hotNewsDetails.size(); i++) {
                HotNewsDetail detail = hotNewsDetails.get(i);
                Map<String, Object> fields = new HashMap<>();
                fields.put("title", detail.getTitle());
                fields.put("hot", detail.getHot());
                fields.put("url", detail.getUrl());
                fields.put("index", i);
                fields.put("detail", detail.toString());
                
                redisService.streamAdd(streamKey, fields);
            }
            
            log.info("分发热点数据到等级 {} 成功，数量: {}, Stream键: {}", level.getName(), distributeCount, streamKey);
            return distributeCount;
        } catch (Exception e) {
            log.error("分发热点数据到等级 {} 失败", level.getName(), e);
            return 0;
        }
    }
    
    private int calculateDistributeCount(int totalCount, UserLevel level) {
        switch (level) {
            case LEVEL_A:
                return totalCount;
            case LEVEL_B:
                return (int) (totalCount * 0.8);
            case LEVEL_C:
                return (int) (totalCount * 0.5);
            default:
                return totalCount;
        }
    }
    
    @Override
    public List<HotNewsDetail> getHotNewsForUser(Long userId, String type, int count) {
        try {
            UserLevel userLevel = userLevelService.getUserLevel(userId);
            String streamKey = hotNewsCacheService.getHotNewsStreamKey(type) + ":" + userLevel.getName();
            
            // 尝试从等级专属Stream获取数据
            List<HotNewsDetail> hotNewsDetails = consumeFromStream(streamKey, userLevel.getName(), count);
            
            if (hotNewsDetails != null && !hotNewsDetails.isEmpty()) {
                log.info("从等级 {} Stream获取热点数据成功，用户ID: {}, 数量: {}", 
                        userLevel.getName(), userId, hotNewsDetails.size());
                return hotNewsDetails;
            }
            
            // 如果Stream中没有数据，从Redis ZSET缓存获取
            return hotNewsCacheService.getHotNewsFromRedisZSet(type, count);
        } catch (Exception e) {
            log.error("获取用户热点数据失败，用户ID: {}, 类型: {}", userId, type, e);
            return new ArrayList<>();
        }
    }
    
    private List<HotNewsDetail> consumeFromStream(String streamKey, String consumerGroup, int count) {
        try {
            List<MapRecord<String, Object, Object>> records = redisTemplate.opsForStream()
                    .read(
                            StreamReadOptions.empty().count(count),
                            StreamOffset.create(streamKey, ReadOffset.from("0"))
                    );
            
            if (records != null && !records.isEmpty()) {
                List<HotNewsDetail> hotNewsDetails = new ArrayList<>();
                for (MapRecord<String, Object, Object> record : records) {
                    Map<Object, Object> value = record.getValue();
                    String title = (String) value.get("title");
                    String hot = (String) value.get("hot");
                    String url = (String) value.get("url");
                    
                    HotNewsDetail detail = new HotNewsDetail();
                    detail.setTitle(title);
                    detail.setHot(hot);
                    detail.setUrl(url);
                    
                    hotNewsDetails.add(detail);
                }
                log.info("从Stream消费热点数据成功，Stream键: {}, 数量: {}", streamKey, hotNewsDetails.size());
                return hotNewsDetails;
            }
            
            return new ArrayList<>();
        } catch (Exception e) {
            log.error("从Stream消费热点数据失败，Stream键: {}", streamKey, e);
            return new ArrayList<>();
        }
    }
    
    @Override
    public DistributionStatus getDistributionStatus(String type) {
        try {
            String statusKey = DISTRIBUTION_STATUS_KEY + type;
            String statusStr = redisService.stringGetString(statusKey);
            if (statusStr == null) {
                return new DistributionStatus(type, false, 0, 0, 0);
            }

            // 解析状态字符串
            return JSON.parseObject(statusStr, DistributionStatus.class);
        } catch (Exception e) {
            log.error("获取分发状态失败，类型: {}", type, e);
            return new DistributionStatus(type, false, 0, 0, 0);
        }
    }
    
    @Override
    public DistributionMetrics getDistributionMetrics(String type) {
        try {
            String metricsKey = DISTRIBUTION_METRICS_KEY + type;
            String metricStr = redisService.stringGetString(metricsKey);
            if (metricStr == null) {
                return new DistributionMetrics(type, 0, new HashMap<>(), 0, 0);
            }
            // 检查是否为有效的JSON字符串
            if (!isValidJson(metricStr)) {
                log.warn("Redis中存储的分发指标不是有效的JSON格式，类型: {}", type);
                return new DistributionMetrics(type, 0, new HashMap<>(), 0, 0);
            }
            return JSON.parseObject(metricStr, DistributionMetrics.class);
        } catch (Exception e) {
            log.error("获取分发指标失败，类型: {}", type, e);
            return new DistributionMetrics(type, 0, new HashMap<>(), 0, 0);
        }
    }
    
    /**
     * 检查字符串是否为有效的JSON格式
     * @param str 待检查的字符串
     * @return 是否为有效的JSON格式
     */
    private boolean isValidJson(String str) {
        if (str == null || str.trim().isEmpty()) {
            return false;
        }
        str = str.trim();
        return (str.startsWith("{") && str.endsWith("}")) || (str.startsWith("[") && str.endsWith("]"));
    }
    
    private void updateDistributionMetrics(String type, int totalCount, 
                                         Map<UserLevel, Integer> levelCounts,
                                         long distributionTime) {
        try {
            String metricsKey = DISTRIBUTION_METRICS_KEY + type;
            
            // 获取现有指标
            DistributionMetrics metrics = getDistributionMetrics(type);
            
            // 更新指标
            metrics.setTotalDistributed(metrics.getTotalDistributed() + totalCount);
            
            Map<UserLevel, Long> levelDistributed = metrics.getLevelDistributed();
            for (Map.Entry<UserLevel, Integer> entry : levelCounts.entrySet()) {
                UserLevel level = entry.getKey();
                int count = entry.getValue();
                levelDistributed.put(level, levelDistributed.getOrDefault(level, 0L) + count);
            }
            
            // 计算平均分发时间
            long totalDistributed = metrics.getTotalDistributed();
            double avgTime = (metrics.getAvgDistributionTime() * (totalDistributed - totalCount) + distributionTime) / totalDistributed;
            metrics.setAvgDistributionTime(avgTime);

            // 存入缓存
            redisService.stringSetString(metricsKey, JSON.toJSONString(metrics), ONE_DAY);
            
            log.info("更新分发指标成功，类型: {}, 总分发数: {}", type, totalDistributed);
        } catch (Exception e) {
            log.error("更新分发指标失败，类型: {}", type, e);
        }
    }
}
