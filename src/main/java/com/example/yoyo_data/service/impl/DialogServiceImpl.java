package com.example.yoyo_data.service.impl;

import com.example.yoyo_data.common.Result;
import com.example.yoyo_data.service.DialogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 对话服务实现类
 */
@Slf4j
@Service
public class DialogServiceImpl implements DialogService {

    /**
     * 从token中获取用户ID的辅助方法
     * 实际项目中应该使用JWT工具类解析token
     */
    private Long getUserIdFromToken(String token) {
        // 模拟从token中获取用户ID
        // 实际项目中应该使用JWT工具类解析token
        return 1L;
    }

    @Override
    public Result<Map<String, Object>> createDialog(String token, Map<String, Object> params) {
        try {
            // 从token中获取用户ID
            Long userId = getUserIdFromToken(token);
            if (userId == null) {
                return Result.error("无效的token");
            }

            // 获取对方用户ID
            Long recipientId = (Long) params.get("recipient_id");
            if (recipientId == null) {
                return Result.error("缺少recipient_id参数");
            }

            // 模拟创建对话
            Map<String, Object> dialog = new HashMap<>();
            dialog.put("id", System.currentTimeMillis());
            dialog.put("user_id", userId);
            dialog.put("recipient_id", recipientId);
            dialog.put("type", params.getOrDefault("type", "private"));
            dialog.put("metadata", params.get("metadata"));
            dialog.put("created_at", System.currentTimeMillis());
            dialog.put("updated_at", System.currentTimeMillis());

            log.info("创建对话成功: userId={}, recipientId={}", userId, recipientId);
            return Result.success(dialog);

        } catch (Exception e) {
            log.error("创建对话失败", e);
            return Result.error("创建对话失败: " + e.getMessage());
        }
    }

    @Override
    public Result<Map<String, Object>> getDialogList(String token, Integer page, Integer size, String type, String status, String sort) {
        try {
            // 从token中获取用户ID
            Long userId = getUserIdFromToken(token);
            if (userId == null) {
                return Result.error("无效的token");
            }

            // 模拟数据
            List<Map<String, Object>> dialogList = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                Map<String, Object> dialog = new HashMap<>();
                dialog.put("id", (long) (i + 1));
                dialog.put("user_id", userId);
                dialog.put("recipient_id", (long) (i + 2));
                dialog.put("recipient_name", "用户" + (i + 2));
                dialog.put("recipient_avatar", "https://example.com/avatar" + (i + 2) + ".jpg");
                dialog.put("type", type != null ? type : "private");
                dialog.put("status", status != null ? status : "active");
                dialog.put("last_message", "这是最后一条消息" + (i + 1));
                dialog.put("last_message_sender_id", (long) (i + 2));
                dialog.put("last_message_at", System.currentTimeMillis() - i * 60000);
                dialog.put("unread_count", i);
                dialog.put("created_at", System.currentTimeMillis() - (i + 1) * 3600000);
                dialog.put("updated_at", System.currentTimeMillis() - i * 60000);
                dialogList.add(dialog);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("items", dialogList);
            result.put("total", 100);
            result.put("page", page);
            result.put("size", size);
            result.put("total_pages", (int) Math.ceil(100.0 / size));

            log.info("获取对话列表成功: userId={}, page={}, size={}, type={}, status={}, sort={}", userId, page, size, type, status, sort);
            return Result.success(result);

        } catch (Exception e) {
            log.error("获取对话列表失败", e);
            return Result.error("获取对话列表失败: " + e.getMessage());
        }
    }

    @Override
    public Result<Map<String, Object>> getDialogDetail(Long dialogId, String token) {
        try {
            // 从token中获取用户ID
            Long userId = getUserIdFromToken(token);
            if (userId == null) {
                return Result.error("无效的token");
            }

            // 模拟数据
            Map<String, Object> dialogDetail = new HashMap<>();
            dialogDetail.put("id", dialogId);
            dialogDetail.put("user_id", userId);
            dialogDetail.put("recipient_id", 2L);
            dialogDetail.put("recipient_name", "用户2");
            dialogDetail.put("recipient_avatar", "https://example.com/avatar2.jpg");
            dialogDetail.put("type", "private");
            dialogDetail.put("status", "active");
            dialogDetail.put("created_at", System.currentTimeMillis() - 3600000);
            dialogDetail.put("updated_at", System.currentTimeMillis());

            log.info("获取对话详情成功: dialogId={}, userId={}", dialogId, userId);
            return Result.success(dialogDetail);

        } catch (Exception e) {
            log.error("获取对话详情失败", e);
            return Result.error("获取对话详情失败: " + e.getMessage());
        }
    }

    @Override
    public Result<?> sendMessage(Long dialogId, String token, Map<String, Object> params) {
        try {
            // 从token中获取用户ID
            Long userId = getUserIdFromToken(token);
            if (userId == null) {
                return Result.error("无效的token");
            }

            // 获取消息内容
            String content = (String) params.get("content");
            if (content == null || content.isEmpty()) {
                return Result.error("缺少content参数");
            }

            // 模拟发送消息
            Map<String, Object> message = new HashMap<>();
            message.put("id", System.currentTimeMillis());
            message.put("dialog_id", dialogId);
            message.put("sender_id", userId);
            message.put("content", content);
            message.put("type", params.getOrDefault("type", "text"));
            message.put("media_urls", params.get("media_urls"));
            message.put("metadata", params.get("metadata"));
            message.put("sent_at", System.currentTimeMillis());
            message.put("status", "sent");

            log.info("发送消息成功: dialogId={}, userId={}, content={}", dialogId, userId, content);
            return Result.success(message);

        } catch (Exception e) {
            log.error("发送消息失败", e);
            return Result.error("发送消息失败: " + e.getMessage());
        }
    }

    @Override
    public Result<?> getMessageList(Long dialogId, String token, Integer page, Integer size, Long before) {
        try {
            // 从token中获取用户ID
            Long userId = getUserIdFromToken(token);
            if (userId == null) {
                return Result.error("无效的token");
            }

            // 模拟数据
            List<Map<String, Object>> messageList = new ArrayList<>();
            long timestamp = before != null ? before : System.currentTimeMillis();

            for (int i = 0; i < size; i++) {
                Map<String, Object> message = new HashMap<>();
                message.put("id", System.currentTimeMillis() - i);
                message.put("dialog_id", dialogId);
                message.put("sender_id", i % 2 == 0 ? userId : 2L);
                message.put("content", "这是消息" + (i + 1));
                message.put("type", "text");
                message.put("sent_at", timestamp - i * 60000);
                message.put("status", "read");
                messageList.add(message);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("items", messageList);
            result.put("total", 200);
            result.put("page", page);
            result.put("size", size);
            result.put("total_pages", (int) Math.ceil(200.0 / size));
            result.put("before", messageList.isEmpty() ? null : messageList.get(messageList.size() - 1).get("sent_at"));

            log.info("获取消息列表成功: dialogId={}, userId={}, page={}, size={}, before={}", dialogId, userId, page, size, before);
            return Result.success(result);

        } catch (Exception e) {
            log.error("获取消息列表失败", e);
            return Result.error("获取消息列表失败: " + e.getMessage());
        }
    }

    @Override
    public Result<Map<String, Object>> updateMessageStatus(Long dialogId, String token, Map<String, Object> params) {
        try {
            // 从token中获取用户ID
            Long userId = getUserIdFromToken(token);
            if (userId == null) {
                return Result.error("无效的token");
            }

            // 获取最后读取的消息ID
            Long lastReadMessageId = (Long) params.get("last_read_message_id");
            if (lastReadMessageId == null) {
                return Result.error("缺少last_read_message_id参数");
            }

            // 模拟更新消息状态
            Map<String, Object> result = new HashMap<>();
            result.put("dialog_id", dialogId);
            result.put("user_id", userId);
            result.put("last_read_message_id", lastReadMessageId);
            result.put("updated_at", System.currentTimeMillis());

            log.info("更新消息状态成功: dialogId={}, userId={}, lastReadMessageId={}", dialogId, userId, lastReadMessageId);
            return Result.success(result);

        } catch (Exception e) {
            log.error("更新消息状态失败", e);
            return Result.error("更新消息状态失败: " + e.getMessage());
        }
    }

    @Override
    public Result<Map<String, Object>> archiveDialog(Long dialogId, String token, Map<String, Object> params) {
        try {
            // 从token中获取用户ID
            Long userId = getUserIdFromToken(token);
            if (userId == null) {
                return Result.error("无效的token");
            }

            // 获取归档状态
            Boolean isArchived = (Boolean) params.get("is_archived");
            if (isArchived == null) {
                return Result.error("缺少is_archived参数");
            }

            // 模拟归档/取消归档对话
            Map<String, Object> result = new HashMap<>();
            result.put("dialog_id", dialogId);
            result.put("user_id", userId);
            result.put("is_archived", isArchived);
            result.put("updated_at", System.currentTimeMillis());

            log.info("{}对话成功: dialogId={}, userId={}", isArchived ? "归档" : "取消归档", dialogId, userId);
            return Result.success(result);

        } catch (Exception e) {
            log.error("归档/取消归档对话失败", e);
            return Result.error("归档/取消归档对话失败: " + e.getMessage());
        }
    }
}