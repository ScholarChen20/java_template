package com.example.yoyo_data.service.impl;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.GifCaptcha;
import com.example.yoyo_data.cache.RedisService;
import com.example.yoyo_data.service.CaptchaService;
import com.example.yoyo_data.utils.CaptchaUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CaptchaServiceImpl implements CaptchaService {
    private static final String CAPTCHA_KEY_PREFIX = "captcha:";

    @Autowired
    private RedisService redisService;
    @Override
    public String getCaptcha(String accountId) {
        // 1. 随机生成四位数字
        String captcha = String.valueOf((int)(Math.random() * 10000));
        log.info("生成验证码：{}", captcha);

        // 2.图像生成
        GifCaptcha gifCaptcha = CaptchaUtil.createGifCaptcha(100, 40);
        log.info("验证码图片保存成功");

        // 3.转为base64
        String imageBase64 = gifCaptcha.getImageBase64();

        // 4. 保存验证码至redis
        redisService.stringSetString(CAPTCHA_KEY_PREFIX + accountId, captcha);
        log.info("验证码保存成功");

        return imageBase64;
    }
}
