package com.example.yoyo_data.infrastructure.scheduler;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.yoyo_data.common.constant.KafkaTopic;
import com.example.yoyo_data.common.constant.ShowEventStatus;
import com.example.yoyo_data.common.constant.TicketRedisKey;
import com.example.yoyo_data.common.dto.ShowEventReminderEvent;
import com.example.yoyo_data.common.entity.ShowEvent;
import com.example.yoyo_data.infrastructure.cache.RedisService;
import com.example.yoyo_data.infrastructure.message.KafkaProducerTemplate;
import com.example.yoyo_data.infrastructure.message.MessageEvent;
import com.example.yoyo_data.infrastructure.repository.ShowEventMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.example.yoyo_data.infrastructure.cache.CacheKeyManager.CacheTTL.TWO_DAYS;

/**
 * 演出活动提醒定时扫描器
 * 在开票前特定时间点（1天、1小时、5分钟）发送提醒消息
 *
 * 工作原理：
 * 1. 定时扫描即将开票的演出活动
 * 2. 检查距离开票时间是否到达提醒时间点（1天、1小时、5分钟）
 * 3. 使用 Redis 记录已发送的提醒，避免重复发送
 * 4. 发送提醒事件到 Kafka，由消费者处理通知逻辑
 *
 * @author YoYo Data Team
 * @version 1.0
 * @since 2026-02-09
 */
@Slf4j
@Component
public class ShowEventReminderScheduler {

    @Autowired
    private ShowEventMapper showEventMapper;

    @Autowired
    private RedisService redisService;

    @Autowired
    private KafkaProducerTemplate kafkaProducerTemplate;

    /**
     * 定时扫描演出活动，发送开票提醒
     * 每分钟执行一次（可根据实际需求调整）
     */
    @Scheduled(cron = "0 * * * * ?") // 每分钟的第0秒执行
    public void scanShowEventsForReminders() {
        try {
            log.debug("【演出提醒扫描】开始扫描演出活动...");

            LocalDateTime now = LocalDateTime.now();

            // 查询即将开票的演出活动
            // 查询范围：当前时间 到 未来25小时内开票的演出（覆盖1天提醒的时间范围）
            LocalDateTime startTime = now;
            LocalDateTime endTime = now.plusHours(25);

            LambdaQueryWrapper<ShowEvent> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ShowEvent::getStatus, ShowEventStatus.PENDING) // 只查询待开票状态
                    .ge(ShowEvent::getSaleStartTime, startTime) // 开票时间 >= 当前时间
                    .le(ShowEvent::getSaleStartTime, endTime) // 开票时间 <= 未来25小时
                    .orderByAsc(ShowEvent::getSaleStartTime);

            List<ShowEvent> upcomingShows = showEventMapper.selectList(queryWrapper);

            if (upcomingShows.isEmpty()) {
                log.debug("【演出提醒扫描】未找到即将开票的演出活动");
                return;
            }

            log.info("【演出提醒扫描】找到 {} 个即将开票的演出活动", upcomingShows.size());

            // 检查每个演出的提醒时间点
            for (ShowEvent show : upcomingShows) {
                checkAndSendReminders(show, now);
            }

            log.debug("【演出提醒扫描】扫描完成");

        } catch (Exception e) {
            log.error("【演出提醒扫描异常】error={}", e.getMessage(), e);
        }
    }

    /**
     * 检查并发送演出提醒
     *
     * @param show 演出活动
     * @param now  当前时间
     */
    private void checkAndSendReminders(ShowEvent show, LocalDateTime now) {
        LocalDateTime saleStartTime = show.getSaleStartTime();

        // 计算距离开票的时间差（毫秒）
        long timeUntilSale = ChronoUnit.MILLIS.between(now, saleStartTime);

        // 如果开票时间已过，跳过
        if (timeUntilSale <= 0) {
            return;
        }

        // 检查各个提醒时间点
        for (ShowEventReminderEvent.ReminderType reminderType : ShowEventReminderEvent.ReminderType.values()) {
            checkAndSendReminderForType(show, now, timeUntilSale, reminderType);
        }
    }

    /**
     * 检查并发送特定类型的提醒
     *
     * @param show         演出活动
     * @param now          当前时间
     * @param timeUntilSale 距离开票时间（毫秒）
     * @param reminderType 提醒类型
     */
    private void checkAndSendReminderForType(ShowEvent show, LocalDateTime now,
                                             long timeUntilSale, ShowEventReminderEvent.ReminderType reminderType) {
        long advanceTime = reminderType.getAdvanceTime();

        // 判断是否到达提醒时间点
        // 允许一定的时间误差（±2分钟），避免因扫描间隔错过提醒时间
        long timeDifference = Math.abs(timeUntilSale - advanceTime);
        long toleranceMillis = 2 * 60 * 1000L; // 2分钟容差

        if (timeDifference > toleranceMillis) {
            // 未到提醒时间点或已错过提醒时间点
            return;
        }

        // 检查是否已发送过该提醒
        String reminderKey = buildReminderKey(show.getId(), reminderType);
        Boolean alreadySent = redisService.exists(reminderKey);

        if (Boolean.TRUE.equals(alreadySent)) {
            log.debug("【演出提醒】已发送过: showEventId={}, reminderType={}", show.getId(), reminderType);
            return;
        }

        // 发送提醒
        boolean sent = sendReminder(show, now, timeUntilSale, reminderType);

        if (sent) {
            // 标记为已发送（过期时间设置为2天，确保不会重复发送）
            redisService.stringSetString(reminderKey, "1", TWO_DAYS);
            log.info("【演出提醒发送成功】showEventId={}, showName={}, reminderType={}",
                    show.getId(), show.getShowName(), reminderType.getDescription());
        }
    }

    /**
     * 发送演出提醒
     *
     * @param show         演出活动
     * @param now          当前时间
     * @param timeUntilSale 距离开票时间（毫秒）
     * @param reminderType 提醒类型
     * @return 是否发送成功
     */
    private boolean sendReminder(ShowEvent show, LocalDateTime now,
                                 long timeUntilSale, ShowEventReminderEvent.ReminderType reminderType) {
        try {
            // 构建提醒事件
            ShowEventReminderEvent reminderEvent = ShowEventReminderEvent.builder()
                    .showEventId(show.getId())
                    .showName(show.getShowName())
                    .showType(show.getShowType())
                    .venueName(show.getVenueName())
                    .showTime(show.getShowTime())
                    .saleStartTime(show.getSaleStartTime())
                    .saleEndTime(show.getSaleEndTime())
                    .availableSeats(show.getAvailableSeats())
                    .totalSeats(show.getTotalSeats())
                    .posterUrl(show.getPosterUrl())
                    .description(show.getDescription())
                    .reminderType(reminderType)
                    .timeUntilSale(timeUntilSale)
                    .reminderTime(now)
                    .build();

            // 根据提醒类型选择 Kafka Topic
            String topic = getTopicByReminderType(reminderType);

            // 构建 Kafka 消息事件
            MessageEvent messageEvent = MessageEvent.builder()
                    .eventType(topic)
                    .source("ShowEventReminderScheduler")
                    .data(JSON.toJSONString(reminderEvent))
                    .timestamp(now)
                    .createdAt(now)
                    .priority(9) // 高优先级
                    .build();

            // 发送到 Kafka
            kafkaProducerTemplate.sendEvent(topic, messageEvent);

            log.info("【演出提醒事件发送】showEventId={}, showName={}, reminderType={}, topic={}",
                    show.getId(), show.getShowName(), reminderType.getDescription(), topic);

            return true;

        } catch (Exception e) {
            log.error("【演出提醒发送失败】showEventId={}, reminderType={}, error={}",
                    show.getId(), reminderType, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 构建提醒记录的 Redis Key
     *
     * @param showEventId  演出活动ID
     * @param reminderType 提醒类型
     * @return Redis Key
     */
    private String buildReminderKey(Long showEventId, ShowEventReminderEvent.ReminderType reminderType) {
        String typeKey;
        switch (reminderType) {
            case ONE_DAY_BEFORE:
                typeKey = "1day";
                break;
            case ONE_HOUR_BEFORE:
                typeKey = "1hour";
                break;
            case FIVE_MINUTES_BEFORE:
                typeKey = "5min";
                break;
            default:
                typeKey = "unknown";
        }
        return TicketRedisKey.SHOW_REMIND_PREFIX + showEventId + ":" + typeKey;
    }

    /**
     * 根据提醒类型获取 Kafka Topic
     *
     * @param reminderType 提醒类型
     * @return Kafka Topic
     */
    private String getTopicByReminderType(ShowEventReminderEvent.ReminderType reminderType) {
        switch (reminderType) {
            case ONE_DAY_BEFORE:
                return KafkaTopic.SHOW_REMIND_ONE_DAY;
            case ONE_HOUR_BEFORE:
                return KafkaTopic.SHOW_REMIND_ONE_HOUR;
            case FIVE_MINUTES_BEFORE:
                return KafkaTopic.SHOW_REMIND_FIVE_MINUTES;
            default:
                return KafkaTopic.SHOW_REMIND_ONE_HOUR;
        }
    }

    /**
     * 手动触发演出提醒扫描（用于测试）
     *
     * @return 扫描结果
     */
    public String manualTriggerScan() {
        log.info("【手动触发】演出提醒扫描");
        scanShowEventsForReminders();
        return "演出提醒扫描已触发";
    }

    /**
     * 清除特定演出的提醒记录（用于测试）
     *
     * @param showEventId 演出活动ID
     * @return 清除结果
     */
    public String clearReminderRecords(Long showEventId) {
        log.info("【清除提醒记录】showEventId={}", showEventId);

        int count = 0;
        for (ShowEventReminderEvent.ReminderType reminderType : ShowEventReminderEvent.ReminderType.values()) {
            String key = buildReminderKey(showEventId, reminderType);
            redisService.delete(key);
            count++;
        }

        return String.format("已清除 %d 条提醒记录", count);
    }
}
