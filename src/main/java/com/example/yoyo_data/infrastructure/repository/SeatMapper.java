package com.example.yoyo_data.infrastructure.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.yoyo_data.common.entity.Seat;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 座位信息表 Mapper 接口
 */
@Mapper
public interface SeatMapper extends BaseMapper<Seat> {

    /**
     * CAS 锁定座位（使用乐观锁）
     * @param seatId 座位ID
     * @param userId 用户ID
     * @param orderId 订单ID
     * @param lockExpireTime 锁定过期时间
     * @param version 当前版本号
     * @return 更新行数（1表示成功，0表示失败）
     */
    @Update("UPDATE tb_seat SET " +
            "status = 'LOCKED', " +
            "lock_time = NOW(), " +
            "lock_expire_time = #{lockExpireTime}, " +
            "order_id = #{orderId}, " +
            "user_id = #{userId}, " +
            "version = version + 1, " +
            "updated_at = NOW() " +
            "WHERE id = #{seatId} " +
            "AND status = 'AVAILABLE' " +
            "AND version = #{version}")
    int lockSeatWithCAS(@Param("seatId") Long seatId,
                        @Param("userId") Long userId,
                        @Param("orderId") Long orderId,
                        @Param("lockExpireTime") LocalDateTime lockExpireTime,
                        @Param("version") Integer version);

    /**
     * 批量 CAS 锁定座位（使用乐观锁）
     * 注意：MyBatis 不支持批量 CAS，需要在 Service 层循环调用 lockSeatWithCAS
     */

    /**
     * 确认座位已售出（支付成功后调用）
     * @param seatId 座位ID
     * @return 更新行数
     */
    @Update("UPDATE tb_seat SET " +
            "status = 'SOLD', " +
            "version = version + 1, " +
            "updated_at = NOW() " +
            "WHERE id = #{seatId} " +
            "AND status = 'LOCKED'")
    int confirmSeatSold(@Param("seatId") Long seatId);

    /**
     * 批量确认座位已售出
     * @param seatIds 座位ID列表
     * @return 更新行数
     */
    @Update("<script>" +
            "UPDATE tb_seat SET " +
            "status = 'SOLD', " +
            "version = version + 1, " +
            "updated_at = NOW() " +
            "WHERE id IN " +
            "<foreach collection='seatIds' item='seatId' open='(' separator=',' close=')'>" +
            "#{seatId}" +
            "</foreach> " +
            "AND status = 'LOCKED'" +
            "</script>")
    int batchConfirmSeatSold(@Param("seatIds") List<Long> seatIds);

    /**
     * 释放座位（订单超时或取消后调用）
     * @param seatId 座位ID
     * @return 更新行数
     */
    @Update("UPDATE tb_seat SET " +
            "status = 'AVAILABLE', " +
            "lock_time = NULL, " +
            "lock_expire_time = NULL, " +
            "order_id = NULL, " +
            "user_id = NULL, " +
            "version = version + 1, " +
            "updated_at = NOW() " +
            "WHERE id = #{seatId} " +
            "AND status = 'LOCKED'")
    int releaseSeat(@Param("seatId") Long seatId);

    /**
     * 批量释放座位
     * @param seatIds 座位ID列表
     * @return 更新行数
     */
    @Update("<script>" +
            "UPDATE tb_seat SET " +
            "status = 'AVAILABLE', " +
            "lock_time = NULL, " +
            "lock_expire_time = NULL, " +
            "order_id = NULL, " +
            "user_id = NULL, " +
            "version = version + 1, " +
            "updated_at = NOW() " +
            "WHERE id IN " +
            "<foreach collection='seatIds' item='seatId' open='(' separator=',' close=')'>" +
            "#{seatId}" +
            "</foreach> " +
            "AND status = 'LOCKED'" +
            "</script>")
    int batchReleaseSeat(@Param("seatIds") List<Long> seatIds);

    /**
     * 释放已过期的锁定座位（定时任务调用）
     * @param now 当前时间
     * @return 释放的座位数
     */
    @Update("UPDATE tb_seat SET " +
            "status = 'AVAILABLE', " +
            "lock_time = NULL, " +
            "lock_expire_time = NULL, " +
            "order_id = NULL, " +
            "user_id = NULL, " +
            "version = version + 1, " +
            "updated_at = NOW() " +
            "WHERE status = 'LOCKED' " +
            "AND lock_expire_time < #{now}")
    int releaseExpiredSeats(@Param("now") LocalDateTime now);
}
