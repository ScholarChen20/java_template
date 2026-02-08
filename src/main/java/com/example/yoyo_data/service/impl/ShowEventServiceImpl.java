package com.example.yoyo_data.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.yoyo_data.common.Result;
import com.example.yoyo_data.common.constant.SeatStatus;
import com.example.yoyo_data.common.constant.TicketRedisKey;
import com.example.yoyo_data.common.dto.QuerySeatDTO;
import com.example.yoyo_data.common.entity.Seat;
import com.example.yoyo_data.common.entity.ShowEvent;
import com.example.yoyo_data.common.vo.SeatVO;
import com.example.yoyo_data.common.vo.ShowEventVO;
import com.example.yoyo_data.infrastructure.cache.RedisService;
import com.example.yoyo_data.infrastructure.repository.SeatMapper;
import com.example.yoyo_data.infrastructure.repository.ShowEventMapper;
import com.example.yoyo_data.service.ShowEventService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 演出活动服务实现类
 */
@Slf4j
@Service
public class ShowEventServiceImpl implements ShowEventService {

    @Autowired
    private ShowEventMapper showEventMapper;

    @Autowired
    private SeatMapper seatMapper;

    @Autowired
    private RedisService redisService;

    @Override
    public Result<Page<ShowEventVO>> getShowEventList(Integer page, Integer size, String status, String showType) {
        try {
            Page<ShowEvent> pageParam = new Page<>(page, size);
            LambdaQueryWrapper<ShowEvent> queryWrapper = new LambdaQueryWrapper<>();

            // 状态筛选
            if (status != null && !status.isEmpty()) {
                queryWrapper.eq(ShowEvent::getStatus, status);
            }

            // 演出类型筛选
            if (showType != null && !showType.isEmpty()) {
                queryWrapper.eq(ShowEvent::getShowType, showType);
            }

            // 按开票时间倒序排列
            queryWrapper.orderByDesc(ShowEvent::getSaleStartTime);

            Page<ShowEvent> showEventPage = showEventMapper.selectPage(pageParam, queryWrapper);

            // 转换为VO
            Page<ShowEventVO> voPage = new Page<>(showEventPage.getCurrent(), showEventPage.getSize(), showEventPage.getTotal());
            List<ShowEventVO> voList = showEventPage.getRecords().stream().map(showEvent -> {
                ShowEventVO vo = new ShowEventVO();
                BeanUtils.copyProperties(showEvent, vo);

                // 查询最低票价和最高票价
                LambdaQueryWrapper<Seat> seatQuery = new LambdaQueryWrapper<>();
                seatQuery.eq(Seat::getShowEventId, showEvent.getId());
                List<Seat> seats = seatMapper.selectList(seatQuery);
                if (!seats.isEmpty()) {
                    BigDecimal minPrice = seats.stream().map(Seat::getPrice).min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
                    BigDecimal maxPrice = seats.stream().map(Seat::getPrice).max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
                    vo.setMinPrice(minPrice);
                    vo.setMaxPrice(maxPrice);
                }

                return vo;
            }).collect(Collectors.toList());

            voPage.setRecords(voList);

            return Result.success(voPage);

        } catch (Exception e) {
            log.error("查询演出活动列表失败", e);
            return Result.error("查询演出活动列表失败: " + e.getMessage());
        }
    }

    @Override
    public Result<ShowEventVO> getShowEventDetail(Long showEventId) {
        try {
            // 尝试从Redis获取缓存
            String cacheKey = TicketRedisKey.SHOW_DETAIL_PREFIX + showEventId;
            String cachedData = redisService.stringGetString(cacheKey);

            if (cachedData != null) {
                ShowEventVO vo = JSON.parseObject(cachedData, ShowEventVO.class);
                log.debug("从Redis获取演出详情: showEventId={}", showEventId);
                return Result.success(vo);
            }

            // 从数据库查询
            ShowEvent showEvent = showEventMapper.selectById(showEventId);
            if (showEvent == null) {
                return Result.error("演出活动不存在");
            }

            ShowEventVO vo = new ShowEventVO();
            BeanUtils.copyProperties(showEvent, vo);

            // 查询最低票价和最高票价
            LambdaQueryWrapper<Seat> seatQuery = new LambdaQueryWrapper<>();
            seatQuery.eq(Seat::getShowEventId, showEventId);
            List<Seat> seats = seatMapper.selectList(seatQuery);
            if (!seats.isEmpty()) {
                BigDecimal minPrice = seats.stream().map(Seat::getPrice).min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
                BigDecimal maxPrice = seats.stream().map(Seat::getPrice).max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
                vo.setMinPrice(minPrice);
                vo.setMaxPrice(maxPrice);
            }

            // 写入Redis缓存
            redisService.stringSetString(cacheKey, JSON.toJSONString(vo), TicketRedisKey.SHOW_DETAIL_EXPIRE * 1000);
            log.debug("从数据库获取演出详情并缓存: showEventId={}", showEventId);

            return Result.success(vo);

        } catch (Exception e) {
            log.error("查询演出活动详情失败: showEventId={}", showEventId, e);
            return Result.error("查询演出活动详情失败: " + e.getMessage());
        }
    }

    @Override
    public Result<Page<SeatVO>> getSeatList(QuerySeatDTO dto) {
        try {
            Page<Seat> pageParam = new Page<>(dto.getPage(), dto.getSize());
            LambdaQueryWrapper<Seat> queryWrapper = new LambdaQueryWrapper<>();

            // 必须指定演出活动ID
            queryWrapper.eq(Seat::getShowEventId, dto.getShowEventId());

            // 座位区域筛选
            if (dto.getSeatZone() != null && !dto.getSeatZone().isEmpty()) {
                queryWrapper.eq(Seat::getSeatZone, dto.getSeatZone());
            }

            // 座位状态筛选
            if (dto.getStatus() != null && !dto.getStatus().isEmpty()) {
                queryWrapper.eq(Seat::getStatus, dto.getStatus());
            }

            // 按区域、排号、座位号排序
            queryWrapper.orderByAsc(Seat::getSeatZone, Seat::getSeatRow, Seat::getSeatNumber);

            Page<Seat> seatPage = seatMapper.selectPage(pageParam, queryWrapper);

            // 转换为VO
            Page<SeatVO> voPage = new Page<>(seatPage.getCurrent(), seatPage.getSize(), seatPage.getTotal());
            List<SeatVO> voList = seatPage.getRecords().stream().map(seat -> {
                SeatVO vo = new SeatVO();
                BeanUtils.copyProperties(seat, vo);
                return vo;
            }).collect(Collectors.toList());

            voPage.setRecords(voList);

            return Result.success(voPage);

        } catch (Exception e) {
            log.error("查询座位列表失败", e);
            return Result.error("查询座位列表失败: " + e.getMessage());
        }
    }

    @Override
    public Result<Integer> getAvailableSeatCount(Long showEventId, String seatZone) {
        try {
            LambdaQueryWrapper<Seat> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Seat::getShowEventId, showEventId)
                    .eq(Seat::getStatus, SeatStatus.AVAILABLE);

            if (seatZone != null && !seatZone.isEmpty()) {
                queryWrapper.eq(Seat::getSeatZone, seatZone);
            }

            Long count = seatMapper.selectCount(queryWrapper);

            return Result.success(count.intValue());

        } catch (Exception e) {
            log.error("查询可售座位数量失败: showEventId={}, seatZone={}", showEventId, seatZone, e);
            return Result.error("查询可售座位数量失败: " + e.getMessage());
        }
    }
}
