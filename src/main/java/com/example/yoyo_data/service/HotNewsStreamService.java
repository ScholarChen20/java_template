package com.example.yoyo_data.service;

import com.example.yoyo_data.common.document.HotNewsDetail;
import java.util.List;

public interface HotNewsStreamService {
    /**
     * 将热点数据发布到Redis Stream
     * @param type 热点类型
     * @param hotNewsDetails 热点数据列表
     */
    void publishHotNewsToStream(String type, List<HotNewsDetail> hotNewsDetails);
    
    /**
     * 从Redis Stream中消费一条热点数据
     * @param type 热点类型
     * @param consumerGroup 消费组
     * @param consumerName 消费者名称
     * @return 热点数据，如果没有则返回null
     */
    HotNewsDetail consumeHotNewsFromStream(String type, String consumerGroup, String consumerName);
    
    /**
     * 创建Redis Stream消费组
     * @param type 热点类型
     * @param consumerGroup 消费组
     */
    void createConsumerGroup(String type, String consumerGroup);
    
    /**
     * 检查Redis Stream是否存在
     * @param type 热点类型
     * @return 是否存在
     */
    boolean existsStream(String type);
}
