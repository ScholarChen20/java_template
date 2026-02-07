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
     * 批量插入订单座位关联记录
     * 注意：MyBatis-Plus 的 saveBatch 方法可以实现批量插入，无需自定义 SQL
     * 但如果需要更高性能，可以使用以下自定义 SQL（需要在 XML 文件中实现）
     */

    /**
     * 根据订单ID删除订单座位关联（订单取消时调用）
     * 注意：这个操作通常不需要，因为订单取消后只需要释放座位状态即可
     * 但为了数据一致性，可以物理删除或软删除订单座位关联
     * @param orderId 订单ID
     * @return 删除行数
     */
    // 通常不需要物理删除，保留历史记录即可

    /**
     * 查询订单的所有座位ID
     * @param orderId 订单ID
     * @return 座位ID列表
     */
    // 可以使用 MyBatis-Plus 的 LambdaQueryWrapper 实现，无需自定义 SQL
}
