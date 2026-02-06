package com.example.yoyo_data.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.yoyo_data.common.Result;
import com.example.yoyo_data.common.dto.request.UpdateUserProfileRequest;
import com.example.yoyo_data.common.pojo.UserProfile;
import com.example.yoyo_data.common.pojo.Users;
import com.example.yoyo_data.common.vo.UserPageVO;
import com.example.yoyo_data.infrastructure.repository.UserMapper;
import com.example.yoyo_data.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

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
    @Autowired
    private UserMapper userMapper;

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
     * @param request 请求对象，用于获取当前用户信息
     * @return 更新结果
     */
    @PutMapping("/me")
    @ApiOperation(value = "更新当前用户信息", notes = "更新当前登录用户的信息")
    public Result<Users> updateCurrentUser(@ApiParam(value = "更新参数") @RequestBody Users users, HttpServletRequest request) {
        // 从请求头获取token
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return userService.updateCurrentUser(token, users);
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
     * @param requestBody 更新参数，包含full_name、gender、birth_date、location、travel_preferences、visited_cities等
     * @param request 请求对象，用于获取当前用户信息
     * @return 更新结果
     */
    @PutMapping("/me/profile")
    @ApiOperation(value = "更新当前用户档案", notes = "更新当前登录用户的档案信息")
    public Result<UserProfile> updateCurrentUserProfile(@ApiParam(value = "更新参数") @RequestBody UpdateUserProfileRequest requestBody, HttpServletRequest request) {
        // 从请求头获取token
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return userService.updateCurrentUserProfile(token, requestBody);
    }

    /**
     * 获取其他用户信息
     * @param id 用户ID
     * @return 用户信息
     */
    @GetMapping("/{id}")
    @ApiOperation(value = "获取其他用户信息", notes = "根据用户ID获取用户的公开信息")
    public Result<Users> getUserById(@ApiParam(value = "用户ID") @PathVariable Long id) {
        return userService.getUserById(id);
    }

    /**
     * 获取分页用户列表--IPage和Page
     * @return 用户档案
     */
    @GetMapping("/list")
    @ApiOperation(value = "获取分页用户列表", notes = "获取分页的用户列表")
    public Result<Page<Users>> getUserList(@RequestBody UserPageVO userPageVO){
        log.info("获取分页用户列表，页码: {}, 每页数量: {}", userPageVO.getCurrent(), userPageVO.getSize());
        IPage<Users> page = new Page<>(userPageVO.getCurrent(), userPageVO.getSize());
        return Result.success(userMapper.getUserPage(userPageVO.getId(),  page));
    }

    /**
     * 获取分页用户列表--LambdaQueryChainWrapper
     * @return 用户档案
     */
    @GetMapping("/list/lambda")
    @ApiOperation(value = "获取分页用户列表-lambda", notes = "获取分页的用户列表")
    public Result<Page<Users>> getUserListLambda(@RequestBody UserPageVO userPageVO){
        log.info("获取分页用户列表-lambda，页码: {}, 每页数量: {}", userPageVO.getCurrent(), userPageVO.getSize());
        Page<Users> page = new Page<>(userPageVO.getCurrent(), userPageVO.getSize());
        LambdaQueryWrapper<Users> usersLambdaQueryWrapper = new LambdaQueryWrapper<>();
        usersLambdaQueryWrapper.eq(Users::getId, userPageVO.getId()); // 添加条件
        return Result.success(userMapper.selectPage(page, usersLambdaQueryWrapper));
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除用户", notes = "根据用户ID删除用户")
    public Result<Void> deleteUser(@ApiParam(value = "用户ID") @PathVariable Long id) {
        return userService.removeById(id) ? Result.success() : Result.error("删除用户失败");
    }
    /**
     * 验证用户是否激活
     */
    @GetMapping("/active")
    @ApiOperation(value = "验证用户是否激活", notes = "验证用户是否激活")
    public Result<Void> verifyUser(@ApiParam(value = "用户ID") @RequestParam Long id) {
        return userService.isActive(id) ? Result.success() : Result.error("用户未激活");
    }
    /**
     * 激活用户
     */
    @PutMapping("/active")
    @ApiOperation(value = "激活用户", notes = "激活用户")
    public Result<Void> activeUser(@ApiParam(value = "用户ID") @RequestParam Long id) {
        return userService.activeUser(id) ? Result.success() : Result.error("激活用户失败");
    }
    /**
     * 批量删除用户
     */
    @DeleteMapping
    @ApiOperation(value = "批量删除用户", notes = "根据用户ID批量删除用户")
    public Result<Void> deleteUsers(@ApiParam(value = "用户ID列表") @RequestBody List<Long> ids) {
        return userService.removeByIds(ids) ? Result.success() : Result.error("批量删除用户失败");
    }
}
