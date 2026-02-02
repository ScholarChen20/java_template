package com.example.yoyo_data.infrastructure.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * 日期时间工具类 - 提供日期时间操作的便利方法
 * 支持LocalDate、LocalDateTime等Java 8 Time API
 *
 * @author Template Framework
 * @version 1.0
 */
public class DateTimeUtils {

    /**
     * 日期格式: yyyy-MM-dd
     */
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * 日期时间格式: yyyy-MM-dd HH:mm:ss
     */
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 时间格式: HH:mm:ss
     */
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    /**
     * 获取当前日期
     *
     * @return 当前日期
     */
    public static LocalDate getCurrentDate() {
        return LocalDate.now();
    }

    /**
     * 获取当前日期时间
     *
     * @return 当前日期时间
     */
    public static LocalDateTime getCurrentDateTime() {
        return LocalDateTime.now();
    }

    /**
     * 获取当前时间
     *
     * @return 当前时间
     */
    public static LocalTime getCurrentTime() {
        return LocalTime.now();
    }

    /**
     * 将LocalDate格式化为字符串
     *
     * @param date 日期
     * @return 格式化后的字符串 (yyyy-MM-dd)
     */
    public static String formatDate(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.format(DATE_FORMATTER);
    }

    /**
     * 将LocalDateTime格式化为字符串
     *
     * @param dateTime 日期时间
     * @return 格式化后的字符串 (yyyy-MM-dd HH:mm:ss)
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(DATETIME_FORMATTER);
    }

    /**
     * 将LocalTime格式化为字符串
     *
     * @param time 时间
     * @return 格式化后的字符串 (HH:mm:ss)
     */
    public static String formatTime(LocalTime time) {
        if (time == null) {
            return null;
        }
        return time.format(TIME_FORMATTER);
    }

    /**
     * 解析日期字符串
     *
     * @param dateStr 日期字符串 (yyyy-MM-dd)
     * @return LocalDate对象
     */
    public static LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }
        return LocalDate.parse(dateStr, DATE_FORMATTER);
    }

    /**
     * 解析日期时间字符串
     *
     * @param dateTimeStr 日期时间字符串 (yyyy-MM-dd HH:mm:ss)
     * @return LocalDateTime对象
     */
    public static LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isEmpty()) {
            return null;
        }
        return LocalDateTime.parse(dateTimeStr, DATETIME_FORMATTER);
    }

    /**
     * 解析时间字符串
     *
     * @param timeStr 时间字符串 (HH:mm:ss)
     * @return LocalTime对象
     */
    public static LocalTime parseTime(String timeStr) {
        if (timeStr == null || timeStr.isEmpty()) {
            return null;
        }
        return LocalTime.parse(timeStr, TIME_FORMATTER);
    }

    /**
     * 检查日期是否是今天
     *
     * @param date 日期
     * @return true: 是今天, false: 不是
     */
    public static boolean isToday(LocalDate date) {
        if (date == null) {
            return false;
        }
        return date.equals(LocalDate.now());
    }

    /**
     * 检查日期是否是昨天
     *
     * @param date 日期
     * @return true: 是昨天, false: 不是
     */
    public static boolean isYesterday(LocalDate date) {
        if (date == null) {
            return false;
        }
        return date.equals(LocalDate.now().minusDays(1));
    }

    /**
     * 计算两个日期之间的天数
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 天数差
     */
    public static long daysBetween(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            return 0;
        }
        return ChronoUnit.DAYS.between(startDate, endDate);
    }

    /**
     * 计算两个日期时间之间的毫秒数
     *
     * @param startDateTime 开始日期时间
     * @param endDateTime 结束日期时间
     * @return 毫秒数
     */
    public static long millisBetween(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        if (startDateTime == null || endDateTime == null) {
            return 0;
        }
        return ChronoUnit.MILLIS.between(startDateTime, endDateTime);
    }

    /**
     * 获取当月的第一天
     *
     * @return 当月第一天
     */
    public static LocalDate getFirstDayOfMonth() {
        return LocalDate.now().withDayOfMonth(1);
    }

    /**
     * 获取当月的最后一天
     *
     * @return 当月最后一天
     */
    public static LocalDate getLastDayOfMonth() {
        return LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());
    }

    /**
     * 获取指定日期所在月份的第一天
     *
     * @param date 指定日期
     * @return 所在月份的第一天
     */
    public static LocalDate getFirstDayOfMonth(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.withDayOfMonth(1);
    }

    /**
     * 获取指定日期所在月份的最后一天
     *
     * @param date 指定日期
     * @return 所在月份的最后一天
     */
    public static LocalDate getLastDayOfMonth(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.withDayOfMonth(date.lengthOfMonth());
    }

    /**
     * 将LocalDate转换为LocalDateTime（时间设为00:00:00）
     *
     * @param date 日期
     * @return 转换后的LocalDateTime
     */
    public static LocalDateTime toDateTime(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.atStartOfDay();
    }

    /**
     * 将LocalDate转换为LocalDateTime（时间设为23:59:59）
     *
     * @param date 日期
     * @return 转换后的LocalDateTime
     */
    public static LocalDateTime toEndOfDay(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.atTime(LocalTime.MAX);
    }

    /**
     * 检查日期是否是闰年
     *
     * @param date 日期
     * @return true: 是闰年, false: 不是
     */
    public static boolean isLeapYear(LocalDate date) {
        if (date == null) {
            return false;
        }
        return date.isLeapYear();
    }
}
