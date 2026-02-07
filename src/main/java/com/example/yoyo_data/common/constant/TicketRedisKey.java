package com.example.yoyo_data.common.constant;

/**
 * 票务系统 Redis Key 常量
 */
public class TicketRedisKey {

    /**
     * 座位库存缓存前缀：ticket:seat:stock:{showEventId}
     */
    public static final String SEAT_STOCK_PREFIX = "ticket:seat:stock:";

    /**
     * 座位锁定缓存前缀：ticket:seat:lock:{seatId}
     */
    public static final String SEAT_LOCK_PREFIX = "ticket:seat:lock:";

    /**
     * 用户购票记录缓存前缀：ticket:user:record:{userId}:{showEventId}
     */
    public static final String USER_RECORD_PREFIX = "ticket:user:record:";

    /**
     * 演出活动详情缓存前缀：ticket:show:detail:{showEventId}
     */
    public static final String SHOW_DETAIL_PREFIX = "ticket:show:detail:";

    /**
     * 订单信息缓存前缀：ticket:order:{orderId}
     */
    public static final String ORDER_PREFIX = "ticket:order:";

    /**
     * 分布式锁前缀：ticket:lock:grab:{userId}:{showEventId}
     */
    public static final String GRAB_LOCK_PREFIX = "ticket:lock:grab:";

    /**
     * 座位库存过期时间（秒）：30分钟
     */
    public static final long SEAT_STOCK_EXPIRE = 1800;

    /**
     * 演出详情缓存过期时间（秒）：1小时
     */
    public static final long SHOW_DETAIL_EXPIRE = 3600;

    /**
     * 订单缓存过期时间（秒）：15分钟
     */
    public static final long ORDER_EXPIRE = 900;

    /**
     * 座位锁定时间（秒）：15分钟
     */
    public static final long SEAT_LOCK_TIME = 900;

    /**
     * 分布式锁过期时间（秒）：10秒
     */
    public static final long GRAB_LOCK_EXPIRE = 10;
}
