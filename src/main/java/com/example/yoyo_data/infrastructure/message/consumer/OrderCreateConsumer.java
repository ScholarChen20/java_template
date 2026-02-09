package com.example.yoyo_data.infrastructure.message.consumer;

import com.alibaba.fastjson.JSON;
import com.example.yoyo_data.common.dto.OrderCreateEvent;
import com.example.yoyo_data.common.entity.OrderSeat;
import com.example.yoyo_data.common.entity.UserTicketRecord;
import com.example.yoyo_data.infrastructure.cache.RedisService;
import com.example.yoyo_data.infrastructure.message.MessageEvent;
import com.example.yoyo_data.infrastructure.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.example.yoyo_data.common.constant.KafkaTopic.ORDER_CREATE;

/**
 * 订单创建事件消费者
 * 异步处理订单创建后的业务逻辑：座位锁定、演出统计更新、订单座位关联、购票记录更新等
 *
 * @author YoYo Data Team
 * @version 1.0
 * @since 2026-02-09
 */
@Slf4j
@Component
public class OrderCreateConsumer {

    @Autowired
    private SeatMapper seatMapper;

    @Autowired
    private ShowEventMapper showEventMapper;

    @Autowired
    private OrderSeatMapper orderSeatMapper;

    @Autowired
    private UserTicketRecordMapper userTicketRecordMapper;

    @Autowired
    private TicketOrderMapper ticketOrderMapper;

    @Autowired
    private RedisService redisService;

    /**
     * 消费订单创建事件
     * 处理订单创建后的异步业务逻辑
     *
     * @param message 消息内容
     */
    @KafkaListener(topics = ORDER_CREATE, groupId = "order-create-consumer-group")
    @Transactional(rollbackFor = Exception.class)
    public void handleOrderCreateEvent(String message) {
        log.info("【订单创建事件】收到消息: {}", message);

        try {
            // 解析消息
            MessageEvent messageEvent = JSON.parseObject(message, MessageEvent.class);
            OrderCreateEvent orderEvent = JSON.parseObject(messageEvent.getData(), OrderCreateEvent.class);

            log.info("【订单创建事件-处理开始】orderId={}, orderNo={}, userId={}, seatCount={}",
                    orderEvent.getOrderId(), orderEvent.getOrderNo(),
                    orderEvent.getUserId(), orderEvent.getSeatCount());

            // 执行业务逻辑
            processOrderCreate(orderEvent);

            log.info("【订单创建事件-处理成功】orderId={}, orderNo={}",
                    orderEvent.getOrderId(), orderEvent.getOrderNo());

        } catch (Exception e) {
            log.error("【订单创建事件-处理失败】message={}, error={}",
                    message, e.getMessage(), e);
            // 抛出异常，Kafka 会自动重试
            throw new RuntimeException("订单创建事件处理失败", e);
        }
    }

    /**
     * 处理订单创建业务逻辑
     *
     * @param orderEvent 订单创建事件
     */
    private void processOrderCreate(OrderCreateEvent orderEvent) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lockExpireTime = orderEvent.getExpireTime();

        // 第一步：CAS 锁定座位
        List<Long> lockedSeatIds = lockSeats(orderEvent, lockExpireTime);
        log.info("【座位锁定完成】orderId={}, 锁定座位数={}/{}",
                orderEvent.getOrderId(), lockedSeatIds.size(), orderEvent.getSeatCount());

        // 第二步：更新演出活动座位数
        updateShowEventSeats(orderEvent);
        log.info("【演出统计更新完成】orderId={}, showEventId={}, 可售座位-{}, 锁定座位+{}",
                orderEvent.getOrderId(), orderEvent.getShowEventId(),
                orderEvent.getSeatCount(), orderEvent.getSeatCount());

        // 第三步：创建订单座位关联（绑定观影人）
        createOrderSeats(orderEvent);
        log.info("【订单座位关联完成】orderId={}, 关联座位数={}, 已绑定观影人信息",
                orderEvent.getOrderId(), orderEvent.getSeatCount());

        // 第四步：更新用户购票记录
        updateUserTicketRecord(orderEvent);
        log.info("【购票记录更新完成】orderId={}, userId={}, showEventId={}, 购票数+{}",
                orderEvent.getOrderId(), orderEvent.getUserId(),
                orderEvent.getShowEventId(), orderEvent.getSeatCount());

        // 第五步：清除相关缓存
        clearCache(orderEvent.getShowEventId());
        log.info("【缓存清除完成】orderId={}, showEventId={}",
                orderEvent.getOrderId(), orderEvent.getShowEventId());
    }

    /**
     * CAS 锁定座位
     *
     * @param orderEvent 订单创建事件
     * @param lockExpireTime 锁定过期时间
     * @return 已锁定的座位ID列表
     */
    private List<Long> lockSeats(OrderCreateEvent orderEvent, LocalDateTime lockExpireTime) {
        List<Long> lockedSeatIds = new ArrayList<>();

        for (OrderCreateEvent.SeatInfo seatInfo : orderEvent.getSeats()) {
            try {
                int lockResult = seatMapper.lockSeatWithCAS(
                        seatInfo.getSeatId(),
                        orderEvent.getUserId(),
                        orderEvent.getOrderId(),
                        lockExpireTime,
                        seatInfo.getVersion()
                );

                if (lockResult == 0) {
                    // CAS 失败，座位已被其他用户抢走
                    log.error("【CAS失败】座位被抢: seatCode={}, seatId={}, version={}, orderId={}",
                            seatInfo.getSeatCode(), seatInfo.getSeatId(), seatInfo.getVersion(),
                            orderEvent.getOrderId());

                    // 回滚：取消订单，释放已锁定的座位
                    rollbackOrder(orderEvent.getOrderId(), lockedSeatIds);
                    throw new RuntimeException("座位已被其他用户抢走: " + seatInfo.getSeatCode());
                }

                lockedSeatIds.add(seatInfo.getSeatId());

            } catch (Exception e) {
                log.error("【座位锁定失败】seatId={}, orderId={}, error={}",
                        seatInfo.getSeatId(), orderEvent.getOrderId(), e.getMessage(), e);
                // 回滚：取消订单，释放已锁定的座位
                rollbackOrder(orderEvent.getOrderId(), lockedSeatIds);
                throw new RuntimeException("座位锁定失败", e);
            }
        }

        return lockedSeatIds;
    }

    /**
     * 更新演出活动座位数
     *
     * @param orderEvent 订单创建事件
     */
    private void updateShowEventSeats(OrderCreateEvent orderEvent) {
        int updateResult = showEventMapper.lockSeats(
                orderEvent.getShowEventId(),
                orderEvent.getSeatCount(),
                orderEvent.getShowEventVersion()
        );

        if (updateResult == 0) {
            // 乐观锁失败，演出座位数已被其他事务修改
            log.error("【乐观锁失败】演出座位数更新失败: showEventId={}, version={}, orderId={}",
                    orderEvent.getShowEventId(), orderEvent.getShowEventVersion(),
                    orderEvent.getOrderId());

            // 回滚：取消订单，释放座位
            rollbackOrder(orderEvent.getOrderId(), orderEvent.getSeatIds());
            throw new RuntimeException("演出座位库存更新失败，请重试");
        }
    }

    /**
     * 创建订单座位关联（绑定观影人）
     *
     * @param orderEvent 订单创建事件
     */
    private void createOrderSeats(OrderCreateEvent orderEvent) {
        List<OrderSeat> orderSeats = new ArrayList<>();

        for (int i = 0; i < orderEvent.getSeats().size(); i++) {
            OrderCreateEvent.SeatInfo seatInfo = orderEvent.getSeats().get(i);
            OrderCreateEvent.TicketUserInfo ticketUser = orderEvent.getTicketUsers().get(i);

            OrderSeat orderSeat = OrderSeat.builder()
                    .orderId(orderEvent.getOrderId())
                    .seatId(seatInfo.getSeatId())
                    .showEventId(orderEvent.getShowEventId())
                    .seatCode(seatInfo.getSeatCode())
                    .price(seatInfo.getPrice())
                    .viewerName(ticketUser.getContactName())
                    .viewerPhone(ticketUser.getContactPhone())
                    .viewerIdCard(ticketUser.getContactIdCard())
                    .build();

            orderSeats.add(orderSeat);
        }

        // 批量插入
        orderSeats.forEach(orderSeatMapper::insert);
    }

    /**
     * 更新用户购票记录
     *
     * @param orderEvent 订单创建事件
     */
    private void updateUserTicketRecord(OrderCreateEvent orderEvent) {
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserTicketRecord> queryWrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        queryWrapper.eq(UserTicketRecord::getUserId, orderEvent.getUserId())
                .eq(UserTicketRecord::getShowEventId, orderEvent.getShowEventId());

        UserTicketRecord record = userTicketRecordMapper.selectOne(queryWrapper);

        if (record == null) {
            // 首次购票，创建记录
            record = UserTicketRecord.builder()
                    .userId(orderEvent.getUserId())
                    .showEventId(orderEvent.getShowEventId())
                    .ticketCount(orderEvent.getSeatCount())
                    .build();
            userTicketRecordMapper.insert(record);
            log.debug("【创建购票记录】userId={}, showEventId={}, ticketCount={}",
                    orderEvent.getUserId(), orderEvent.getShowEventId(), orderEvent.getSeatCount());
        } else {
            // 增加购票数（使用原子性 SQL）
            userTicketRecordMapper.increaseTicketCount(
                    orderEvent.getUserId(),
                    orderEvent.getShowEventId(),
                    orderEvent.getSeatCount()
            );
            log.debug("【更新购票记录】userId={}, showEventId={}, ticketCount+{}",
                    orderEvent.getUserId(), orderEvent.getShowEventId(), orderEvent.getSeatCount());
        }
    }

    /**
     * 清除缓存
     *
     * @param showEventId 演出活动ID
     */
    private void clearCache(Long showEventId) {
        try {
            String cacheKey = "ticket:show:detail:" + showEventId;
            redisService.delete(cacheKey);
            log.debug("【缓存清除】showEventId={}", showEventId);
        } catch (Exception e) {
            // 缓存清除失败不影响主流程
            log.warn("【缓存清除失败】showEventId={}, error={}", showEventId, e.getMessage());
        }
    }

    /**
     * 回滚订单（取消订单并释放座位）
     *
     * @param orderId 订单ID
     * @param lockedSeatIds 已锁定的座位ID列表
     */
    private void rollbackOrder(Long orderId, List<Long> lockedSeatIds) {
        try {
            log.warn("【订单回滚开始】orderId={}, 需释放座位数={}", orderId, lockedSeatIds.size());

            // 1. 取消订单状态
            ticketOrderMapper.updateOrderToCancelled(orderId);

            // 2. 释放已锁定的座位
            if (!lockedSeatIds.isEmpty()) {
                seatMapper.batchReleaseSeat(lockedSeatIds);
            }

            log.warn("【订单回滚完成】orderId={}, 已释放座位数={}", orderId, lockedSeatIds.size());

        } catch (Exception e) {
            log.error("【订单回滚失败】orderId={}, error={}", orderId, e.getMessage(), e);
        }
    }
}
