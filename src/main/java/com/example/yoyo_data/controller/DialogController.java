package com.example.yoyo_data.controller;

import com.example.yoyo_data.common.Result;
import com.example.yoyo_data.common.dto.DialogSessionDTO;
import com.example.yoyo_data.common.dto.PageResponseDTO;
import com.example.yoyo_data.service.DialogService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 对话控制器
 * 处理对话相关操作
 */
@Slf4j
@RestController
@RequestMapping("/api/dialogs")
@Api(tags = "对话模块")
public class DialogController {

    @Autowired
    private DialogService dialogService;

    /**
     * 创建对话
     *
     * @param params 创建对话参数，包含recipient_id、type、metadata等
     * @param request 请求对象，用于获取当前用户信息
     * @return 创建结果
     */
    @PostMapping
    @ApiOperation(value = "创建对话", notes = "创建新的对话")
    public Result<DialogSessionDTO> createDialog(@ApiParam(value = "创建对话参数") @RequestBody Map<String, Object> params, HttpServletRequest request) {
        // 从请求头获取token
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return dialogService.createDialog(token, params);
    }

    /**
     * 获取对话列表
     *
     * @param page     页码，默认 1
     * @param size     每页数量，默认 20
     * @param type     类型，可选 private, group
     * @param status   状态，可选 active, archived
     * @param sort     排序方式，默认 updated_at
     * @param request  请求对象，用于获取当前用户信息
     * @return 对话列表
     */
    @GetMapping
    @ApiOperation(value = "获取对话列表", notes = "获取当前用户的对话列表")
    public Result<PageResponseDTO<DialogSessionDTO>> getDialogList(
            @ApiParam(value = "页码", defaultValue = "1") @RequestParam(defaultValue = "1") Integer page,
            @ApiParam(value = "每页数量", defaultValue = "20") @RequestParam(defaultValue = "20") Integer size,
            @ApiParam(value = "类型") @RequestParam(required = false) String type,
            @ApiParam(value = "状态") @RequestParam(required = false) String status,
            @ApiParam(value = "排序方式") @RequestParam(required = false) String sort,
            HttpServletRequest request) {
        // 从请求头获取token
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return dialogService.getDialogList(token, page, size, type, status, sort);
    }

    /**
     * 获取对话详情
     *
     * @param id 对话ID
     * @param request 请求对象，用于获取当前用户信息
     * @return 对话详情
     */
    @GetMapping("/{id}")
    @ApiOperation(value = "获取对话详情", notes = "根据对话ID获取对话详情")
    public Result<DialogSessionDTO> getDialogDetail(@ApiParam(value = "对话ID") @PathVariable Long id, HttpServletRequest request) {
        // 从请求头获取token
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return dialogService.getDialogDetail(id, token);
    }

    /**
     * 发送消息
     *
     * @param id     对话ID
     * @param params 发送消息参数，包含content、type、media_urls等
     * @param request 请求对象，用于获取当前用户信息
     * @return 发送结果
     */
    @PostMapping("/{id}/messages")
    @ApiOperation(value = "发送消息", notes = "向对话发送消息")
    public Result<DialogSessionDTO.MessageDTO> sendMessage(@ApiParam(value = "对话ID") @PathVariable Long id, @ApiParam(value = "发送消息参数") @RequestBody Map<String, Object> params, HttpServletRequest request) {
        // 从请求头获取token
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        return dialogService.sendMessage(id, token, params);
    }

    /**
     * 获取消息列表
     *
     * @param id    对话ID
     * @param page  页码，默认 1
     * @param size  每页数量，默认 50
     * @param before 分页标记，获取此时间戳之前的消息
     * @param request 请求对象，用于获取当前用户信息
     * @return 消息列表
     */
    @GetMapping("/{id}/messages")
    @ApiOperation(value = "获取消息列表", notes = "获取对话的消息列表")
    public Result<PageResponseDTO<DialogSessionDTO.MessageDTO>> getMessageList(
            @ApiParam(value = "对话ID") @PathVariable Long id,
            @ApiParam(value = "页码", defaultValue = "1") @RequestParam(defaultValue = "1") Integer page,
            @ApiParam(value = "每页数量", defaultValue = "50") @RequestParam(defaultValue = "50") Integer size,
            @ApiParam(value = "分页标记") @RequestParam(required = false) Long before,
            HttpServletRequest request) {
        // 从请求头获取token
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return dialogService.getMessageList(id, token, page, size, before);
    }

    /**
     * 更新消息状态
     *
     * @param id     对话ID
     * @param params 更新参数，包含last_read_message_id等
     * @param request 请求对象，用于获取当前用户信息
     * @return 更新结果
     */
    @PutMapping("/{id}/read-status")
    @ApiOperation(value = "更新消息状态", notes = "更新对话的消息阅读状态")
    public Result<DialogSessionDTO> updateMessageStatus(@ApiParam(value = "对话ID") @PathVariable Long id, @ApiParam(value = "更新参数") @RequestBody Map<String, Object> params, HttpServletRequest request) {
        // 从请求头获取token
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return dialogService.updateMessageStatus(id, token, params);
    }

    /**
     * 归档/取消归档对话
     *
     * @param id     对话ID
     * @param params 更新参数，包含is_archived等
     * @param request 请求对象，用于获取当前用户信息
     * @return 更新结果
     */
    @PutMapping("/{id}/archive")
    @ApiOperation(value = "归档/取消归档对话", notes = "归档或取消归档对话")
    public Result<DialogSessionDTO> archiveDialog(@ApiParam(value = "对话ID") @PathVariable Long id, @ApiParam(value = "更新参数") @RequestBody Map<String, Object> params, HttpServletRequest request) {
        // 从请求头获取token
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return dialogService.archiveDialog(id, token, params);
    }
}
