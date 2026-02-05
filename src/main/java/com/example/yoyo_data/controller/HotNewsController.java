package com.example.yoyo_data.controller;

import com.example.yoyo_data.common.document.HotNewsDetail;
import com.example.yoyo_data.common.document.HotNewsMain;
import com.example.yoyo_data.service.HotNewsCacheService;
import com.example.yoyo_data.service.HotNewsService;
import com.example.yoyo_data.service.HotNewsStreamService;
import com.example.yoyo_data.common.Result;
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
}
