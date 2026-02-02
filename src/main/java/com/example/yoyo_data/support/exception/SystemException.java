package com.example.yoyo_data.support.exception;

/**
 * 系统异常基类 - 所有系统异常的父类
 * 用于表示系统内部错误（非业务错误）
 *
 * @author Template Framework
 * @version 1.0
 */
public class SystemException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private int code;
    private String message;

    /**
     * 默认构造方法
     */
    public SystemException() {
        this(500, "系统内部错误");
    }

    /**
     * 根据错误信息构造
     *
     * @param message 错误信息
     */
    public SystemException(String message) {
        this(500, message);
    }

    /**
     * 根据错误码和错误信息构造
     *
     * @param code 错误码
     * @param message 错误信息
     */
    public SystemException(int code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    /**
     * 根据错误信息和原因构造
     *
     * @param message 错误信息
     * @param cause 原因异常
     */
    public SystemException(String message, Throwable cause) {
        this(500, message, cause);
    }

    /**
     * 根据错误码、错误信息和原因构造
     *
     * @param code 错误码
     * @param message 错误信息
     * @param cause 原因异常
     */
    public SystemException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
