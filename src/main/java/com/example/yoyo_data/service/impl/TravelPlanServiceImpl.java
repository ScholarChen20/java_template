package com.example.yoyo_data.service.impl;

import com.alibaba.fastjson.JSON;
import com.example.yoyo_data.cache.RedisService;
import com.example.yoyo_data.common.Result;
import com.example.yoyo_data.common.document.TravelPlan;
import com.example.yoyo_data.common.dto.PageResponseDTO;
import com.example.yoyo_data.common.dto.TravelPlanDTO;
import com.example.yoyo_data.repository.TravelPlanRepository;
import com.example.yoyo_data.service.TravelPlanService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 旅行计划服务实现类
 */
@Slf4j
@Service
public class TravelPlanServiceImpl implements TravelPlanService {

    @Autowired
    private TravelPlanRepository travelPlanRepository;

    @Autowired
    private RedisService redisService;

    private static final String TRAVEL_PLAN_CACHE_PREFIX = "travel_plan:";
    private static final String TRAVEL_PLAN_LIST_CACHE_PREFIX = "travel_plan:list:";
    private static final long CACHE_EXPIRE_TIME = 3600000L; // 1小时（毫秒）

    @Override
    public Result<PageResponseDTO<TravelPlanDTO>> getTravelPlanList(Long userId, Integer page, Integer size) {
        try {
            // 尝试从Redis获取缓存
            String cacheKey = TRAVEL_PLAN_LIST_CACHE_PREFIX + userId + ":" + page + ":" + size;
            String cachedData = redisService.stringGetString(cacheKey);
            if (cachedData != null) {
                log.info("从Redis缓存获取旅行计划列表: userId={}", userId);
                PageResponseDTO<TravelPlanDTO> result = JSON.parseObject(cachedData, PageResponseDTO.class);
                return Result.success(result);
            }

            // 从MongoDB获取数据
            List<TravelPlan> planList = travelPlanRepository.findByUserId(userId);

            // 按创建时间倒序排列
            planList.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));

            // 分页
            int start = (page - 1) * size;
            int end = Math.min(start + size, planList.size());
            List<TravelPlan> pagedList = planList.subList(start, end);

            List<TravelPlanDTO> items = pagedList.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

            PageResponseDTO<TravelPlanDTO> result = PageResponseDTO.<TravelPlanDTO>builder()
                    .items(items)
                    .total(planList.size())
                    .page(page)
                    .size(size)
                    .totalPages((int) Math.ceil((double) planList.size() / size))
                    .build();

            // 缓存到Redis
            redisService.stringSetString(cacheKey, JSON.toJSONString(result), CACHE_EXPIRE_TIME);

            log.info("从MongoDB获取旅行计划列表成功: userId={}, page={}, size={}", userId, page, size);
            return Result.success(result);

        } catch (Exception e) {
            log.error("获取旅行计划列表失败", e);
            return Result.error("获取旅行计划列表失败: " + e.getMessage());
        }
    }

    @Override
    public Result<TravelPlanDTO> getTravelPlanDetail(Long planId) {
        try {
            // 尝试从Redis获取缓存
            String cacheKey = TRAVEL_PLAN_CACHE_PREFIX + planId;
            String cachedData = redisService.stringGetString(cacheKey);
            if (cachedData != null) {
                log.info("从Redis缓存获取旅行计划详情: planId={}", planId);
                TravelPlanDTO result = JSON.parseObject(cachedData, TravelPlanDTO.class);
                return Result.success(result);
            }

            // 从MongoDB获取数据
            Optional<TravelPlan> planOpt = travelPlanRepository.findById(String.valueOf(planId));
            if (!planOpt.isPresent()) {
                return Result.error("旅行计划不存在");
            }

            TravelPlan plan = planOpt.get();
            TravelPlanDTO result = convertToDTO(plan);

            // 缓存到Redis（热点数据）
            redisService.stringSetString(cacheKey, JSON.toJSONString(result), CACHE_EXPIRE_TIME);

            log.info("从MongoDB获取旅行计划详情成功: planId={}", planId);
            return Result.success(result);

        } catch (Exception e) {
            log.error("获取旅行计划详情失败", e);
            return Result.error("获取旅行计划详情失败: " + e.getMessage());
        }
    }

    @Override
    public Result<TravelPlanDTO> createTravelPlan(Long userId, String title, String description, String destination, String startDate, String endDate) {
        try {
            // 创建旅行计划
            TravelPlan plan = TravelPlan.builder()
                    .userId(userId)
                    .title(title)
                    .description(description)
                    .destination(destination)
                    .startDate(startDate)
                    .endDate(endDate)
                    .days(calculateDays(startDate, endDate))
                    .status("active")
                    .createdAt(new Date())
                    .updatedAt(new Date())
                    .build();

            plan = travelPlanRepository.save(plan);

            // 清除列表缓存
            clearTravelPlanListCache(userId);

            TravelPlanDTO result = convertToDTO(plan);

            log.info("创建旅行计划成功: userId={}, title={}, planId={}", userId, title, plan.getId());
            return Result.success(result);

        } catch (Exception e) {
            log.error("创建旅行计划失败", e);
            return Result.error("创建旅行计划失败: " + e.getMessage());
        }
    }

    @Override
    public Result<TravelPlanDTO> updateTravelPlan(Long planId, Long userId, String title, String description, String destination, String startDate, String endDate) {
        try {
            // 获取现有计划
            Optional<TravelPlan> planOpt = travelPlanRepository.findById(String.valueOf(planId));
            if (!planOpt.isPresent()) {
                return Result.error("旅行计划不存在");
            }

            TravelPlan plan = planOpt.get();

            // 检查权限
            if (!plan.getUserId().equals(userId)) {
                return Result.error("无权修改此旅行计划");
            }

            // 更新计划
            plan.setTitle(title);
            plan.setDescription(description);
            plan.setDestination(destination);
            plan.setStartDate(startDate);
            plan.setEndDate(endDate);
            plan.setDays(calculateDays(startDate, endDate));
            plan.setUpdatedAt(new Date());

            plan = travelPlanRepository.save(plan);

            // 清除缓存
            clearTravelPlanCache(planId);
            clearTravelPlanListCache(userId);

            TravelPlanDTO result = convertToDTO(plan);

            log.info("更新旅行计划成功: planId={}, userId={}", planId, userId);
            return Result.success(result);

        } catch (Exception e) {
            log.error("更新旅行计划失败", e);
            return Result.error("更新旅行计划失败: " + e.getMessage());
        }
    }

    @Override
    public Result<String> deleteTravelPlan(Long planId) {
        try {
            Optional<TravelPlan> planOpt = travelPlanRepository.findById(String.valueOf(planId));
            if (!planOpt.isPresent()) {
                return Result.error("旅行计划不存在");
            }

            TravelPlan plan = planOpt.get();
            Long userId = plan.getUserId();

            travelPlanRepository.deleteById(String.valueOf(planId));

            // 清除缓存
            clearTravelPlanCache(planId);
            clearTravelPlanListCache(userId);

            log.info("删除旅行计划成功: planId={}", planId);
            return Result.success("旅行计划删除成功");

        } catch (Exception e) {
            log.error("删除旅行计划失败", e);
            return Result.error("删除旅行计划失败: " + e.getMessage());
        }
    }

    /**
     * 将TravelPlan转换为DTO
     */
    private TravelPlanDTO convertToDTO(TravelPlan plan) {
        List<TravelPlanDTO.DailyItineraryDTO> dailyItineraryDTOs = null;
        if (plan.getDailyItinerary() != null) {
            dailyItineraryDTOs = plan.getDailyItinerary().stream()
                    .map(this::convertDailyItineraryToDTO)
                    .collect(Collectors.toList());
        }

        return TravelPlanDTO.builder()
                .id(plan.getId())
                .userId(plan.getUserId())
                .title(plan.getTitle())
                .description(plan.getDescription())
                .destination(plan.getDestination())
                .startDate(plan.getStartDate())
                .endDate(plan.getEndDate())
                .days(plan.getDays())
                .dailyItinerary(dailyItineraryDTOs)
                .budget(plan.getBudget())
                .status(plan.getStatus())
                .metadata(plan.getMetadata())
                .createdAt(plan.getCreatedAt())
                .updatedAt(plan.getUpdatedAt())
                .build();
    }

    /**
     * 将DailyItinerary转换为DTO
     */
    private TravelPlanDTO.DailyItineraryDTO convertDailyItineraryToDTO(TravelPlan.DailyItinerary itinerary) {
        return TravelPlanDTO.DailyItineraryDTO.builder()
                .day(itinerary.getDay())
                .date(itinerary.getDate())
                .activities(itinerary.getActivities())
                .metadata(itinerary.getMetadata())
                .build();
    }

    /**
     * 清除旅行计划缓存
     */
    private void clearTravelPlanCache(Long planId) {
        String cacheKey = TRAVEL_PLAN_CACHE_PREFIX + planId;
        redisService.delete(cacheKey);
    }

    /**
     * 清除旅行计划列表缓存
     */
    private void clearTravelPlanListCache(Long userId) {
        Set<String> keys = redisService.keys(TRAVEL_PLAN_LIST_CACHE_PREFIX + userId + ":*");
        if (keys != null && !keys.isEmpty()) {
            redisService.delete(keys);
        }
    }

    /**
     * 计算两个日期之间的天数
     */
    private Integer calculateDays(String startDate, String endDate) {
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
            Date start = sdf.parse(startDate);
            Date end = sdf.parse(endDate);
            long diffTime = end.getTime() - start.getTime();
            return (int) (diffTime / (1000 * 60 * 60 * 24)) + 1;
        } catch (Exception e) {
            return 1;
        }
    }
}
