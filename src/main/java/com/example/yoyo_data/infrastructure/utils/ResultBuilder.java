package com.example.yoyo_data.infrastructure.utils;

import com.example.yoyo_data.common.Result;

/**
 * 结果构建工具 - 提供流式API用于构建Result对象
 *
 * @author Template Framework
 * @version 1.0
 */
public class ResultBuilder {

    private int code;
    private String message;
    private Object data;

    /**
     * 开始构建成功结果
     *
     * @param data 返回数据
     * @return 构建器实例
     */
    public static ResultBuilder success(Object data) {
        ResultBuilder builder = new ResultBuilder();
        builder.code = 200;
        builder.message = "success";
        builder.data = data;
        return builder;
    }

    /**
     * 开始构建成功结果（无数据）
     *
     * @return 构建器实例
     */
    public static ResultBuilder success() {
        return success(null);
    }

    /**
     * 开始构建错误结果
     *
     * @param code 错误码
     * @param message 错误信息
     * @return 构建器实例
     */
    public static ResultBuilder error(int code, String message) {
        ResultBuilder builder = new ResultBuilder();
        builder.code = code;
        builder.message = message;
        builder.data = null;
        return builder;
    }

    /**
     * 开始构建错误结果（默认500）
     *
     * @param message 错误信息
     * @return 构建器实例
     */
    public static ResultBuilder error(String message) {
        return error(500, message);
    }

    /**
     * 开始构建400错误结果
     *
     * @param message 错误信息
     * @return 构建器实例
     */
    public static ResultBuilder badRequest(String message) {
        return error(400, message);
    }

    /**
     * 开始构建401错误结果
     *
     * @param message 错误信息
     * @return 构建器实例
     */
    public static ResultBuilder unauthorized(String message) {
        return error(401, message);
    }

    /**
     * 开始构建403错误结果
     *
     * @param message 错误信息
     * @return 构建器实例
     */
    public static ResultBuilder forbidden(String message) {
        return error(403, message);
    }

    /**
     * 开始构建404错误结果
     *
     * @param message 错误信息
     * @return 构建器实例
     */
    public static ResultBuilder notFound(String message) {
        return error(404, message);
    }

    /**
     * 设置错误码
     *
     * @param code 错误码
     * @return 当前构建器实例
     */
    public ResultBuilder code(int code) {
        this.code = code;
        return this;
    }

    /**
     * 设置消息
     *
     * @param message 消息
     * @return 当前构建器实例
     */
    public ResultBuilder message(String message) {
        this.message = message;
        return this;
    }

    /**
     * 设置数据
     *
     * @param data 数据
     * @return 当前构建器实例
     */
    public ResultBuilder data(Object data) {
        this.data = data;
        return this;
    }

    /**
     * 构建Result对象
     *
     * @return Result对象
     */
    public Result<?> build() {
        return Result.builder()
                .code(code)
                .message(message)
                .data(data)
                .timestamp(DateTimeUtils.getCurrentDateTime())
                .build();
    }
}
