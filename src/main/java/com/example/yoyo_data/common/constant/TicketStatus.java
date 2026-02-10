package com.example.yoyo_data.common.constant;

public class TicketStatus {
    /**
     * Token无效或已过期
     */
    public static final String TOKEN_INVALID = "Token无效或已过期";

    // ============================ 抢票 ===============================
    /**
     * 演出活动不存在
     */
    public static final String NOT_EXIST = "演出活动不存在";
    /**
     * 演出未开始售票或已结束
     */
    public static final String NOT_START = "演出未开始售票或已结束";
    /**
     * 演出尚未开始售票
     */
    public static final String NOT_SELLING = "演出尚未开始售票";
    /**
     * 演出已结束售票
     */
    public static final String SELLING_END = "演出已结束售票";
    /**
     * 座位数量必须与观影人数量一致
     */
    public static final String SEAT_NUM_NOT_EQUAL_TO_VIEWER_NUM = "座位数量必须与观影人数量一致";
    /**
     * 请勿重复提交抢票请求，请稍后再试
     */
    public static final String REPEAT_REQUEST = "请勿重复提交抢票请求，请稍后再试";

    /**
     * 座位已被锁定或售出
     */
    public static final String SEAT_LOCKED_OR_SOLD = "座位已被锁定或售出";

    /**
     * 请勿重复下单
     */
    public static final String REPEAT_ORDER = "请勿重复下单";
    /**
     * 抢票过程中线程被中断，请重试
     */
    public static final String ORDER_PAY_SUCCESS = "抢票过程中线程被中断，请重试";

    /**
     * 座位与活动不匹配
     */
    public static final String SEAT_NOT_MATCH = "座位与活动不匹配";

    // ============================ 订单支付 ===============================
    /**
     * 订单不存在
     */
    public static final String ORDER_NOT_EXIST = "订单不存在";
    /**
     * 无权限操作此订单
     */
    public static final String NO_PERMISSION = "无权限操作此订单";
    /**
     * 订单状态不正确，无法支付
     */
    public static final String ORDER_STATUS_ERROR = "订单状态不正确，无法支付";
    /**
     * 订单已过期
     */
    public static final String ORDER_EXPIRED = "订单已过期";
    /**
     * 订单状态已变更，请重新支付
     */
    public static final String ORDER_STATUS_CHANGED = "订单状态已变更，支付失败";

    // ============================ 订单查询 ===============================

    // ============================ 订单取消 ===============================
    /**
     * 成功取消
     */
    public static final String CANCEL_SUCCESS = "取消成功";

}
