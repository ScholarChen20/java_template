package com.example.yoyo_data.controller;

import com.example.yoyo_data.common.Result;
import com.example.yoyo_data.common.document.HotNewsDetail;
import com.example.yoyo_data.common.document.HotNewsMain;
import com.example.yoyo_data.common.dto.DistributionMetrics;
import com.example.yoyo_data.common.dto.DistributionResult;
import com.example.yoyo_data.common.dto.DistributionStatus;
import com.example.yoyo_data.service.DataDistributionService;
import com.example.yoyo_data.service.HotNewsCacheService;
import com.example.yoyo_data.service.HotNewsService;
import com.example.yoyo_data.service.HotNewsStreamService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Api(tags = "热点排行接口")
@RestController
@RequestMapping("/api/hot-news")
@Slf4j
public class HotNewsController {
    
    @Autowired
    private HotNewsService hotNewsService;
    
    @Autowired
    private DataDistributionService dataDistributionService;
    
    @ApiOperation(value = "获取热点排行数据", notes = "从Redis缓存获取热点排行数据，没有缓存则从数据库获取")
    @GetMapping("/ranking")
    public Result<List<HotNewsDetail>> getHotNewsRanking(
            @ApiParam(name = "type", value = "热点类型，如：douyinhot", required = true) @RequestParam String type,
            @ApiParam(name = "count", value = "获取数量，默认10", defaultValue = "10") @RequestParam(defaultValue = "10") int count) {
        try {
            log.info("从数据库获取热点排行数据，类型: {}, 数量: {}", type, count);
            List<HotNewsDetail> hotNewsDetails = hotNewsService.getLatestHotNews(type, count);
            return Result.success(hotNewsDetails);

        } catch (Exception e) {
            log.error("获取热点排行数据失败", e);
            return Result.error(500, "获取热点排行数据失败");
        }
    }

    
    @ApiOperation(value = "刷新热点数据", notes = "从接口获取最新热点数据并保存到数据库和Redis缓存")
    @GetMapping("/refresh")
    public Result<HotNewsMain> refreshHotNews(
            @ApiParam(name = "type", value = "热点类型，如：douyinhot", required = true) @RequestParam String type) {
        try {
            log.info("刷新热点数据，类型: {}", type);
            HotNewsMain hotNewsMain = hotNewsService.fetchAndSaveHotNews(type);
            if (hotNewsMain != null) {
                return Result.success(hotNewsMain);
            } else {
                return Result.error(500, "刷新热点数据失败");
            }
        } catch (Exception e) {
            log.error("刷新热点数据失败", e);
            return Result.error(500, "刷新热点数据失败");
        }
    }
    
    @ApiOperation(value = "获取用户专属热点数据", notes = "根据用户等级获取热点数据，优先从等级专属Stream获取")
    @GetMapping("/user")
    public Result<List<HotNewsDetail>> getHotNewsForUser(
            @ApiParam(name = "userId", value = "用户ID", required = true) @RequestParam Long userId,
            @ApiParam(name = "type", value = "热点类型，如：douyinhot", required = true) @RequestParam String type,
            @ApiParam(name = "count", value = "获取数量，默认10", defaultValue = "10") @RequestParam(defaultValue = "10") int count) {
        try {
            log.info("获取用户专属热点数据，用户ID: {}, 类型: {}, 数量: {}", userId, type, count);
            List<HotNewsDetail> hotNewsDetails = dataDistributionService.getHotNewsForUser(userId, type, count);
            return Result.success(hotNewsDetails);
        } catch (Exception e) {
            log.error("获取用户专属热点数据失败", e);
            return Result.error(500, "获取用户专属热点数据失败");
        }
    }
    
    @ApiOperation(value = "手动触发数据分发", notes = "手动触发热点数据的分级分发")
    @GetMapping("/distribute")
    public Result<DistributionResult> distributeHotNews(
            @ApiParam(name = "type", value = "热点类型，如：douyinhot", required = true) @RequestParam String type) {
        try {
            log.info("手动触发数据分发，类型: {}", type);
            // 从数据库获取最新热点数据
            HotNewsMain hotNewsMain = hotNewsService.getLatestHotNews(type);
            if (hotNewsMain == null || hotNewsMain.getData() == null || hotNewsMain.getData().isEmpty()) {
                return Result.error(404, "暂无热点数据可分发");
            }
            
            // 分发数据
            DistributionResult result = dataDistributionService.distributeHotNews(type, hotNewsMain.getData());
            
            if (result.isSuccess()) {
                return Result.success(result);
            } else {
                return Result.error(500, result.getMessage());
            }
        } catch (Exception e) {
            log.error("手动触发数据分发失败", e);
            return Result.error(500, "手动触发数据分发失败");
        }
    }
    
    @ApiOperation(value = "获取分发状态", notes = "查看热点数据的分发状态")
    @GetMapping("/distribution/status")
    public Result<DistributionStatus> getDistributionStatus(
            @ApiParam(name = "type", value = "热点类型，如：douyinhot", required = true) @RequestParam String type) {
        try {
            log.info("获取分发状态，类型: {}", type);
            DistributionStatus status = dataDistributionService.getDistributionStatus(type);
            return Result.success(status);
        } catch (Exception e) {
            log.error("获取分发状态失败", e);
            return Result.error(500, "获取分发状态失败");
        }
    }
    
    @ApiOperation(value = "获取分发指标", notes = "查看热点数据的分发指标")
    @GetMapping("/distribution/metrics")
    public Result<DistributionMetrics> getDistributionMetrics(
            @ApiParam(name = "type", value = "热点类型，如：douyinhot", required = true) @RequestParam String type) {
        try {
            log.info("获取分发指标，类型: {}", type);
            DistributionMetrics metrics = dataDistributionService.getDistributionMetrics(type);
            return Result.success(metrics);
        } catch (Exception e) {
            log.error("获取分发指标失败", e);
            return Result.error(500, "获取分发指标失败");
        }
    }
}
