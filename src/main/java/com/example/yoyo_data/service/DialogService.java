package com.example.yoyo_data.service;

import com.example.yoyo_data.common.Result;
import com.example.yoyo_data.common.dto.DialogSessionDTO;
import com.example.yoyo_data.common.dto.PageResponseDTO;

import java.util.Map;

/**
 * 对话服务接口
 */
public interface DialogService {
    /**
     * 创建对话
     *
     * @param token 请求token
     * @param params 创建对话参数
     * @return 创建结果
     */
    Result<DialogSessionDTO> createDialog(String token, Map<String, Object> params);

    /**
     * 获取对话列表
     *
     * @param token 请求token
     * @param page 页码
     * @param size 每页大小
     * @param type 类型
     * @param status 状态
     * @param sort 排序方式
     * @return 对话列表
     */
    Result<PageResponseDTO<DialogSessionDTO>> getDialogList(String token, Integer page, Integer size, String type, String status, String sort);

    /**
     * 获取对话详情
     *
     * @param dialogId 对话ID
     * @param token 请求token
     * @return 对话详情
     */
    Result<DialogSessionDTO> getDialogDetail(Long dialogId, String token);

    /**
     * 发送消息
     *
     * @param dialogId 对话ID
     * @param token 请求token
     * @param params 发送消息参数
     * @return 发送结果
     */
    Result<DialogSessionDTO.MessageDTO> sendMessage(Long dialogId, String token, Map<String, Object> params);

    /**
     * 获取消息列表
     *
     * @param dialogId 对话ID
     * @param token 请求token
     * @param page 页码
     * @param size 每页大小
     * @param before 分页标记
     * @return 消息列表
     */
    Result<PageResponseDTO<DialogSessionDTO.MessageDTO>> getMessageList(Long dialogId, String token, Integer page, Integer size, Long before);

    /**
     * 更新消息状态
     *
     * @param dialogId 对话ID
     * @param token 请求token
     * @param params 更新参数
     * @return 更新结果
     */
    Result<DialogSessionDTO> updateMessageStatus(Long dialogId, String token, Map<String, Object> params);

    /**
     * 归档/取消归档对话
     *
     * @param dialogId 对话ID
     * @param token 请求token
     * @param params 更新参数
     * @return 更新结果
     */
    Result<DialogSessionDTO> archiveDialog(Long dialogId, String token, Map<String, Object> params);
}