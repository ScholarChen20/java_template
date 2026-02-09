package com.example.yoyo_data.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.yoyo_data.common.Result;
import com.example.yoyo_data.common.dto.GrabTicketDTO;
import com.example.yoyo_data.common.dto.PayOrderDTO;
import com.example.yoyo_data.common.vo.TicketOrderVO;
import com.example.yoyo_data.service.TicketService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

/**
 * 抢票控制器
 * 提供抢票、支付、取消订单等核心业务接口
 */
@Slf4j
@RestController
@RequestMapping("/api/ticket")
@Api(tags = "抢票模块", description = "抢票、支付订单、取消订单、查询订单等操作")
public class TicketController {

    @Autowired
    private TicketService ticketService;

    /**
     * 抢票接口（核心）
     * 注意：此接口需要限流保护（建议 100 req/s），防止恶意刷票
     * 可以使用 Guava RateLimiter 或 Redis + Lua 脚本实现限流
     */
    @PostMapping("/grab")
    @ApiOperation(value = "抢票", notes = "核心抢票接口，支持选座模式和快速抢票模式。需要JWT认证。建议添加限流保护（100 req/s）")
    public Result<TicketOrderVO> grabTicket(
            @Valid @RequestBody GrabTicketDTO dto,
            HttpServletRequest request
    ) {
        // 从请求头获取 token
        String token = extractToken(request);

        log.info("抢票请求: showEventId={}, seatIds={}, seatCount={}",
                dto.getShowEventId(), dto.getSeatIds(), dto.getSeatCount());

        return ticketService.grabTicket(dto, token);
    }

    /**
     * 支付订单
     */
    @PostMapping("/pay")
    @ApiOperation(value = "支付订单", notes = "支付待支付状态的订单。需要JWT认证")
    public Result<TicketOrderVO> payOrder(
            @Valid @RequestBody PayOrderDTO dto,
            HttpServletRequest request
    ) {
        // 从请求头获取 token
        String token = extractToken(request);

        log.info("支付订单请求: orderId={}, payType={}", dto.getOrderId(), dto.getPayType());

        return ticketService.payOrder(dto, token);
    }

    /**
     * 取消订单
     */
    @PostMapping("/cancel/{orderId}")
    @ApiOperation(value = "取消订单", notes = "取消待支付状态的订单，释放座位。需要JWT认证")
    public Result<Void> cancelOrder(
            @ApiParam(value = "订单ID", required = true) @PathVariable("orderId") Long orderId,
            HttpServletRequest request
    ) {
        // 从请求头获取 token
        String token = extractToken(request);

        log.info("取消订单请求: orderId={}", orderId);

        return ticketService.cancelOrder(orderId, token);
    }

    /**
     * 查询订单详情
     */
    @GetMapping("/{orderId}")
    @ApiOperation(value = "查询订单详情", notes = "根据订单ID查询订单详细信息。需要JWT认证")
    public Result<TicketOrderVO> queryOrder(
            @ApiParam(value = "订单ID", required = true) @PathVariable("orderId") Long orderId,
            HttpServletRequest request
    ) {
        // 从请求头获取 token
        String token = extractToken(request);

        log.info("查询订单详情: orderId={}", orderId);

        return ticketService.queryOrder(orderId, token);
    }

    /**
     * 查询我的订单列表
     */
    @GetMapping("/orders")
    @ApiOperation(value = "查询我的订单列表", notes = "分页查询当前用户的所有订单，支持状态筛选。需要JWT认证")
    public Result<Page<TicketOrderVO>> queryMyOrders(
            @ApiParam(value = "页码", defaultValue = "1") @RequestParam(value = "page", defaultValue = "1") Integer page,
            @ApiParam(value = "每页大小", defaultValue = "10") @RequestParam(value = "size", defaultValue = "10") Integer size,
            @ApiParam(value = "订单状态筛选（PENDING-待支付, PAID-已支付, CANCELLED-已取消, TIMEOUT-超时取消）") @RequestParam(value = "status", required = false) String status,
            HttpServletRequest request
    ) {
        // 从请求头获取 token
        String token = extractToken(request);

        log.info("查询我的订单列表: page={}, size={}, status={}", page, size, status);

        return ticketService.queryMyOrders(token, page, size, status);
    }

    /**
     * 从请求头提取 token
     * @param request HTTP 请求
     * @return token 字符串（去掉 "Bearer " 前缀）
     */
    private String extractToken(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return token;
    }
}
