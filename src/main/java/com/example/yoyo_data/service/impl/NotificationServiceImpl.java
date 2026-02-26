package com.example.yoyo_data.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.yoyo_data.common.Result;
import com.example.yoyo_data.common.entity.Notification;
import com.example.yoyo_data.common.vo.NotificationVO;
import com.example.yoyo_data.infrastructure.cache.RedisService;
import com.example.yoyo_data.infrastructure.repository.NotificationMapper;
import com.example.yoyo_data.service.NotificationService;
import com.example.yoyo_data.util.jwt.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 消息通知服务实现类
 */
@Slf4j
@Service
public class NotificationServiceImpl implements NotificationService {

    private static final String UNREAD_COUNT_PREFIX = "ticket:notification:unread:";
    private static final long UNREAD_COUNT_EXPIRE = 5 * 60 * 1000L;

    @Autowired
    private NotificationMapper notificationMapper;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private RedisService redisService;

    @Override
    public Result<Map<String, Object>> getNotifications(String token, String type, Boolean read,
                                                        Integer page, Integer size) {
        Long userId = getUserId(token);
        if (userId == null) return Result.unauthorized("请先登录");

        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId);
        if (StringUtils.hasText(type)) {
            wrapper.eq(Notification::getType, type);
        }
        if (read != null) {
            wrapper.eq(Notification::getIsRead, read);
        }
        wrapper.orderByDesc(Notification::getCreatedAt);

        Page<Notification> notificationPage = notificationMapper.selectPage(new Page<>(page, size), wrapper);

        List<NotificationVO> voList = notificationPage.getRecords().stream()
                .map(this::toVO).collect(Collectors.toList());

        // 未读总数
        long unreadCount = notificationMapper.selectCount(
                new LambdaQueryWrapper<Notification>()
                        .eq(Notification::getUserId, userId)
                        .eq(Notification::getIsRead, false));

        Map<String, Object> result = new HashMap<>();
        result.put("total", notificationPage.getTotal());
        result.put("current", notificationPage.getCurrent());
        result.put("size", notificationPage.getSize());
        result.put("records", voList);
        result.put("unreadCount", unreadCount);

        return Result.success(result);
    }

    @Override
    public Result<Map<String, Object>> getUnreadCount(String token) {
        Long userId = getUserId(token);
        if (userId == null) return Result.unauthorized("请先登录");

        String cacheKey = UNREAD_COUNT_PREFIX + userId;
        String cached = redisService.stringGetString(cacheKey);
        if (cached != null) {
            return Result.success(JSON.parseObject(cached, new com.alibaba.fastjson.TypeReference<Map<String, Object>>() {}));
        }

        long total = notificationMapper.selectCount(
                new LambdaQueryWrapper<Notification>()
                        .eq(Notification::getUserId, userId)
                        .eq(Notification::getIsRead, false));

        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        redisService.stringSetString(cacheKey, JSON.toJSONString(result), UNREAD_COUNT_EXPIRE);

        return Result.success(result);
    }

    @Override
    public Result<String> markRead(Long notificationId, String token) {
        Long userId = getUserId(token);
        if (userId == null) return Result.unauthorized("请先登录");

        Notification notification = notificationMapper.selectById(notificationId);
        if (notification == null || !notification.getUserId().equals(userId)) {
            return Result.error(404, "通知不存在");
        }

        LambdaUpdateWrapper<Notification> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Notification::getId, notificationId)
                .set(Notification::getIsRead, true)
                .set(Notification::getReadAt, LocalDateTime.now());
        notificationMapper.update(null, updateWrapper);

        redisService.delete(UNREAD_COUNT_PREFIX + userId);
        return Result.success("已标记为已读");
    }

    @Override
    public Result<String> markAllRead(String token, String type) {
        Long userId = getUserId(token);
        if (userId == null) return Result.unauthorized("请先登录");

        LambdaUpdateWrapper<Notification> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Notification::getUserId, userId)
                .eq(Notification::getIsRead, false);
        if (StringUtils.hasText(type)) {
            updateWrapper.eq(Notification::getType, type);
        }
        updateWrapper.set(Notification::getIsRead, true)
                .set(Notification::getReadAt, LocalDateTime.now());
        notificationMapper.update(null, updateWrapper);

        redisService.delete(UNREAD_COUNT_PREFIX + userId);
        return Result.success("全部标记为已读");
    }

    @Override
    public Result<String> deleteNotification(Long notificationId, String token) {
        Long userId = getUserId(token);
        if (userId == null) return Result.unauthorized("请先登录");

        Notification notification = notificationMapper.selectById(notificationId);
        if (notification == null || !notification.getUserId().equals(userId)) {
            return Result.error(404, "通知不存在");
        }

        notificationMapper.deleteById(notificationId);
        redisService.delete(UNREAD_COUNT_PREFIX + userId);
        return Result.success("删除成功");
    }

    private NotificationVO toVO(Notification n) {
        return NotificationVO.builder()
                .id(n.getId())
                .type(n.getType())
                .title(n.getTitle())
                .content(n.getContent())
                .isRead(n.getIsRead())
                .relatedId(n.getRelatedId())
                .relatedType(n.getRelatedType())
                .actionUrl(n.getActionUrl())
                .createdAt(n.getCreatedAt())
                .build();
    }

    private Long getUserId(String token) {
        if (!StringUtils.hasText(token)) return null;
        if (!Boolean.TRUE.equals(jwtUtils.validateToken(token))) return null;
        return jwtUtils.getUserIdFromToken(token);
    }
}
