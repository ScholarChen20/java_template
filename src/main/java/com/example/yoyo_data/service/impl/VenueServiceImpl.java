package com.example.yoyo_data.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.yoyo_data.common.Result;
import com.example.yoyo_data.common.entity.ShowEvent;
import com.example.yoyo_data.common.entity.Venue;
import com.example.yoyo_data.common.vo.ShowEventVO;
import com.example.yoyo_data.common.vo.SimpleShowVO;
import com.example.yoyo_data.common.vo.VenueVO;
import com.example.yoyo_data.infrastructure.repository.ShowEventMapper;
import com.example.yoyo_data.infrastructure.repository.VenueMapper;
import com.example.yoyo_data.service.VenueService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 场馆服务实现类
 */
@Slf4j
@Service
public class VenueServiceImpl implements VenueService {

    private static final String VENUE_CACHE_PREFIX = "ticket:venue:";
    private static final String VENUE_LIST_CACHE_PREFIX = "ticket:venue:list:";
    private static final long VENUE_CACHE_EXPIRE = 24 * 3600 * 1000L;

    @Autowired
    private VenueMapper venueMapper;

    @Autowired
    private ShowEventMapper showEventMapper;

    @Autowired
    private com.example.yoyo_data.infrastructure.cache.RedisService redisService;

    @Override
    public Result<Page<VenueVO>> getVenueList(String city, Integer page, Integer size) {
        try {
            String cacheKey = VENUE_LIST_CACHE_PREFIX + (city == null ? "all" : city) + ":" + page + ":" + size;
            String cached = redisService.stringGetString(cacheKey);
            if (cached != null) {
                return Result.success(JSON.parseObject(cached, new com.alibaba.fastjson.TypeReference<Page<VenueVO>>() {}));
            }

            LambdaQueryWrapper<Venue> wrapper = new LambdaQueryWrapper<>();
            if (StringUtils.hasText(city)) {
                wrapper.eq(Venue::getCity, city);
            }
            wrapper.orderByAsc(Venue::getName);
            Page<Venue> venuePage = venueMapper.selectPage(new Page<>(page, size), wrapper);

            Page<VenueVO> voPage = new Page<>(venuePage.getCurrent(), venuePage.getSize(), venuePage.getTotal());
            voPage.setRecords(venuePage.getRecords().stream().map(this::toVO).collect(Collectors.toList()));

            redisService.stringSetString(cacheKey, JSON.toJSONString(voPage), VENUE_CACHE_EXPIRE);
            return Result.success(voPage);
        } catch (Exception e) {
            log.error("获取场馆列表失败", e);
            return Result.error("获取场馆列表失败: " + e.getMessage());
        }
    }

    @Override
    public Result<VenueVO> getVenueDetail(Long venueId) {
        try {
            String cacheKey = VENUE_CACHE_PREFIX + venueId;
            String cached = redisService.stringGetString(cacheKey);
            if (cached != null) {
                return Result.success(JSON.parseObject(cached, VenueVO.class));
            }

            Venue venue = venueMapper.selectById(venueId);
            if (venue == null) return Result.error(404, "场馆不存在");

            VenueVO vo = toVO(venue);

            // 附加即将上演演出
            LambdaQueryWrapper<ShowEvent> showWrapper = new LambdaQueryWrapper<>();
            showWrapper.eq(ShowEvent::getVenueId, venueId)
                    .in(ShowEvent::getStatus, "PENDING", "SELLING")
                    .orderByAsc(ShowEvent::getShowTime)
                    .last("LIMIT 5");
            List<ShowEvent> upcomingShows = showEventMapper.selectList(showWrapper);
            vo.setUpcomingShowCount(upcomingShows.size());
            vo.setUpcomingShows(upcomingShows.stream().map(s -> SimpleShowVO.builder()
                    .id(s.getId())
                    .showName(s.getShowName())
                    .showTime(s.getShowTime() != null
                            ? s.getShowTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : null)
                    .status(s.getStatus())
                    .build()).collect(Collectors.toList()));

            redisService.stringSetString(cacheKey, JSON.toJSONString(vo), VENUE_CACHE_EXPIRE);
            return Result.success(vo);
        } catch (Exception e) {
            log.error("获取场馆详情失败: venueId={}", venueId, e);
            return Result.error("获取场馆详情失败: " + e.getMessage());
        }
    }

    @Override
    public Result<Page<ShowEventVO>> getShowsByVenue(Long venueId, String status, Integer page, Integer size) {
        try {
            LambdaQueryWrapper<ShowEvent> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ShowEvent::getVenueId, venueId);
            if (StringUtils.hasText(status)) {
                wrapper.eq(ShowEvent::getStatus, status);
            }
            wrapper.orderByDesc(ShowEvent::getShowTime);

            Page<ShowEvent> showPage = showEventMapper.selectPage(new Page<>(page, size), wrapper);
            Page<ShowEventVO> voPage = new Page<>(showPage.getCurrent(), showPage.getSize(), showPage.getTotal());
            voPage.setRecords(showPage.getRecords().stream().map(s -> {
                ShowEventVO vo = new ShowEventVO();
                BeanUtils.copyProperties(s, vo);
                return vo;
            }).collect(Collectors.toList()));

            return Result.success(voPage);
        } catch (Exception e) {
            log.error("获取场馆演出列表失败: venueId={}", venueId, e);
            return Result.error("获取场馆演出列表失败: " + e.getMessage());
        }
    }

    private VenueVO toVO(Venue venue) {
        return VenueVO.builder()
                .id(venue.getId())
                .name(venue.getName())
                .city(venue.getCity())
                .district(venue.getDistrict())
                .address(venue.getAddress())
                .capacity(venue.getCapacity())
                .logoUrl(venue.getLogoUrl())
                .panoramaUrl(venue.getPanoramaUrl())
                .seatMapUrl(venue.getSeatMapUrl())
                .transportInfo(venue.getTransportInfo())
                .amenities(venue.getAmenities())
                .build();
    }
}
