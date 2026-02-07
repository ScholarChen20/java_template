package com.example.yoyo_data.infrastructure.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.yoyo_data.common.entity.TicketOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;

/**
 * 票务订单表 Mapper 接口
 */
@Mapper
public interface TicketOrderMapper extends BaseMapper<TicketOrder> {

    /**
     * 更新订单状态为已支付
     * @param orderId 订单ID
     * @param payType 支付方式
     * @param payTime 支付时间
     * @return 更新行数
     */
    @Update("UPDATE tb_ticket_order SET " +
            "status = 'PAID', " +
            "pay_type = #{payType}, " +
            "pay_time = #{payTime}, " +
            "updated_at = NOW() " +
            "WHERE id = #{orderId} " +
            "AND status = 'PENDING'")
    int updateOrderToPaid(@Param("orderId") Long orderId,
                          @Param("payType") String payType,
                          @Param("payTime") LocalDateTime payTime);

    /**
     * 更新订单状态为已取消
     * @param orderId 订单ID
     * @return 更新行数
     */
    @Update("UPDATE tb_ticket_order SET " +
            "status = 'CANCELLED', " +
            "updated_at = NOW() " +
            "WHERE id = #{orderId} " +
            "AND status = 'PENDING'")
    int updateOrderToCancelled(@Param("orderId") Long orderId);

    /**
     * 更新订单状态为超时取消
     * @param orderId 订单ID
     * @return 更新行数
     */
    @Update("UPDATE tb_ticket_order SET " +
            "status = 'TIMEOUT', " +
            "updated_at = NOW() " +
            "WHERE id = #{orderId} " +
            "AND status = 'PENDING'")
    int updateOrderToTimeout(@Param("orderId") Long orderId);

    /**
     * 批量将已过期的订单设置为超时状态（定时任务调用）
     * @param now 当前时间
     * @return 更新行数
     */
    @Update("UPDATE tb_ticket_order SET " +
            "status = 'TIMEOUT', " +
            "updated_at = NOW() " +
            "WHERE status = 'PENDING' " +
            "AND expire_time < #{now}")
    int batchUpdateExpiredOrders(@Param("now") LocalDateTime now);
}
