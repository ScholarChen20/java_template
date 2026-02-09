package com.example.yoyo_data.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 演出活动提醒事件DTO
 * 用于在开票前特定时间点发送提醒通知
 *
 * @author YoYo Data Team
 * @version 1.0
 * @since 2026-02-09
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShowEventReminderEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 演出活动ID
     */
    private Long showEventId;

    /**
     * 演出名称
     */
    private String showName;

    /**
     * 演出类型
     */
    private String showType;

    /**
     * 场馆名称
     */
    private String venueName;

    /**
     * 演出时间
     */
    private LocalDateTime showTime;

    /**
     * 开票时间
     */
    private LocalDateTime saleStartTime;

    /**
     * 结束售票时间
     */
    private LocalDateTime saleEndTime;

    /**
     * 可售座位数
     */
    private Integer availableSeats;

    /**
     * 总座位数
     */
    private Integer totalSeats;

    /**
     * 海报URL
     */
    private String posterUrl;

    /**
     * 演出介绍
     */
    private String description;

    /**
     * 提醒类型
     */
    private ReminderType reminderType;

    /**
     * 距离开票剩余时间（毫秒）
     */
    private Long timeUntilSale;

    /**
     * 提醒发送时间
     */
    private LocalDateTime reminderTime;

    /**
     * 提醒类型枚举
     */
    public enum ReminderType {
        /**
         * 开票前1天
         */
        ONE_DAY_BEFORE("1天前", 24 * 60 * 60 * 1000L),

        /**
         * 开票前1小时
         */
        ONE_HOUR_BEFORE("1小时前", 60 * 60 * 1000L),

        /**
         * 开票前5分钟
         */
        FIVE_MINUTES_BEFORE("5分钟前", 5 * 60 * 1000L);

        /**
         * 类型描述
         */
        private final String description;

        /**
         * 提前时间（毫秒）
         */
        private final Long advanceTime;

        ReminderType(String description, Long advanceTime) {
            this.description = description;
            this.advanceTime = advanceTime;
        }

        public String getDescription() {
            return description;
        }

        public Long getAdvanceTime() {
            return advanceTime;
        }
    }

    /**
     * 获取提醒消息内容
     *
     * @return 提醒消息
     */
    public String getReminderMessage() {
        switch (reminderType) {
            case ONE_DAY_BEFORE:
                return String.format("【开票提醒】%s 将在明天 %s 开始售票，请提前做好准备！",
                        showName, formatTime(saleStartTime));
            case ONE_HOUR_BEFORE:
                return String.format("【开票提醒】%s 将在1小时后 %s 开始售票，请准备好抢票！",
                        showName, formatTime(saleStartTime));
            case FIVE_MINUTES_BEFORE:
                return String.format("【开票提醒】%s 即将在5分钟后 %s 开始售票，请立即进入抢票页面！",
                        showName, formatTime(saleStartTime));
            default:
                return String.format("【开票提醒】%s 即将开始售票", showName);
        }
    }

    /**
     * 格式化时间
     *
     * @param time 时间
     * @return 格式化后的时间字符串
     */
    private String formatTime(LocalDateTime time) {
        return time.format(java.time.format.DateTimeFormatter.ofPattern("MM-dd HH:mm"));
    }
}
