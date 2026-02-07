package com.example.yoyo_data.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.yoyo_data.common.entity.Users;

public interface LimiterService extends IService<Users> {
    Users getRateLimiter(Integer  id);

}
