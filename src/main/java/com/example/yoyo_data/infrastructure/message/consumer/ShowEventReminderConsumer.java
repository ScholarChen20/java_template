package com.example.yoyo_data.infrastructure.message.consumer;

import com.alibaba.fastjson.JSON;
import com.example.yoyo_data.common.constant.KafkaTopic;
import com.example.yoyo_data.infrastructure.message.MessageEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 演出活动提醒事件消费者
 * 处理演出活动开票前的提醒通知：1天、1小时、5分钟
 *
 * @author YoYo Data Team
 * @version 1.0
 * @since 2026-02-10
 */
@Slf4j
@Component
public class ShowEventReminderConsumer {

    /**
     * 消费开票前1天提醒事件
     *
     * @param message 消息内容
     */
    @KafkaListener(topics = KafkaTopic.SHOW_REMIND_ONE_DAY, groupId = "show-remind-one-day-group")
    public void handleOneDayReminder(String message) {
        log.info("【开票前1天提醒】收到消息: {}", message);

        try {
            MessageEvent messageEvent = JSON.parseObject(message, MessageEvent.class);
            Map<String, Object> eventData = JSON.parseObject(messageEvent.getData(), Map.class);

            Long showEventId = Long.valueOf(eventData.get("showEventId").toString());
            String showName = eventData.get("showName").toString();

            log.info("【开票前1天提醒-处理开始】showEventId={}, showName={}", showEventId, showName);

            // 处理提醒业务
            processReminder(showEventId, showName, eventData, "1天");

            log.info("【开票前1天提醒-处理成功】showEventId={}, showName={}", showEventId, showName);

        } catch (Exception e) {
            log.error("【开票前1天提醒-处理失败】message={}, error={}",
                    message, e.getMessage(), e);
        }
    }

    /**
     * 消费开票前1小时提醒事件
     *
     * @param message 消息内容
     */
    @KafkaListener(topics = KafkaTopic.SHOW_REMIND_ONE_HOUR, groupId = "show-remind-one-hour-group")
    public void handleOneHourReminder(String message) {
        log.info("【开票前1小时提醒】收到消息: {}", message);

        try {
            MessageEvent messageEvent = JSON.parseObject(message, MessageEvent.class);
            Map<String, Object> eventData = JSON.parseObject(messageEvent.getData(), Map.class);

            Long showEventId = Long.valueOf(eventData.get("showEventId").toString());
            String showName = eventData.get("showName").toString();

            log.info("【开票前1小时提醒-处理开始】showEventId={}, showName={}", showEventId, showName);

            // 处理提醒业务
            processReminder(showEventId, showName, eventData, "1小时");

            log.info("【开票前1小时提醒-处理成功】showEventId={}, showName={}", showEventId, showName);

        } catch (Exception e) {
            log.error("【开票前1小时提醒-处理失败】message={}, error={}",
                    message, e.getMessage(), e);
        }
    }

    /**
     * 消费开票前5分钟提醒事件
     *
     * @param message 消息内容
     */
    @KafkaListener(topics = KafkaTopic.SHOW_REMIND_FIVE_MINUTES, groupId = "show-remind-five-minutes-group")
    public void handleFiveMinutesReminder(String message) {
        log.info("【开票前5分钟提醒】收到消息: {}", message);

        try {
            MessageEvent messageEvent = JSON.parseObject(message, MessageEvent.class);
            Map<String, Object> eventData = JSON.parseObject(messageEvent.getData(), Map.class);

            Long showEventId = Long.valueOf(eventData.get("showEventId").toString());
            String showName = eventData.get("showName").toString();

            log.info("【开票前5分钟提醒-处理开始】showEventId={}, showName={}", showEventId, showName);

            // 处理提醒业务
            processReminder(showEventId, showName, eventData, "5分钟");

            log.info("【开票前5分钟提醒-处理成功】showEventId={}, showName={}", showEventId, showName);

        } catch (Exception e) {
            log.error("【开票前5分钟提醒-处理失败】message={}, error={}",
                    message, e.getMessage(), e);
        }
    }

    /**
     * 处理提醒业务（通用方法）
     *
     * @param showEventId 演出活动ID
     * @param showName 演出名称
     * @param eventData 事件数据
     * @param reminderType 提醒类型（1天/1小时/5分钟）
     */
    private void processReminder(Long showEventId, String showName, Map<String, Object> eventData, String reminderType) {
        // 1. 查询关注该演出的用户
        sendReminderToFollowers(showEventId, showName, eventData, reminderType);

        // 2. 发送全站推送（首页推荐位）
        sendGlobalPushNotification(showEventId, showName, eventData, reminderType);

        // 3. 记录提醒日志
        recordReminderLog(showEventId, showName, reminderType);
    }

    /**
     * 发送提醒给关注用户
     *
     * @param showEventId 演出活动ID
     * @param showName 演出名称
     * @param eventData 事件数据
     * @param reminderType 提醒类型
     */
    private void sendReminderToFollowers(Long showEventId, String showName, Map<String, Object> eventData, String reminderType) {
        try {
            // TODO: 查询关注该演出的用户列表
            log.info("【用户提醒】演出={}, 提醒类型=开票前{}", showName, reminderType);

            String saleStartTime = eventData.get("saleStartTime").toString();
            String venueName = eventData.get("venueName").toString();

            // 示例：查询关注用户
            // List<Long> followerIds = showFollowMapper.selectFollowersByShowId(showEventId);
            // for (Long userId : followerIds) {
            //     // 发送短信
            //     String phone = userMapper.selectById(userId).getPhone();
            //     sendSmsReminder(phone, showName, saleStartTime, reminderType);
            //
            //     // 发送推送
            //     sendPushReminder(userId, showName, saleStartTime, reminderType);
            // }

            // 通知内容根据提醒类型不同
            String notificationContent = buildNotificationContent(showName, venueName, saleStartTime, reminderType);
            log.info("【通知内容】{}", notificationContent);

        } catch (Exception e) {
            log.warn("【用户提醒失败】showEventId={}, error={}", showEventId, e.getMessage());
        }
    }

    /**
     * 构建通知内容
     *
     * @param showName 演出名称
     * @param venueName 场馆名称
     * @param saleStartTime 开票时间
     * @param reminderType 提醒类型
     * @return 通知内容
     */
    private String buildNotificationContent(String showName, String venueName, String saleStartTime, String reminderType) {
        switch (reminderType) {
            case "1天":
                return String.format("【开票提醒】您关注的演出《%s》将于明天%s开票！地点：%s。手速要快哦～",
                        showName, saleStartTime, venueName);

            case "1小时":
                return String.format("【开票倒计时】您关注的演出《%s》将于1小时后（%s）开票！地点：%s。准备好抢票啦！",
                        showName, saleStartTime, venueName);

            case "5分钟":
                return String.format("【即将开票】您关注的演出《%s》将于5分钟后（%s）开票！地点：%s。立即前往抢票！",
                        showName, saleStartTime, venueName);

            default:
                return String.format("【开票提醒】您关注的演出《%s》即将开票，开票时间：%s，地点：%s",
                        showName, saleStartTime, venueName);
        }
    }

    /**
     * 发送全站推送通知（首页推荐位）
     *
     * @param showEventId 演出活动ID
     * @param showName 演出名称
     * @param eventData 事件数据
     * @param reminderType 提醒类型
     */
    private void sendGlobalPushNotification(Long showEventId, String showName, Map<String, Object> eventData, String reminderType) {
        try {
            // TODO: 更新首页推荐位/热门演出列表
            log.info("【全站推送】演出={}, 提醒类型=开票前{}", showName, reminderType);

            // 示例：更新首页推荐
            // if ("5分钟".equals(reminderType)) {
            //     // 开票前5分钟，置顶到首页推荐位
            //     homeRecommendMapper.addTopRecommend(showEventId, "即将开票");
            // }

            // 示例：发送全站推送
            // pushService.sendGlobalPush(
            //     "热门演出即将开票",
            //     "《" + showName + "》即将开票，不要错过！"
            // );

        } catch (Exception e) {
            log.warn("【全站推送失败】showEventId={}, error={}", showEventId, e.getMessage());
        }
    }

    /**
     * 记录提醒日志
     *
     * @param showEventId 演出活动ID
     * @param showName 演出名称
     * @param reminderType 提醒类型
     */
    private void recordReminderLog(Long showEventId, String showName, String reminderType) {
        try {
            // TODO: 写入提醒日志表或发送到数据分析系统
            log.info("【提醒日志】showEventId={}, showName={}, reminderType=开票前{}",
                    showEventId, showName, reminderType);

            // 示例：写入提醒日志表
            // showReminderLogMapper.insert(ShowReminderLog.builder()
            //         .showEventId(showEventId)
            //         .showName(showName)
            //         .reminderType(reminderType)
            //         .reminderTime(LocalDateTime.now())
            //         .build());

            // 示例：发送到数据分析系统
            // dataAnalysisProducer.send("show-reminder-analysis", Map.of(
            //     "showEventId", showEventId,
            //     "showName", showName,
            //     "reminderType", reminderType,
            //     "timestamp", System.currentTimeMillis()
            // ));

        } catch (Exception e) {
            log.warn("【提醒日志记录失败】showEventId={}, error={}", showEventId, e.getMessage());
        }
    }
}
