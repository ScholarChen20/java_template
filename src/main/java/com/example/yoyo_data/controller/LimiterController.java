package com.example.yoyo_data.controller;

import com.example.yoyo_data.common.Result;
import com.example.yoyo_data.common.pojo.Users;
import com.example.yoyo_data.service.LimiterService;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequestMapping("/api/limiter")
@RestController
public class LimiterController {
    @Autowired
    private LimiterService limiterService;
    @GetMapping("/test/{id}")
    public Result<Users> test(@PathVariable Integer id) {
        log.info("test called with id: {}", id);
        Users user = null;
        try {
            user = limiterService.getRateLimiter(id);
            if (user == null){
                return Result.notFound("用户不存在");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return Result.success(user);
    }
}
