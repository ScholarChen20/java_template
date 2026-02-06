package com.example.yoyo_data.util.encrypt;

import lombok.extern.slf4j.Slf4j;
import org.mindrot.jbcrypt.BCrypt;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * HashUtils 类
 */
@Slf4j
public class HashUtils {
    public static String hash(String input) throws NoSuchAlgorithmException {
        // 创建一个MD5对象
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        byte[] hashBytes = sha1.digest(input.getBytes(StandardCharsets.UTF_8));

        StringBuilder hexString = new StringBuilder();
        for (byte b : hashBytes) {
            String hex = Integer.toHexString(0xff & b);  // 获取字节的16进制表示，并确保长度为2位
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    /**
     * 哈希密码
     *
     * @param password 明文密码
     * @return 哈希后的密码
     */
    public static String hashPassword(String password) {
        // bcrypt限制密码长度为72字节，需要手动截断（按字节计算）
        byte[] passwordBytes = password.getBytes(StandardCharsets.UTF_8);
        byte[] truncatedPasswordBytes = new byte[Math.min(passwordBytes.length, 72)];
        System.arraycopy(passwordBytes, 0, truncatedPasswordBytes, 0, truncatedPasswordBytes.length);

        // 调试日志
        log.debug("原始密码长度: " + password.length() + " 字符, " + passwordBytes.length + " 字节");
        log.debug("截断后密码长度: " + truncatedPasswordBytes.length + " 字节");

        // 使用bcrypt库进行哈希
        String salt = BCrypt.gensalt(12);
        String hashedPassword = BCrypt.hashpw(new String(truncatedPasswordBytes, StandardCharsets.UTF_8), salt);
        return hashedPassword;
    }
}
