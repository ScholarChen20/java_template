package com.example.yoyo_data.controller;

import com.example.yoyo_data.common.Result;
import com.example.yoyo_data.service.NotificationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 消息通知控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/notifications")
@Api(tags = "消息通知", description = "通知查询、标记已读、删除等操作")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping
    @ApiOperation(value = "获取通知列表", notes = "分页查询通知列表，支持类型和已读状态筛选。需要JWT认证")
    public Result<Map<String, Object>> getNotifications(
            @ApiParam(value = "通知类型（TICKET/ORDER/REFUND/REMIND/SYSTEM）") @RequestParam(required = false) String type,
            @ApiParam(value = "已读状态") @RequestParam(required = false) Boolean read,
            @ApiParam(value = "页码", defaultValue = "1") @RequestParam(defaultValue = "1") Integer page,
            @ApiParam(value = "每页大小", defaultValue = "20") @RequestParam(defaultValue = "20") Integer size,
            HttpServletRequest request
    ) {
        String token = extractToken(request);
        return notificationService.getNotifications(token, type, read, page, size);
    }

    @GetMapping("/unread-count")
    @ApiOperation(value = "获取未读通知数量", notes = "获取当前用户的未读通知总数（Redis缓存5min）。需要JWT认证")
    public Result<Map<String, Object>> getUnreadCount(HttpServletRequest request) {
        String token = extractToken(request);
        return notificationService.getUnreadCount(token);
    }

    @PutMapping("/{notificationId}/read")
    @ApiOperation(value = "标记单条通知为已读", notes = "需要JWT认证")
    public Result<String> markRead(
            @ApiParam(value = "通知ID", required = true) @PathVariable Long notificationId,
            HttpServletRequest request
    ) {
        String token = extractToken(request);
        return notificationService.markRead(notificationId, token);
    }

    @PutMapping("/read-all")
    @ApiOperation(value = "全部标记为已读", notes = "支持按类型批量标记。需要JWT认证")
    public Result<String> markAllRead(
            @ApiParam(value = "通知类型（可选）") @RequestParam(required = false) String type,
            HttpServletRequest request
    ) {
        String token = extractToken(request);
        return notificationService.markAllRead(token, type);
    }

    @DeleteMapping("/{notificationId}")
    @ApiOperation(value = "删除通知", notes = "需要JWT认证")
    public Result<String> deleteNotification(
            @ApiParam(value = "通知ID", required = true) @PathVariable Long notificationId,
            HttpServletRequest request
    ) {
        String token = extractToken(request);
        return notificationService.deleteNotification(notificationId, token);
    }

    private String extractToken(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return token;
    }
}
