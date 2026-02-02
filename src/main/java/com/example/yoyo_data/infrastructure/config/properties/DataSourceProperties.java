package com.example.yoyo_data.infrastructure.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 数据源配置属性 - 从application.yml读取数据库配置
 *
 * @author Template Framework
 * @version 1.0
 */
@Data
@Component
@ConfigurationProperties(prefix = "spring.datasource")
public class DataSourceProperties {

    /**
     * JDBC驱动类名
     */
    private String driverClassName;

    /**
     * 数据库连接URL
     */
    private String url;

    /**
     * 数据库用户名
     */
    private String username;

    /**
     * 数据库密码
     */
    private String password;

    /**
     * 连接池类型
     */
    private String type;

    /**
     * 是否自动提交事务
     */
    private boolean autoCommit = true;

    /**
     * 连接超时时间（毫秒）
     */
    private long connectionTimeout = 30000;

    /**
     * 空闲连接超时时间（毫秒）
     */
    private long idleTimeout = 600000;

    /**
     * 最大连接生存时间（毫秒）
     */
    private long maxLifetime = 1800000;

    /**
     * 最大连接数
     */
    private int maximumPoolSize = 10;

    /**
     * 最小空闲连接数
     */
    private int minimumIdle = 5;
}
