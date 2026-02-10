package com.example.yoyo_data.common.constant;

/**
 * Kafka主题名称常量
 * 定义系统中使用的所有Kafka主题
 */
public class KafkaTopic {
    // ==================== 评论相关主题 ====================
    /**
     * 评论删除事件主题
     */
    public static final String COMMENT_DELETE = "comment-delete";
    /**
     * 评论创建事件主题
     */
    public static final String COMMENT_CREATE = "comment-create";

    // ==================== 旅行计划相关主题 ====================

    /**
     * 旅行计划创建事件主题
     */
    public static final String TRAVEL_PLAN_CREATED = "travel-plan-created";

    /**
     * 旅行计划更新事件主题
     */
    public static final String TRAVEL_PLAN_UPDATED = "travel-plan-updated";

    /**
     * 旅行计划删除事件主题
     */
    public static final String TRAVEL_PLAN_DELETED = "travel-plan-deleted";

    /**
     * 旅行计划缓存同步主题
     */
    public static final String TRAVEL_PLAN_CACHE_SYNC = "travel-plan-cache-sync";

    // ==================== 对话相关主题 ====================

    /**
     * 对话创建事件主题
     */
    public static final String DIALOG_CREATED = "dialog-created";

    /**
     * 消息发送事件主题
     */
    public static final String MESSAGE_SENT = "message-sent";

    /**
     * 消息状态更新主题
     */
    public static final String MESSAGE_STATUS_UPDATED = "message-status-updated";

    /**
     * 对话归档主题
     */
    public static final String DIALOG_ARCHIVED = "dialog-archived";

    /**
     * 对话缓存同步主题
     */
    public static final String DIALOG_CACHE_SYNC = "dialog-cache-sync";

    // ==================== 系统相关主题 ====================

    /**
     * 系统健康检查主题
     */
    public static final String SYSTEM_HEALTH_CHECK = "system-health-check";

    /**
     * 审计日志主题
     */
    public static final String AUDIT_LOG = "audit-log";

    /**
     * 缓存失效通知主题
     */
    public static final String CACHE_INVALIDATION = "cache-invalidation";

    /**
     * 数据同步主题
     */
    public static final String DATA_SYNC = "data-sync";

    // ==================== 通知相关主题 ====================

    /**
     * 用户通知主题
     */
    public static final String USER_NOTIFICATION = "user-notification";

    /**
     * 系统通知主题
     */
    public static final String SYSTEM_NOTIFICATION = "system-notification";

    // ==================== 订单相关主题 ====================

    /**
     * 订单创建事件主题
     */
    public static final String ORDER_CREATE = "order-create";

    /**
     * 订单支付成功事件主题
     */
    public static final String ORDER_PAID = "order-paid";

    /**
     * 订单支付后置处理事件主题（异步处理座位确认、演出统计、缓存清理）
     */
    public static final String ORDER_PAID_POST_PROCESS = "order-paid-post-process";

    /**
     * 订单取消事件主题
     */
    public static final String ORDER_CANCEL = "order-cancel";

    /**
     * 订单超时事件主题
     */
    public static final String ORDER_TIMEOUT = "order-timeout";

    // ==================== 演出活动提醒相关主题 ====================

    /**
     * 演出活动开票前1天提醒主题
     */
    public static final String SHOW_REMIND_ONE_DAY = "show-remind-one-day";

    /**
     * 演出活动开票前1小时提醒主题
     */
    public static final String SHOW_REMIND_ONE_HOUR = "show-remind-one-hour";

    /**
     * 演出活动开票前5分钟提醒主题
     */
    public static final String SHOW_REMIND_FIVE_MINUTES = "show-remind-five-minutes";

    // ==================== 私有构造函数 ====================

    private KafkaTopic() {
        // 私有构造函数，防止实例化
    }
}
