package com.example.yoyo_data.utils;

import com.example.yoyo_data.common.dto.JwtUserDTO;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtils {
    private static final Logger log = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration:7200000}")
    private Long expiration;

    // 缓存密钥，确保整个应用生命周期使用同一个密钥
    private SecretKey cachedKey;

    private SecretKey getSigningKey() {
        if (cachedKey != null) {
            return cachedKey;
        }

        try {
            // 尝试使用配置的密钥
            SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());
            if (key.getEncoded().length >= 64) {
                log.info("使用配置的 JWT 密钥");
                cachedKey = key;
                return cachedKey;
            }
            log.warn("配置的 JWT 密钥大小不够安全（需要至少64字节），当前大小: {} 字节", key.getEncoded().length);
        } catch (Exception e) {
            log.warn("使用配置的 JWT 密钥失败", e);
        }

        // 使用配置密钥的扩展版本作为固定密钥（确保一致性）
        log.warn("使用配置密钥的扩展版本作为签名密钥");
        String extendedSecret = secret;
        // 如果密钥不够长，重复拼接直到满足长度要求
        while (extendedSecret.getBytes().length < 64) {
            extendedSecret += secret;
        }
        cachedKey = Keys.hmacShaKeyFor(extendedSecret.substring(0, 64).getBytes());
        return cachedKey;
    }

    public String generateToken(JwtUserDTO jwtUser) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", jwtUser.getId());
        claims.put("username", jwtUser.getUsername());
        claims.put("nickname", jwtUser.getNickname());
        claims.put("roles", jwtUser.getRoles());
        claims.put("permissions", jwtUser.getPermissions());
        return generateToken(claims, jwtUser.getUsername());
    }

    private String generateToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    public String getUsernameFromToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return claims != null ? claims.getSubject() : null;
        } catch (Exception e) {
            log.error("获取用户名失败", e);
            return null;
        }
    }

    public Long getUserIdFromToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return claims != null ? claims.get("userId", Long.class) : null;
        } catch (Exception e) {
            log.error("获取用户ID失败", e);
            return null;
        }
    }

    public Date getExpirationDateFromToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return claims != null ? claims.getExpiration() : null;
        } catch (Exception e) {
            log.error("获取过期时间失败", e);
            return null;
        }
    }

    public Claims getClaimsFromToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            log.error("解析Token失败", e);
            return null;
        }
    }

    public Boolean isTokenExpired(String token) {
        try {
            Date expiration = getExpirationDateFromToken(token);
            return expiration.before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    public Boolean validateToken(String token, String username) {
        try {
            String tokenUsername = getUsernameFromToken(token);
            return tokenUsername != null && tokenUsername.equals(username) && !isTokenExpired(token);
        } catch (Exception e) {
            log.error("验证Token失败", e);
            return false;
        }
    }

    public Boolean validateToken(String token) {
        try {
            return !isTokenExpired(token);
        } catch (Exception e) {
            log.error("验证Token失败", e);
            return false;
        }
    }

    public String refreshToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            if (claims != null) {
                Map<String, Object> newClaims = new HashMap<>(claims);
                newClaims.remove(Claims.EXPIRATION);
                return generateToken(newClaims, claims.getSubject());
            }
        } catch (Exception e) {
            log.error("刷新Token失败", e);
        }
        return null;
    }
}
