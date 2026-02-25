package com.example.yoyo_data.controller;

import com.example.yoyo_data.common.Result;
import com.example.yoyo_data.common.dto.CaptchaDTO;
import com.example.yoyo_data.common.dto.request.CaptchaVerifyRequest;
import com.example.yoyo_data.service.CaptchaService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 风控模块控制器
 * 提供图形验证码的获取与校验，用于抢票前的人机验证
 *
 * 在抢票流程中的位置：
 *   Step 5: GET  /api/risk/captcha          - 获取图形验证码
 *   Step 6: POST /api/risk/captcha/verify   - 校验图形验证码，通过后方可提交抢票
 */
@Slf4j
@RestController
@RequestMapping("/api/risk")
@Api(tags = "风控模块", description = "图形验证码获取与校验，用于人机验证防刷票")
public class RiskController {

    @Autowired
    private CaptchaService captchaService;

    /**
     * Step 5：获取图形验证码
     * 抢票前必须先通过验证码验证，防止机器人刷票。
     * 返回 Base64 格式的验证码图片，前端直接渲染展示。
     */
    @GetMapping("/captcha")
    @ApiOperation(value = "获取图形验证码",
            notes = "抢票前调用，生成图形验证码并缓存至 Redis（5分钟有效）。返回 Base64 验证码图片及唯一 UUID，验证时携带 UUID 和用户输入的验证码内容。")
    public Result<CaptchaDTO> getCaptcha(
            @ApiParam(value = "用户名（用于绑定验证码）", required = true, example = "zhangsan")
            @RequestParam("username") String username
    ) {
        log.info("获取验证码请求: username={}", username);
        CaptchaDTO captchaDTO = captchaService.getCaptcha(username);
        return Result.success(captchaDTO);
    }

    /**
     * Step 6：校验图形验证码
     * 提交抢票之前必须先通过本接口校验验证码。
     * 验证通过后，前端方可提交 POST /api/ticket/grab 请求。
     */
    @PostMapping("/captcha/verify")
    @ApiOperation(value = "校验图形验证码",
            notes = "校验用户输入的验证码是否正确。验证通过（true）后才能提交抢票请求，防止机器刷票。")
    public Result<Boolean> verifyCaptcha(
            @Valid @RequestBody CaptchaVerifyRequest request
    ) {
        log.info("校验验证码请求: username={}, uuid={}", request.getUsername(), request.getUuid());
        boolean valid = captchaService.validateCaptcha(
                request.getUsername(),
                request.getUuid(),
                request.getCaptcha()
        );
        if (!valid) {
            log.warn("验证码校验失败: username={}", request.getUsername());
            return Result.badRequest("验证码错误或已过期，请重新获取");
        }
        log.info("验证码校验通过: username={}", request.getUsername());
        return Result.success(true);
    }
}
