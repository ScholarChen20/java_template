package com.example.yoyo_data.domain.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * 高并发请求DTO
 * 用于处理高流量的请求数据
 *
 * @author Template Framework
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HighTrafficRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 请求ID（唯一标识）
     */
    private String requestId;

    /**
     * 请求类型
     */
    @NotBlank(message = "请求类型不能为空")
    @Size(max = 50, message = "请求类型长度不能超过50个字符")
    private String requestType;

    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /**
     * 业务数据（JSON格式）
     */
    @NotBlank(message = "业务数据不能为空")
    private String businessData;

    /**
     * 优先级（0-10，默认5）
     */
    private int priority = 5;

    /**
     * 时间戳
     */
    private long timestamp;

    /**
     * 客户端IP
     */
    private String clientIp;

    /**
     * 客户端标识
     */
    private String clientId;

    /**
     * 操作类型
     */
    private String operation;

    /**
     * 额外参数
     */
    private String extraParams;
}