package com.example.yoyo_data.utils;

import org.apache.commons.codec.binary.Base64;

public class CaptchaUtils {
    // 随即四位数字生成特定长宽高的图片验证码
    public static String generateCaptchaImage(String captcha) {
        return "data:image/png;base64," + Base64.encodeBase64String(captcha.getBytes());
    }
}
