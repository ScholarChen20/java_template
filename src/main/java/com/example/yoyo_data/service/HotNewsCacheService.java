package com.example.yoyo_data.service;

import com.example.yoyo_data.common.document.HotNewsDetail;
import java.util.List;

public interface HotNewsCacheService {
    /**
     * 将热点数据存入Redis ZSET集合
     * @param type 热点类型
     * @param hotNewsDetails 热点数据列表
     */
    void saveHotNewsToRedisZSet(String type, List<HotNewsDetail> hotNewsDetails);
    
    /**
     * 从Redis ZSET集合中获取热点数据
     * @param type 热点类型
     * @param count 获取数量
     * @return 热点数据列表
     */
    List<HotNewsDetail> getHotNewsFromRedisZSet(String type, int count);
    
    /**
     * 检查Redis中是否存在热点数据缓存
     * @param type 热点类型
     * @return 是否存在缓存
     */
    boolean hasHotNewsCache(String type);
    
    /**
     * 清除热点数据缓存
     * @param type 热点类型
     */
    void clearHotNewsCache(String type);
}
