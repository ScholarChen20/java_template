package com.example.yoyo_data.infrastructure.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Redis配置属性 - 从application.yml读取Redis配置
 *
 * @author Template Framework
 * @version 1.0
 */
@Data
@Component
@ConfigurationProperties(prefix = "spring.redis")
public class RedisProperties {

    /**
     * Redis服务器地址
     */
    private String host;

    /**
     * Redis服务器端口
     */
    private int port;

    /**
     * Redis密码
     */
    private String password;

    /**
     * Redis数据库编号
     */
    private int database;

    /**
     * 连接超时时间（毫秒）
     */
    private long timeout;

    /**
     * Lettuce连接池配置
     */
    private Lettuce lettuce = new Lettuce();

    /**
     * Lettuce连接池配置
     */
    @Data
    public static class Lettuce {
        private Pool pool = new Pool();

        /**
         * 连接池配置
         */
        @Data
        public static class Pool {
            /**
             * 最小空闲连接数
             */
            private int minIdle;

            /**
             * 最大空闲连接数
             */
            private int maxIdle;

            /**
             * 最大活跃连接数
             */
            private int maxActive;

            /**
             * 连接等待超时时间
             */
            private long maxWait;
        }
    }
}
