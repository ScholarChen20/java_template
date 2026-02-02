package com.example.yoyo_data.infrastructure.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * MinIO配置属性 - 从application.yml读取MinIO配置
 * MinIO是一个兼容AWS S3的对象存储服务
 *
 * @author Template Framework
 * @version 1.0
 */
@Data
@Component
@ConfigurationProperties(prefix = "minio")
public class MinIOProperties {

    /**
     * MinIO服务器地址
     * 格式: http://host:port
     */
    private String endpoint;

    /**
     * MinIO API访问Key
     */
    private String accessKey;

    /**
     * MinIO API秘钥
     */
    private String secretKey;

    /**
     * 默认的Bucket名称
     */
    private String bucketName;

    /**
     * 是否使用https
     */
    private boolean secure = false;

    /**
     * 连接超时时间（毫秒）
     */
    private long connectTimeout = 10000;

    /**
     * 写超时时间（毫秒）
     */
    private long writeTimeout = 10000;

    /**
     * 读超时时间（毫秒）
     */
    private long readTimeout = 10000;

    /**
     * 分片大小（字节）
     */
    private long partSize = 5242880; // 5MB

    /**
     * 文件过期时间（天数，0表示不过期）
     */
    private int expirationDays = 0;
}
