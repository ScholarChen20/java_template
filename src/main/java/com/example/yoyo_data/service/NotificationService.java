package com.example.yoyo_data.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.yoyo_data.common.Result;
import com.example.yoyo_data.common.vo.NotificationVO;

import java.util.Map;

/**
 * 消息通知服务接口
 */
public interface NotificationService {

    Result<Map<String, Object>> getNotifications(String token, String type, Boolean read, Integer page, Integer size);

    Result<Map<String, Object>> getUnreadCount(String token);

    Result<String> markRead(Long notificationId, String token);

    Result<String> markAllRead(String token, String type);

    Result<String> deleteNotification(Long notificationId, String token);
}
