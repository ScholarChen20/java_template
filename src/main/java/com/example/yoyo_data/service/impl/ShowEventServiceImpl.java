package com.example.yoyo_data.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.yoyo_data.common.Result;
import com.example.yoyo_data.common.constant.SeatStatus;
import com.example.yoyo_data.common.constant.ShowCategory;
import com.example.yoyo_data.common.constant.TicketRedisKey;
import com.example.yoyo_data.common.dto.QuerySeatDTO;
import com.example.yoyo_data.common.dto.ZoneDTO;
import com.example.yoyo_data.common.entity.Seat;
import com.example.yoyo_data.common.entity.ShowEvent;
import com.example.yoyo_data.common.vo.*;
import com.example.yoyo_data.infrastructure.cache.RedisService;
import com.example.yoyo_data.infrastructure.repository.SeatMapper;
import com.example.yoyo_data.infrastructure.repository.ShowEventMapper;
import com.example.yoyo_data.service.ShowEventService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 演出活动服务实现类
 */
@Slf4j
@Service
public class ShowEventServiceImpl implements ShowEventService {

    private static final String CITIES_CACHE_KEY = "ticket:show:cities";
    private static final long CITIES_CACHE_EXPIRE = 3600 * 1000L;
    private static final String ZONE_CACHE_PREFIX = "ticket:show:zones:";
    private static final long ZONE_CACHE_EXPIRE = 5 * 60 * 1000L;

    @Autowired
    private ShowEventMapper showEventMapper;

    @Autowired
    private SeatMapper seatMapper;

    @Autowired
    private RedisService redisService;

    @Override
    public Result<Page<ShowEventVO>> getShowEventList(Integer page, Integer size, String status, String showType,
                                                      String city, String category,
                                                      String startDate, String endDate, String sortBy) {
        try {
            Page<ShowEvent> pageParam = new Page<>(page, size);
            LambdaQueryWrapper<ShowEvent> queryWrapper = new LambdaQueryWrapper<>();

            if (StringUtils.hasText(status)) {
                queryWrapper.eq(ShowEvent::getStatus, status);
            }
            if (StringUtils.hasText(showType)) {
                queryWrapper.eq(ShowEvent::getShowType, showType);
            }
            if (StringUtils.hasText(city)) {
                queryWrapper.eq(ShowEvent::getCity, city);
            }
            if (StringUtils.hasText(category)) {
                queryWrapper.eq(ShowEvent::getCategory, category);
            }
            if (StringUtils.hasText(startDate)) {
                queryWrapper.ge(ShowEvent::getShowTime,
                        LocalDate.parse(startDate, DateTimeFormatter.ISO_LOCAL_DATE).atStartOfDay());
            }
            if (StringUtils.hasText(endDate)) {
                queryWrapper.le(ShowEvent::getShowTime,
                        LocalDate.parse(endDate, DateTimeFormatter.ISO_LOCAL_DATE).atTime(23, 59, 59));
            }

            if ("hot".equals(sortBy)) {
                queryWrapper.orderByDesc(ShowEvent::getHotScore);
            } else if ("time".equals(sortBy)) {
                queryWrapper.orderByAsc(ShowEvent::getShowTime);
            } else {
                queryWrapper.orderByDesc(ShowEvent::getSaleStartTime);
            }

            Page<ShowEvent> showEventPage = showEventMapper.selectPage(pageParam, queryWrapper);
            Page<ShowEventVO> voPage = new Page<>(showEventPage.getCurrent(), showEventPage.getSize(), showEventPage.getTotal());
            List<ShowEventVO> voList = showEventPage.getRecords().stream()
                    .map(this::toVO)
                    .collect(Collectors.toList());
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
            String cacheKey = TicketRedisKey.SHOW_DETAIL_PREFIX + showEventId;
            String cachedData = redisService.stringGetString(cacheKey);

            if (cachedData != null) {
                ShowEventVO vo = JSON.parseObject(cachedData, ShowEventVO.class);
                log.debug("从Redis获取演出详情: showEventId={}", showEventId);
                return Result.success(vo);
            }

            ShowEvent showEvent = showEventMapper.selectById(showEventId);
            if (showEvent == null) {
                return Result.error("演出活动不存在");
            }

            ShowEventVO vo = toVO(showEvent);
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
            queryWrapper.eq(Seat::getShowEventId, dto.getShowEventId());

            if (dto.getSeatZone() != null && !dto.getSeatZone().isEmpty()) {
                queryWrapper.eq(Seat::getSeatZone, dto.getSeatZone());
            }
            if (dto.getStatus() != null && !dto.getStatus().isEmpty()) {
                queryWrapper.eq(Seat::getStatus, dto.getStatus());
            }
            queryWrapper.orderByAsc(Seat::getSeatZone, Seat::getSeatRow, Seat::getSeatNumber);

            Page<Seat> seatPage = seatMapper.selectPage(pageParam, queryWrapper);
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

    @Override
    public Result<Page<ShowEventVO>> searchShows(String keyword, String city, String category,
                                                 Integer page, Integer size) {
        try {
            Page<ShowEvent> pageParam = new Page<>(page, size);
            LambdaQueryWrapper<ShowEvent> queryWrapper = new LambdaQueryWrapper<>();

            if (StringUtils.hasText(keyword)) {
                queryWrapper.like(ShowEvent::getShowName, keyword)
                        .or().like(ShowEvent::getVenueName, keyword);
            }
            if (StringUtils.hasText(city)) {
                queryWrapper.eq(ShowEvent::getCity, city);
            }
            if (StringUtils.hasText(category)) {
                queryWrapper.eq(ShowEvent::getCategory, category);
            }
            queryWrapper.orderByDesc(ShowEvent::getHotScore);

            Page<ShowEvent> showEventPage = showEventMapper.selectPage(pageParam, queryWrapper);
            Page<ShowEventVO> voPage = new Page<>(showEventPage.getCurrent(), showEventPage.getSize(), showEventPage.getTotal());
            voPage.setRecords(showEventPage.getRecords().stream().map(this::toVO).collect(Collectors.toList()));

            return Result.success(voPage);
        } catch (Exception e) {
            log.error("搜索演出失败: keyword={}", keyword, e);
            return Result.error("搜索演出失败: " + e.getMessage());
        }
    }

    @Override
    public Result<List<CityVO>> getCities() {
        try {
            String cacheKey = CITIES_CACHE_KEY;
            String cached = redisService.stringGetString(cacheKey);
            if (cached != null) {
                return Result.success(JSON.parseArray(cached, CityVO.class));
            }

            List<CityVO> cities = showEventMapper.selectCityStats();

            redisService.stringSetString(cacheKey, JSON.toJSONString(cities), CITIES_CACHE_EXPIRE);
            return Result.success(cities);
        } catch (Exception e) {
            log.error("获取城市列表失败", e);
            return Result.error("获取城市列表失败: " + e.getMessage());
        }
    }

    @Override
    public Result<List<CategoryVO>> getCategories() {
        List<CategoryVO> categories = Arrays.asList(
                CategoryVO.builder().code(ShowCategory.CONCERT).name("演唱会").showCount(0).build(),
                CategoryVO.builder().code(ShowCategory.DRAMA).name("话剧").showCount(0).build(),
                CategoryVO.builder().code(ShowCategory.MUSICAL).name("音乐剧").showCount(0).build(),
                CategoryVO.builder().code(ShowCategory.SPORTS).name("体育").showCount(0).build(),
                CategoryVO.builder().code(ShowCategory.COMEDY).name("脱口秀").showCount(0).build(),
                CategoryVO.builder().code(ShowCategory.CHILDREN).name("亲子").showCount(0).build()
        );
        return Result.success(categories);
    }

    @Override
    public Result<Map<String, Object>> getHomepageData(String city) {
        try {
            LambdaQueryWrapper<ShowEvent> hotWrapper = new LambdaQueryWrapper<>();
            hotWrapper.eq(ShowEvent::getStatus, "SELLING");
            if (StringUtils.hasText(city)) {
                hotWrapper.eq(ShowEvent::getCity, city);
            }
            hotWrapper.orderByDesc(ShowEvent::getHotScore).last("LIMIT 10");
            List<ShowEvent> hotShows = showEventMapper.selectList(hotWrapper);

            LambdaQueryWrapper<ShowEvent> upcomingWrapper = new LambdaQueryWrapper<>();
            upcomingWrapper.eq(ShowEvent::getStatus, "PENDING");
            if (StringUtils.hasText(city)) {
                upcomingWrapper.eq(ShowEvent::getCity, city);
            }
            upcomingWrapper.orderByAsc(ShowEvent::getSaleStartTime).last("LIMIT 10");
            List<ShowEvent> upcomingShows = showEventMapper.selectList(upcomingWrapper);

            Map<String, Object> result = new HashMap<>();
            result.put("hotShows", hotShows.stream().map(this::toVO).collect(Collectors.toList()));
            result.put("upcomingShows", upcomingShows.stream().map(this::toVO).collect(Collectors.toList()));
            result.put("recommendShows", Collections.emptyList());

            return Result.success(result);
        } catch (Exception e) {
            log.error("获取首页数据失败", e);
            return Result.error("获取首页数据失败: " + e.getMessage());
        }
    }

    @Override
    public Result<List<ZoneVO>> getZonesBySeat(Long showEventId) {
        try {
            String cacheKey = ZONE_CACHE_PREFIX + showEventId;
            String cached = redisService.stringGetString(cacheKey);
            if (cached != null) {
                return Result.success(JSON.parseArray(cached, ZoneVO.class));
            }

            List<ZoneDTO> stats = showEventMapper.selectZoneStats(showEventId);
            List<ZoneVO> zones = stats.stream().map(row -> {
                String zone = row.getSeatZone();
                int total = row.getTotalSeats();
                int available = row.getAvailableSeats();
                BigDecimal minPrice = row.getMinPrice();
                String soldRate = total > 0
                        ? new BigDecimal((total - available) * 100.0 / total)
                                .setScale(1, RoundingMode.HALF_UP) + "%"
                        : "0%";
                return ZoneVO.builder()
                        .zone(zone)
                        .zoneName(zone + "区")
                        .price(minPrice)
                        .totalSeats(total)
                        .availableSeats(available)
                        .soldRate(soldRate)
                        .build();
            }).collect(Collectors.toList());

            redisService.stringSetString(cacheKey, JSON.toJSONString(zones), ZONE_CACHE_EXPIRE);
            return Result.success(zones);
        } catch (Exception e) {
            log.error("获取座位区域列表失败: showEventId={}", showEventId, e);
            return Result.error("获取座位区域列表失败: " + e.getMessage());
        }
    }

    /**
     * ShowEvent -> ShowEventVO 转换（含价格计算）
     */
    private ShowEventVO toVO(ShowEvent showEvent) {
        ShowEventVO vo = new ShowEventVO();
        BeanUtils.copyProperties(showEvent, vo);

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
    }
}
