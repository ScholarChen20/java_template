package com.example.yoyo_data.controller;

import com.example.yoyo_data.common.Result;
import com.example.yoyo_data.common.dto.request.*;
import com.example.yoyo_data.common.dto.response.*;
import com.example.yoyo_data.service.AuthService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 认证模块控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@Api(tags = "认证模块", description = "用户登录、注册、刷新token等认证相关操作")
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * 用户登录
     *
     * @param request 登录请求
     * @return 登录结果，包含用户信息和token
     */
    @PostMapping("/login")
    @ApiOperation(value = "用户登录", notes = "用户登录接口，返回token和用户信息")
    public Result<LoginResponse> login(
            @ApiParam(value = "登录请求参数", required = true) @RequestBody LoginRequest request
    ) {
        log.info("用户登录: username={}", request.getUsername());
        return authService.login(request);
    }

    /**
     * 用户注册
     *
     * @param request 注册请求
     * @return 注册结果，包含用户信息
     */
    @PostMapping("/register")
    @ApiOperation(value = "用户注册", notes = "用户注册接口，返回用户信息")
    public Result<?> register(
            @ApiParam(value = "注册请求参数", required = true) @RequestBody RegisterRequest request
    ) {
        log.info("用户注册: username={}, email={}", request.getUsername(), request.getEmail());
        return authService.register(request);
    }

    /**
     * 刷新Token
     *
     * @param token 当前token
     * @return 刷新结果，包含新token和过期时间
     */
    @PostMapping("/refresh")
    @ApiOperation(value = "刷新Token", notes = "刷新token接口，返回新token")
    public Result<TokenResponse> refreshToken(
            @ApiParam(value = "当前token", required = true) @RequestParam("token") String token
    ) {
        log.info("刷新Token");
        return authService.refreshToken(token);
    }

    /**
     * 发送验证码
     *
     * @param request 发送验证码请求
     * @return 发送结果
     */
    @PostMapping("/send-code")
    @ApiOperation(value = "发送验证码", notes = "发送验证码接口，用于注册、找回密码等场景")
    public Result<SendCodeResponse> sendCode(
            @ApiParam(value = "发送验证码请求参数", required = true) @RequestBody SendCodeRequest request
    ) {
        log.info("发送验证码: email={}", request.getEmail());
        return authService.sendCode(request);
    }

    /**
     * 验证邮箱
     *
     * @param request 验证邮箱请求
     * @return 验证结果
     */
    @PostMapping("/verify-email")
    @ApiOperation(value = "验证邮箱", notes = "验证邮箱接口，用于激活账户")
    public Result<VerifyEmailResponse> verifyEmail(
            @ApiParam(value = "验证邮箱请求参数", required = true) @RequestBody VerifyEmailRequest request
    ) {
        log.info("验证邮箱: email={}", request.getEmail());
        return authService.verifyEmail(request);
    }
}