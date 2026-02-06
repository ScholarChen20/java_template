package com.example.yoyo_data.common.dto;

import com.example.yoyo_data.common.enums.UserLevel;
import lombok.Data;

import java.util.Map;

/**
 * 数据分发指标
 */
@Data
public class DistributionMetrics {
    private String type;
    private long totalDistributed;
    private Map<UserLevel, Long> levelDistributed;
    private double avgDistributionTime;
    private long successRate;
    
    public DistributionMetrics(String type, long totalDistributed, 
                             Map<UserLevel, Long> levelDistributed, 
                             double avgDistributionTime, long successRate) {
        this.type = type;
        this.totalDistributed = totalDistributed;
        this.levelDistributed = levelDistributed;
        this.avgDistributionTime = avgDistributionTime;
        this.successRate = successRate;
    }
}