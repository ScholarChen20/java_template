package com.example.yoyo_data.support.exception;

/**
 * 参数验证异常 - 表示请求参数验证失败
 * 用于参数验证层抛出验证失败错误
 *
 * @author Template Framework
 * @version 1.0
 */
public class ValidationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private int code;
    private String message;
    private String field;

    /**
     * 默认构造方法
     */
    public ValidationException() {
        this(400, "参数验证失败");
    }

    /**
     * 根据错误信息构造
     *
     * @param message 错误信息
     */
    public ValidationException(String message) {
        this(400, message);
    }

    /**
     * 根据错误码和错误信息构造
     *
     * @param code 错误码
     * @param message 错误信息
     */
    public ValidationException(int code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    /**
     * 根据字段名和错误信息构造
     *
     * @param field 字段名
     * @param message 错误信息
     */
    public ValidationException(String field, String message) {
        this(400, message);
        this.field = field;
    }

    /**
     * 根据错误码、字段名和错误信息构造
     *
     * @param code 错误码
     * @param field 字段名
     * @param message 错误信息
     */
    public ValidationException(int code, String field, String message) {
        super(message);
        this.code = code;
        this.field = field;
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

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }
}
