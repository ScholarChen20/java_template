package com.example.yoyo_data.controller;

import com.example.yoyo_data.common.Result;
import com.example.yoyo_data.common.pojo.UserProfile;
import com.example.yoyo_data.common.pojo.Users;
import com.example.yoyo_data.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 用户控制器
 * 处理用户信息相关操作
 */
@Slf4j
@RestController
@RequestMapping("/api/users")
@Api(tags = "用户模块")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 获取当前用户信息
     *
     * @param request 请求对象，用于获取当前用户信息
     * @return 当前用户信息
     */
    @GetMapping("/me")
    @ApiOperation(value = "获取当前用户信息", notes = "获取当前登录用户的详细信息")
    public Result<Users> getCurrentUser(HttpServletRequest request) {
        // 从请求头获取token
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return userService.getCurrentUser(token);
    }

    /**
     * 更新当前用户信息
     *
     * @param params 更新参数，包含phone、avatar_url、bio等
     * @param request 请求对象，用于获取当前用户信息
     * @return 更新结果
     */
    @PutMapping("/me")
    @ApiOperation(value = "更新当前用户信息", notes = "更新当前登录用户的信息")
    public Result<Users> updateCurrentUser(@ApiParam(value = "更新参数") @RequestBody Map<String, Object> params, HttpServletRequest request) {
        // 从请求头获取token
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return userService.updateCurrentUser(token, params);
    }

    /**
     * 获取当前用户档案
     *
     * @param request 请求对象，用于获取当前用户信息
     * @return 当前用户档案
     */
    @GetMapping("/me/profile")
    @ApiOperation(value = "获取当前用户档案", notes = "获取当前登录用户的详细档案信息")
    public Result<UserProfile> getCurrentUserProfile(HttpServletRequest request) {
        // 从请求头获取token
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return userService.getCurrentUserProfile(token);
    }

    /**
     * 更新当前用户档案
     *
     * @param params 更新参数，包含full_name、gender、birth_date、location、travel_preferences、visited_cities等
     * @param request 请求对象，用于获取当前用户信息
     * @return 更新结果
     */
    @PutMapping("/me/profile")
    @ApiOperation(value = "更新当前用户档案", notes = "更新当前登录用户的档案信息")
    public Result<UserProfile> updateCurrentUserProfile(@ApiParam(value = "更新参数") @RequestBody Map<String, Object> params, HttpServletRequest request) {
        // 从请求头获取token
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return userService.updateCurrentUserProfile(token, params);
    }

    /**
     * 获取其他用户信息
     *
     * @param id 用户ID
     * @return 用户信息
     */
    @GetMapping("/{id}")
    @ApiOperation(value = "获取其他用户信息", notes = "根据用户ID获取用户的公开信息")
    public Result<Users> getUserById(@ApiParam(value = "用户ID") @PathVariable Long id) {
        return userService.getUserById(id);
    }
}
