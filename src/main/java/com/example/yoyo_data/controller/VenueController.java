package com.example.yoyo_data.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.yoyo_data.common.Result;
import com.example.yoyo_data.common.vo.ShowEventVO;
import com.example.yoyo_data.common.vo.VenueVO;
import com.example.yoyo_data.service.VenueService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 场馆信息控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/ticket/venues")
@Api(tags = "场馆信息", description = "场馆列表、详情及场馆演出查询")
public class VenueController {

    @Autowired
    private VenueService venueService;

    @GetMapping
    @ApiOperation(value = "获取场馆列表", notes = "分页查询场馆列表，支持城市筛选")
    public Result<Page<VenueVO>> getVenueList(
            @ApiParam(value = "城市") @RequestParam(required = false) String city,
            @ApiParam(value = "页码", defaultValue = "1") @RequestParam(defaultValue = "1") Integer page,
            @ApiParam(value = "每页大小", defaultValue = "10") @RequestParam(defaultValue = "10") Integer size
    ) {
        log.info("获取场馆列表: city={}, page={}, size={}", city, page, size);
        return venueService.getVenueList(city, page, size);
    }

    @GetMapping("/{venueId}")
    @ApiOperation(value = "获取场馆详情", notes = "根据场馆ID获取详细信息（Redis缓存24h）")
    public Result<VenueVO> getVenueDetail(
            @ApiParam(value = "场馆ID", required = true) @PathVariable Long venueId
    ) {
        log.info("获取场馆详情: venueId={}", venueId);
        return venueService.getVenueDetail(venueId);
    }

    @GetMapping("/{venueId}/shows")
    @ApiOperation(value = "获取场馆演出列表", notes = "查询指定场馆的演出列表")
    public Result<Page<ShowEventVO>> getShowsByVenue(
            @ApiParam(value = "场馆ID", required = true) @PathVariable Long venueId,
            @ApiParam(value = "演出状态") @RequestParam(required = false) String status,
            @ApiParam(value = "页码", defaultValue = "1") @RequestParam(defaultValue = "1") Integer page,
            @ApiParam(value = "每页大小", defaultValue = "10") @RequestParam(defaultValue = "10") Integer size
    ) {
        log.info("获取场馆演出列表: venueId={}, status={}", venueId, status);
        return venueService.getShowsByVenue(venueId, status, page, size);
    }
}
