package com.example.yoyo_data.service.impl;

import com.alibaba.fastjson.JSON;
import com.example.yoyo_data.infrastructure.cache.RedisService;
import com.example.yoyo_data.common.Result;
import com.example.yoyo_data.common.document.DialogSession;
import com.example.yoyo_data.common.dto.DialogSessionDTO;
import com.example.yoyo_data.common.dto.PageResponseDTO;
import com.example.yoyo_data.infrastructure.repository.mongodb.DialogSessionRepository;
import com.example.yoyo_data.service.DialogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 对话服务实现类
 */
@Slf4j
@Service
public class DialogServiceImpl implements DialogService {

    @Autowired
    private DialogSessionRepository dialogSessionRepository;

    @Autowired
    private RedisService redisService;

    private static final String DIALOG_CACHE_PREFIX = "dialog:";
    private static final String DIALOG_LIST_CACHE_PREFIX = "dialog:list:";
    private static final long CACHE_EXPIRE_TIME = 3600000L; // 1小时（毫秒）

    /**
     * 从token中获取用户ID的辅助方法
     */
    private Long getUserIdFromToken(String token) {
        // 模拟从token中获取用户ID
        // 实际项目中应该使用JWT工具类解析token
        return 1L;
    }

    @Override
    public Result<DialogSessionDTO> createDialog(String token, Map<String, Object> params) {
        try {
            Long userId = getUserIdFromToken(token);
            if (userId == null) {
                return Result.error("无效的token");
            }

            Long recipientId = ((Number) params.get("recipient_id")).longValue();
            if (recipientId == null) {
                return Result.error("缺少recipient_id参数");
            }

            // 检查是否已存在对话
            Optional<DialogSession> existingDialog = dialogSessionRepository.findByUserIdAndRecipientId(userId, recipientId);
            if (existingDialog.isPresent()) {
                return Result.success(convertToDTO(existingDialog.get()));
            }

            // 创建新对话
            DialogSession dialogSession = DialogSession.builder()
                    .userId(userId)
                    .recipientId(recipientId)
                    .type((String) params.getOrDefault("type", "private"))
                    .status("active")
                    .unreadCount(0)
                    .metadata((Map<String, Object>) params.get("metadata"))
                    .messages(new ArrayList<>())
                    .createdAt(new Date())
                    .updatedAt(new Date())
                    .isArchived(false)
                    .build();

            dialogSession = dialogSessionRepository.save(dialogSession);

            // 清除列表缓存
            clearDialogListCache(userId);

            log.info("创建对话成功: userId={}, recipientId={}", userId, recipientId);
            return Result.success(convertToDTO(dialogSession));

        } catch (Exception e) {
            log.error("创建对话失败", e);
            return Result.error("创建对话失败: " + e.getMessage());
        }
    }

    @Override
    public Result<PageResponseDTO<DialogSessionDTO>> getDialogList(String token, Integer page, Integer size, String type, String status, String sort) {
        try {
            Long userId = getUserIdFromToken(token);
            if (userId == null) {
                return Result.error("无效的token");
            }

            // 尝试从Redis获取缓存
            String cacheKey = DIALOG_LIST_CACHE_PREFIX + userId + ":" + page + ":" + size + ":" + type + ":" + status;
            String cachedData = redisService.stringGetString(cacheKey);
            if (cachedData != null) {
                log.info("从Redis缓存获取对话列表: userId={}", userId);
                PageResponseDTO<DialogSessionDTO> result = JSON.parseObject(cachedData, PageResponseDTO.class);
                return Result.success(result);
            }

            // 从MongoDB获取数据
            List<DialogSession> dialogList;
            if (type != null && !type.isEmpty()) {
                dialogList = dialogSessionRepository.findByUserIdAndType(userId, type);
            } else if (status != null && !status.isEmpty()) {
                dialogList = dialogSessionRepository.findByUserIdAndStatus(userId, status);
            } else {
                dialogList = dialogSessionRepository.findByUserId(userId);
            }

            // 排序
            if ("updated_at".equals(sort) || sort == null) {
                dialogList.sort((a, b) -> b.getUpdatedAt().compareTo(a.getUpdatedAt()));
            }

            // 分页
            int start = (page - 1) * size;
            int end = Math.min(start + size, dialogList.size());
            List<DialogSession> pagedList = dialogList.subList(start, end);

            List<DialogSessionDTO> items = pagedList.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

            PageResponseDTO<DialogSessionDTO> result = PageResponseDTO.<DialogSessionDTO>builder()
                    .items(items)
                    .total(dialogList.size())
                    .page(page)
                    .size(size)
                    .totalPages((int) Math.ceil((double) dialogList.size() / size))
                    .build();

            // 缓存到Redis
            redisService.stringSetString(cacheKey, JSON.toJSONString(result), CACHE_EXPIRE_TIME);

            log.info("从MongoDB获取对话列表成功: userId={}, page={}, size={}", userId, page, size);
            return Result.success(result);

        } catch (Exception e) {
            log.error("获取对话列表失败", e);
            return Result.error("获取对话列表失败: " + e.getMessage());
        }
    }

    @Override
    public Result<DialogSessionDTO> getDialogDetail(Long dialogId, String token) {
        try {
            Long userId = getUserIdFromToken(token);
            if (userId == null) {
                return Result.error("无效的token");
            }

            // 尝试从Redis获取缓存
            String cacheKey = DIALOG_CACHE_PREFIX + dialogId;
            String cachedData = redisService.stringGetString(cacheKey);
            if (cachedData != null) {
                log.info("从Redis缓存获取对话详情: dialogId={}", dialogId);
                DialogSessionDTO result = JSON.parseObject(cachedData, DialogSessionDTO.class);
                return Result.success(result);
            }

            // 从MongoDB获取数据
            Optional<DialogSession> dialogOpt = dialogSessionRepository.findByIdAndUserId(String.valueOf(dialogId), userId);
            if (!dialogOpt.isPresent()) {
                return Result.error("对话不存在或无权访问");
            }

            DialogSession dialog = dialogOpt.get();
            DialogSessionDTO result = convertToDTO(dialog);

            // 缓存到Redis（热点数据）
            redisService.stringSetString(cacheKey, JSON.toJSONString(result), CACHE_EXPIRE_TIME);

            log.info("从MongoDB获取对话详情成功: dialogId={}, userId={}", dialogId, userId);
            return Result.success(result);

        } catch (Exception e) {
            log.error("获取对话详情失败", e);
            return Result.error("获取对话详情失败: " + e.getMessage());
        }
    }

    @Override
    public Result<DialogSessionDTO.MessageDTO> sendMessage(Long dialogId, String token, Map<String, Object> params) {
        try {
            Long userId = getUserIdFromToken(token);
            if (userId == null) {
                return Result.error("无效的token");
            }

            String content = (String) params.get("content");
            if (content == null || content.isEmpty()) {
                return Result.error("缺少content参数");
            }

            // 获取对话
            Optional<DialogSession> dialogOpt = dialogSessionRepository.findByIdAndUserId(String.valueOf(dialogId), userId);
            if (!dialogOpt.isPresent()) {
                return Result.error("对话不存在或无权访问");
            }

            DialogSession dialog = dialogOpt.get();

            // 创建消息
            DialogSession.Message message = DialogSession.Message.builder()
                    .id(UUID.randomUUID().toString())
                    .senderId(userId)
                    .content(content)
                    .type((String) params.getOrDefault("type", "text"))
                    .mediaUrls((List<String>) params.get("media_urls"))
                    .metadata((Map<String, Object>) params.get("metadata"))
                    .sentAt(new Date())
                    .status("sent")
                    .build();

            // 添加消息到对话
            if (dialog.getMessages() == null) {
                dialog.setMessages(new ArrayList<>());
            }
            dialog.getMessages().add(message);
            dialog.setLastMessage(content);
            dialog.setLastMessageSenderId(userId);
            dialog.setLastMessageAt(new Date());
            dialog.setUpdatedAt(new Date());

            dialogSessionRepository.save(dialog);

            // 清除缓存
            clearDialogCache(dialogId);
            clearDialogListCache(userId);

            DialogSessionDTO.MessageDTO messageDTO = convertMessageToDTO(message);

            log.info("发送消息成功: dialogId={}, userId={}", dialogId, userId);
            return Result.success(messageDTO);

        } catch (Exception e) {
            log.error("发送消息失败", e);
            return Result.error("发送消息失败: " + e.getMessage());
        }
    }

    @Override
    public Result<PageResponseDTO<DialogSessionDTO.MessageDTO>> getMessageList(Long dialogId, String token, Integer page, Integer size, Long before) {
        try {
            Long userId = getUserIdFromToken(token);
            if (userId == null) {
                return Result.error("无效的token");
            }

            Optional<DialogSession> dialogOpt = dialogSessionRepository.findByIdAndUserId(String.valueOf(dialogId), userId);
            if (!dialogOpt.isPresent()) {
                return Result.error("对话不存在或无权访问");
            }

            DialogSession dialog = dialogOpt.get();
            List<DialogSession.Message> messages = dialog.getMessages();
            if (messages == null) {
                messages = new ArrayList<>();
            }

            // 过滤before时间之前的消息
            if (before != null) {
                Date beforeDate = new Date(before);
                messages = messages.stream()
                        .filter(m -> m.getSentAt().before(beforeDate))
                        .collect(Collectors.toList());
            }

            // 排序（最新的在前）
            messages.sort((a, b) -> b.getSentAt().compareTo(a.getSentAt()));

            // 分页
            int start = (page - 1) * size;
            int end = Math.min(start + size, messages.size());
            List<DialogSession.Message> pagedMessages = messages.subList(start, end);

            List<DialogSessionDTO.MessageDTO> items = pagedMessages.stream()
                    .map(this::convertMessageToDTO)
                    .collect(Collectors.toList());

            PageResponseDTO<DialogSessionDTO.MessageDTO> result = PageResponseDTO.<DialogSessionDTO.MessageDTO>builder()
                    .items(items)
                    .total(messages.size())
                    .page(page)
                    .size(size)
                    .totalPages((int) Math.ceil((double) messages.size() / size))
                    .build();

            log.info("获取消息列表成功: dialogId={}, userId={}", dialogId, userId);
            return Result.success(result);

        } catch (Exception e) {
            log.error("获取消息列表失败", e);
            return Result.error("获取消息列表失败: " + e.getMessage());
        }
    }

    @Override
    public Result<DialogSessionDTO> updateMessageStatus(Long dialogId, String token, Map<String, Object> params) {
        try {
            Long userId = getUserIdFromToken(token);
            if (userId == null) {
                return Result.error("无效的token");
            }

            Long lastReadMessageId = ((Number) params.get("last_read_message_id")).longValue();
            if (lastReadMessageId == null) {
                return Result.error("缺少last_read_message_id参数");
            }

            Optional<DialogSession> dialogOpt = dialogSessionRepository.findByIdAndUserId(String.valueOf(dialogId), userId);
            if (!dialogOpt.isPresent()) {
                return Result.error("对话不存在或无权访问");
            }

            DialogSession dialog = dialogOpt.get();
            dialog.setUnreadCount(0);
            dialog.setUpdatedAt(new Date());
            dialogSessionRepository.save(dialog);

            // 清除缓存
            clearDialogCache(dialogId);
            clearDialogListCache(userId);

            log.info("更新消息状态成功: dialogId={}, userId={}", dialogId, userId);
            return Result.success(convertToDTO(dialog));

        } catch (Exception e) {
            log.error("更新消息状态失败", e);
            return Result.error("更新消息状态失败: " + e.getMessage());
        }
    }

    @Override
    public Result<DialogSessionDTO> archiveDialog(Long dialogId, String token, Map<String, Object> params) {
        try {
            Long userId = getUserIdFromToken(token);
            if (userId == null) {
                return Result.error("无效的token");
            }

            Boolean isArchived = (Boolean) params.get("is_archived");
            if (isArchived == null) {
                return Result.error("缺少is_archived参数");
            }

            Optional<DialogSession> dialogOpt = dialogSessionRepository.findByIdAndUserId(String.valueOf(dialogId), userId);
            if (!dialogOpt.isPresent()) {
                return Result.error("对话不存在或无权访问");
            }

            DialogSession dialog = dialogOpt.get();
            dialog.setIsArchived(isArchived);
            dialog.setUpdatedAt(new Date());
            dialogSessionRepository.save(dialog);

            // 清除缓存
            clearDialogCache(dialogId);
            clearDialogListCache(userId);

            log.info("{}对话成功: dialogId={}, userId={}", isArchived ? "归档" : "取消归档", dialogId, userId);
            return Result.success(convertToDTO(dialog));

        } catch (Exception e) {
            log.error("归档/取消归档对话失败", e);
            return Result.error("归档/取消归档对话失败: " + e.getMessage());
        }
    }

    /**
     * 将DialogSession转换为DTO
     */
    private DialogSessionDTO convertToDTO(DialogSession dialog) {
        List<DialogSessionDTO.MessageDTO> messageDTOs = null;
        if (dialog.getMessages() != null) {
            messageDTOs = dialog.getMessages().stream()
                    .map(this::convertMessageToDTO)
                    .collect(Collectors.toList());
        }

        return DialogSessionDTO.builder()
                .id(dialog.getId())
                .userId(dialog.getUserId())
                .recipientId(dialog.getRecipientId())
                .type(dialog.getType())
                .status(dialog.getStatus())
                .lastMessage(dialog.getLastMessage())
                .lastMessageSenderId(dialog.getLastMessageSenderId())
                .lastMessageAt(dialog.getLastMessageAt())
                .unreadCount(dialog.getUnreadCount())
                .metadata(dialog.getMetadata())
                .messages(messageDTOs)
                .createdAt(dialog.getCreatedAt())
                .updatedAt(dialog.getUpdatedAt())
                .isArchived(dialog.getIsArchived())
                .build();
    }

    /**
     * 将Message转换为DTO
     */
    private DialogSessionDTO.MessageDTO convertMessageToDTO(DialogSession.Message message) {
        return DialogSessionDTO.MessageDTO.builder()
                .id(message.getId())
                .senderId(message.getSenderId())
                .content(message.getContent())
                .type(message.getType())
                .mediaUrls(message.getMediaUrls())
                .metadata(message.getMetadata())
                .sentAt(message.getSentAt())
                .status(message.getStatus())
                .build();
    }

    /**
     * 清除对话缓存
     */
    private void clearDialogCache(Long dialogId) {
        String cacheKey = DIALOG_CACHE_PREFIX + dialogId;
        redisService.delete(cacheKey);
    }

    /**
     * 清除对话列表缓存
     */
    private void clearDialogListCache(Long userId) {
        Set<String> keys = redisService.keys(DIALOG_LIST_CACHE_PREFIX + userId + ":*");
        if (keys != null && !keys.isEmpty()) {
            redisService.delete(keys);
        }
    }
}
