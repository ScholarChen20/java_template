package com.example.yoyo_data.controller;

import com.example.yoyo_data.common.Result;
import com.example.yoyo_data.pojo.Users;
import com.example.yoyo_data.service.LimiterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequestMapping("/api/users")
@RestController
public class UsersController {

    @Autowired
    private LimiterService limiterService;

    /**
     * 获取所有用户
     * @return 用户列表
     */
    @GetMapping
    public Result<List<Users>> getAllUsers() {
        try {
            List<Users> users = limiterService.list();
            return Result.success(users);
        } catch (Exception e) {
            log.error("获取用户列表失败", e);
            return Result.error("获取用户列表失败");
        }
    }

    /**
     * 根据ID获取用户
     * @param id 用户ID
     * @return 用户信息
     */
    @GetMapping("/{id}")
    public Result<Users> getUserById(@PathVariable Integer id) {
        try {
            Users user = limiterService.getById(id);
            if (user == null) {
                return Result.notFound("用户不存在");
            }
            return Result.success(user);
        } catch (Exception e) {
            log.error("获取用户失败", e);
            return Result.error("获取用户失败");
        }
    }

    /**
     * 创建新用户
     * @param user 用户信息
     * @return 创建结果
     */
    @PostMapping
    public Result<Users> createUser(@RequestBody Users user) {
        try {
            boolean success = limiterService.save(user);
            if (success) {
                return Result.success(user);
            } else {
                return Result.error("创建用户失败");
            }
        } catch (Exception e) {
            log.error("创建用户失败", e);
            return Result.error("创建用户失败");
        }
    }

    /**
     * 更新用户信息
     * @param id 用户ID
     * @param user 用户信息
     * @return 更新结果
     */
    @PutMapping("/{id}")
    public Result<Users> updateUser(@PathVariable Integer id, @RequestBody Users user) {
        try {
            // 检查用户是否存在
            Users oldUser = limiterService.getById(id);
            if (oldUser == null) {
                return Result.notFound("用户不存在");
            }
            
            // 设置用户ID
            user.setId(id);
            
            boolean success = limiterService.updateById(user);
            if (success) {
                return Result.success(user);
            } else {
                return Result.error("更新用户失败");
            }
        } catch (Exception e) {
            log.error("更新用户失败", e);
            return Result.error("更新用户失败");
        }
    }

    /**
     * 删除用户
     * @param id 用户ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteUser(@PathVariable Integer id) {
        try {
            // 检查用户是否存在
            Users oldUser = limiterService.getById(id);
            if (oldUser == null) {
                return Result.notFound("用户不存在");
            }
            
            boolean success = limiterService.removeById(id);
            if (success) {
                return Result.success();
            } else {
                return Result.error("删除用户失败");
            }
        } catch (Exception e) {
            log.error("删除用户失败", e);
            return Result.error("删除用户失败");
        }
    }
}
