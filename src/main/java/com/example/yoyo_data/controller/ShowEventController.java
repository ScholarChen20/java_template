package com.example.yoyo_data.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.yoyo_data.common.Result;
import com.example.yoyo_data.common.dto.QuerySeatDTO;
import com.example.yoyo_data.common.vo.ShowEventVO;
import com.example.yoyo_data.common.vo.SeatVO;
import com.example.yoyo_data.service.ShowEventService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 演出活动控制器
 * 提供演出活动和座位信息的查询接口
 */
@Slf4j
@RestController
@RequestMapping("/api/ticket/shows")
@Api(tags = "演出活动模块", description = "演出活动查询、座位查询等操作")
public class ShowEventController {

    @Autowired
    private ShowEventService showEventService;

    /**
     * 查询演出活动列表（分页）
     */
    @GetMapping
    @ApiOperation(value = "查询演出活动列表", notes = "分页查询演出活动列表，支持状态和类型筛选")
    public Result<Page<ShowEventVO>> getShowEventList(
            @ApiParam(value = "页码", defaultValue = "1") @RequestParam(value = "page", defaultValue = "1") Integer page,
            @ApiParam(value = "每页大小", defaultValue = "10") @RequestParam(value = "size", defaultValue = "10") Integer size,
            @ApiParam(value = "状态筛选（PENDING-待开票, SELLING-售票中, SOLD_OUT-售罄, ENDED-已结束）") @RequestParam(value = "status", required = false) String status,
            @ApiParam(value = "演出类型筛选（CONCERT-演唱会, MOVIE-电影, DRAMA-话剧）") @RequestParam(value = "showType", required = false) String showType
    ) {
        log.info("查询演出活动列表: page={}, size={}, status={}, showType={}", page, size, status, showType);
        return showEventService.getShowEventList(page, size, status, showType);
    }

    /**
     * 查询演出活动详情
     */
    @GetMapping("/{id}")
    @ApiOperation(value = "查询演出活动详情", notes = "根据演出活动ID查询详细信息（使用Redis缓存）")
    public Result<ShowEventVO> getShowEventDetail(
            @ApiParam(value = "演出活动ID", required = true) @PathVariable("id") Long id
    ) {
        log.info("查询演出活动详情: id={}", id);
        return showEventService.getShowEventDetail(id);
    }

    /**
     * 查询座位列表
     */
    @GetMapping("/{showEventId}/seats")
    @ApiOperation(value = "查询座位列表", notes = "分页查询指定演出的座位列表，支持区域和状态筛选")
    public Result<Page<SeatVO>> getSeatList(
            @ApiParam(value = "演出活动ID", required = true) @PathVariable("showEventId") Long showEventId,
            @ApiParam(value = "页码", defaultValue = "1") @RequestParam(value = "page", defaultValue = "1") Integer page,
            @ApiParam(value = "每页大小", defaultValue = "20") @RequestParam(value = "size", defaultValue = "20") Integer size,
            @ApiParam(value = "座位区域筛选（VIP, A, B, C）") @RequestParam(value = "seatZone", required = false) String seatZone,
            @ApiParam(value = "座位状态筛选（AVAILABLE-可售, LOCKED-已锁定, SOLD-已售出）") @RequestParam(value = "status", required = false) String status
    ) {
        log.info("查询座位列表: showEventId={}, page={}, size={}, seatZone={}, status={}",
                showEventId, page, size, seatZone, status);

        QuerySeatDTO dto = QuerySeatDTO.builder()
                .showEventId(showEventId)
                .page(page)
                .size(size)
                .seatZone(seatZone)
                .status(status)
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
            @ApiParam(value = "座位区域（可选，不传则查询所有区域）") @RequestParam(value = "seatZone", required = false) String seatZone
    ) {
        log.info("查询可售座位数量: showEventId={}, seatZone={}", showEventId, seatZone);
        return showEventService.getAvailableSeatCount(showEventId, seatZone);
    }
}
