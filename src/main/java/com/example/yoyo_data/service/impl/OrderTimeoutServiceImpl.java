package com.example.yoyo_data.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.yoyo_data.common.constant.KafkaTopic;
import com.example.yoyo_data.common.constant.OrderStatus;
import com.example.yoyo_data.common.entity.OrderSeat;
import com.example.yoyo_data.common.entity.TicketOrder;
import com.example.yoyo_data.infrastructure.cache.RedisService;
import com.example.yoyo_data.infrastructure.message.KafkaProducerTemplate;
import com.example.yoyo_data.infrastructure.message.MessageEvent;
import com.example.yoyo_data.infrastructure.repository.*;
import com.example.yoyo_data.service.OrderTimeoutService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 订单超时处理服务实现类
 * 定时任务：每分钟执行一次，处理过期订单和座位
 */
@Slf4j
@Service
public class OrderTimeoutServiceImpl implements OrderTimeoutService {

    @Autowired
    private TicketOrderMapper ticketOrderMapper;

    @Autowired
    private OrderSeatMapper orderSeatMapper;

    @Autowired
    private SeatMapper seatMapper;

    @Autowired
    private ShowEventMapper showEventMapper;

    @Autowired
    private UserTicketRecordMapper userTicketRecordMapper;

    @Autowired
    private KafkaProducerTemplate kafkaProducerTemplate;

    @Autowired
    private RedisService redisService;

    /**
     * 定时任务：处理过期订单
     * 每分钟执行一次
     */
    @Override
    @Scheduled(cron = "0 * * * * ?") // 每分钟执行
    @Transactional(rollbackFor = Exception.class)
    public int handleExpiredOrders() {
        try {
            LocalDateTime now = LocalDateTime.now();
            log.debug("开始处理过期订单，当前时间: {}", now);

            // 1. 查询所有已过期的待支付订单
            LambdaQueryWrapper<TicketOrder> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(TicketOrder::getStatus, OrderStatus.PENDING)
                    .lt(TicketOrder::getExpireTime, now);

            List<TicketOrder> expiredOrders = ticketOrderMapper.selectList(queryWrapper);

            if (expiredOrders.isEmpty()) {
                log.debug("无过期订单需要处理");
                return 0;
            }

            log.info("发现过期订单 {} 笔，开始处理", expiredOrders.size());

            // 2. 逐个处理过期订单
            int processedCount = 0;
            for (TicketOrder order : expiredOrders) {
                try {
                    // 2.1 查询订单座位
                    LambdaQueryWrapper<OrderSeat> osQuery = new LambdaQueryWrapper<>();
                    osQuery.eq(OrderSeat::getOrderId, order.getId());
                    List<OrderSeat> orderSeats = orderSeatMapper.selectList(osQuery);
                    List<Long> seatIds = orderSeats.stream()
                            .map(OrderSeat::getSeatId)
                            .collect(Collectors.toList());

                    // 2.2 批量释放座位
                    if (!seatIds.isEmpty()) {
                        int releasedCount = seatMapper.batchReleaseSeat(seatIds);
                        log.debug("订单 {} 释放座位: 总数={}, 实际释放={}",
                                order.getOrderNo(), seatIds.size(), releasedCount);
                    }

                    // 2.3 更新演出活动座位数（locked -> available）
                    int updateShowResult = showEventMapper.releaseSeats(
                            order.getShowEventId(),
                            order.getSeatCount()
                    );
                    log.debug("订单 {} 更新演出座位数: {}", order.getOrderNo(), updateShowResult);

                    // 2.4 更新订单状态为超时
                    int updateOrderResult = ticketOrderMapper.updateOrderToTimeout(order.getId());
                    if (updateOrderResult > 0) {
                        log.info("订单超时处理成功: orderNo={}, showEventId={}, seatCount={}",
                                order.getOrderNo(), order.getShowEventId(), order.getSeatCount());

                        // 2.5 减少用户购票记录
                        userTicketRecordMapper.decreaseTicketCount(
                                order.getUserId(),
                                order.getShowEventId(),
                                order.getSeatCount()
                        );

                        // 2.6 发送 Kafka 消息
                        sendOrderTimeoutEvent(order);

                        processedCount++;
                    }

                } catch (Exception e) {
                    log.error("处理过期订单失败: orderNo={}", order.getOrderNo(), e);
                    // 继续处理下一个订单，不影响其他订单
                }
            }

            log.info("过期订单处理完成: 总数={}, 成功处理={}", expiredOrders.size(), processedCount);
            return processedCount;

        } catch (Exception e) {
            log.error("处理过期订单任务失败", e);
            return 0;
        }
    }

    /**
     * 定时任务：处理过期的锁定座位
     * 每分钟执行一次
     * 作为兜底策略，防止订单表更新失败导致座位永久锁定
     */
    @Override
    @Scheduled(cron = "0 * * * * ?") // 每分钟执行
    public int handleExpiredSeats() {
        try {
            LocalDateTime now = LocalDateTime.now();
            log.debug("开始处理过期锁定座位，当前时间: {}", now);

            // 释放所有过期的锁定座位
            int releasedCount = seatMapper.releaseExpiredSeats(now);

            if (releasedCount > 0) {
                log.info("释放过期锁定座位: 数量={}", releasedCount);
            } else {
                log.debug("无过期锁定座位需要处理");
            }

            return releasedCount;

        } catch (Exception e) {
            log.error("处理过期锁定座位任务失败", e);
            return 0;
        }
    }

    /**
     * 发送订单超时事件
     */
    private void sendOrderTimeoutEvent(TicketOrder order) {
        try {
            Map<String, Object> eventData = new HashMap<>();
            eventData.put("orderId", order.getId());
            eventData.put("orderNo", order.getOrderNo());
            eventData.put("userId", order.getUserId());
            eventData.put("showEventId", order.getShowEventId());
            eventData.put("seatCount", order.getSeatCount());
            eventData.put("totalAmount", order.getTotalAmount());
            eventData.put("expireTime", order.getExpireTime());

            MessageEvent event = MessageEvent.builder()
                    .eventType(KafkaTopic.ORDER_TIMEOUT)
                    .source("OrderTimeoutService")
                    .userId(order.getUserId())
                    .data(JSON.toJSONString(eventData))
                    .timestamp(LocalDateTime.now())
                    .createdAt(LocalDateTime.now())
                    .priority(7)
                    .build();

            kafkaProducerTemplate.sendEvent(KafkaTopic.ORDER_TIMEOUT, event);
            log.debug("发送订单超时事件: orderNo={}", order.getOrderNo());

        } catch (Exception e) {
            log.error("发送订单超时事件失败: orderNo={}", order.getOrderNo(), e);
        }
    }
}
