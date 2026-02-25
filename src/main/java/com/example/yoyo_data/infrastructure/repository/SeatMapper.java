package com.example.yoyo_data.infrastructure.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.yoyo_data.common.dto.SeatCacheDTO;
import com.example.yoyo_data.common.entity.Seat;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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

    /**
     * 检查座位与活动是否关联
     * @param eventId
     * @param seatIds
     * @return
     */
    int checkSeatExist(@Param("eventId") long eventId, @Param("seatIds") @Size(min = 1, max = 5) List<Long> seatIds);

    /**
     * 查询活动下的座位信息
     * @param showEventId 演出活动ID
     * @return 座位缓存DTO列表
     */
    List<SeatCacheDTO> selectByEventId(@Param("showEventId") Long showEventId);

    /**
     * 查询活动下的指定座位信息
     * @param showEventId 演出活动ID
     * @param seatIds 座位ID列表
     * @return 座位缓存DTO列表
     */
    List<SeatCacheDTO> selectByEventIdAndSeatIds(@Param("showEventId") Long showEventId,
                                                   @Param("seatIds") List<Long> seatIds);

    /**
     * 查询被特定订单锁定的座位（用于回滚清理Redis缓存）
     * @param orderId 订单ID
     * @return 座位信息列表
     */
    List<SeatCacheDTO> selectLockedSeatsByOrderId(@Param("orderId") Long orderId);

    /**
     * 快速抢票：按区域查询可售座位（DB兜底，按行号、座位号升序）
     * 当 Redis 座位缓存尚未预热时使用，limit 建议传 seatCount * 5 以保证连座算法有足够候选
     *
     * @param showEventId 演出活动ID
     * @param zone        座位区域（如 VIP/A/B/C）
     * @param limit       最多返回行数
     * @return 可售座位列表，按 (seat_row, seat_number) 升序排列
     */
    @org.apache.ibatis.annotations.Select(
            "SELECT * FROM tb_seat " +
            "WHERE show_event_id = #{showEventId} " +
            "  AND seat_zone = #{zone} " +
            "  AND status = 'AVAILABLE' " +
            "ORDER BY seat_row, seat_number " +
            "LIMIT #{limit}"
    )
    List<Seat> selectAvailableByZone(@Param("showEventId") Long showEventId,
                                     @Param("zone") String zone,
                                     @Param("limit") Integer limit);
}
