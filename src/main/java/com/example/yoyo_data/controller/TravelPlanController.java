package com.example.yoyo_data.controller;

import com.example.yoyo_data.common.Result;
import com.example.yoyo_data.service.TravelPlanService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 旅行计划控制器
 * 处理旅行计划相关操作
 */
@Slf4j
@RestController
@RequestMapping("/api/travel-plans")
@Api(tags = "旅行计划模块")
public class TravelPlanController {

    @Autowired
    private TravelPlanService travelPlanService;

    /**
     * 创建旅行计划
     *
     * @param params 创建旅行计划参数，包含title、description、start_date、end_date、destinations、budget等
     * @param request 请求对象，用于获取当前用户信息
     * @return 创建结果
     */
    @PostMapping
    @ApiOperation(value = "创建旅行计划", notes = "创建新的旅行计划")
    public Result<Map<String, Object>> createTravelPlan(@ApiParam(value = "创建旅行计划参数") @RequestBody Map<String, Object> params, HttpServletRequest request) {
        // 从请求头获取token
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return travelPlanService.createTravelPlan(token, params);
    }

    /**
     * 获取旅行计划列表
     *
     * @param page     页码，默认 1
     * @param size     每页数量，默认 10
     * @param status   状态，可选 draft, active, completed, cancelled
     * @param sort     排序方式，默认 created_at
     * @param request  请求对象，用于获取当前用户信息
     * @return 旅行计划列表
     */
    @GetMapping
    @ApiOperation(value = "获取旅行计划列表", notes = "获取当前用户的旅行计划列表")
    public Result<Map<String, Object>> getTravelPlanList(
            @ApiParam(value = "页码", defaultValue = "1") @RequestParam(defaultValue = "1") Integer page,
            @ApiParam(value = "每页数量", defaultValue = "10") @RequestParam(defaultValue = "10") Integer size,
            @ApiParam(value = "状态") @RequestParam(required = false) String status,
            @ApiParam(value = "排序方式") @RequestParam(required = false) String sort,
            HttpServletRequest request) {
        // 从请求头获取token
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return travelPlanService.getTravelPlanList(token, page, size, status, sort);
    }

    /**
     * 获取旅行计划详情
     *
     * @param id 旅行计划ID
     * @param request 请求对象，用于获取当前用户信息
     * @return 旅行计划详情
     */
    @GetMapping("/{id}")
    @ApiOperation(value = "获取旅行计划详情", notes = "根据旅行计划ID获取旅行计划详情")
    public Result<Map<String, Object>> getTravelPlanDetail(@ApiParam(value = "旅行计划ID") @PathVariable Long id, HttpServletRequest request) {
        // 从请求头获取token
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return travelPlanService.getTravelPlanDetail(id, token);
    }

    /**
     * 更新旅行计划
     *
     * @param id     旅行计划ID
     * @param params 更新参数，包含title、description、start_date、end_date、destinations、budget等
     * @param request 请求对象，用于获取当前用户信息
     * @return 更新结果
     */
    @PutMapping("/{id}")
    @ApiOperation(value = "更新旅行计划", notes = "更新旅行计划信息")
    public Result<Map<String, Object>> updateTravelPlan(@ApiParam(value = "旅行计划ID") @PathVariable Long id, @ApiParam(value = "更新参数") @RequestBody Map<String, Object> params, HttpServletRequest request) {
        // 从请求头获取token
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return travelPlanService.updateTravelPlan(id, token, params);
    }

    /**
     * 删除旅行计划
     *
     * @param id 旅行计划ID
     * @param request 请求对象，用于获取当前用户信息
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除旅行计划", notes = "删除旅行计划")
    public Result<Map<String, Object>> deleteTravelPlan(@ApiParam(value = "旅行计划ID") @PathVariable Long id, HttpServletRequest request) {
        // 从请求头获取token
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return travelPlanService.deleteTravelPlan(id, token);
    }

    /**
     * 分享旅行计划
     *
     * @param id 旅行计划ID
     * @param request 请求对象，用于获取当前用户信息
     * @return 分享结果
     */
    @PostMapping("/{id}/share")
    @ApiOperation(value = "分享旅行计划", notes = "生成旅行计划的分享链接")
    public Result<Map<String, Object>> shareTravelPlan(@ApiParam(value = "旅行计划ID") @PathVariable Long id, HttpServletRequest request) {
        // 从请求头获取token
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return travelPlanService.shareTravelPlan(id, token);
    }

    /**
     * 获取公开的旅行计划
     *
     * @param shareId 分享ID
     * @return 旅行计划详情
     */
    @GetMapping("/shared/{shareId}")
    @ApiOperation(value = "获取公开的旅行计划", notes = "通过分享ID获取公开的旅行计划")
    public Result<Map<String, Object>> getSharedTravelPlan(@ApiParam(value = "分享ID") @PathVariable String shareId) {
        return travelPlanService.getSharedTravelPlan(shareId);
    }
}
