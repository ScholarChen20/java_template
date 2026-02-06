package com.example.yoyo_data.util;

/**
 * ThreadLocal上下文管理工具 - 用于在请求处理过程中管理线程本地变量
 * 可用于存储用户信息、请求ID等请求范围内的数据
 *
 * @author Template Framework
 * @version 1.0
 */
public class ThreadLocalUtils {

    /**
     * 存储用户ID
     */
    private static final ThreadLocal<Long> USER_ID_HOLDER = new ThreadLocal<>();

    /**
     * 存储用户名
     */
    private static final ThreadLocal<String> USERNAME_HOLDER = new ThreadLocal<>();

    /**
     * 存储请求ID
     */
    private static final ThreadLocal<String> REQUEST_ID_HOLDER = new ThreadLocal<>();

    /**
     * 存储IP地址
     */
    private static final ThreadLocal<String> IP_ADDRESS_HOLDER = new ThreadLocal<>();

    /**
     * 设置用户ID
     *
     * @param userId 用户ID
     */
    public static void setUserId(Long userId) {
        USER_ID_HOLDER.set(userId);
    }

    /**
     * 获取用户ID
     *
     * @return 用户ID
     */
    public static Long getUserId() {
        return USER_ID_HOLDER.get();
    }

    /**
     * 设置用户名
     *
     * @param username 用户名
     */
    public static void setUsername(String username) {
        USERNAME_HOLDER.set(username);
    }

    /**
     * 获取用户名
     *
     * @return 用户名
     */
    public static String getUsername() {
        return USERNAME_HOLDER.get();
    }

    /**
     * 设置请求ID
     *
     * @param requestId 请求ID
     */
    public static void setRequestId(String requestId) {
        REQUEST_ID_HOLDER.set(requestId);
    }

    /**
     * 获取请求ID
     *
     * @return 请求ID
     */
    public static String getRequestId() {
        return REQUEST_ID_HOLDER.get();
    }

    /**
     * 设置IP地址
     *
     * @param ipAddress IP地址
     */
    public static void setIpAddress(String ipAddress) {
        IP_ADDRESS_HOLDER.set(ipAddress);
    }

    /**
     * 获取IP地址
     *
     * @return IP地址
     */
    public static String getIpAddress() {
        return IP_ADDRESS_HOLDER.get();
    }

    /**
     * 清空所有ThreadLocal变量
     * 应在请求处理完成后调用，防止内存泄漏
     */
    public static void clear() {
        USER_ID_HOLDER.remove();
        USERNAME_HOLDER.remove();
        REQUEST_ID_HOLDER.remove();
        IP_ADDRESS_HOLDER.remove();
    }

    /**
     * 清空所有ThreadLocal变量（备用方法）
     */
    public static void clearAll() {
        clear();
    }
}
