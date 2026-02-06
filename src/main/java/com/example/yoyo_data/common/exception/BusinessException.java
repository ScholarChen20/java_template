package com.example.yoyo_data.common.exception;

/**
 * 业务异常 - 表示业务逻辑错误
 * 用于业务层抛出已知的业务错误
 *
 * @author Template Framework
 * @version 1.0
 */
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private int code;
    private String message;

    /**
     * 默认构造方法
     */
    public BusinessException() {
        this(400, "业务处理异常");
    }

    /**
     * 根据错误信息构造
     *
     * @param message 错误信息
     */
    public BusinessException(String message) {
        this(400, message);
    }

    /**
     * 根据错误码和错误信息构造
     *
     * @param code 错误码
     * @param message 错误信息
     */
    public BusinessException(int code, String message) {
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
    public BusinessException(String message, Throwable cause) {
        this(400, message, cause);
    }

    /**
     * 根据错误码、错误信息和原因构造
     *
     * @param code 错误码
     * @param message 错误信息
     * @param cause 原因异常
     */
    public BusinessException(int code, String message, Throwable cause) {
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
