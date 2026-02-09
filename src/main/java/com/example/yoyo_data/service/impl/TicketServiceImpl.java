package com.example.yoyo_data.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.yoyo_data.common.Result;
import com.example.yoyo_data.common.constant.*;
import com.example.yoyo_data.common.dto.GrabTicketDTO;
import com.example.yoyo_data.common.dto.PayOrderDTO;
import com.example.yoyo_data.common.entity.*;
import com.example.yoyo_data.common.vo.SeatVO;
import com.example.yoyo_data.common.vo.TicketOrderVO;
import com.example.yoyo_data.infrastructure.cache.RedisService;
import com.example.yoyo_data.infrastructure.message.KafkaProducerTemplate;
import com.example.yoyo_data.infrastructure.message.MessageEvent;
import com.example.yoyo_data.infrastructure.repository.*;
import com.example.yoyo_data.service.TicketService;
import com.example.yoyo_data.util.jwt.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 抢票服务实现类 - 核心业务逻辑
 */
@Slf4j
//@Service("ticketServiceSimple") // 改为备用实现，使用简单 Redis 锁
public class TicketServiceImpl implements TicketService {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private RedisService redisService;

    @Autowired
    private ShowEventMapper showEventMapper;

    @Autowired
    private SeatMapper seatMapper;

    @Autowired
    private TicketOrderMapper ticketOrderMapper;

    @Autowired
    private OrderSeatMapper orderSeatMapper;

    @Autowired
    private UserTicketRecordMapper userTicketRecordMapper;

    @Autowired
    private KafkaProducerTemplate kafkaProducerTemplate;

    /**
     * 抢票 - 核心接口
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<TicketOrderVO> grabTicket(GrabTicketDTO dto, String token) {
        // 1. 验证 token
        if (token == null || !jwtUtils.validateToken(token)) {
            return Result.unauthorized("Token无效或已过期");
        }
        Long userId = jwtUtils.getUserIdFromToken(token);

        // 2. 验证演出活动
        ShowEvent showEvent = showEventMapper.selectById(dto.getShowEventId());
        if (showEvent == null) {
            return Result.error("演出活动不存在");
        }

        // 检查演出状态
        if (!ShowEventStatus.SELLING.equals(showEvent.getStatus())) {
            return Result.error("演出未开始售票或已结束");
        }

        // 检查是否在售票时间内
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(showEvent.getSaleStartTime())) {
            return Result.error("演出尚未开始售票");
        }
        if (now.isAfter(showEvent.getSaleEndTime())) {
            return Result.error("演出已结束售票");
        }

        // 验证座位数量与观影人数量一致
        if (dto.getSeatIds().size() != dto.getTicketUsers().size()) {
            log.warn("【抢票失败】座位数量与观影人数量不一致: seatCount={}, userCount={}, userId={}",
                    dto.getSeatIds().size(), dto.getTicketUsers().size(), userId);
            return Result.error("座位数量必须与观影人数量一致");
        }

        // 3. 获取分布式锁（防止用户重复抢票）
        String lockKey = TicketRedisKey.GRAB_LOCK_PREFIX + userId + ":" + dto.getShowEventId();
        boolean locked = redisService.setIfAbsent(lockKey, "1", TicketRedisKey.GRAB_LOCK_EXPIRE);
        if (!locked) {
            return Result.error("请勿重复提交抢票请求");
        }

        try {
            // 4. 检查用户限购
            boolean limitCheckResult = checkUserPurchaseLimit(userId, dto.getShowEventId(),
                    dto.getSeatIds().size(), showEvent.getMaxBuyLimit());
            if (!limitCheckResult) {
                return Result.error("超过限购数量，每人最多购买" + showEvent.getMaxBuyLimit() + "张");
            }

            // 5. 验证座位并锁定
            List<Seat> seats = new ArrayList<>();
            for (Long seatId : dto.getSeatIds()) {
                Seat seat = seatMapper.selectById(seatId);
                if (seat == null) {
                    return Result.error("座位不存在: " + seatId);
                }
                if (!seat.getShowEventId().equals(dto.getShowEventId())) {
                    return Result.error("座位不属于该演出");
                }
                if (!SeatStatus.AVAILABLE.equals(seat.getStatus())) {
                    return Result.error("座位已被锁定或售出: " + seat.getSeatCode());
                }
                seats.add(seat);
            }

            // 6. 创建订单
            String orderNo = generateOrderNo(userId);
            BigDecimal totalAmount = seats.stream()
                    .map(Seat::getPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // 使用第一个观影人作为主联系人
            GrabTicketDTO.TicketUser primaryContact = dto.getTicketUsers().get(0);

            TicketOrder order = TicketOrder.builder()
                    .orderNo(orderNo)
                    .showEventId(dto.getShowEventId())
                    .userId(userId)
                    .seatCount(seats.size())
                    .totalAmount(totalAmount)
                    .status(OrderStatus.PENDING)
                    .expireTime(now.plusMinutes(15)) // 15分钟过期
                    .contactName(primaryContact.getContactName())
                    .contactPhone(primaryContact.getContactPhone())
                    .contactIdCard(primaryContact.getContactIdCard())
                    .build();

            ticketOrderMapper.insert(order);

            // 7. CAS 锁定座位
            LocalDateTime lockExpireTime = now.plusMinutes(15);
            for (Seat seat : seats) {
                int lockResult = seatMapper.lockSeatWithCAS(
                        seat.getId(),
                        userId,
                        order.getId(),
                        lockExpireTime,
                        seat.getVersion()
                );
                if (lockResult == 0) {
                    // CAS 失败，座位已被其他用户抢走
                    throw new RuntimeException("座位已被其他用户抢走: " + seat.getSeatCode());
                }
            }

            // 8. 更新演出活动座位数（乐观锁）
            int updateResult = showEventMapper.lockSeats(
                    dto.getShowEventId(),
                    seats.size(),
                    showEvent.getVersion()
            );
            if (updateResult == 0) {
                throw new RuntimeException("演出座位库存更新失败，请重试");
            }

            // 9. 创建订单座位关联（绑定观影人）
            List<OrderSeat> orderSeats = new ArrayList<>();
            for (int i = 0; i < seats.size(); i++) {
                Seat seat = seats.get(i);
                GrabTicketDTO.TicketUser ticketUser = dto.getTicketUsers().get(i);

                OrderSeat orderSeat = OrderSeat.builder()
                        .orderId(order.getId())
                        .seatId(seat.getId())
                        .showEventId(dto.getShowEventId())
                        .seatCode(seat.getSeatCode())
                        .price(seat.getPrice())
                        .viewerName(ticketUser.getContactName())
                        .viewerPhone(ticketUser.getContactPhone())
                        .viewerIdCard(ticketUser.getContactIdCard())
                        .build();

                orderSeats.add(orderSeat);
            }
            orderSeats.forEach(orderSeatMapper::insert);

            // 10. 更新用户购票记录
            updateUserTicketRecord(userId, dto.getShowEventId(), seats.size());

            // 11. 清除演出详情缓存
            clearShowEventCache(dto.getShowEventId());

            // 12. 发送 Kafka 消息
            sendGrabTicketEvent(order, seats);

            // 13. 构建返回结果
            TicketOrderVO vo = buildOrderVO(order, showEvent, seats);

            log.info("抢票成功: userId={}, orderId={}, orderNo={}, seatCount={}",
                    userId, order.getId(), orderNo, seats.size());

            return Result.success(vo);

        } catch (Exception e) {
            log.error("抢票失败: userId={}, showEventId={}", userId, dto.getShowEventId(), e);
            return Result.error("抢票失败: " + e.getMessage());
        } finally {
            // 14. 释放分布式锁
            redisService.delete(lockKey);
        }
    }

    /**
     * 支付订单
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<TicketOrderVO> payOrder(PayOrderDTO dto, String token) {
        // 1. 验证 token
        if (token == null || !jwtUtils.validateToken(token)) {
            return Result.unauthorized("Token无效或已过期");
        }
        Long userId = jwtUtils.getUserIdFromToken(token);

        try {
            // 2. 查询订单
            TicketOrder order = ticketOrderMapper.selectById(dto.getOrderId());
            if (order == null) {
                return Result.error("订单不存在");
            }

            // 验证订单所有权
            if (!order.getUserId().equals(userId)) {
                return Result.error("无权限操作此订单");
            }

            // 验证订单状态
            if (!OrderStatus.PENDING.equals(order.getStatus())) {
                return Result.error("订单状态不正确，无法支付");
            }

            // 验证订单是否过期
            if (LocalDateTime.now().isAfter(order.getExpireTime())) {
                return Result.error("订单已过期");
            }

            // 3. 查询订单座位
            LambdaQueryWrapper<OrderSeat> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(OrderSeat::getOrderId, dto.getOrderId());
            List<OrderSeat> orderSeats = orderSeatMapper.selectList(queryWrapper);
            List<Long> seatIds = orderSeats.stream()
                    .map(OrderSeat::getSeatId)
                    .collect(Collectors.toList());

            // 4. 更新订单状态为已支付
            int updateOrderResult = ticketOrderMapper.updateOrderToPaid(
                    dto.getOrderId(),
                    dto.getPayType(),
                    LocalDateTime.now()
            );
            if (updateOrderResult == 0) {
                return Result.error("订单状态已变更，支付失败");
            }

            // 5. 批量确认座位为已售出
            int updateSeatResult = seatMapper.batchConfirmSeatSold(seatIds);
            log.info("批量确认座位已售出: orderId={}, seatCount={}, updatedCount={}",
                    dto.getOrderId(), seatIds.size(), updateSeatResult);

            // 6. 更新演出活动座位数（locked -> sold）
            showEventMapper.confirmSeats(order.getShowEventId(), order.getSeatCount());

            // 7. 清除缓存
            clearShowEventCache(order.getShowEventId());

            // 8. 发送 Kafka 消息
            sendPayOrderEvent(order);

            // 9. 构建返回结果
            ShowEvent showEvent = showEventMapper.selectById(order.getShowEventId());
            List<Seat> seats = seatIds.stream()
                    .map(seatMapper::selectById)
                    .collect(Collectors.toList());
            TicketOrderVO vo = buildOrderVO(order, showEvent, seats);

            log.info("支付订单成功: userId={}, orderId={}, orderNo={}", userId, dto.getOrderId(), order.getOrderNo());

            return Result.success(vo);

        } catch (Exception e) {
            log.error("支付订单失败: userId={}, orderId={}", userId, dto.getOrderId(), e);
            return Result.error("支付订单失败: " + e.getMessage());
        }
    }

    /**
     * 取消订单
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> cancelOrder(Long orderId, String token) {
        // 1. 验证 token
        if (token == null || !jwtUtils.validateToken(token)) {
            return Result.unauthorized("Token无效或已过期");
        }
        Long userId = jwtUtils.getUserIdFromToken(token);

        try {
            // 2. 查询订单
            TicketOrder order = ticketOrderMapper.selectById(orderId);
            if (order == null) {
                return Result.error("订单不存在");
            }

            // 验证订单所有权
            if (!order.getUserId().equals(userId)) {
                return Result.error("无权限操作此订单");
            }

            // 验证订单状态（只能取消待支付的订单）
            if (!OrderStatus.PENDING.equals(order.getStatus())) {
                return Result.error("订单状态不正确，无法取消");
            }

            // 3. 查询订单座位
            LambdaQueryWrapper<OrderSeat> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(OrderSeat::getOrderId, orderId);
            List<OrderSeat> orderSeats = orderSeatMapper.selectList(queryWrapper);
            List<Long> seatIds = orderSeats.stream()
                    .map(OrderSeat::getSeatId)
                    .collect(Collectors.toList());

            // 4. 批量释放座位
            int releaseSeatResult = seatMapper.batchReleaseSeat(seatIds);
            log.info("批量释放座位: orderId={}, seatCount={}, releasedCount={}",
                    orderId, seatIds.size(), releaseSeatResult);

            // 5. 更新演出活动座位数（locked -> available）
            showEventMapper.releaseSeats(order.getShowEventId(), order.getSeatCount());

            // 6. 更新订单状态为已取消
            ticketOrderMapper.updateOrderToCancelled(orderId);

            // 7. 减少用户购票记录
            userTicketRecordMapper.decreaseTicketCount(userId, order.getShowEventId(), order.getSeatCount());

            // 8. 清除缓存
            clearShowEventCache(order.getShowEventId());

            // 9. 发送 Kafka 消息
            sendCancelOrderEvent(order);

            log.info("取消订单成功: userId={}, orderId={}, orderNo={}", userId, orderId, order.getOrderNo());

            return Result.success(null);

        } catch (Exception e) {
            log.error("取消订单失败: userId={}, orderId={}", userId, orderId, e);
            return Result.error("取消订单失败: " + e.getMessage());
        }
    }

    /**
     * 查询订单详情
     */
    @Override
    public Result<TicketOrderVO> queryOrder(Long orderId, String token) {
        // 验证 token
        if (token == null || !jwtUtils.validateToken(token)) {
            return Result.unauthorized("Token无效或已过期");
        }
        Long userId = jwtUtils.getUserIdFromToken(token);

        try {
            // 查询订单
            TicketOrder order = ticketOrderMapper.selectById(orderId);
            if (order == null) {
                return Result.error("订单不存在");
            }

            // 验证订单所有权
            if (!order.getUserId().equals(userId)) {
                return Result.error("无权限查看此订单");
            }

            // 查询演出活动
            ShowEvent showEvent = showEventMapper.selectById(order.getShowEventId());

            // 查询订单座位
            LambdaQueryWrapper<OrderSeat> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(OrderSeat::getOrderId, orderId);
            List<OrderSeat> orderSeats = orderSeatMapper.selectList(queryWrapper);
            List<Seat> seats = orderSeats.stream()
                    .map(os -> seatMapper.selectById(os.getSeatId()))
                    .collect(Collectors.toList());

            // 构建返回结果
            TicketOrderVO vo = buildOrderVO(order, showEvent, seats);

            return Result.success(vo);

        } catch (Exception e) {
            log.error("查询订单详情失败: userId={}, orderId={}", userId, orderId, e);
            return Result.error("查询订单详情失败: " + e.getMessage());
        }
    }

    /**
     * 查询我的订单列表
     */
    @Override
    public Result<Page<TicketOrderVO>> queryMyOrders(String token, Integer page, Integer size, String status) {
        // 验证 token
        if (token == null || !jwtUtils.validateToken(token)) {
            return Result.unauthorized("Token无效或已过期");
        }
        Long userId = jwtUtils.getUserIdFromToken(token);

        try {
            Page<TicketOrder> pageParam = new Page<>(page, size);
            LambdaQueryWrapper<TicketOrder> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(TicketOrder::getUserId, userId);

            // 状态筛选
            if (status != null && !status.isEmpty()) {
                queryWrapper.eq(TicketOrder::getStatus, status);
            }

            // 按创建时间倒序
            queryWrapper.orderByDesc(TicketOrder::getCreatedAt);

            Page<TicketOrder> orderPage = ticketOrderMapper.selectPage(pageParam, queryWrapper);

            // 转换为VO
            Page<TicketOrderVO> voPage = new Page<>(orderPage.getCurrent(), orderPage.getSize(), orderPage.getTotal());
            List<TicketOrderVO> voList = orderPage.getRecords().stream().map(order -> {
                ShowEvent showEvent = showEventMapper.selectById(order.getShowEventId());

                // 查询订单座位
                LambdaQueryWrapper<OrderSeat> osQuery = new LambdaQueryWrapper<>();
                osQuery.eq(OrderSeat::getOrderId, order.getId());
                List<OrderSeat> orderSeats = orderSeatMapper.selectList(osQuery);
                List<Seat> seats = orderSeats.stream()
                        .map(os -> seatMapper.selectById(os.getSeatId()))
                        .collect(Collectors.toList());

                return buildOrderVO(order, showEvent, seats);
            }).collect(Collectors.toList());

            voPage.setRecords(voList);

            return Result.success(voPage);

        } catch (Exception e) {
            log.error("查询订单列表失败: userId={}", userId, e);
            return Result.error("查询订单列表失败: " + e.getMessage());
        }
    }


    /**
     * 检查用户限购
     */
    private boolean checkUserPurchaseLimit(Long userId, Long showEventId, Integer requestCount, Integer maxBuyLimit) {
        LambdaQueryWrapper<UserTicketRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserTicketRecord::getUserId, userId)
                .eq(UserTicketRecord::getShowEventId, showEventId);

        UserTicketRecord record = userTicketRecordMapper.selectOne(queryWrapper);
        int currentCount = record == null ? 0 : record.getTicketCount();

        return currentCount + requestCount <= maxBuyLimit;
    }

    /**
     * 更新用户购票记录
     */
    private void updateUserTicketRecord(Long userId, Long showEventId, Integer ticketCount) {
        LambdaQueryWrapper<UserTicketRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserTicketRecord::getUserId, userId)
                .eq(UserTicketRecord::getShowEventId, showEventId);

        UserTicketRecord record = userTicketRecordMapper.selectOne(queryWrapper);
        if (record == null) {
            // 首次购票，创建记录
            record = UserTicketRecord.builder()
                    .userId(userId)
                    .showEventId(showEventId)
                    .ticketCount(ticketCount)
                    .build();
            userTicketRecordMapper.insert(record);
        } else {
            // 增加购票数
            userTicketRecordMapper.increaseTicketCount(userId, showEventId, ticketCount);
        }
    }

    /**
     * 生成订单编号
     */
    private String generateOrderNo(Long userId) {
        return "TK" + System.currentTimeMillis() + String.format("%06d", userId % 1000000);
    }

    /**
     * 构建订单VO
     */
    private TicketOrderVO buildOrderVO(TicketOrder order, ShowEvent showEvent, List<Seat> seats) {
        TicketOrderVO vo = new TicketOrderVO();
        BeanUtils.copyProperties(order, vo);

        if (showEvent != null) {
            vo.setShowName(showEvent.getShowName());
            vo.setVenueName(showEvent.getVenueName());
            vo.setShowTime(showEvent.getShowTime());
        }

        if (seats != null && !seats.isEmpty()) {
            List<SeatVO> seatVOs = seats.stream().map(seat -> {
                SeatVO seatVO = new SeatVO();
                BeanUtils.copyProperties(seat, seatVO);
                return seatVO;
            }).collect(Collectors.toList());
            vo.setSeats(seatVOs);
        }

        return vo;
    }

    /**
     * 清除演出详情缓存
     */
    private void clearShowEventCache(Long showEventId) {
        try {
            String cacheKey = TicketRedisKey.SHOW_DETAIL_PREFIX + showEventId;
            redisService.delete(cacheKey);
            log.debug("清除演出详情缓存: showEventId={}", showEventId);
        } catch (Exception e) {
            log.error("清除演出详情缓存失败: showEventId={}", showEventId, e);
        }
    }

    /**
     * 发送抢票成功事件
     */
    private void sendGrabTicketEvent(TicketOrder order, List<Seat> seats) {
        try {
            Map<String, Object> eventData = new HashMap<>();
            eventData.put("orderId", order.getId());
            eventData.put("orderNo", order.getOrderNo());
            eventData.put("userId", order.getUserId());
            eventData.put("showEventId", order.getShowEventId());
            eventData.put("seatCount", order.getSeatCount());
            eventData.put("totalAmount", order.getTotalAmount());
            eventData.put("seats", seats.stream().map(Seat::getSeatCode).collect(Collectors.toList()));

            MessageEvent event = MessageEvent.builder()
                    .eventType(KafkaTopic.ORDER_CREATE)
                    .source("TicketService")
                    .userId(order.getUserId())
                    .data(JSON.toJSONString(eventData))
                    .timestamp(LocalDateTime.now())
                    .createdAt(LocalDateTime.now())
                    .priority(8) // 高优先级
                    .build();

            kafkaProducerTemplate.sendEvent(KafkaTopic.ORDER_CREATE, event);
            log.debug("发送抢票成功事件: orderNo={}", order.getOrderNo());

        } catch (Exception e) {
            log.error("发送抢票成功事件失败: orderNo={}", order.getOrderNo(), e);
        }
    }

    /**
     * 发送支付订单事件
     */
    private void sendPayOrderEvent(TicketOrder order) {
        try {
            Map<String, Object> eventData = new HashMap<>();
            eventData.put("orderId", order.getId());
            eventData.put("orderNo", order.getOrderNo());
            eventData.put("userId", order.getUserId());
            eventData.put("payType", order.getPayType());
            eventData.put("totalAmount", order.getTotalAmount());

            MessageEvent event = MessageEvent.builder()
                    .eventType(KafkaTopic.ORDER_PAID)
                    .source("TicketService")
                    .userId(order.getUserId())
                    .data(JSON.toJSONString(eventData))
                    .timestamp(LocalDateTime.now())
                    .createdAt(LocalDateTime.now())
                    .priority(9) // 最高优先级
                    .build();

            kafkaProducerTemplate.sendEvent(KafkaTopic.ORDER_PAID, event);
            log.debug("发送支付订单事件: orderNo={}", order.getOrderNo());

        } catch (Exception e) {
            log.error("发送支付订单事件失败: orderNo={}", order.getOrderNo(), e);
        }
    }

    /**
     * 发送取消订单事件
     */
    private void sendCancelOrderEvent(TicketOrder order) {
        try {
            Map<String, Object> eventData = new HashMap<>();
            eventData.put("orderId", order.getId());
            eventData.put("orderNo", order.getOrderNo());
            eventData.put("userId", order.getUserId());
            eventData.put("seatCount", order.getSeatCount());

            MessageEvent event = MessageEvent.builder()
                    .eventType(KafkaTopic.ORDER_CANCEL)
                    .source("TicketService")
                    .userId(order.getUserId())
                    .data(JSON.toJSONString(eventData))
                    .timestamp(LocalDateTime.now())
                    .createdAt(LocalDateTime.now())
                    .priority(7)
                    .build();

            kafkaProducerTemplate.sendEvent(KafkaTopic.ORDER_CANCEL, event);
            log.debug("发送取消订单事件: orderNo={}", order.getOrderNo());

        } catch (Exception e) {
            log.error("发送取消订单事件失败: orderNo={}", order.getOrderNo(), e);
        }
    }
}
