package com.example.yoyo_data.infrastructure.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT配置属性 - 从application.yml读取JWT配置
 *
 * @author Template Framework
 * @version 1.0
 */
@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /**
     * JWT密钥
     */
    private String secret;

    /**
     * 访问令牌过期时间（毫秒）
     */
    private long expiration;

    /**
     * 令牌过期时间（毫秒）- 备用配置
     */
    private long tokenExpire;

    /**
     * 令牌类型（默认：Bearer）
     */
    private String tokenType = "Bearer";

    /**
     * 令牌头名称（默认：Authorization）
     */
    private String headerName = "Authorization";

    /**
     * 获取有效的过期时间
     *
     * @return 过期时间（毫秒）
     */
    public long getValidExpiration() {
        if (expiration > 0) {
            return expiration;
        }
        if (tokenExpire > 0) {
            return tokenExpire;
        }
        return 7200000; // 默认2小时
    }
}
