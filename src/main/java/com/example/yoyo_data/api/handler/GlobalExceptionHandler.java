package com.example.yoyo_data.api.handler;

import com.example.yoyo_data.common.Result;
import com.example.yoyo_data.support.exception.BusinessException;
import com.example.yoyo_data.support.exception.SystemException;
import com.example.yoyo_data.support.exception.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.time.LocalDateTime;

/**
 * 全局异常处理器 - 统一处理所有异常
 * 使用@RestControllerAdvice注解拦截所有异常并返回统一的Result格式
 *
 * @author Template Framework
 * @version 1.0
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     *
     * @param e 业务异常
     * @return 统一响应
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleBusinessException(BusinessException e) {
        log.warn("业务异常: {}", e.getMessage());
        return Result.<Void>builder()
                .code(e.getCode())
                .message(e.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 处理参数验证异常
     *
     * @param e 验证异常
     * @return 统一响应
     */
    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleValidationException(ValidationException e) {
        log.warn("参数验证异常: {}", e.getMessage());
        return Result.<Void>builder()
                .code(e.getCode())
                .message(e.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 处理系统异常
     *
     * @param e 系统异常
     * @return 统一响应
     */
    @ExceptionHandler(SystemException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleSystemException(SystemException e) {
        log.error("系统异常", e);
        return Result.<Void>builder()
                .code(e.getCode())
                .message(e.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 处理方法参数验证失败异常（Spring内置）
     *
     * @param e 验证异常
     * @return 统一响应
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        String message;
        if (e.getBindingResult().getFieldError() != null) {
            message = e.getBindingResult().getFieldError().getField() + ": " + e.getBindingResult().getFieldError().getDefaultMessage();
        } else {
            message = "参数验证失败";
        }
        log.warn("参数验证失败: {}", message);
        return Result.<Void>builder()
                .code(400)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 处理参数类型不匹配异常
     *
     * @param e 类型不匹配异常
     * @return 统一响应
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException e) {
        String message = String.format("参数类型错误: %s 应该是 %s 类型",
                e.getName(), e.getRequiredType().getSimpleName());
        log.warn("参数类型错误: {}", message);
        return Result.<Void>builder()
                .code(400)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 处理404异常
     *
     * @param e 404异常
     * @return 统一响应
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result<Void> handleNoHandlerFound(NoHandlerFoundException e) {
        log.warn("请求路径不存在: {} {}", e.getHttpMethod(), e.getRequestURL());
        return Result.<Void>builder()
                .code(404)
                .message("请求的资源不存在")
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 处理所有其他异常
     *
     * @param e 异常
     * @return 统一响应
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleException(Exception e) {
        log.error("未捕获的异常", e);
        return Result.<Void>builder()
                .code(500)
                .message("系统内部错误，请稍后重试")
                .timestamp(LocalDateTime.now())
                .build();
    }
}
