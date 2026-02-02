package com.example.yoyo_data.service;

import com.example.yoyo_data.common.Result;
import java.util.Map;

/**
 * 旅行计划服务接口
 * 处理旅行计划相关业务逻辑
 */
public interface TravelPlanService {

    /**
     * 创建旅行计划
     *
     * @param token  当前用户的token
     * @param params 创建旅行计划参数，包含title、description、start_date、end_date、destinations、budget等
     * @return 创建结果
     */
    Result<Map<String, Object>> createTravelPlan(String token, Map<String, Object> params);

    /**
     * 获取旅行计划列表
     *
     * @param token   当前用户的token
     * @param page    页码，默认 1
     * @param size    每页数量，默认 10
     * @param status  状态，可选 draft, active, completed, cancelled
     * @param sort    排序方式，默认 created_at
     * @return 旅行计划列表
     */
    Result<Map<String, Object>> getTravelPlanList(String token, Integer page, Integer size, String status, String sort);

    /**
     * 获取旅行计划详情
     *
     * @param id    旅行计划ID
     * @param token 当前用户的token
     * @return 旅行计划详情
     */
    Result<Map<String, Object>> getTravelPlanDetail(Long id, String token);

    /**
     * 更新旅行计划
     *
     * @param id     旅行计划ID
     * @param token  当前用户的token
     * @param params 更新参数，包含title、description、start_date、end_date、destinations、budget等
     * @return 更新结果
     */
    Result<Map<String, Object>> updateTravelPlan(Long id, String token, Map<String, Object> params);

    /**
     * 删除旅行计划
     *
     * @param id    旅行计划ID
     * @param token 当前用户的token
     * @return 删除结果
     */
    Result<Map<String, Object>> deleteTravelPlan(Long id, String token);

    /**
     * 分享旅行计划
     *
     * @param id    旅行计划ID
     * @param token 当前用户的token
     * @return 分享结果
     */
    Result<Map<String, Object>> shareTravelPlan(Long id, String token);

    /**
     * 获取公开的旅行计划
     *
     * @param shareId 分享ID
     * @return 旅行计划详情
     */
    Result<Map<String, Object>> getSharedTravelPlan(String shareId);
}
