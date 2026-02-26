package com.example.yoyo_data.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.yoyo_data.common.Result;
import com.example.yoyo_data.common.dto.QuerySeatDTO;
import com.example.yoyo_data.common.vo.*;

import java.util.List;
import java.util.Map;

/**
 * 演出活动服务接口
 */
public interface ShowEventService {

    /**
     * 查询演出活动列表（分页，支持多维度筛选）
     */
    Result<Page<ShowEventVO>> getShowEventList(Integer page, Integer size, String status, String showType,
                                               String city, String category,
                                               String startDate, String endDate, String sortBy);

    /**
     * 兼容旧接口（原签名）
     */
    default Result<Page<ShowEventVO>> getShowEventList(Integer page, Integer size, String status, String showType) {
        return getShowEventList(page, size, status, showType, null, null, null, null, null);
    }

    /**
     * 查询演出活动详情
     */
    Result<ShowEventVO> getShowEventDetail(Long showEventId);

    /**
     * 查询座位列表（支持分页和筛选）
     */
    Result<Page<SeatVO>> getSeatList(QuerySeatDTO dto);

    /**
     * 查询指定区域的可售座位数量
     */
    Result<Integer> getAvailableSeatCount(Long showEventId, String seatZone);

    /**
     * 关键词搜索演出
     */
    Result<Page<ShowEventVO>> searchShows(String keyword, String city, String category,
                                          Integer page, Integer size);

    /**
     * 获取城市列表（含演出数量，Redis缓存1h）
     */
    Result<List<CityVO>> getCities();

    /**
     * 获取演出分类列表
     */
    Result<List<CategoryVO>> getCategories();

    /**
     * 首页数据聚合
     */
    Result<Map<String, Object>> getHomepageData(String city);

    /**
     * 获取演出的座位区域列表（GROUP BY seat_zone，Redis缓存5min）
     */
    Result<List<ZoneVO>> getZonesBySeat(Long showEventId);
}

