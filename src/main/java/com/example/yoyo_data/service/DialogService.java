package com.example.yoyo_data.service;

import com.example.yoyo_data.common.Result;
import java.util.Map;

/**
 * 对话服务接口
 * 处理对话相关业务逻辑
 */
public interface DialogService {

    /**
     * 创建对话
     *
     * @param token  当前用户的token
     * @param params 创建对话参数，包含recipient_id、type、metadata等
     * @return 创建结果
     */
    Result<Map<String, Object>> createDialog(String token, Map<String, Object> params);

    /**
     * 获取对话列表
     *
     * @param token   当前用户的token
     * @param page    页码，默认 1
     * @param size    每页数量，默认 20
     * @param type    类型，可选 private, group
     * @param status  状态，可选 active, archived
     * @param sort    排序方式，默认 updated_at
     * @return 对话列表
     */
    Result<Map<String, Object>> getDialogList(String token, Integer page, Integer size, String type, String status, String sort);

    /**
     * 获取对话详情
     *
     * @param id    对话ID
     * @param token 当前用户的token
     * @return 对话详情
     */
    Result<Map<String, Object>> getDialogDetail(Long id, String token);

    /**
     * 发送消息
     *
     * @param id     对话ID
     * @param token  当前用户的token
     * @param params 发送消息参数，包含content、type、media_urls等
     * @return 发送结果
     */
    Result<Map<String, Object>> sendMessage(Long id, String token, Map<String, Object> params);

    /**
     * 获取消息列表
     *
     * @param id     对话ID
     * @param token  当前用户的token
     * @param page   页码，默认 1
     * @param size   每页数量，默认 50
     * @param before 分页标记，获取此时间戳之前的消息
     * @return 消息列表
     */
    Result<Map<String, Object>> getMessageList(Long id, String token, Integer page, Integer size, Long before);

    /**
     * 更新消息状态
     *
     * @param id     对话ID
     * @param token  当前用户的token
     * @param params 更新参数，包含last_read_message_id等
     * @return 更新结果
     */
    Result<Map<String, Object>> updateMessageStatus(Long id, String token, Map<String, Object> params);

    /**
     * 归档/取消归档对话
     *
     * @param id     对话ID
     * @param token  当前用户的token
     * @param params 更新参数，包含is_archived等
     * @return 更新结果
     */
    Result<Map<String, Object>> archiveDialog(Long id, String token, Map<String, Object> params);
}
