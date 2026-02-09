package com.example.yoyo_data.common.constant;

/**
 * 演出活动状态常量
 */
public class ShowEventStatus {

    /**
     * 待开票
     */
    public static final String PENDING = "PENDING";

    /**
     * 售票中
     */
    public static final String SELLING = "SELLING";

    /**
     * 售罄
     */
    public static final String SOLD_OUT = "SOLD_OUT";

    /**
     * 已结束
     */
    public static final String ENDED = "ENDED";

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
}
