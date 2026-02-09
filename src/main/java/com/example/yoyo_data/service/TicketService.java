package com.example.yoyo_data.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.yoyo_data.common.Result;
import com.example.yoyo_data.common.dto.GrabTicketDTO;
import com.example.yoyo_data.common.dto.PayOrderDTO;
import com.example.yoyo_data.common.vo.TicketOrderVO;

/**
 * 抢票服务接口
 */
public interface TicketService {

    /**
     * 抢票（核心接口）
     * @param dto 抢票请求参数
     * @param token JWT token
     * @return 订单信息
     */
    Result<TicketOrderVO> grabTicket(GrabTicketDTO dto, String token);

    /**
     * 支付订单
     * @param dto 支付请求参数
     * @param token JWT token
     * @return 支付结果
     */
    Result<TicketOrderVO> payOrder(PayOrderDTO dto, String token);

    /**
     * 取消订单
     * @param orderId 订单ID
     * @param token JWT token
     * @return 取消结果
     */
    Result<Void> cancelOrder(Long orderId, String token);

    /**
     * 查询订单详情
     * @param orderId 订单ID
     * @param token JWT token
     * @return 订单详情
     */
    Result<TicketOrderVO> queryOrder(Long orderId, String token);

    /**
     * 查询我的订单列表
     * @param token JWT token
     * @param page 页码
     * @param size 每页大小
     * @param status 订单状态筛选（可选）
     * @return 订单列表
     */
    Result<Page<TicketOrderVO>> queryMyOrders(String token, Integer page, Integer size, String status);
}
