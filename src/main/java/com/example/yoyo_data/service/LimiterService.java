package com.example.yoyo_data.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.yoyo_data.common.pojo.Users;

public interface LimiterService extends IService<Users> {
    Users getRateLimiter(Integer  id);

}
