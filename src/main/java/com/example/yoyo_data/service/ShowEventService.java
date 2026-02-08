package com.example.yoyo_data.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.yoyo_data.common.Result;
import com.example.yoyo_data.common.dto.QuerySeatDTO;
import com.example.yoyo_data.common.vo.ShowEventVO;
import com.example.yoyo_data.common.vo.SeatVO;

/**
 * 演出活动服务接口
 */
public interface ShowEventService {

    /**
     * 查询演出活动列表（分页）
     * @param page 页码
     * @param size 每页大小
     * @param status 状态筛选（可选）
     * @param showType 演出类型筛选（可选）
     * @return 演出活动列表
     */
    Result<Page<ShowEventVO>> getShowEventList(Integer page, Integer size, String status, String showType);

    /**
     * 查询演出活动详情
     * @param showEventId 演出活动ID
     * @return 演出活动详情
     */
    Result<ShowEventVO> getShowEventDetail(Long showEventId);

    /**
     * 查询座位列表（支持分页和筛选）
     * @param dto 查询条件
     * @return 座位列表
     */
    Result<Page<SeatVO>> getSeatList(QuerySeatDTO dto);

    /**
     * 查询指定区域的可售座位数量
     * @param showEventId 演出活动ID
     * @param seatZone 座位区域
     * @return 可售座位数量
     */
    Result<Integer> getAvailableSeatCount(Long showEventId, String seatZone);
}
