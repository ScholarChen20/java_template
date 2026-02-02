package com.example.yoyo_data.service;

import com.example.yoyo_data.common.Result;

/**
 * 旅行计划服务接口
 */
public interface TravelPlanService {
    /**
     * 获取旅行计划列表
     *
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页大小
     * @return 旅行计划列表
     */
    Result<?> getTravelPlanList(Long userId, Integer page, Integer size);

    /**
     * 获取旅行计划详情
     *
     * @param planId 旅行计划ID
     * @return 旅行计划详情
     */
    Result<?> getTravelPlanDetail(Long planId);

    /**
     * 创建旅行计划
     *
     * @param userId 用户ID
     * @param title 标题
     * @param description 描述
     * @param destination 目的地
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 创建结果
     */
    Result<?> createTravelPlan(Long userId, String title, String description, String destination, String startDate, String endDate);

    /**
     * 更新旅行计划
     *
     * @param planId 旅行计划ID
     * @param userId 用户ID
     * @param title 标题
     * @param description 描述
     * @param destination 目的地
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 更新结果
     */
    Result<?> updateTravelPlan(Long planId, Long userId, String title, String description, String destination, String startDate, String endDate);

    /**
     * 删除旅行计划
     *
     * @param planId 旅行计划ID
     * @return 删除结果
     */
    Result<?> deleteTravelPlan(Long planId);
}