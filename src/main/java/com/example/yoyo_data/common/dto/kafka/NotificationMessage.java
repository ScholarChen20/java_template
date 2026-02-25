package com.example.yoyo_data.common.dto.kafka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

/**
 * 通知消息
 *
 * @description 用于异步发送各类通知（短信、邮件、推送）
 * @author yoyo_data
 * @date 2026-02-25
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 通知类型: SMS-短信, EMAIL-邮件, PUSH-推送, SYSTEM-站内信
     */
    private String notificationType;

    /**
     * 接收者用户ID
     */
    private Long userId;

    /**
     * 接收者手机号（短信通知时使用）
     */
    private String phone;

    /**
     * 接收者邮箱（邮件通知时使用）
     */
    private String email;

    /**
     * 消息标题
     */
    private String title;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 业务类型: ORDER-订单, SHOW-演出, REFUND-退款
     */
    private String businessType;

    /**
     * 关联业务ID
     */
    private Long relatedId;

    /**
     * 模板ID（如短信模板、邮件模板）
     */
    private String templateId;

    /**
     * 模板参数
     */
    private Map<String, String> templateParams;

    /**
     * 发送时间戳
     */
    private Long sendTime;
}
