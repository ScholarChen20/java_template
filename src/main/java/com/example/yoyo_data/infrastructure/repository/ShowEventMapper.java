package com.example.yoyo_data.infrastructure.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.yoyo_data.common.entity.ShowEvent;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

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
    @Update("UPDATE tb_show_event SET " +
            "available_seats = available_seats - #{seatCount}, " +
            "locked_seats = locked_seats + #{seatCount}, " +
            "version = version + 1, " +
            "updated_at = NOW() " +
            "WHERE id = #{showEventId} " +
            "AND available_seats >= #{seatCount} " +
            "AND version = #{version}")
    int lockSeats(@Param("showEventId") Long showEventId,
                  @Param("seatCount") Integer seatCount,
                  @Param("version") Integer version);

    /**
     * 减少锁定座位数，增加已售座位数（支付成功后调用）
     * @param showEventId 演出活动ID
     * @param seatCount 座位数量
     * @return 更新行数
     */
    @Update("UPDATE tb_show_event SET " +
            "locked_seats = locked_seats - #{seatCount}, " +
            "sold_seats = sold_seats + #{seatCount}, " +
            "version = version + 1, " +
            "updated_at = NOW() " +
            "WHERE id = #{showEventId} " +
            "AND locked_seats >= #{seatCount}")
    int confirmSeats(@Param("showEventId") Long showEventId,
                     @Param("seatCount") Integer seatCount);

    /**
     * 释放锁定座位（订单超时取消后调用）
     * @param showEventId 演出活动ID
     * @param seatCount 座位数量
     * @return 更新行数
     */
    @Update("UPDATE tb_show_event SET " +
            "available_seats = available_seats + #{seatCount}, " +
            "locked_seats = locked_seats - #{seatCount}, " +
            "version = version + 1, " +
            "updated_at = NOW() " +
            "WHERE id = #{showEventId} " +
            "AND locked_seats >= #{seatCount}")
    int releaseSeats(@Param("showEventId") Long showEventId,
                     @Param("seatCount") Integer seatCount);
}
