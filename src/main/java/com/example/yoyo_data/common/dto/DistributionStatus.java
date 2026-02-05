package com.example.yoyo_data.common.dto;

import lombok.Data;

/**
 * 数据分发状态
 */
@Data
public class DistributionStatus {
    private String type;
    private boolean isDistributing;
    private long lastDistributeTime;
    private int pendingCount;
    private int processedCount;
    
    public DistributionStatus(String type, boolean isDistributing, 
                           long lastDistributeTime, int pendingCount, int processedCount) {
        this.type = type;
        this.isDistributing = isDistributing;
        this.lastDistributeTime = lastDistributeTime;
        this.pendingCount = pendingCount;
        this.processedCount = processedCount;
    }
}