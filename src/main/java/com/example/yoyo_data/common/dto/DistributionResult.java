package com.example.yoyo_data.common.dto;

import com.example.yoyo_data.common.Enum.UserLevel;
import lombok.Data;

import java.util.Map;

/**
 * 数据分发结果
 */
@Data
public class DistributionResult {
    private boolean success;
    private String type;
    private int totalCount;
    private Map<UserLevel, Integer> levelCounts;
    private String message;
    
    public DistributionResult(boolean success, String type, int totalCount, 
                            Map<UserLevel, Integer> levelCounts, String message) {
        this.success = success;
        this.type = type;
        this.totalCount = totalCount;
        this.levelCounts = levelCounts;
        this.message = message;
    }
}