package com.example.yoyo_data.service.impl;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.example.yoyo_data.cache.RedisService;
import com.example.yoyo_data.common.dto.CaptchaDTO;
import com.example.yoyo_data.service.CaptchaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.example.yoyo_data.data.cache.CacheKeyManager.CAPTCHA_KEY_PREFIX;
import static com.example.yoyo_data.data.cache.CacheKeyManager.CacheTTL.FIVE_MINUTES;

@Service
@Slf4j
public class CaptchaServiceImpl implements CaptchaService {

    @Autowired
    private RedisService redisService;

    @Override
    public CaptchaDTO getCaptcha(String username) {
        // 1.图形随机验证码
        LineCaptcha captcha = CaptchaUtil.createLineCaptcha(200, 100);
        captcha.setTextAlpha(0.8f);  // 透明度
        log.info("生成验证码：{}", captcha.getCode());

        // 2.转为base64
        String imageBase64 = captcha.getImageBase64Data();

        // 3. 保存验证码至redis
        redisService.stringSetString(CAPTCHA_KEY_PREFIX + username, captcha.getCode(), FIVE_MINUTES);
        log.info("验证码保存成功");

        // 4. 生成uuid
        String uuid = IdUtil.fastSimpleUUID();

        // 5. 构建返回结果
        CaptchaDTO captchaDTO = CaptchaDTO.builder()
                .uuid(uuid)
                .img(imageBase64)
                .build();

        return captchaDTO;
    }

    @Override
    public boolean validateCaptcha(String username, String uuid, String captcha) {
        if (StrUtil.isEmpty(uuid) || StrUtil.isEmpty(captcha)) {
            log.error("验证码参数不能为空");
            return false;
        }
        log.info("接收到验证码: uuId:{}, 验证码:{}", uuid, captcha);

        String code = redisService.stringGetString(CAPTCHA_KEY_PREFIX + username);
        return code != null && code.equals(captcha);
    }
}
