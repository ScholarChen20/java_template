package com.example.yoyo_data.controller;

import com.example.yoyo_data.common.Result;
import com.example.yoyo_data.common.dto.CaptchaDTO;
import com.example.yoyo_data.common.vo.CaptchaVO;
import com.example.yoyo_data.service.CaptchaService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/common")
@Api(tags = "通用模块", description = "随机验证码图片生成")
public class CommonController {
    @Autowired
    private CaptchaService captchaService;

    /**
     * 随机验证码生成
     * @param name
     * @return
     */
    @GetMapping("/captcha")
    @ApiOperation("随机验证码生成")
    public Result<CaptchaDTO> captcha(@RequestParam  String name) {
        log.info("用户名：{}", name);
        CaptchaDTO captcha = captchaService.getCaptcha(name);
        return Result.success(captcha);
    }
    /**
     * 验证码验证
     */
    @PostMapping("/captcha/validate")
    @ApiOperation("验证码验证")
    public Result<Boolean> validateCaptcha(@RequestBody CaptchaVO captchaVO) {
        String uuid = captchaVO.getUuid();
        String img = captchaVO.getImg();
        String username = captchaVO.getUsername();
        return Result.success(captchaService.validateCaptcha(username, uuid, img));
    }
}
