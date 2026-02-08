package com.example.yoyo_data.service;

/**
 * 订单超时处理服务接口
 */
public interface OrderTimeoutService {

    /**
     * 处理过期订单（定时任务调用）
     * 每分钟执行一次，将过期的待支付订单设置为超时状态，并释放座位
     * @return 处理的订单数量
     */
    int handleExpiredOrders();

    /**
     * 处理过期的锁定座位（定时任务调用）
     * 每分钟执行一次，释放锁定时间已过期的座位
     * @return 释放的座位数量
     */
    int handleExpiredSeats();
}
