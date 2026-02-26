package com.example.yoyo_data.infrastructure.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.yoyo_data.common.dto.ZoneDTO;
import com.example.yoyo_data.common.entity.ShowEvent;
import com.example.yoyo_data.common.vo.CityVO;
import com.example.yoyo_data.common.vo.ZoneVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 演出活动表 Mapper 接口
 */
@Mapper
public interface ShowEventMapper extends BaseMapper<ShowEvent> {

    /**
     * 减少可售座位数，增加锁定座位数（使用乐观锁）
     * @param showEventId 演出活动ID
     * @param seatCount 座位数量
     * @param version 当前版本号
     * @return 更新行数（1表示成功，0表示失败）
     */
    int lockSeats(@Param("showEventId") Long showEventId,
                  @Param("seatCount") Integer seatCount,
                  @Param("version") Integer version);

    /**
     * 减少锁定座位数，增加已售座位数（支付成功后调用）
     * @param showEventId 演出活动ID
     * @param seatCount 座位数量
     * @return 更新行数
     */
    int confirmSeats(@Param("showEventId") Long showEventId,
                     @Param("seatCount") Integer seatCount);

    /**
     * 释放锁定座位（订单超时取消后调用）
     * @param showEventId 演出活动ID
     * @param seatCount 座位数量
     * @return 更新行数
     */
    int releaseSeats(@Param("showEventId") Long showEventId,
                     @Param("seatCount") Integer seatCount);

    /**
     * 查询城市列表（含演出数量）
     */
    List<CityVO> selectCityStats();

    /**
     * 按分区统计座位（用于ZoneVO）
     */
    List<ZoneDTO> selectZoneStats(@Param("showEventId") Long showEventId);
}
