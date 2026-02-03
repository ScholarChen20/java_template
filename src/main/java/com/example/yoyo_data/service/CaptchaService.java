package com.example.yoyo_data.service;

import com.example.yoyo_data.common.dto.CaptchaDTO;

public interface CaptchaService {
    /**
     * 获取验证码
     * @param username
     * @return
     */
    CaptchaDTO getCaptcha(String username);

    /**
     *  验证码验证
     * @param username
     * @param uuid
     * @param captcha
     * @return
     */
    boolean validateCaptcha(String username, String uuid, String captcha);
}
