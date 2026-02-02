package com.example.yoyo_data.controller;

import com.example.yoyo_data.common.Result;
import com.example.yoyo_data.common.dto.JwtUserDTO;
import com.example.yoyo_data.common.vo.UserVO;
import com.example.yoyo_data.service.AuthService;
import com.example.yoyo_data.utils.JwtUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * 认证控制器
 * 处理用户登录、注册、刷新token等认证相关操作
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@Api(tags = "认证模块")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtUtils jwtUtils;

    /**
     * 用户注册
     *
     * @param params 注册参数，包含username、email、password、phone
     * @return 注册结果
     */
    @PostMapping("/register")
    @ApiOperation(value = "用户注册", notes = "用户注册接口")
    public Result<Map<String, Object>> register(@ApiParam(value = "注册参数") @RequestBody Map<String, Object> params) {
        return authService.register(params);
    }

    /**
     * 用户登录
     *
     * @param params 登录参数，包含username、password
     * @return 登录结果，包含token、用户信息、过期时间
     */
    @PostMapping("/login")
    @ApiOperation(value = "用户登录", notes = "用户登录接口")
    public Result<Map<String, Object>> login(@ApiParam(value = "登录参数") @RequestBody Map<String, Object> params) {
        return authService.login(params);
    }

    /**
     * 刷新Token
     *
     * @param request 请求对象，用于获取当前token
     * @return 刷新结果，包含新token和过期时间
     */
    @PostMapping("/refresh")
    @ApiOperation(value = "刷新Token", notes = "刷新Token接口")
    public Result<Map<String, Object>> refresh(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return authService.refreshToken(token);
    }

    /**
     * 用户登出
     *
     * @param request 请求对象，用于获取当前token
     * @return 登出结果
     */
    @PostMapping("/logout")
    @ApiOperation(value = "用户登出", notes = "用户登出接口")
    public Result<Void> logout(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return authService.logout(token);
    }

    /**
     * 发送验证码
     *
     * @param params 发送验证码参数，包含email、type
     * @return 发送结果
     */
    @PostMapping("/send-code")
    @ApiOperation(value = "发送验证码", notes = "发送验证码接口")
    public Result<Map<String, Object>> sendCode(@ApiParam(value = "发送验证码参数") @RequestBody Map<String, Object> params) {
        return authService.sendCode(params);
    }

    /**
     * 验证邮箱
     *
     * @param params 验证邮箱参数，包含email、code
     * @return 验证结果
     */
    @PostMapping("/verify-email")
    @ApiOperation(value = "验证邮箱", notes = "验证邮箱接口")
    public Result<Map<String, Object>> verifyEmail(@ApiParam(value = "验证邮箱参数") @RequestBody Map<String, Object> params) {
        return authService.verifyEmail(params);
    }
}
