package com.example.yoyo_data.controller;

import com.example.yoyo_data.common.Result;
import com.example.yoyo_data.infrastructure.scheduler.SeatCacheWarmupTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 座位缓存管理接口
 * 用于手动触发座位缓存预热、清理等操作
 *
 * @author YoYo Data Team
 * @version 1.0
 * @since 2026-02-10
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/seat-cache")
public class SeatCacheAdminController {

    @Autowired
    private SeatCacheWarmupTask seatCacheWarmupTask;

    /**
     * 手动触发座位缓存预热
     *
     * @param showEventId 演出活动ID
     * @return 预热结果
     */
    @PostMapping("/warmup")
    public Result<String> warmupSeatCache(@RequestParam Long showEventId) {
        log.info("【管理接口】手动触发座位缓存预热: showEventId={}", showEventId);

        try {
            seatCacheWarmupTask.manualWarmup(showEventId);
            return Result.success("座位缓存预热成功");

        } catch (Exception e) {
            log.error("【管理接口】座位缓存预热失败: showEventId={}, error={}",
                    showEventId, e.getMessage(), e);
            return Result.error("座位缓存预热失败: " + e.getMessage());
        }
    }

    /**
     * 清除演出的座位缓存
     *
     * @param showEventId 演出活动ID
     * @return 清理结果
     */
    @DeleteMapping("/clear")
    public Result<String> clearSeatCache(@RequestParam Long showEventId) {
        log.info("【管理接口】清除座位缓存: showEventId={}", showEventId);

        try {
            seatCacheWarmupTask.clearWarmupCache(showEventId);
            return Result.success("座位缓存清理成功");

        } catch (Exception e) {
            log.error("【管理接口】座位缓存清理失败: showEventId={}, error={}",
                    showEventId, e.getMessage(), e);
            return Result.error("座位缓存清理失败: " + e.getMessage());
        }
    }

    /**
     * 立即执行定时任务（预热所有即将开票的演出）
     *
     * @return 执行结果
     */
    @PostMapping("/warmup-all")
    public Result<String> warmupAllUpcomingEvents() {
        log.info("【管理接口】手动执行定时任务：预热所有即将开票的演出");

        try {
            seatCacheWarmupTask.warmupSeatCache();
            return Result.success("定时任务执行成功");

        } catch (Exception e) {
            log.error("【管理接口】定时任务执行失败: error={}", e.getMessage(), e);
            return Result.error("定时任务执行失败: " + e.getMessage());
        }
    }
}
