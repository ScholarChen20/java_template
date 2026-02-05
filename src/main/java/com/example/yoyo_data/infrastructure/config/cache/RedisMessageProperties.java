package com.example.yoyo_data.infrastructure.config.cache;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author panzn
 * @date 2023/8/18 00:50
 * @description TODO
 */
@Configuration
@Data
@ConfigurationProperties(prefix = RedisMessageProperties.PREFIX)
public class RedisMessageProperties {

    public static final String PREFIX = "spring.redis";

    private String host;

    private Integer port;

    private String password;

    private Integer database;

    private Integer maxIdle;

    private Integer minIdle;

    private Integer maxActive;

    private Integer maxWait;

    private Integer timeBetweenEvictionRunsMillis;

    private Integer minEvictableIdleTimeMillis;

    private Integer numTestsPerEvictionRun;

    private Boolean testOnBorrow;

    private Boolean testOnReturn;

    private Boolean testWhileIdle;

    private Boolean blockWhenExhausted;

    private Lettuce lettuce;

    private Integer timeout;

    private String databaseList;

    @Data
    public class Lettuce {

        private Pool pool;

    }


    @Data
    public static class Pool {

        private Integer maxIdle;

        private Integer minIdle;

        private Integer maxActive;

        private Integer maxWait;

        private Integer timeBetweenEvictionRunsMillis;

        private Integer minEvictableIdleTimeMillis;

        private Integer numTestsPerEvictionRun;

        private Boolean testOnBorrow;

        private Boolean testOnReturn;

        private Boolean testWhileIdle;

        private Boolean blockWhenExhausted;

    }
}
