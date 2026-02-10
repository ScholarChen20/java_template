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

    @Autowired
    private com.example.yoyo_data.infrastructure.scheduler.OrderTimeoutHandler orderTimeoutHandler;

    @Autowired
    private org.springframework.data.redis.core.StringRedisTemplate stringRedisTemplate;

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
                    rollbackOrder(orderEvent, lockedSeatIds);
                    throw new RuntimeException("座位已被其他用户抢走: " + seatInfo.getSeatCode());
                }

                lockedSeatIds.add(seatInfo.getSeatId());

            } catch (Exception e) {
                log.error("【座位锁定失败】seatId={}, orderId={}, error={}",
                        seatInfo.getSeatId(), orderEvent.getOrderId(), e.getMessage(), e);
                // 回滚：取消订单，释放已锁定的座位
                rollbackOrder(orderEvent, lockedSeatIds);
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
            rollbackOrder(orderEvent, orderEvent.getSeatIds());
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
     * @param orderEvent 订单创建事件（包含showEventId、userId等信息）
     * @param lockedSeatIds 已锁定的座位ID列表（数据库中已锁定的座位）
     */
    private void rollbackOrder(OrderCreateEvent orderEvent, List<Long> lockedSeatIds) {
        Long orderId = orderEvent.getOrderId();
        Long userId = orderEvent.getUserId();
        Long showEventId = orderEvent.getShowEventId();

        try {
            log.warn("【订单回滚开始】orderId={}, userId={}, showEventId={}, 需释放座位数={}",
                    orderId, userId, showEventId, lockedSeatIds.size());

            // 查询订单信息（如果事务未提交，订单可能不存在）
            com.example.yoyo_data.common.entity.TicketOrder order = ticketOrderMapper.selectById(orderId);

            // 1. 取消订单状态（如果订单存在）
            if (order != null) {
                ticketOrderMapper.updateOrderToCancelled(orderId);
                log.info("【订单状态更新】orderId={}, status -> CANCELLED", orderId);
            } else {
                log.warn("【订单不存在】orderId={}, 可能事务未提交或已被删除，跳过订单状态更新", orderId);
            }

            // 2. 释放数据库中已锁定的座位（如果有）
            int releasedCount = 0;
            if (!lockedSeatIds.isEmpty()) {
                releasedCount = seatMapper.batchReleaseSeat(lockedSeatIds);
                log.info("【数据库座位释放】orderId={}, 预期释放数={}, 实际释放数={}",
                        orderId, lockedSeatIds.size(), releasedCount);
            }

            // 3. 【关键修复】清理Redis座位缓存（将Lua脚本已锁定的座位恢复为可售状态）
            // 注意：即使订单不存在，Redis中的座位可能已被Lua脚本锁定
            // 使用orderEvent中的座位信息清理Redis
            clearRedisSeatCacheByEvent(orderEvent);

            // 4. 【关键修复】删除用户防重key（允许用户重新下单）
            clearUserAntiDuplicateKey(userId, showEventId);

            // 5. 【关键修复】从延迟队列移除订单（避免超时处理重复执行）
            // 注意：如果订单不存在，延迟队列中可能也没有（因为事务后操作还没执行）
            removeOrderFromDelayQueue(orderId);

            log.warn("【订单回滚完成】orderId={}, 数据库座位释放数={}, Redis缓存已清理, 延迟队列已移除",
                    orderId, releasedCount);

        } catch (Exception e) {
            log.error("【订单回滚失败】orderId={}, error={}", orderId, e.getMessage(), e);
        }
    }

    /**
     * 清理Redis座位缓存（使用OrderEvent中的座位信息）
     * 将Lua脚本已锁定的座位恢复为可售状态
     *
     * @param orderEvent 订单创建事件
     */
    private void clearRedisSeatCacheByEvent(OrderCreateEvent orderEvent) {
        try {
            Long showEventId = orderEvent.getShowEventId();
            Long orderId = orderEvent.getOrderId();

            // 从orderEvent中获取座位信息
            List<OrderCreateEvent.SeatInfo> seatInfos = orderEvent.getSeats();
            if (seatInfos == null || seatInfos.isEmpty()) {
                log.warn("【Redis缓存清理】订单事件中没有座位信息: orderId={}", orderId);
                return;
            }

            // 查询座位详细信息（需要seat_zone、seat_row、seat_number）
            List<Long> seatIds = seatInfos.stream()
                    .map(OrderCreateEvent.SeatInfo::getSeatId)
                    .collect(java.util.stream.Collectors.toList());

            List<com.example.yoyo_data.common.dto.SeatCacheDTO> seats =
                    seatMapper.selectByEventIdAndSeatIds(showEventId, seatIds);

            if (seats.isEmpty()) {
                log.warn("【Redis缓存清理】未找到座位信息: orderId={}, seatIds={}", orderId, seatIds);
                return;
            }

            // 按分区分组
            java.util.Map<String, List<com.example.yoyo_data.common.dto.SeatCacheDTO>> seatsByZone =
                    seats.stream()
                            .collect(java.util.stream.Collectors.groupingBy(
                                    com.example.yoyo_data.common.dto.SeatCacheDTO::getSeatZone
                            ));

            // 更新Redis缓存：将座位状态从"1"（已锁定）恢复为"0"（可售）
            int totalUpdated = 0;
            for (java.util.Map.Entry<String, List<com.example.yoyo_data.common.dto.SeatCacheDTO>> entry : seatsByZone.entrySet()) {
                String zone = entry.getKey();
                List<com.example.yoyo_data.common.dto.SeatCacheDTO> zoneSeats = entry.getValue();

                String hashKey = com.example.yoyo_data.common.constant.TicketRedisKey.SEAT_STOCK_PREFIX
                        + showEventId + ":" + zone;

                // 批量更新Hash Field
                for (com.example.yoyo_data.common.dto.SeatCacheDTO seat : zoneSeats) {
                    String field = seat.getSeatKey(); // "row_col" 格式
                    stringRedisTemplate.opsForHash().put(hashKey, field,
                            com.example.yoyo_data.common.constant.SeatStatus.CACHE_AVAILABLE); // "0"
                    totalUpdated++;
                }
            }

            log.info("【Redis缓存清理】orderId={}, showEventId={}, 恢复座位数={}, zones={}",
                    orderId, showEventId, totalUpdated, seatsByZone.keySet());

        } catch (Exception e) {
            // Redis缓存清理失败不影响回滚主流程，只记录错误
            log.error("【Redis缓存清理失败】orderId={}, error={}",
                    orderEvent.getOrderId(), e.getMessage(), e);
        }
    }

    /**
     * 清理Redis座位缓存
     * 将Lua脚本已锁定的座位恢复为可售状态
     *
     * @param order 订单实体
     */
    /**
     * 清理用户防重key
     * 删除Lua脚本设置的防重key，允许用户重新下单
     *
     * @param userId 用户ID
     * @param showEventId 演出活动ID
     */
    private void clearUserAntiDuplicateKey(Long userId, Long showEventId) {
        try {
            // Lua脚本中的key格式：seckill:user:{userId}:activity:{activityId}
            String antiDuplicateKey = "seckill:user:" + userId + ":activity:" + showEventId;
            Boolean deleted = stringRedisTemplate.delete(antiDuplicateKey);

            if (Boolean.TRUE.equals(deleted)) {
                log.info("【用户防重key清理】userId={}, showEventId={}, key已删除", userId, showEventId);
            } else {
                log.warn("【用户防重key清理】userId={}, showEventId={}, key不存在或删除失败", userId, showEventId);
            }

        } catch (Exception e) {
            log.error("【用户防重key清理失败】userId={}, showEventId={}, error={}",
                    userId, showEventId, e.getMessage(), e);
        }
    }

    /**
     * 从延迟队列移除订单
     * 避免订单超时处理重复执行
     *
     * @param orderId 订单ID
     */
    private void removeOrderFromDelayQueue(Long orderId) {
        try {
            boolean removed = orderTimeoutHandler.removeOrderFromDelayQueue(orderId);
            if (removed) {
                log.info("【延迟队列移除】orderId={}, 已从延迟队列移除", orderId);
            } else {
                log.warn("【延迟队列移除】orderId={}, 移除失败（可能已超时或不存在）", orderId);
            }

        } catch (Exception e) {
            log.error("【延迟队列移除失败】orderId={}, error={}", orderId, e.getMessage(), e);
        }
    }
}
