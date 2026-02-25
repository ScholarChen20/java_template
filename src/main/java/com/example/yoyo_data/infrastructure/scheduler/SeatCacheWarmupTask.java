package com.example.yoyo_data.infrastructure.scheduler;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.yoyo_data.common.constant.SeatStatus;
import com.example.yoyo_data.common.constant.ShowEventStatus;
import com.example.yoyo_data.common.constant.TicketRedisKey;
import com.example.yoyo_data.common.constant.TicketStatus;
import com.example.yoyo_data.common.dto.SeatCacheDTO;
import com.example.yoyo_data.common.entity.Seat;
import com.example.yoyo_data.common.entity.ShowEvent;
import com.example.yoyo_data.infrastructure.cache.RedisService;
import com.example.yoyo_data.infrastructure.repository.SeatMapper;
import com.example.yoyo_data.infrastructure.repository.ShowEventMapper;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 座位缓存预热定时任务
 *
 * 功能：在演出开票前5分钟预热座位缓存到Redis Hash结构
 *
 * Redis Hash 结构：
 * Key: ticket:seat:stock:{showEventId}:{zone}
 * Field: {row}_{col}  （例如：1_5）
 * Value: 座位状态
 *   - "0" = 可售 (AVAILABLE)
 *   - "1" = 已锁定 (LOCKED)
 *   - "2" = 已售出 (SOLD)
 *
 * 执行时机：
 * - 定时任务每分钟执行一次
 * - 检查未来5分钟内开票的演出
 * - 为这些演出预热座位缓存
 *
 * 缓存过期时间：
 * - 演出开始后1小时自动过期
 *
 * @author YoYo Data Team
 * @version 1.0
 * @since 2026-02-10
 */
@Slf4j
@Component
public class SeatCacheWarmupTask {

    @Autowired
    private ShowEventMapper showEventMapper;

    @Autowired
    private SeatMapper seatMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 定时任务：每10分钟执行一次，预热即将开票的演出座位缓存
     * cron表达式：每10分钟的第0秒执行
     */
    @Scheduled(cron = "0 0/10 11,12,13,14,15,16,18 * * ?")
    public void warmupSeatCache() {
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime fiveThirtyMinutesLater = now.plusMinutes(15);

            // 查询未来15分钟内开票的演出
            LambdaQueryWrapper<ShowEvent> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ShowEvent::getStatus, ShowEventStatus.SELLING)
                    .between(ShowEvent::getSaleStartTime, now, fiveThirtyMinutesLater);

            List<ShowEvent> upcomingEvents = showEventMapper.selectList(queryWrapper);

            if (upcomingEvents.isEmpty()) {
                log.debug("【座位缓存预热】未找到需要预热的演出");
                return;
            }

            log.info("【座位缓存预热】开始预热，演出数量={}, events={}",
                    upcomingEvents.size(),
                    upcomingEvents.stream().map(ShowEvent::getId).collect(Collectors.toList()));

            // 为每个演出预热座位缓存
            for (ShowEvent event : upcomingEvents) {
                warmupSeatsForEvent(event);
            }

            log.info("【座位缓存预热】预热完成，演出数量={}", upcomingEvents.size());

        } catch (Exception e) {
            log.error("【座位缓存预热】预热失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 为单个演出预热座位缓存
     *
     * @param event 演出活动
     */
    private void warmupSeatsForEvent(ShowEvent event) {
        long startTime = System.currentTimeMillis();

        try {
            // 检查是否已经预热过
            String checkKey = TicketRedisKey.SEAT_STOCK_PREFIX + event.getId() + ":warmed";
            Boolean isWarmed = stringRedisTemplate.hasKey(checkKey);
            if (isWarmed) {
                log.debug("【座位缓存预热】演出已预热，跳过: showEventId={}", event.getId());
                return;
            }

            // 查询该演出的所有座位
            List<SeatCacheDTO> seats = seatMapper.selectByEventId(event.getId());

            if (seats.isEmpty()) {
                log.warn("【座位缓存预热】演出无座位数据: showEventId={}", event.getId());
                return;
            }

            // 按分区分组座位
            Map<String, List<SeatCacheDTO>> seatsByZone = seats.stream()
                    .collect(Collectors.groupingBy(SeatCacheDTO::getSeatZone));

            int totalSeats = 0;
            for (Map.Entry<String, List<SeatCacheDTO>> entry : seatsByZone.entrySet()) {
                String zone = entry.getKey();
                List<SeatCacheDTO> zoneSeats = entry.getValue();

                String hashKey = TicketRedisKey.SEAT_STOCK_PREFIX + event.getId() + ":" + zone;

                // 构建Hash Field-Value Map
                Map<String, String> seatStatusMap = new HashMap<>();
                for (SeatCacheDTO seat : zoneSeats) {
                    String status = mapSeatStatus(seat.getStatus());
                    seatStatusMap.put(seat.getSeatKey(), status);
                }

                // 批量写入Redis Hash
                stringRedisTemplate.opsForHash().putAll(hashKey, seatStatusMap);

                // 设置过期时间：演出开始后1小时
                long ttlSeconds = calculateTTL(event);
                stringRedisTemplate.expire(hashKey, ttlSeconds, TimeUnit.SECONDS);

                totalSeats += zoneSeats.size();

                log.debug("【座位缓存预热】分区预热完成: showEventId={}, zone={}, seatCount={}",
                        event.getId(), zone, zoneSeats.size());
            }

            // 标记该演出已预热（防止重复预热）
            stringRedisTemplate.opsForValue().set(checkKey, "1",
                    calculateTTL(event), TimeUnit.SECONDS);

            long elapsedTime = System.currentTimeMillis() - startTime;
            log.info("【座位缓存预热】演出预热成功: showEventId={}, showName={}, totalSeats={}, zoneCount={}, 耗时={}ms",
                    event.getId(), event.getShowName(), totalSeats, seatsByZone.size(), elapsedTime);

        } catch (Exception e) {
            log.error("【座位缓存预热】演出预热失败: showEventId={}, error={}",
                    event.getId(), e.getMessage(), e);
        }
    }

    /**
     * 映射座位状态到Redis值
     *
     * @param status 座位状态
     * @return Redis中的状态值
     */
    private String mapSeatStatus(String status) {
        switch (status) {
            case SeatStatus.AVAILABLE:
                return "0";  // 可售
            case SeatStatus.LOCKED:
                return "1";  // 已锁定
            case SeatStatus.SOLD:
                return "2";  // 已售出
            default:
                return "2";  // 默认为已售出（安全起见）
        }
    }

    /**
     * 计算缓存过期时间（秒）
     * 演出开始后1小时自动过期
     *
     * @param event 演出活动
     * @return TTL秒数
     */
    private long calculateTTL(ShowEvent event) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expireTime = event.getSaleEndTime().plusHours(1);

        long secondsUntilExpire = java.time.Duration.between(now, expireTime).getSeconds();

        // 最少保留1小时，最多保留7天
        if (secondsUntilExpire < 3600) {
            return 3600;  // 1小时
        } else if (secondsUntilExpire > 604800) {
            return 604800;  // 7天
        } else {
            return secondsUntilExpire;
        }
    }

    /**
     * 手动触发预热（用于测试或紧急情况）
     *
     * @param showEventId 演出活动ID
     */
    public void manualWarmup(Long showEventId) {
        log.info("【座位缓存预热】手动触发预热: showEventId={}", showEventId);

        ShowEvent event = showEventMapper.selectById(showEventId);
        if (event == null) {
            log.warn("【座位缓存预热】演出不存在: showEventId={}", showEventId);
            return;
        }

        warmupSeatsForEvent(event);
    }

    /**
     * 清除演出的座位缓存（用于取消预热或重新预热）
     *
     * @param showEventId 演出活动ID
     */
    public void clearWarmupCache(Long showEventId) {
        try {
            // 删除所有分区的Hash Key
            // ticket:seat:stock:{showEventId}:*
            String pattern = TicketRedisKey.SEAT_STOCK_PREFIX + showEventId + ":*";

            // 注意：在生产环境中，使用SCAN代替KEYS命令
            Set<String> keys = stringRedisTemplate.keys(pattern);
            if (!keys.isEmpty()) {
                stringRedisTemplate.delete(keys);
                log.info("【座位缓存清理】清理成功: showEventId={}, keyCount={}", showEventId, keys.size());
            }
        } catch (Exception e) {
            log.error("【座位缓存清理】清理失败: showEventId={}, error={}", showEventId, e.getMessage(), e);
        }
    }
}
