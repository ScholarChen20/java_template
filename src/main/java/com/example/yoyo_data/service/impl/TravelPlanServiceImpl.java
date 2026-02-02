package com.example.yoyo_data.service.impl;

import com.example.yoyo_data.common.Result;
import com.example.yoyo_data.service.TravelPlanService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 旅行计划服务实现类
 */
@Slf4j
@Service
public class TravelPlanServiceImpl implements TravelPlanService {

    @Override
    public Result<?> getTravelPlanList(Long userId, Integer page, Integer size) {
        try {
            // 模拟数据
            List<Map<String, Object>> planList = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                Map<String, Object> plan = new HashMap<>();
                plan.put("id", (long) (i + 1));
                plan.put("userId", userId);
                plan.put("title", "旅行计划" + (i + 1));
                plan.put("description", "这是一个详细的旅行计划" + (i + 1));
                plan.put("destination", "目的地" + (i + 1));
                plan.put("startDate", "2024-01-0" + (i + 1));
                plan.put("endDate", "2024-01-0" + (i + 5));
                plan.put("days", 5);
                plan.put("createdAt", System.currentTimeMillis() - i * 86400000);
                planList.add(plan);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("planList", planList);
            result.put("total", 100);
            result.put("page", page);
            result.put("size", size);

            log.info("获取旅行计划列表成功: userId={}, page={}, size={}", userId, page, size);
            return Result.success(result);

        } catch (Exception e) {
            log.error("获取旅行计划列表失败", e);
            return Result.error("获取旅行计划列表失败: " + e.getMessage());
        }
    }

    @Override
    public Result<?> getTravelPlanDetail(Long planId) {
        try {
            // 模拟数据
            Map<String, Object> planDetail = new HashMap<>();
            planDetail.put("id", planId);
            planDetail.put("userId", 1L);
            planDetail.put("title", "旅行计划" + planId);
            planDetail.put("description", "这是一个详细的旅行计划" + planId);
            planDetail.put("destination", "目的地" + planId);
            planDetail.put("startDate", "2024-01-01");
            planDetail.put("endDate", "2024-01-05");
            planDetail.put("days", 5);

            // 模拟每日行程
            List<Map<String, Object>> dailyItinerary = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                Map<String, Object> dayPlan = new HashMap<>();
                dayPlan.put("day", i + 1);
                dayPlan.put("date", "2024-01-0" + (i + 1));
                dayPlan.put("activities", Arrays.asList(
                        "上午: 参观景点A",
                        "下午: 参观景点B",
                        "晚上: 自由活动"
                ));
                dailyItinerary.add(dayPlan);
            }
            planDetail.put("dailyItinerary", dailyItinerary);

            planDetail.put("createdAt", System.currentTimeMillis() - 86400000);
            planDetail.put("updatedAt", System.currentTimeMillis() - 43200000);

            log.info("获取旅行计划详情成功: planId={}", planId);
            return Result.success(planDetail);

        } catch (Exception e) {
            log.error("获取旅行计划详情失败", e);
            return Result.error("获取旅行计划详情失败: " + e.getMessage());
        }
    }

    @Override
    public Result<?> createTravelPlan(Long userId, String title, String description, String destination, String startDate, String endDate) {
        try {
            // 模拟创建旅行计划
            Map<String, Object> plan = new HashMap<>();
            plan.put("id", System.currentTimeMillis());
            plan.put("userId", userId);
            plan.put("title", title);
            plan.put("description", description);
            plan.put("destination", destination);
            plan.put("startDate", startDate);
            plan.put("endDate", endDate);
            plan.put("days", 5); // 模拟天数
            plan.put("createdAt", System.currentTimeMillis());
            plan.put("updatedAt", System.currentTimeMillis());

            log.info("创建旅行计划成功: userId={}, title={}", userId, title);
            return Result.success(plan);

        } catch (Exception e) {
            log.error("创建旅行计划失败", e);
            return Result.error("创建旅行计划失败: " + e.getMessage());
        }
    }

    @Override
    public Result<?> updateTravelPlan(Long planId, Long userId, String title, String description, String destination, String startDate, String endDate) {
        try {
            // 模拟更新旅行计划
            Map<String, Object> plan = new HashMap<>();
            plan.put("id", planId);
            plan.put("userId", userId);
            plan.put("title", title);
            plan.put("description", description);
            plan.put("destination", destination);
            plan.put("startDate", startDate);
            plan.put("endDate", endDate);
            plan.put("days", 5); // 模拟天数
            plan.put("updatedAt", System.currentTimeMillis());

            log.info("更新旅行计划成功: planId={}, userId={}", planId, userId);
            return Result.success(plan);
        }
        catch (Exception e) {
            log.error("更新旅行计划失败", e);
            return Result.error("更新旅行计划失败: " + e.getMessage());
        }
    }

    @Override
    public Result<?> deleteTravelPlan(Long planId) {
        try {
            // 模拟删除旅行计划
            log.info("删除旅行计划成功: planId={}", planId);
            return Result.success();
        }
        catch (Exception e) {
            log.error("删除旅行计划失败", e);
            return Result.error("删除旅行计划失败: " + e.getMessage());
        }
    }
}
