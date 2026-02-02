package com.example.yoyo_data.controller;

import com.example.yoyo_data.common.Result;
import com.example.yoyo_data.service.TravelPlanService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 旅行计划模块控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/travel-plans")
@Api(tags = "旅行计划模块", description = "旅行计划的创建、查询、更新、删除等操作")
public class TravelPlanController {
    @Autowired
    private TravelPlanService travelPlanService;

    /**
     * 获取旅行计划列表
     *
     * @param page 页码
     * @param size 每页大小
     * @return 旅行计划列表
     */
    @GetMapping("/list")
    @ApiOperation(value = "获取旅行计划列表", notes = "获取旅行计划列表，支持分页")
    public Result<?> getTravelPlanList(
            @ApiParam(value = "页码", required = false, defaultValue = "1") @RequestParam(value = "page", defaultValue = "1") Integer page,
            @ApiParam(value = "每页大小", required = false, defaultValue = "10") @RequestParam(value = "size", defaultValue = "10") Integer size
    ) {
        log.info("获取旅行计划列表: page={}, size={}", page, size);
        
        // 模拟数据
        Map<String, Object> result = new HashMap<>();
        result.put("page", page);
        result.put("size", size);
        result.put("total", 100);
        result.put("plans", new java.util.ArrayList<>());
        
        return Result.success(result);
    }

    /**
     * 获取旅行计划详情
     *
     * @param planId 旅行计划ID
     * @return 旅行计划详情
     */
    @GetMapping("/detail/{planId}")
    @ApiOperation(value = "获取旅行计划详情", notes = "获取旅行计划的详细信息")
    public Result<?> getTravelPlanDetail(
            @ApiParam(value = "旅行计划ID", required = true) @PathVariable("planId") Long planId
    ) {
        log.info("获取旅行计划详情: planId={}", planId);
        
        // 模拟数据
        Map<String, Object> result = new HashMap<>();
        result.put("id", planId);
        result.put("title", "旅行计划" + planId);
        result.put("description", "这是一个详细的旅行计划" + planId);
        result.put("destination", "目的地" + planId);
        result.put("startDate", "2024-01-01");
        result.put("endDate", "2024-01-05");
        result.put("days", 5);
        result.put("userId", 1L);
        result.put("username", "用户1");
        result.put("createdAt", System.currentTimeMillis());
        result.put("updatedAt", System.currentTimeMillis());
        
        return Result.success(result);
    }

    /**
     * 创建旅行计划
     *
     * @param title 标题
     * @param description 描述
     * @param destination 目的地
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 创建结果
     */
    @PostMapping("/create")
    @ApiOperation(value = "创建旅行计划", notes = "创建新的旅行计划")
    public Result<?> createTravelPlan(
            @ApiParam(value = "标题", required = true) @RequestParam("title") String title,
            @ApiParam(value = "描述", required = true) @RequestParam("description") String description,
            @ApiParam(value = "目的地", required = true) @RequestParam("destination") String destination,
            @ApiParam(value = "开始日期", required = true) @RequestParam("startDate") String startDate,
            @ApiParam(value = "结束日期", required = true) @RequestParam("endDate") String endDate
    ) {
        log.info("创建旅行计划: title={}, destination={}, startDate={}, endDate={}", title, destination, startDate, endDate);
        
        // 模拟数据
        Map<String, Object> result = new HashMap<>();
        result.put("id", System.currentTimeMillis());
        result.put("title", title);
        result.put("description", description);
        result.put("destination", destination);
        result.put("startDate", startDate);
        result.put("endDate", endDate);
        result.put("days", 5);
        result.put("userId", 1L);
        result.put("username", "用户1");
        result.put("createdAt", System.currentTimeMillis());
        result.put("updatedAt", System.currentTimeMillis());
        
        return Result.success(result);
    }

    /**
     * 更新旅行计划
     *
     * @param planId 旅行计划ID
     * @param title 标题
     * @param description 描述
     * @param destination 目的地
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 更新结果
     */
    @PutMapping("/update/{planId}")
    @ApiOperation(value = "更新旅行计划", notes = "更新旅行计划信息")
    public Result<?> updateTravelPlan(
            @ApiParam(value = "旅行计划ID", required = true) @PathVariable("planId") Long planId,
            @ApiParam(value = "标题", required = true) @RequestParam("title") String title,
            @ApiParam(value = "描述", required = true) @RequestParam("description") String description,
            @ApiParam(value = "目的地", required = true) @RequestParam("destination") String destination,
            @ApiParam(value = "开始日期", required = true) @RequestParam("startDate") String startDate,
            @ApiParam(value = "结束日期", required = true) @RequestParam("endDate") String endDate
    ) {
        log.info("更新旅行计划: planId={}, title={}, destination={}, startDate={}, endDate={}", planId, title, destination, startDate, endDate);
        
        // 模拟数据
        Map<String, Object> result = new HashMap<>();
        result.put("id", planId);
        result.put("title", title);
        result.put("description", description);
        result.put("destination", destination);
        result.put("startDate", startDate);
        result.put("endDate", endDate);
        result.put("days", 5);
        result.put("updatedAt", System.currentTimeMillis());
        
        return Result.success(result);
    }

    /**
     * 删除旅行计划
     *
     * @param planId 旅行计划ID
     * @return 删除结果
     */
    @DeleteMapping("/delete/{planId}")
    @ApiOperation(value = "删除旅行计划", notes = "删除旅行计划")
    public Result<?> deleteTravelPlan(@ApiParam(value = "旅行计划ID", required = true) @PathVariable("planId") Long planId) {
        travelPlanService.deleteTravelPlan(planId);
        log.info("删除旅行计划: planId={}", planId);
        return Result.success("删除旅行计划成功");
    }
}