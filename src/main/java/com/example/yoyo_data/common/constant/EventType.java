package com.example.yoyo_data.common.constant;

/**
 * 事件类型常量
 * 定义系统中的所有事件类型
 */
public class EventType {

    // ==================== 旅行计划事件 ====================

    /**
     * 旅行计划创建
     */
    public static final String TRAVEL_PLAN_CREATE = "TRAVEL_PLAN_CREATE";

    /**
     * 旅行计划更新
     */
    public static final String TRAVEL_PLAN_UPDATE = "TRAVEL_PLAN_UPDATE";

    /**
     * 旅行计划删除
     */
    public static final String TRAVEL_PLAN_DELETE = "TRAVEL_PLAN_DELETE";

    /**
     * 旅行计划查看
     */
    public static final String TRAVEL_PLAN_VIEW = "TRAVEL_PLAN_VIEW";

    // ==================== 对话事件 ====================

    /**
     * 对话创建
     */
    public static final String DIALOG_CREATE = "DIALOG_CREATE";

    /**
     * 消息发送
     */
    public static final String MESSAGE_SEND = "MESSAGE_SEND";

    /**
     * 消息接收
     */
    public static final String MESSAGE_RECEIVE = "MESSAGE_RECEIVE";

    /**
     * 消息已读
     */
    public static final String MESSAGE_READ = "MESSAGE_READ";

    /**
     * 对话归档
     */
    public static final String DIALOG_ARCHIVE = "DIALOG_ARCHIVE";

    /**
     * 对话取消归档
     */
    public static final String DIALOG_UNARCHIVE = "DIALOG_UNARCHIVE";

    // ==================== 缓存事件 ====================

    /**
     * 缓存更新
     */
    public static final String CACHE_UPDATE = "CACHE_UPDATE";

    /**
     * 缓存失效
     */
    public static final String CACHE_INVALIDATE = "CACHE_INVALIDATE";

    /**
     * 缓存刷新
     */
    public static final String CACHE_REFRESH = "CACHE_REFRESH";

    /**
     * 缓存清空
     */
    public static final String CACHE_CLEAR = "CACHE_CLEAR";

    // ==================== 系统事件 ====================

    /**
     * 系统启动
     */
    public static final String SYSTEM_START = "SYSTEM_START";

    /**
     * 系统关闭
     */
    public static final String SYSTEM_SHUTDOWN = "SYSTEM_SHUTDOWN";

    /**
     * 健康检查
     */
    public static final String HEALTH_CHECK = "HEALTH_CHECK";

    /**
     * 审计日志
     */
    public static final String AUDIT_LOG = "AUDIT_LOG";

    // ==================== 数据同步事件 ====================

    /**
     * 数据库到缓存同步
     */
    public static final String DB_TO_CACHE_SYNC = "DB_TO_CACHE_SYNC";

    /**
     * 缓存到数据库同步
     */
    public static final String CACHE_TO_DB_SYNC = "CACHE_TO_DB_SYNC";

    /**
     * 数据一致性检查
     */
    public static final String DATA_CONSISTENCY_CHECK = "DATA_CONSISTENCY_CHECK";

    // ==================== 通知事件 ====================

    /**
     * 用户通知
     */
    public static final String USER_NOTIFY = "USER_NOTIFY";

    /**
     * 系统通知
     */
    public static final String SYSTEM_NOTIFY = "SYSTEM_NOTIFY";

    private EventType() {
        // 私有构造函数，防止实例化
    }
}
