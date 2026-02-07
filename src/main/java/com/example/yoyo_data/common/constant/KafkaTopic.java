package com.example.yoyo_data.common.constant;

/**
 * Kafka主题名称常量
 * 定义系统中使用的所有Kafka主题
 */
public class KafkaTopic {

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

    private KafkaTopic() {
        // 私有构造函数，防止实例化
    }
}
