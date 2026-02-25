package com.example.yoyo_data.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.yoyo_data.common.Result;
import com.example.yoyo_data.common.dto.GrabTicketDTO;
import com.example.yoyo_data.common.dto.PayOrderDTO;
import com.example.yoyo_data.common.vo.TicketOrderVO;

import com.example.yoyo_data.common.dto.request.QuickGrabRequest;

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
    Result<String> cancelOrder(Long orderId, String token);

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

    /**
     * 查看我的电子票（已支付订单）
     * 对应大麦"我的票夹"功能，只返回 PAID 状态的订单，包含二维码入场码
     * @param token JWT token
     * @param page 页码
     * @param size 每页大小
     * @return 电子票列表（含二维码URL）
     */
    Result<Page<TicketOrderVO>> getMyTickets(String token, Integer page, Integer size);

    /**
     * 快速抢票（系统自动分配座位）
     *
     * 对应大麦网"快速购票"模式：用户选择价位区域（如 880元=A区）和购票张数，
     * 系统自动从该区域中挑选可售座位（支持优先连座），无需用户手动选座。
     * 演出前一周才会告知具体座位号。
     *
     * @param request 快速抢票请求（区域 + 数量 + 观影人列表）
     * @param token   JWT token
     * @return 订单信息（含系统分配的座位）
     */
    Result<TicketOrderVO> quickGrabTicket(QuickGrabRequest request, String token);
}
