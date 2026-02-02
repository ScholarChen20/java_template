package com.example.yoyo_data.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 统一响应类
 * 使用泛型支持不同类型的返回数据
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 响应状态码
     * 200: 成功
     * 400: 请求错误
     * 401: 未授权
     * 403: 禁止访问
     * 404: 资源不存在
     * 500: 服务器内部错误
     */
    private int code;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 响应时间戳
     */
    private LocalDateTime timestamp;

    /**
     * 成功响应
     *
     * @param data 响应数据
     * @param <T>  数据类型
     * @return 响应对象
     */
    public static <T> Result<T> success(T data) {
        return Result.<T>builder()
                .code(200)
                .message("success")
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 成功响应（无数据）
     *
     * @param <T> 数据类型
     * @return 响应对象
     */
    public static <T> Result<T> success() {
        return success(null);
    }

    /**
     * 失败响应
     *
     * @param code    错误码
     * @param message 错误消息
     * @param <T>     数据类型
     * @return 响应对象
     */
    public static <T> Result<T> error(int code, String message) {
        return Result.<T>builder()
                .code(code)
                .message(message)
                .data(null)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 失败响应（默认500错误）
     *
     * @param message 错误消息
     * @param <T>     数据类型
     * @return 响应对象
     */
    public static <T> Result<T> error(String message) {
        return error(500, message);
    }

    /**
     * 失败响应（默认400错误）
     *
     * @param message 错误消息
     * @param <T>     数据类型
     * @return 响应对象
     */
    public static <T> Result<T> badRequest(String message) {
        return error(400, message);
    }

    /**
     * 失败响应（默认401错误）
     *
     * @param message 错误消息
     * @param <T>     数据类型
     * @return 响应对象
     */
    public static <T> Result<T> unauthorized(String message) {
        return error(401, message);
    }

    /**
     * 失败响应（默认403错误）
     *
     * @param message 错误消息
     * @param <T>     数据类型
     * @return 响应对象
     */
    public static <T> Result<T> forbidden(String message) {
        return error(403, message);
    }

    /**
     * 失败响应（默认404错误）
     *
     * @param message 错误消息
     * @param <T>     数据类型
     * @return 响应对象
     */
    public static <T> Result<T> notFound(String message) {
        return error(404, message);
    }
}