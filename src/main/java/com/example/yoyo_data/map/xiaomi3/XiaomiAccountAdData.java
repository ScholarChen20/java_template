package com.example.yoyo_data.map.xiaomi3;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * <p>
 * 小米账号数据看板-按天消耗数据
 * </p>
 *
 * @author zhouziqi
 * @since 2025-07-31
 */
@Data
@TableName("xiaomi_account_ad_data")
public class XiaomiAccountAdData implements Serializable {

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
     * 精确花费
     */
    @TableField("accurate_cost_format")
    private BigDecimal accurateCostFormat;

    /**
     * 广告属性（0普通广告，1衍生广告,-1无效）仅维度为adId(创意id)时此属性才有效
     */
    @TableField("ad_attribute")
    private Integer adAttribute;

    /**
     * 现金费用
     */
    @TableField("cash_cost_format")
    private BigDecimal cashCostFormat;

    /**
     * 点击量
     */
    @TableField("click_sum_format")
    private Long clickSumFormat;

    /**
     * 费用
     */
    @TableField("cost_format")
    private BigDecimal costFormat;

    /**
     * 下载均价
     */
    @TableField("cost_per_download_format")
    private BigDecimal costPerDownloadFormat;

    /**
     * 点击均价
     */
    @TableField("cpc_format")
    private BigDecimal cpcFormat;

    /**
     * 点击率
     */
    @TableField("ctr_format")
    private BigDecimal ctrFormat;

    /**
     * 数据时间
     */
    @TableField("report_date")
    private Date reportDate;

    /**
     * 目录名
     */
    @TableField("dim_name")
    private String dimName;

    /**
     * 广告组版位 仅维度为adId(创意id)/groupId(组id)时此属性才有效
     */
    @TableField("display_type_id")
    private Long displayTypeId;

    /**
     * 下载率
     */
    @TableField("download_ratio_format")
    private BigDecimal downloadRatioFormat;

    /**
     * 下载量
     */
    @TableField("download_sum_format")
    private Long downloadSumFormat;

    /**
     * ECPM
     */
    @TableField("ecpm_format")
    private BigDecimal ecpmFormat;

    /**
     * 素材规格 仅维度为adId(创意id)时此属性才有效
     */
    @TableField("emi_material_type")
    private String emiMaterialType;

    /**
     * 素材规格名称 仅维度为adId(创意id)时此属性才有效效
     */
    @TableField("emi_material_type_name")
    private String emiMaterialTypeName;

    /**
     * 广告投放范围
     */
    @TableField("platform_type_id")
    private String platformTypeId;

    /**
     * 状态
     */
    @TableField("status")
    private Integer status;

    /**
     * 曝光量
     */
    @TableField("view_sum_format")
    private Long viewSumFormat;

    /**
     * 虚拟金费用
     */
    @TableField("virtual_cost_format")
    private BigDecimal virtualCostFormat;

    /**
     * 付费金额(z)
     */
    @TableField("app_pay_fee_z_format")
    private BigDecimal appPayFeeZFormat;

    /**
     * 安装完成量(z)
     */
    @TableField("install_sum_z_format")
    private Long installSumZFormat;

    /**
     * 新增激活量V2(z)
     */
    @TableField("active_new_v2_sum_z_format")
    private Long activeNewV2SumZFormat;

    /**
     * 首单购买量(z)
     */
    @TableField("new_user_purchase_sum_z_format")
    private Long newUserPurchaseSumZFormat;

    /**
     * 留存量(z)
     */
    @TableField("retent_sum_z_format")
    private Long retentSumZFormat;

    /**
     * APP召回量(z)
     */
    @TableField("uninstall_sum_z_format")
    private Long uninstallSumZFormat;

    /**
     * 自定义3日留存(z)
     */
    @TableField("d3_retained_num_z_format")
    private BigDecimal d3RetainedNumZFormat;

    /**
     * 自定义5日留存(z)
     */
    @TableField("d5_retained_num_z_format")
    private BigDecimal d5RetainedNumZFormat;

    /**
     * IPVUV数(z)
     */
    @TableField("ipvuv_num_z_format")
    private Long ipvuvNumZFormat;

    /**
     * 快应用调起量(z)
     */
    @TableField("hybrid_open_num_z_format")
    private Long hybridOpenNumZFormat;

    /**
     * 授信(z)
     */
    @TableField("credit_num_z_format")
    private BigDecimal creditNumZFormat;

    /**
     * 付费量(z)
     */
    @TableField("pay_sum_z_format")
    private Long paySumZFormat;

    /**
     * 下单量(z)
     */
    @TableField("complete_order_sum_z_format")
    private Long completeOrderSumZFormat;

    /**
     * 自定义4日留存(z)
     */
    @TableField("d4_retained_num_z_format")
    private BigDecimal d4RetainedNumZFormat;

    /**
     * 自定义7日留存(z)
     */
    @TableField("d7_retained_num_z_format")
    private BigDecimal d7RetainedNumZFormat;

    /**
     * 激活量(z)
     */
    @TableField("active_sum_z_format")
    private Long activeSumZFormat;

    /**
     * 快应用加桌量(z)
     */
    @TableField("app_add_desktop_num_z_format")
    private Long appAddDesktopNumZFormat;

    /**
     * 激活量V2(z)
     */
    @TableField("active_v2_sum_z_format")
    private Long activeV2SumZFormat;

    /**
     * APP调起量(z)
     */
    @TableField("wake_up_sum_z_format")
    private Long wakeUpSumZFormat;

    /**
     * 线索数(z)
     */
    @TableField("inquire_num_z_format")
    private Long inquireNumZFormat;

    /**
     * 自定义6日留存(z)
     */
    @TableField("d6_retained_num_z_format")
    private BigDecimal d6RetainedNumZFormat;

    /**
     * 首次付费总数(z)
     */
    @TableField("first_pay_num_z_format")
    private Long firstPayNumZFormat;

    /**
     * 每日留存总数(z)
     */
    @TableField("retention_daily_v2_num_z_format")
    private Long retentionDailyV2NumZFormat;

    /**
     * 购买量(z)
     */
    @TableField("app_purchase_sum_z_format")
    private Long appPurchaseSumZFormat;

    /**
     * 拉活量(z)
     */
    @TableField("re_active_wake_up_sum_z_format")
    private Long reActiveWakeUpSumZFormat;

    /**
     * 首次拉活量(z)
     */
    @TableField("re_active_sum_z_format")
    private Long reActiveSumZFormat;

    /**
     * 注册量(z)
     */
    @TableField("register_sum_z_format")
    private Long registerSumZFormat;

    /**
     * 完件(z)
     */
    @TableField("submit_num_z_format")
    private BigDecimal submitNumZFormat;

    /**
     * 24小时变现金额(z)
     */
    @TableField("h24_conv_cash_z_format")
    private BigDecimal h24ConvCashZFormat;

    /**
     * 自定义每日留存总数(z)
     */
    @TableField("retention_daily_num_z_format")
    private Long retentionDailyNumZFormat;

    /**
     * 首次下单量(z)
     */
    @TableField("first_app_complete_order_num_z_format")
    private Long firstAppCompleteOrderNumZFormat;

    /**
     * 新增激活量(z)
     */
    @TableField("active_new_sum_z_format")
    private Long activeNewSumZFormat;

    /**
     * 关键行为数(z)
     */
    @TableField("addiction_num_z_format")
    private Long addictionNumZFormat;

    /**
     * 关键行为成本(z)
     */
    @TableField("addiction_cost_z_format")
    private BigDecimal addictionCostZFormat;

    /**
     * 快应用加桌成本(z)
     */
    @TableField("app_add_desktop_cost_z_format")
    private BigDecimal appAddDesktopCostZFormat;

    /**
     * 拉活自定义留存率(z)
     */
    @TableField("re_active_wake_up_retent_ratio_z_format")
    private BigDecimal reActiveWakeUpRetentRatioZFormat;

    /**
     * 每日留存总成本(z)
     */
    @TableField("retention_daily_v2_cost_z_format")
    private BigDecimal retentionDailyV2CostZFormat;

    /**
     * 安装完成均价(z)
     */
    @TableField("cost_per_install_z_format")
    private BigDecimal costPerInstallZFormat;

    /**
     * 注册均价(z)
     */
    @TableField("cost_per_register_z_format")
    private BigDecimal costPerRegisterZFormat;

    /**
     * 拉活均价(z)
     */
    @TableField("cost_per_re_active_wake_up_z_format")
    private BigDecimal costPerReActiveWakeUpZFormat;

    /**
     * 留存均价(z)
     */
    @TableField("cost_per_retent_z_format")
    private BigDecimal costPerRetentZFormat;

    /**
     * 完件成本(z)
     */
    @TableField("submit_cost_z_format")
    private BigDecimal submitCostZFormat;

    /**
     * 24小时变现ROI(z)
     */
    @TableField("h24_conv_cash_roi_z_format")
    private BigDecimal h24ConvCashRoiZFormat;

    /**
     * 快应用次留率(z)
     */
    @TableField("app_dct_desktop_rate_z_format")
    private BigDecimal appDctDesktopRateZFormat;

    /**
     * 快应用调起成本(z)
     */
    @TableField("hybrid_open_cost_z_format")
    private BigDecimal hybridOpenCostZFormat;

    /**
     * IPVUV率(z)
     */
    @TableField("ipvuv_ratio_z_format")
    private BigDecimal ipvuvRatioZFormat;

    /**
     * 快应用加桌率(z)
     */
    @TableField("app_add_desktop_rate_z_format")
    private BigDecimal appAddDesktopRateZFormat;

    /**
     * 首单购买均价(z)
     */
    @TableField("new_user_purchase_cost_z_format")
    private BigDecimal newUserPurchaseCostZFormat;

    /**
     * 首次付费总成本(z)
     */
    @TableField("first_pay_cost_z_format")
    private BigDecimal firstPayCostZFormat;

    /**
     * 首次下单成本(z)
     */
    @TableField("first_app_complete_order_cost_z_format")
    private BigDecimal firstAppCompleteOrderCostZFormat;

    /**
     * 线索成本(z)
     */
    @TableField("inquire_cost_z_format")
    private BigDecimal inquireCostZFormat;

    /**
     * 关键行为点击率(z)
     */
    @TableField("addiction_click_ratio_z_format")
    private BigDecimal addictionClickRatioZFormat;

    /**
     * 新增激活均价V2(z)
     */
    @TableField("cost_per_active_new_v2_z_format")
    private BigDecimal costPerActiveNewV2ZFormat;

    /**
     * 线索激活率(z)
     */
    @TableField("inquire_active_ratio_z_format")
    private BigDecimal inquireActiveRatioZFormat;

    /**
     * 激活均价V2(z)
     */
    @TableField("cost_per_active_v2_z_format")
    private BigDecimal costPerActiveV2ZFormat;

    /**
     * 激活均价(z)
     */
    @TableField("cost_per_active_z_format")
    private BigDecimal costPerActiveZFormat;

    /**
     * 购买均价(z)
     */
    @TableField("cost_per_app_purchase_z_format")
    private BigDecimal costPerAppPurchaseZFormat;

    /**
     * 关键行为下载率(z)
     */
    @TableField("addiction_download_ratio_z_format")
    private BigDecimal addictionDownloadRatioZFormat;

    /**
     * 注册次留率(z)
     */
    @TableField("register_retention_ratio_z_format")
    private BigDecimal registerRetentionRatioZFormat;

    /**
     * 首次拉活均价(z)
     */
    @TableField("cost_per_re_active_z_format")
    private BigDecimal costPerReActiveZFormat;

    /**
     * 自定义每日留存总成本(z)
     */
    @TableField("retention_daily_cost_z_format")
    private BigDecimal retentionDailyCostZFormat;

    /**
     * APP召回均价(z)
     */
    @TableField("cost_per_uninstall_z_format")
    private BigDecimal costPerUninstallZFormat;

    /**
     * 召回次留率(z)
     */
    @TableField("recall_retention_ratio_z_format")
    private BigDecimal recallRetentionRatioZFormat;

    /**
     * 付费均价(z)
     */
    @TableField("cost_per_pay_z_format")
    private BigDecimal costPerPayZFormat;

    /**
     * 快应用次留成本(z)
     */
    @TableField("app_dct_desktop_cost_z_format")
    private BigDecimal appDctDesktopCostZFormat;

    /**
     * APP调起均价(z)
     */
    @TableField("cost_per_wake_up_z_format")
    private BigDecimal costPerWakeUpZFormat;

    /**
     * IPVUV成本(z)
     */
    @TableField("ipvuv_cost_z_format")
    private BigDecimal ipvuvCostZFormat;

    /**
     * 新增激活均价(z)
     */
    @TableField("cost_per_active_new_z_format")
    private BigDecimal costPerActiveNewZFormat;

    /**
     * 下单成本(z)
     */
    @TableField("app_complete_order_cost_z_format")
    private BigDecimal appCompleteOrderCostZFormat;

    /**
     * 授信成本(z)
     */
    @TableField("credit_cost_z_format")
    private BigDecimal creditCostZFormat;

    /**
     * 每日留存总数(j)
     */
    @TableField("retention_daily_v2_num_j_format")
    private Long retentionDailyV2NumJFormat;

    /**
     * 注册量(j)
     */
    @TableField("register_sum_j_format")
    private Long registerSumJFormat;

    /**
     * 安装完成量(j)
     */
    @TableField("install_sum_j_format")
    private Long installSumJFormat;

    /**
     * 拉活量(j)
     */
    @TableField("re_active_wake_up_sum_j_format")
    private Long reActiveWakeUpSumJFormat;

    /**
     * 首次付费总数(j)
     */
    @TableField("first_pay_num_j_format")
    private Long firstPayNumJFormat;

    /**
     * 留存量(j)
     */
    @TableField("retent_sum_j_format")
    private Long retentSumJFormat;

    /**
     * 授信(j)
     */
    @TableField("credit_num_j_format")
    private BigDecimal creditNumJFormat;

    /**
     * 自定义4日留存(j)
     */
    @TableField("d4_retained_num_j_format")
    private BigDecimal d4RetainedNumJFormat;

    /**
     * 首次下单量(j)
     */
    @TableField("first_app_complete_order_num_j_format")
    private Long firstAppCompleteOrderNumJFormat;

    /**
     * 首次拉活量(j)
     */
    @TableField("re_active_sum_j_format")
    private Long reActiveSumJFormat;

    /**
     * 线索数(j)
     */
    @TableField("inquire_num_j_format")
    private Long inquireNumJFormat;

    /**
     * 购买量(j)
     */
    @TableField("app_purchase_sum_j_format")
    private Long appPurchaseSumJFormat;

    /**
     * 首单购买量(j)
     */
    @TableField("new_user_purchase_sum_j_format")
    private Long newUserPurchaseSumJFormat;

    /**
     * 快应用加桌量(j)
     */
    @TableField("app_add_desktop_num_j_format")
    private Long appAddDesktopNumJFormat;

    /**
     * 付费金额(j)
     */
    @TableField("app_pay_fee_j_format")
    private BigDecimal appPayFeeJFormat;

    /**
     * 加购量(j)
     */
    @TableField("add_cart_sum_j_format")
    private Long addCartSumJFormat;

    /**
     * IPVUV数(j)
     */
    @TableField("ipvuv_num_j_format")
    private Long ipvuvNumJFormat;

    /**
     * APP调起量(j)
     */
    @TableField("wake_up_sum_j_format")
    private Long wakeUpSumJFormat;

    /**
     * 自定义每日留存总数(j)
     */
    @TableField("retention_daily_num_j_format")
    private Long retentionDailyNumJFormat;

    /**
     * 激活量V2(j)
     */
    @TableField("active_v2_sum_j_format")
    private Long activeV2SumJFormat;

    /**
     * 关键行为数(j)
     */
    @TableField("addiction_num_j_format")
    private Long addictionNumJFormat;

    /**
     * 完件(j)
     */
    @TableField("submit_num_j_format")
    private BigDecimal submitNumJFormat;

    /**
     * 付费量(j)
     */
    @TableField("pay_sum_j_format")
    private Long paySumJFormat;

    /**
     * 新增激活量V2(j)
     */
    @TableField("active_new_v2_sum_j_format")
    private Long activeNewV2SumJFormat;

    /**
     * 快应用调起量(j)
     */
    @TableField("hybrid_open_num_j_format")
    private Long hybridOpenNumJFormat;

    /**
     * 自定义5日留存(j)
     */
    @TableField("d5_retained_num_j_format")
    private BigDecimal d5RetainedNumJFormat;

    /**
     * 自定义3日留存(j)
     */
    @TableField("d3_retained_num_j_format")
    private BigDecimal d3RetainedNumJFormat;

    /**
     * 24小时变现金额(j)
     */
    @TableField("h24_conv_cash_j_format")
    private BigDecimal h24ConvCashJFormat;

    /**
     * 下单量(j)
     */
    @TableField("complete_order_sum_j_format")
    private Long completeOrderSumJFormat;

    /**
     * 激活量(j)
     */
    @TableField("active_sum_j_format")
    private Long activeSumJFormat;

    /**
     * 自定义6日留存(j)
     */
    @TableField("d6_retained_num_j_format")
    private BigDecimal d6RetainedNumJFormat;

    /**
     * 新增激活量(j)
     */
    @TableField("active_new_sum_j_format")
    private Long activeNewSumJFormat;

    /**
     * APP召回量(j)
     */
    @TableField("uninstall_sum_j_format")
    private Long uninstallSumJFormat;

    /**
     * 自定义7日留存(j)
     */
    @TableField("d7_retained_num_j_format")
    private BigDecimal d7RetainedNumJFormat;

    /**
     * 激活均价(j)
     */
    @TableField("cost_per_active_j_format")
    private BigDecimal costPerActiveJFormat;

    /**
     * 新增激活均价(j)
     */
    @TableField("cost_per_active_new_j_format")
    private BigDecimal costPerActiveNewJFormat;

    /**
     * 完件成本(j)
     */
    @TableField("submit_cost_j_format")
    private BigDecimal submitCostJFormat;

    /**
     * 拉活均价(j)
     */
    @TableField("cost_per_re_active_wake_up_j_format")
    private BigDecimal costPerReActiveWakeUpJFormat;

    /**
     * 快应用次留率(j)
     */
    @TableField("app_dct_desktop_rate_j_format")
    private BigDecimal appDctDesktopRateJFormat;

    /**
     * 注册次留率(j)
     */
    @TableField("register_retention_ratio_j_format")
    private BigDecimal registerRetentionRatioJFormat;

    /**
     * 关键行为成本(j)
     */
    @TableField("addiction_cost_j_format")
    private BigDecimal addictionCostJFormat;

    /**
     * 24小时变现ROI(j)
     */
    @TableField("h24_conv_cash_roi_j_format")
    private BigDecimal h24ConvCashRoiJFormat;

    /**
     * 召回次留率(j)
     */
    @TableField("recall_retention_ratio_j_format")
    private BigDecimal recallRetentionRatioJFormat;

    /**
     * 首次拉活均价(j)
     */
    @TableField("cost_per_re_active_j_format")
    private BigDecimal costPerReActiveJFormat;

    /**
     * 快应用调起成本(j)
     */
    @TableField("hybrid_open_cost_j_format")
    private BigDecimal hybridOpenCostJFormat;

    /**
     * 注册均价(j)
     */
    @TableField("cost_per_register_j_format")
    private BigDecimal costPerRegisterJFormat;

    /**
     * 快应用次留成本(j)
     */
    @TableField("app_dct_desktop_cost_j_format")
    private BigDecimal appDctDesktopCostJFormat;

    /**
     * 安装完成均价(j)
     */
    @TableField("cost_per_install_j_format")
    private BigDecimal costPerInstallJFormat;

    /**
     * 线索激活率(j)
     */
    @TableField("inquire_active_ratio_j_format")
    private BigDecimal inquireActiveRatioJFormat;

    /**
     * 每日留存总成本(j)
     */
    @TableField("retention_daily_v2_cost_j_format")
    private BigDecimal retentionDailyV2CostJFormat;

    /**
     * 关键行为点击率(j)
     */
    @TableField("addiction_click_ratio_j_format")
    private BigDecimal addictionClickRatioJFormat;

    /**
     * 首次下单成本(j)
     */
    @TableField("first_app_complete_order_cost_j_format")
    private BigDecimal firstAppCompleteOrderCostJFormat;

    /**
     * 付费均价(j)
     */
    @TableField("cost_per_pay_j_format")
    private BigDecimal costPerPayJFormat;

    /**
     * 授信成本(j)
     */
    @TableField("credit_cost_j_format")
    private BigDecimal creditCostJFormat;

    /**
     * 留存均价(j)
     */
    @TableField("cost_per_retent_j_format")
    private BigDecimal costPerRetentJFormat;

    /**
     * 拉活自定义留存率(j)
     */
    @TableField("re_active_wake_up_retent_ratio_j_format")
    private BigDecimal reActiveWakeUpRetentRatioJFormat;

    /**
     * IPVUV成本(j)
     */
    @TableField("ipvuv_cost_j_format")
    private BigDecimal ipvuvCostJFormat;

    /**
     * 快应用加桌率(j)
     */
    @TableField("app_add_desktop_rate_j_format")
    private BigDecimal appAddDesktopRateJFormat;

    /**
     * 首单购买均价(j)
     */
    @TableField("new_user_purchase_cost_j_format")
    private BigDecimal newUserPurchaseCostJFormat;

    /**
     * 快应用加桌成本(j)
     */
    @TableField("app_add_desktop_cost_j_format")
    private BigDecimal appAddDesktopCostJFormat;

    /**
     * 购买均价(j)
     */
    @TableField("cost_per_app_purchase_j_format")
    private BigDecimal costPerAppPurchaseJFormat;

    /**
     * APP调起均价(j)
     */
    @TableField("cost_per_wake_up_j_format")
    private BigDecimal costPerWakeUpJFormat;

    /**
     * 自定义每日留存总成本(j)
     */
    @TableField("retention_daily_cost_j_format")
    private BigDecimal retentionDailyCostJFormat;

    /**
     * APP召回均价(j)
     */
    @TableField("cost_per_uninstall_j_format")
    private BigDecimal costPerUninstallJFormat;

    /**
     * 线索成本(j)
     */
    @TableField("inquire_cost_j_format")
    private BigDecimal inquireCostJFormat;

    /**
     * 关键行为下载率(j)
     */
    @TableField("addiction_download_ratio_j_format")
    private BigDecimal addictionDownloadRatioJFormat;

    /**
     * 下单成本(j)
     */
    @TableField("app_complete_order_cost_j_format")
    private BigDecimal appCompleteOrderCostJFormat;

    /**
     * IPVUV率(j)
     */
    @TableField("ipvuv_ratio_j_format")
    private BigDecimal ipvuvRatioJFormat;

    /**
     * 首次付费总成本(j)
     */
    @TableField("first_pay_cost_j_format")
    private BigDecimal firstPayCostJFormat;

    /**
     * 激活均价V2(j)
     */
    @TableField("cost_per_active_v2_j_format")
    private BigDecimal costPerActiveV2JFormat;

    /**
     * 新增激活均价V2(j)
     */
    @TableField("cost_per_active_new_v2_j_format")
    private BigDecimal costPerActiveNewV2JFormat;

    /**
     * 转化数(z)
     */
    @TableField("shallow_target_conv_num_z_format")
    private Long shallowTargetConvNumZFormat;

    /**
     * 深度转化数(z)
     */
    @TableField("deep_target_conv_num_z_format")
    private Long deepTargetConvNumZFormat;

    /**
     * 转化率(z)
     */
    @TableField("stcn_ratio_z_format")
    private BigDecimal stcnRatioZFormat;

    /**
     * 平均转化成本(z)
     */
    @TableField("avg_conv_cost_z_format")
    private BigDecimal avgConvCostZFormat;

    /**
     * 深度转化率(z)
     */
    @TableField("dtcn_ratio_z_format")
    private BigDecimal dtcnRatioZFormat;

    /**
     * 深度平均转化成本(z)
     */
    @TableField("deep_avg_conv_cost_z_format")
    private BigDecimal deepAvgConvCostZFormat;

    /**
     * 转化数(j)
     */
    @TableField("shallow_target_conv_num_j_format")
    private Long shallowTargetConvNumJFormat;

    /**
     * 深度转化数(j)
     */
    @TableField("deep_target_conv_num_j_format")
    private Long deepTargetConvNumJFormat;

    /**
     * 深度转化率(j)
     */
    @TableField("dtcn_ratio_j_format")
    private BigDecimal dtcnRatioJFormat;

    /**
     * 平均转化成本(j)
     */
    @TableField("avg_conv_cost_j_format")
    private BigDecimal avgConvCostJFormat;

    /**
     * 深度平均转化成本(j)
     */
    @TableField("deep_avg_conv_cost_j_format")
    private BigDecimal deepAvgConvCostJFormat;

    /**
     * 转化率(j)
     */
    @TableField("stcn_ratio_j_format")
    private BigDecimal stcnRatioJFormat;

}
