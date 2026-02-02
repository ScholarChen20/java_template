package com.example.yoyo_data.controller;

import com.example.yoyo_data.common.Result;
import com.example.yoyo_data.service.CaptchaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController("/api/common")
public class CommonController {
    @Autowired
    private CaptchaService captchaService;

    /**
     * 随机验证码生成
     * @param accountId
     * @return
     */
    @GetMapping("/captcha")
    public Result<String> captcha(String accountId) {
        String captcha = captchaService.getCaptcha(accountId);
        return Result.success(captcha);
    }
}
