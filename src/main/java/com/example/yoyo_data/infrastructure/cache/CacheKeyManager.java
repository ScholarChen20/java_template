package com.example.yoyo_data.infrastructure.cache;

/**
 * 缓存键管理器 - 统一管理和生成缓存键
 * 提供统一的缓存键命名规范
 *
 * @author Template Framework
 * @version 1.0
 */
public class CacheKeyManager {

    /**
     * 用户相关缓存键前缀
     */
    public static final String USER_PREFIX = "user:";
    /**
     * 验证码缓存键前缀
     */
    public static final String CAPTCHA_KEY_PREFIX = "captcha:";
    /**
     * 用户Token缓存键前缀
     */
    public static final String USER_TOKEN_PREFIX = "user:token:";
    /**
     * 黑名单缓存键前缀
     */
    public static final String BLACKLIST_TOKEN = "blacklist:token";
    /**
     * 用户信息缓存键前缀
     */
    public static final String USER_PROFILE_PREFIX = "user:profile:";
    /**
     * 验证码缓存键前缀
     */
    public static final String VERIFY_CODE_PREFIX = "verify:code:";
    /**
     * 用户关注列表缓存键前缀
     */
    public static final String USER_FOLLOWER_LIST_PREFIX = "user:follower:list:";

    /**
     * 热点数据缓存键前缀
     */
    public static final String HOT_NEWS_ZSET_KEY_PREFIX = "hot_news:zset:";
    /**
     * 热点数据Stream缓存键前缀
     */
    public static final String HOT_NEWS_STREAM_KEY_PREFIX = "hot_news:stream:";
    /**
     * 产品相关缓存键前缀
     */
    public static final String PRODUCT_PREFIX = "product:";

    /**
     * 订单相关缓存键前缀
     */
    public static final String ORDER_PREFIX = "order:";

    /**
     * 配置相关缓存键前缀
     */
    public static final String CONFIG_PREFIX = "config:";

    /**
     * 会话相关缓存键前缀
     */
    public static final String SESSION_PREFIX = "session:";

    /**
     * 验证码相关缓存键前缀
     */
    public static final String CAPTCHA_PREFIX = "captcha:";
    /**
     * 热点数据状态分布锁缓存键前缀
     */
    public static final String DISTRIBUTION_STATUS_KEY = "hot_news:distribution:status:";
    /**
     * 热点指标数据分布锁缓存键前缀
     */
    public static final String DISTRIBUTION_METRICS_KEY = "hot_news:distribution:metrics:";
    /**
     * 热点数据分布锁缓存键前缀
     */
    public static final String DISTRIBUTION_LOCK_KEY = "hot_news:distribution:lock:";
    /**
     * 点赞状态缓存键前缀
     */
    public static final String LIKE_STATUS_PREFIX = "like:status:";
    /**
     * 点赞列表缓存键前缀
     */
    public static final String LIKE_LIST_PREFIX = "like:list:";
    /**
     * 点赞排名缓存键前缀
     */
    public static final String LIKE_RANK_PREFIX = "like:rank:";
    /**
     * 点赞数量缓存键前缀
     */
    public static final String LIKE_COUNT_PREFIX = "like:count:";

    /**
     * 对话缓存键前缀
     */
    public static final String DIALOG_CACHE_PREFIX = "dialog:";
    /**
     * 会话列表缓存键前缀
     */
    public static final String DIALOG_LIST_CACHE_PREFIX = "dialog:list:";
    /**
     * 未读消息数量缓存键前缀
     */
    public static final String UNREAD_COUNT_PREFIX = "dialog:unread:";


    /**
     * 缓存默认过期时间（秒） 默认1小时
     */
    public static final long DEFAULT_TTL = 3600;

    /**
     * 缓存过期时间定义
     */
    public static class CacheTTL {
        /**
         * 1分钟
         */
        public static final long ONE_MINUTE = 60;
        /**
         * 5分钟
         */
        public static final long FIVE_MINUTES = 5 * 60;
        /**
         * 15分钟
         */
        public static final long FIFTEEN_MINUTES = 15 * 60;

        /**
         * 30分钟
         */
        public static final long THIRTY_MINUTES = 30 * 60;

        /**
         * 1小时
         */
        public static final long ONE_HOUR = 60 * 60;

        /**
         * 2小时
         */
        public static final long TWO_HOURS = 2 * 60 * 60;

        /**
         * 1天
         */
        public static final long ONE_DAY = 24 * 60 * 60;

        /**
         * 7天
         */
        public static final long SEVEN_DAYS = 7 * 24 * 60 * 60;

        /**
         * 30天
         */
        public static final long THIRTY_DAYS = 30 * 24 * 60 * 60;
    }

    /**
     * 生成用户信息缓存键
     *
     * @param userId 用户ID
     * @return 缓存键
     */
    public static String getUserKey(Long userId) {
        return USER_PREFIX + "info:" + userId;
    }

    /**
     * 生成用户列表缓存键
     *
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 缓存键
     */
    public static String getUserListKey(int pageNum, int pageSize) {
        return USER_PREFIX + "list:" + pageNum + ":" + pageSize;
    }

    /**
     * 生成用户权限缓存键
     *
     * @param userId 用户ID
     * @return 缓存键
     */
    public static String getUserPermissionKey(Long userId) {
        return USER_PREFIX + "permission:" + userId;
    }

    /**
     * 生成产品信息缓存键
     *
     * @param productId 产品ID
     * @return 缓存键
     */
    public static String getProductKey(Long productId) {
        return PRODUCT_PREFIX + "info:" + productId;
    }

    /**
     * 生成产品列表缓存键
     *
     * @param category 分类
     * @param pageNum 页码
     * @return 缓存键
     */
    public static String getProductListKey(String category, int pageNum) {
        return PRODUCT_PREFIX + "list:" + category + ":" + pageNum;
    }

    /**
     * 生成订单信息缓存键
     *
     * @param orderId 订单ID
     * @return 缓存键
     */
    public static String getOrderKey(Long orderId) {
        return ORDER_PREFIX + "info:" + orderId;
    }

    /**
     * 生成验证码缓存键
     *
     * @param accountId 账户ID
     * @return 缓存键
     */
    public static String getCaptchaKey(String accountId) {
        return CAPTCHA_PREFIX + accountId;
    }

    /**
     * 生成会话缓存键
     *
     * @param sessionId 会话ID
     * @return 缓存键
     */
    public static String getSessionKey(String sessionId) {
        return SESSION_PREFIX + sessionId;
    }

    /**
     * 生成配置缓存键
     *
     * @param configKey 配置键
     * @return 缓存键
     */
    public static String getConfigKey(String configKey) {
        return CONFIG_PREFIX + configKey;
    }

    /**
     * 生成自定义缓存键
     *
     * @param prefix 前缀
     * @param key 键
     * @return 缓存键
     */
    public static String getCustomKey(String prefix, String key) {
        return prefix + ":" + key;
    }

    /**
     * 从缓存键中提取ID
     *
     * @param cacheKey 缓存键
     * @return 提取的ID
     */
    public static String extractId(String cacheKey) {
        if (cacheKey == null || !cacheKey.contains(":")) {
            return null;
        }
        return cacheKey.substring(cacheKey.lastIndexOf(":") + 1);
    }
}
