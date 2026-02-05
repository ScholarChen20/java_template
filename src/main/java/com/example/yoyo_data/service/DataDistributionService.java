package com.example.yoyo_data.service;

import com.example.yoyo_data.common.document.HotNewsDetail;
import com.example.yoyo_data.common.dto.DistributionMetrics;
import com.example.yoyo_data.common.dto.DistributionResult;
import com.example.yoyo_data.common.dto.DistributionStatus;

import java.util.List;

public interface DataDistributionService {
    /**
     * 分发热点数据到不同等级的用户
     * @param type 热点类型
     * @param hotNewsDetails 热点数据列表
     * @return 分发结果
     */
    DistributionResult distributeHotNews(String type, List<HotNewsDetail> hotNewsDetails);
    
    /**
     * 获取指定等级的用户热点数据
     * @param userId 用户ID
     * @param type 热点类型
     * @param count 获取数量
     * @return 热点数据列表
     */
    List<HotNewsDetail> getHotNewsForUser(Long userId, String type, int count);
    
    /**
     * 获取分发状态
     * @param type 热点类型
     * @return 分发状态
     */
    DistributionStatus getDistributionStatus(String type);
    
    /**
     * 获取分发指标
     * @param type 热点类型
     * @return 分发指标
     */
    DistributionMetrics getDistributionMetrics(String type);
}
