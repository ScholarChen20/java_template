package com.example.yoyo_data.map.xiaomi4;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * <p>
 * 小米账号数据看板-按天消耗数据
 * </p>
 *
 * @author zhouziqi
 * @since 2025-11-13
 */
@Data
@TableName("xiaomi_account_ad_data_v4")
public class XiaomiAccountAdDataV4 {

    private static final long serialVersionUID = 1L;

    /**
     * 账号id
     */
    @TableField("account_id")
    private Long accountId;

    /**
     * 媒体的账号id
     */
    @TableField("media_account_id")
    private Long mediaAccountId;

    /**
     * 账号名称
     */
    @TableField("account_name")
    private String accountName;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private Date createTime;

    /**
     * 当前时间，到小时即可（YYYY-MM-DD hh:00:00）
     */
    @TableField("cur_time")
    private Date curTime;

    @TableField("report_data_date")
    private Date reportDataDate;

    /**
     * 失败原因
     */
    @TableField("reason")
    private String reason;

    /**
     * 现金花费
     */
    @TableField("cash_cost")
    private BigDecimal cashCost;

    /**
     * 点击量
     */
    @TableField("click_num")
    private Long clickNum;

    /**
     * 花费
     */
    @TableField("cost")
    private BigDecimal cost;

    /**
     * 点击均价
     */
    @TableField("cpc")
    private BigDecimal cpc;

    /**
     * 下载均价
     */
    @TableField("cpd")
    private BigDecimal cpd;

    /**
     * 单次计费均价
     */
    @TableField("cpj")
    private BigDecimal cpj;

    /**
     * 点击率
     */
    @TableField("ctr")
    private BigDecimal ctr;

    /**
     * 账户id
     */
    @TableField("customer_id")
    private Long customerId;

    /**
     * 下载量
     */
    @TableField("download_num")
    private Long downloadNum;

    /**
     * 下载率
     */
    @TableField("dtr")
    private BigDecimal dtr;

    /**
     * ecpm
     */
    @TableField("ecpm")
    private BigDecimal ecpm;

    /**
     * 曝光量
     */
    @TableField("expose_num")
    private Long exposeNum;

    /**
     * 计费率
     */
    @TableField("jtr")
    private BigDecimal jtr;

    /**
     * 实际预约量
     */
    @TableField("real_reservation")
    private Long realReservation;

    /**
     * APP预约
     */
    @TableField("reservation")
    private Long reservation;

    /**
     * APP预约成本
     */
    @TableField("rpa")
    private BigDecimal rpa;

    /**
     * 总花费
     */
    @TableField("total_cost")
    private BigDecimal totalCost;

    /**
     * 虚拟金花费
     */
    @TableField("virtual_cost")
    private BigDecimal virtualCost;

    /**
     * 留存均价(z)
     */
    @TableField("cost_per_retentz_format")
    private BigDecimal costPerRetentzFormat;

    /**
     * H5购买均价(z)
     */
    @TableField("h5_purchase_costz_format")
    private BigDecimal h5PurchaseCostzFormat;

    /**
     * 注册量(j)
     */
    @TableField("register_sumj_format")
    private Long registerSumjFormat;

    /**
     * 每日留存总数(j)
     */
    @TableField("retention_daily_v2_numj_format")
    private Long retentionDailyV2NumjFormat;

    /**
     * 新增激活量(j)
     */
    @TableField("active_new_sumj_format")
    private Long activeNewSumjFormat;

    /**
     * 首次拉活均价(z)
     */
    @TableField("cost_per_re_activez_format")
    private BigDecimal costPerReActivezFormat;

    /**
     * APP召回量(z)
     */
    @TableField("uninstall_sumz_format")
    private Long uninstallSumzFormat;

    /**
     * 关键行为数(j)
     */
    @TableField("addiction_numj_format")
    private Long addictionNumjFormat;

    /**
     * 首单购买量(z)
     */
    @TableField("new_user_purchase_sumz_format")
    private Long newUserPurchaseSumzFormat;

    /**
     * 首次付费总数(z)
     */
    @TableField("first_pay_numz_format")
    private Long firstPayNumzFormat;

    /**
     * 购买量(z)
     */
    @TableField("purchase_sumz_format")
    private Long purchaseSumzFormat;

    /**
     * 激活均价(z)
     */
    @TableField("cost_per_activez_format")
    private BigDecimal costPerActivezFormat;

    /**
     * 激活量(j)
     */
    @TableField("active_sumj_format")
    private Long activeSumjFormat;

    /**
     * 快应用调起量(j)
     */
    @TableField("hybrid_open_numj_format")
    private Long hybridOpenNumjFormat;

    /**
     * 自定义每日留存总成本(z)
     */
    @TableField("retention_daily_costz_format")
    private BigDecimal retentionDailyCostzFormat;

    /**
     * 24小时变现ROI(j)
     */
    @TableField("h24_conv_cash_roij_format")
    private BigDecimal h24ConvCashRoijFormat;

    /**
     * 付费量(z)
     */
    @TableField("pay_sumz_format")
    private Long paySumzFormat;

    /**
     * 自定义7日留存(z)
     */
    @TableField("d7_retained_numz_format")
    private Long d7RetainedNumzFormat;

    /**
     * 自定义5日留存(j)
     */
    @TableField("d5_retained_numj_format")
    private Long d5RetainedNumjFormat;

    /**
     * 关键行为成本(j)
     */
    @TableField("addiction_costj_format")
    private BigDecimal addictionCostjFormat;

    /**
     * 线索成本(j)
     */
    @TableField("inquire_costj_format")
    private BigDecimal inquireCostjFormat;

    /**
     * 自定义激活5日留存率(z)
     */
    @TableField("active_d5_retained_ratioz_format")
    private BigDecimal activeD5RetainedRatiozFormat;

    /**
     * 留存量(z)
     */
    @TableField("retent_sumz_format")
    private Long retentSumzFormat;

    /**
     * 关键行为点击率(z)
     */
    @TableField("addiction_click_ratioz_format")
    private BigDecimal addictionClickRatiozFormat;

    /**
     * 自定义激活7日留存率(z)
     */
    @TableField("active_d7_retained_ratioz_format")
    private BigDecimal activeD7RetainedRatiozFormat;

    /**
     * 拉活量(j)
     */
    @TableField("re_active_wake_up_sumj_format")
    private Long reActiveWakeUpSumjFormat;

    /**
     * 每日留存总成本(z)
     */
    @TableField("retention_daily_v2_costz_format")
    private BigDecimal retentionDailyV2CostzFormat;

    /**
     * 自定义6日留存(z)
     */
    @TableField("d6_retained_numz_format")
    private Long d6RetainedNumzFormat;

    /**
     * APP召回均价(z)
     */
    @TableField("cost_per_uninstallz_format")
    private BigDecimal costPerUninstallzFormat;

    /**
     * 加桌均价(z)
     */
    @TableField("add_desktop_costz_format")
    private BigDecimal addDesktopCostzFormat;

    /**
     * 拉活量(z)
     */
    @TableField("re_active_wake_up_sumz_format")
    private Long reActiveWakeUpSumzFormat;

    /**
     * 首次拉活量(j)
     */
    @TableField("re_active_sumj_format")
    private Long reActiveSumjFormat;

    /**
     * 关键行为下载率(z)
     */
    @TableField("addiction_download_ratioz_format")
    private BigDecimal addictionDownloadRatiozFormat;

    /**
     * 自定义7日留存(j)
     */
    @TableField("d7_retained_numj_format")
    private Long d7RetainedNumjFormat;

    /**
     * 注册均价(j)
     */
    @TableField("cost_per_registerj_format")
    private BigDecimal costPerRegisterjFormat;

    /**
     * 留存量(j)
     */
    @TableField("retent_sumj_format")
    private Long retentSumjFormat;

    /**
     * 付费金额(j)
     */
    @TableField("app_pay_feej_format")
    private BigDecimal appPayFeejFormat;

    /**
     * 自定义激活留存率(z)
     */
    @TableField("active_retent_ratioz_format")
    private BigDecimal activeRetentRatiozFormat;

    /**
     * 拉活均价(z)
     */
    @TableField("cost_per_re_active_wake_upz_format")
    private BigDecimal costPerReActiveWakeUpzFormat;

    /**
     * 24小时变现金额(z)
     */
    @TableField("h24_conv_cashz_format")
    private BigDecimal h24ConvCashzFormat;

    /**
     * APP召回均价(j)
     */
    @TableField("cost_per_uninstallj_format")
    private BigDecimal costPerUninstalljFormat;

    /**
     * 激活量(z)
     */
    @TableField("active_sumz_format")
    private Long activeSumzFormat;

    /**
     * 每日留存总成本(j)
     */
    @TableField("retention_daily_v2_costj_format")
    private BigDecimal retentionDailyV2CostjFormat;

    /**
     * APP召回量(j)
     */
    @TableField("uninstall_sumj_format")
    private Long uninstallSumjFormat;

    /**
     * 关键行为数(z)
     */
    @TableField("addiction_numz_format")
    private Long addictionNumzFormat;

    /**
     * 购买量(z)
     */
    @TableField("app_purchase_sumz_format")
    private Long appPurchaseSumzFormat;

    /**
     * 购买均价(z)
     */
    @TableField("average_purchase_pricez_format")
    private BigDecimal averagePurchasePricezFormat;

    /**
     * 表单均价(z)
     */
    @TableField("average_submit_pricez_format")
    private BigDecimal averageSubmitPricezFormat;

    /**
     * 24小时变现金额(j)
     */
    @TableField("h24_conv_cashj_format")
    private BigDecimal h24ConvCashjFormat;

    /**
     * 付费均价(z)
     */
    @TableField("cost_per_payz_format")
    private BigDecimal costPerPayzFormat;

    /**
     * 注册均价(z)
     */
    @TableField("cost_per_registerz_format")
    private BigDecimal costPerRegisterzFormat;

    /**
     * 激活均价V2(j)
     */
    @TableField("cost_per_active_v2j_format")
    private BigDecimal costPerActiveV2jFormat;

    /**
     * 首次下单成本(j)
     */
    @TableField("first_app_complete_order_costj_format")
    private BigDecimal firstAppCompleteOrderCostjFormat;

    /**
     * 自定义新增激活留存率(z)
     */
    @TableField("active_new_retent_ratioz_format")
    private BigDecimal activeNewRetentRatiozFormat;

    /**
     * H5购买量(z)
     */
    @TableField("h5_purchase_numz_format")
    private Long h5PurchaseNumzFormat;

    /**
     * 首次拉活均价(j)
     */
    @TableField("cost_per_re_activej_format")
    private BigDecimal costPerReActivejFormat;

    /**
     * 拉活均价(j)
     */
    @TableField("cost_per_re_active_wake_upj_format")
    private BigDecimal costPerReActiveWakeUpjFormat;

    /**
     * 新增激活量(z)
     */
    @TableField("active_new_sumz_format")
    private Long activeNewSumzFormat;

    /**
     * 首次付费总数(z)
     */
    @TableField("first_pay_numj_format")
    private Long firstPayNumjFormat;

    /**
     * 购买量(j)
     */
    @TableField("app_purchase_sumj_format")
    private Long appPurchaseSumjFormat;

    /**
     * 留存均价(j)
     */
    @TableField("cost_per_retentj_format")
    private BigDecimal costPerRetentjFormat;

    /**
     * 快应用加桌成本(z)
     */
    @TableField("app_add_desktop_costz_format")
    private BigDecimal appAddDesktopCostzFormat;

    /**
     * 首次拉活量(z)
     */
    @TableField("re_active_sumz_format")
    private Long reActiveSumzFormat;

    /**
     * 自定义注册量(z)
     */
    @TableField("register_sumz_format")
    private Long registerSumzFormat;

}
