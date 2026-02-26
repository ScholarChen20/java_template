package com.example.yoyo_data.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.yoyo_data.common.Result;
import com.example.yoyo_data.common.dto.QuerySeatDTO;
import com.example.yoyo_data.common.vo.*;
import com.example.yoyo_data.service.ShowEventService;
import com.example.yoyo_data.service.ShowRemindService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * 演出活动控制器
 * 提供演出活动、座位、搜索、城市、分类、提醒、想看等接口
 */
@Slf4j
@RestController
@RequestMapping("/api/ticket/shows")
@Api(tags = "演出活动模块", description = "演出活动查询、搜索、座位、开票提醒、想看等操作")
public class ShowEventController {

    @Autowired
    private ShowEventService showEventService;

    @Autowired
    private ShowRemindService showRemindService;

    /**
     * 查询演出活动列表（分页，支持多维度筛选）
     */
    @GetMapping
    @ApiOperation(value = "查询演出活动列表", notes = "分页查询演出活动列表，支持城市、分类、状态、日期、排序筛选")
    public Result<Page<ShowEventVO>> getShowEventList(
            @ApiParam(value = "页码", defaultValue = "1") @RequestParam(defaultValue = "1") Integer page,
            @ApiParam(value = "每页大小", defaultValue = "10") @RequestParam(defaultValue = "10") Integer size,
            @ApiParam(value = "状态筛选") @RequestParam(required = false) String status,
            @ApiParam(value = "演出类型筛选") @RequestParam(required = false) String showType,
            @ApiParam(value = "城市") @RequestParam(required = false) String city,
            @ApiParam(value = "演出分类") @RequestParam(required = false) String category,
            @ApiParam(value = "开始日期（yyyy-MM-dd）") @RequestParam(required = false) String startDate,
            @ApiParam(value = "结束日期（yyyy-MM-dd）") @RequestParam(required = false) String endDate,
            @ApiParam(value = "排序方式：hot-热度, time-时间, 默认-开票时间") @RequestParam(required = false) String sortBy
    ) {
        log.info("查询演出活动列表: page={}, size={}, status={}, city={}, category={}", page, size, status, city, category);
        return showEventService.getShowEventList(page, size, status, showType, city, category, startDate, endDate, sortBy);
    }

    /**
     * 演出搜索
     */
    @GetMapping("/search")
    @ApiOperation(value = "搜索演出", notes = "关键词搜索演出（演出名称/场馆名称），支持城市和分类筛选")
    public Result<Page<ShowEventVO>> searchShows(
            @ApiParam(value = "搜索关键词") @RequestParam(required = false) String keyword,
            @ApiParam(value = "城市") @RequestParam(required = false) String city,
            @ApiParam(value = "分类") @RequestParam(required = false) String category,
            @ApiParam(value = "页码", defaultValue = "1") @RequestParam(defaultValue = "1") Integer page,
            @ApiParam(value = "每页大小", defaultValue = "10") @RequestParam(defaultValue = "10") Integer size
    ) {
        log.info("搜索演出: keyword={}, city={}, category={}", keyword, city, category);
        return showEventService.searchShows(keyword, city, category, page, size);
    }

    /**
     * 获取城市列表
     */
    @GetMapping("/cities")
    @ApiOperation(value = "获取城市列表", notes = "获取有演出的城市列表及演出数量（Redis缓存1h）")
    public Result<List<CityVO>> getCities() {
        return showEventService.getCities();
    }

    /**
     * 获取演出分类
     */
    @GetMapping("/categories")
    @ApiOperation(value = "获取演出分类", notes = "获取所有演出分类")
    public Result<List<CategoryVO>> getCategories() {
        return showEventService.getCategories();
    }

    /**
     * 首页数据
     */
    @GetMapping("/homepage")
    @ApiOperation(value = "首页数据", notes = "获取首页聚合数据（热门演出、即将开票、推荐演出）")
    public Result<Map<String, Object>> getHomepageData(
            @ApiParam(value = "城市（可选，不传则全国）") @RequestParam(required = false) String city
    ) {
        return showEventService.getHomepageData(city);
    }

    /**
     * 查询演出活动详情
     */
    @GetMapping("/{id}")
    @ApiOperation(value = "查询演出活动详情", notes = "根据演出活动ID查询详细信息（Redis缓存）")
    public Result<ShowEventVO> getShowEventDetail(
            @ApiParam(value = "演出活动ID", required = true) @PathVariable("id") Long id
    ) {
        log.info("查询演出活动详情: id={}", id);
        return showEventService.getShowEventDetail(id);
    }

    /**
     * 获取座位区域列表
     */
    @GetMapping("/{showEventId}/zones")
    @ApiOperation(value = "获取座位区域列表", notes = "获取演出的座位区域及价格信息（Redis缓存5min）")
    public Result<List<ZoneVO>> getZonesBySeat(
            @ApiParam(value = "演出活动ID", required = true) @PathVariable Long showEventId
    ) {
        return showEventService.getZonesBySeat(showEventId);
    }

    /**
     * 查询座位列表
     */
    @GetMapping("/{showEventId}/seats")
    @ApiOperation(value = "查询座位列表", notes = "分页查询指定演出的座位列表，支持区域和状态筛选")
    public Result<Page<SeatVO>> getSeatList(
            @ApiParam(value = "演出活动ID", required = true) @PathVariable("showEventId") Long showEventId,
            @ApiParam(value = "页码", defaultValue = "1") @RequestParam(defaultValue = "1") Integer page,
            @ApiParam(value = "每页大小", defaultValue = "20") @RequestParam(defaultValue = "20") Integer size,
            @ApiParam(value = "座位区域筛选") @RequestParam(required = false) String seatZone,
            @ApiParam(value = "座位状态筛选") @RequestParam(required = false) String status
    ) {
        log.info("查询座位列表: showEventId={}, page={}, size={}, seatZone={}, status={}",
                showEventId, page, size, seatZone, status);
        QuerySeatDTO dto = QuerySeatDTO.builder()
                .showEventId(showEventId).page(page).size(size).seatZone(seatZone).status(status)
                .build();
        return showEventService.getSeatList(dto);
    }

    /**
     * 查询可售座位数量
     */
    @GetMapping("/{showEventId}/seats/available-count")
    @ApiOperation(value = "查询可售座位数量", notes = "查询指定演出和区域的可售座位数量")
    public Result<Integer> getAvailableSeatCount(
            @ApiParam(value = "演出活动ID", required = true) @PathVariable("showEventId") Long showEventId,
            @ApiParam(value = "座位区域（可选）") @RequestParam(required = false) String seatZone
    ) {
        return showEventService.getAvailableSeatCount(showEventId, seatZone);
    }

    // ==================== 开票提醒接口 ====================

    @PostMapping("/{showEventId}/remind")
    @ApiOperation(value = "设置开票提醒", notes = "为指定演出设置开票提醒（幂等）。需要JWT认证")
    public Result<ShowRemindVO> setRemind(
            @ApiParam(value = "演出活动ID", required = true) @PathVariable Long showEventId,
            HttpServletRequest request
    ) {
        String token = extractToken(request);
        return showRemindService.setRemind(showEventId, token);
    }

    @DeleteMapping("/{showEventId}/remind")
    @ApiOperation(value = "取消开票提醒", notes = "取消指定演出的开票提醒。需要JWT认证")
    public Result<String> cancelRemind(
            @ApiParam(value = "演出活动ID", required = true) @PathVariable Long showEventId,
            HttpServletRequest request
    ) {
        String token = extractToken(request);
        return showRemindService.cancelRemind(showEventId, token);
    }

    @GetMapping("/{showEventId}/remind")
    @ApiOperation(value = "查询提醒状态", notes = "查询当前用户对指定演出的提醒状态。需要JWT认证")
    public Result<ShowRemindVO> getRemindStatus(
            @ApiParam(value = "演出活动ID", required = true) @PathVariable Long showEventId,
            HttpServletRequest request
    ) {
        String token = extractToken(request);
        return showRemindService.getRemindStatus(showEventId, token);
    }

    @GetMapping("/reminded")
    @ApiOperation(value = "查询我的提醒列表", notes = "分页查询当前用户设置了提醒的演出列表。需要JWT认证")
    public Result<Page<ShowEventVO>> getMyRemindList(
            @ApiParam(value = "页码", defaultValue = "1") @RequestParam(defaultValue = "1") Integer page,
            @ApiParam(value = "每页大小", defaultValue = "10") @RequestParam(defaultValue = "10") Integer size,
            HttpServletRequest request
    ) {
        String token = extractToken(request);
        return showRemindService.getMyRemindList(token, page, size);
    }

    // ==================== 想看（心愿单）接口 ====================

    @PostMapping("/{showEventId}/wish")
    @ApiOperation(value = "标记想看", notes = "将演出加入想看列表（幂等，同时更新hot_score）。需要JWT认证")
    public Result<ShowWishVO> setWish(
            @ApiParam(value = "演出活动ID", required = true) @PathVariable Long showEventId,
            HttpServletRequest request
    ) {
        String token = extractToken(request);
        return showRemindService.setWish(showEventId, token);
    }

    @DeleteMapping("/{showEventId}/wish")
    @ApiOperation(value = "取消想看", notes = "从想看列表移除演出。需要JWT认证")
    public Result<String> cancelWish(
            @ApiParam(value = "演出活动ID", required = true) @PathVariable Long showEventId,
            HttpServletRequest request
    ) {
        String token = extractToken(request);
        return showRemindService.cancelWish(showEventId, token);
    }

    @GetMapping("/wish-list")
    @ApiOperation(value = "查询我的想看列表", notes = "分页查询当前用户的想看演出列表。需要JWT认证")
    public Result<Page<ShowEventVO>> getMyWishList(
            @ApiParam(value = "演出状态筛选") @RequestParam(required = false) String status,
            @ApiParam(value = "页码", defaultValue = "1") @RequestParam(defaultValue = "1") Integer page,
            @ApiParam(value = "每页大小", defaultValue = "10") @RequestParam(defaultValue = "10") Integer size,
            HttpServletRequest request
    ) {
        String token = extractToken(request);
        return showRemindService.getMyWishList(token, status, page, size);
    }

    private String extractToken(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return token;
    }
}
