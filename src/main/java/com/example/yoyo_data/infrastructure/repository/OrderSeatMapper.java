package com.example.yoyo_data.infrastructure.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.yoyo_data.common.entity.OrderSeat;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 订单座位关联表 Mapper 接口
 */
@Mapper
public interface OrderSeatMapper extends BaseMapper<OrderSeat> {
    /**
     * 根据订单id查询座位id
     * @param orderId
     * @return
     */
    List<Long> selectIdsByOrderId(@Param("orderId") Long orderId);
}
