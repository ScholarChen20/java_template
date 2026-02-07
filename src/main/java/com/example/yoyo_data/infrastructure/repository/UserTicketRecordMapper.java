package com.example.yoyo_data.infrastructure.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.yoyo_data.common.entity.UserTicketRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 用户购票记录表 Mapper 接口
 */
@Mapper
public interface UserTicketRecordMapper extends BaseMapper<UserTicketRecord> {

    /**
     * 增加用户购票数（使用原子操作）
     * @param userId 用户ID
     * @param showEventId 演出活动ID
     * @param ticketCount 购票数量
     * @return 更新行数
     */
    @Update("UPDATE tb_user_ticket_record SET " +
            "ticket_count = ticket_count + #{ticketCount}, " +
            "updated_at = NOW() " +
            "WHERE user_id = #{userId} " +
            "AND show_event_id = #{showEventId}")
    int increaseTicketCount(@Param("userId") Long userId,
                            @Param("showEventId") Long showEventId,
                            @Param("ticketCount") Integer ticketCount);

    /**
     * 减少用户购票数（订单取消时调用）
     * @param userId 用户ID
     * @param showEventId 演出活动ID
     * @param ticketCount 购票数量
     * @return 更新行数
     */
    @Update("UPDATE tb_user_ticket_record SET " +
            "ticket_count = ticket_count - #{ticketCount}, " +
            "updated_at = NOW() " +
            "WHERE user_id = #{userId} " +
            "AND show_event_id = #{showEventId} " +
            "AND ticket_count >= #{ticketCount}")
    int decreaseTicketCount(@Param("userId") Long userId,
                            @Param("showEventId") Long showEventId,
                            @Param("ticketCount") Integer ticketCount);
}
