package com.example.yoyo_data.controller;

import com.example.yoyo_data.common.Result;
import com.example.yoyo_data.common.pojo.UserLevelInfo;
import com.example.yoyo_data.service.UserLevelService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "用户等级接口")
@RestController
@RequestMapping("/api/user")
@Slf4j
public class UserLevelController {
    
    @Autowired
    private UserLevelService userLevelService;
    
    @ApiOperation(value = "获取用户等级信息", notes = "根据用户ID获取用户的等级信息和粉丝数")
    @GetMapping("/level")
    public Result<UserLevelInfo> getUserLevelInfo(
            @ApiParam(name = "userId", value = "用户ID", required = true) @RequestParam Long userId) {
        try {
            log.info("获取用户等级信息，用户ID: {}", userId);
            UserLevelInfo levelInfo = userLevelService.getUserLevelInfo(userId);
            
            if (levelInfo != null) {
                return Result.success(levelInfo);
            } else {
                return Result.error(404, "用户不存在");
            }
        } catch (Exception e) {
            log.error("获取用户等级信息失败，用户ID: {}", userId, e);
            return Result.error(500, "获取用户等级信息失败");
        }
    }
    
    @ApiOperation(value = "获取用户粉丝数", notes = "根据用户ID获取用户的粉丝数")
    @GetMapping("/followers")
    public Result<Integer> getFollowerCount(
            @ApiParam(name = "userId", value = "用户ID", required = true) @RequestParam Long userId) {
        try {
            log.info("获取用户粉丝数，用户ID: {}", userId);
            int followerCount = userLevelService.getFollowerCount(userId);
            return Result.success(followerCount);
        } catch (Exception e) {
            log.error("获取用户粉丝数失败，用户ID: {}", userId, e);
            return Result.error(500, "获取用户粉丝数失败");
        }
    }
}
