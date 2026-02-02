package com.example.yoyo_data.infrastructure.utils;

import com.example.yoyo_data.support.dto.JwtUserDTO;
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

    @Value("${jwt.secret:mySecretKeyForJwtTokenGenerationAndValidation1234567890}")
    private String secret;

    @Value("${jwt.expiration:7200000}")
    private Long expiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
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