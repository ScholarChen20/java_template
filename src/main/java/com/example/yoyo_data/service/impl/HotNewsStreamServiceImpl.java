package com.example.yoyo_data.service.impl;

import com.example.yoyo_data.common.document.HotNewsDetail;
import com.example.yoyo_data.service.HotNewsStreamService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
@Service
public class HotNewsStreamServiceImpl implements HotNewsStreamService {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @Autowired
    private RedisConnectionFactory redisConnectionFactory;
    
    @Autowired
    private HotNewsCacheServiceImpl hotNewsCacheService;
    
    @Override
    public void publishHotNewsToStream(String type, List<HotNewsDetail> hotNewsDetails) {
        try {
            String streamKey = hotNewsCacheService.getHotNewsStreamKey(type);
            
            // 1. 清除旧的Stream数据
            try (RedisConnection connection = redisConnectionFactory.getConnection()) {
                byte[] streamKeyBytes = streamKey.getBytes(StandardCharsets.UTF_8);
                if (connection.exists(streamKeyBytes) > 0) {
                    connection.del(streamKeyBytes);
                }
            }
            
            // 2. 发布数据到Stream
            for (int i = 0; i < hotNewsDetails.size(); i++) {
                HotNewsDetail detail = hotNewsDetails.get(i);
                Map<String, Object> fields = new HashMap<>();
                fields.put("title", detail.getTitle());
                fields.put("hot", detail.getHot());
                fields.put("url", detail.getUrl());
                fields.put("index", i);
                fields.put("detail", detail.toString());
                
                redisTemplate.opsForStream().add(streamKey, fields);
            }
            
            log.info("发布热点数据到Redis Stream成功，类型: {}, 数据条数: {}, Stream键: {}", type, hotNewsDetails.size(), streamKey);
        } catch (Exception e) {
            log.error("发布热点数据到Redis Stream失败", e);
        }
    }
    
    @Override
    public HotNewsDetail consumeHotNewsFromStream(String type, String consumerGroup, String consumerName) {
        try {
            String streamKey = hotNewsCacheService.getHotNewsStreamKey(type);
            
            // 确保消费组存在
            createConsumerGroup(type, consumerGroup);
            
            // 从Stream中消费一条消息
            Map<String, List<MapRecord<String, Object, Object>>> records = redisTemplate.opsForStream()
                    .read(Consumer.from(consumerGroup, consumerName),
                            StreamReadOptions.empty().count(1),
                            StreamOffset.create(streamKey, ReadOffset.lastConsumed()));
            
            if (records != null && !records.isEmpty()) {
                for (Map.Entry<String, List<MapRecord<String, Object, Object>>> entry : records.entrySet()) {
                    List<MapRecord<String, Object, Object>> streamRecords = entry.getValue();
                    for (MapRecord<String, Object, Object> record : streamRecords) {
                        // 解析消息
                        Map<Object, Object> value = record.getValue();
                        String title = (String) value.get("title");
                        String hot = (String) value.get("hot");
                        String url = (String) value.get("url");
                        Integer index = (Integer) value.get("index");
                        
                        // 创建HotNewsDetail对象
                        HotNewsDetail detail = new HotNewsDetail();
                        detail.setTitle(title);
                        detail.setHot(hot);
                        detail.setUrl(url);
                        // 其他字段可以根据需要设置
                        
                        log.info("从Redis Stream消费热点数据成功，类型: {}, 标题: {}, Stream键: {}", type, detail.getTitle(), streamKey);
                        return detail;
                    }
                }
            }
            
            log.info("Redis Stream中无未消费的热点数据，类型: {}, Stream键: {}", type, streamKey);
            return null;
        } catch (Exception e) {
            log.error("从Redis Stream消费热点数据失败", e);
            return null;
        }
    }
    
    @Override
    public void createConsumerGroup(String type, String consumerGroup) {
        try {
            String streamKey = hotNewsCacheService.getHotNewsStreamKey(type);
            
            try (RedisConnection connection = redisConnectionFactory.getConnection()) {
                byte[] streamKeyBytes = streamKey.getBytes(StandardCharsets.UTF_8);
                byte[] consumerGroupBytes = consumerGroup.getBytes(StandardCharsets.UTF_8);
                
                // 检查消费组是否存在
                boolean groupExists = false;
                try {
                    connection.xInfoGroups(streamKeyBytes);
                    groupExists = true;
                } catch (Exception e) {
                    // 消费组不存在，会抛出异常
                }
                
                if (!groupExists) {
                    // 创建消费组，从Stream开头开始消费
                    connection.xGroupCreate(streamKeyBytes, Arrays.toString(consumerGroupBytes), ReadOffset.from("0"), true);
                    log.info("创建Redis Stream消费组成功，类型: {}, 消费组: {}, Stream键: {}", type, consumerGroup, streamKey);
                }
            }
        } catch (Exception e) {
            log.error("创建Redis Stream消费组失败", e);
        }
    }
    
    @Override
    public boolean existsStream(String type) {
        try {
            String streamKey = hotNewsCacheService.getHotNewsStreamKey(type);
            
            try (RedisConnection connection = redisConnectionFactory.getConnection()) {
                byte[] streamKeyBytes = streamKey.getBytes(StandardCharsets.UTF_8);
                return connection.exists(streamKeyBytes) > 0;
            }
        } catch (Exception e) {
            log.error("检查Redis Stream是否存在失败", e);
            return false;
        }
    }
}
