package com.example.yoyo_data.service.impl;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.yoyo_data.cache.RedisService;
import com.example.yoyo_data.mapper.UserMapper;
import com.example.yoyo_data.pojo.Users;
import com.example.yoyo_data.service.LimiterService;
import com.example.yoyo_data.utils.HashUtils;
import com.example.yoyo_data.utils.RRateLimiterUtils;
import com.example.yoyo_data.utils.RedisUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Map;

@Service
public class LimiterServiceImpl extends ServiceImpl<UserMapper, Users>  implements LimiterService {
    @Autowired
    private RRateLimiterUtils rRateLimiterUtils;

    @Autowired
    private RedisService redisService;
    private static final String USER_KEY_PREFIX = "user:";
    private static final String DEFAULT_PASSWORD = "123456";
    @Override
    public Users getRateLimiter(Integer id) {
        // 1.从user对象中获取账号id
        String key = USER_KEY_PREFIX + id;
        String userName;
        // 1.1 先从缓存中取值
        String userString = redisService.stringGetString(key); // 容易出现空指针异常
        if (userString != null) {
            JSONObject jsonObject = new JSONObject(userString.isEmpty());
            userName = jsonObject.getString("userName");
        }else{
            // 1.2 缓存中没有，从数据库中取值
            Users user = baseMapper.selectOne(Wrappers.<Users>lambdaQuery().eq(Users::getId, id));
            if (user == null) {
                return null;
            }
            userName = user.getUserName();
        }

        if (userName == null || userName.isEmpty()) {
            return null;
        }

        // 2. 调用限流器
        rRateLimiterUtils.acquire(userName);

        // 3. 新增用户信息
        Users newUser = Users.builder()
                .userName("heihei")
                .email("12346@qq.com")
                .phone("12345678901")
                .avatarUrl("https://avatars.githubusercontent.com/u/123456789?v=4")
                .bio("这个人很懒")
                .role("user")
                .isActive(1)
                .createdAt(LocalDateTime.now())
                .build();
        // 3.1 哈希加密密码
        newUser.setPassword(HashUtils.hashPassword(DEFAULT_PASSWORD));
        // 3.2 插入数据库
        baseMapper.insert(newUser);


        // 4. 对象存入缓存中
        Users oneUser = baseMapper.selectOne(Wrappers.<Users>lambdaQuery().eq(Users::getUserName, newUser.getUserName()));
        redisService.stringSetString(USER_KEY_PREFIX + oneUser.getUserName(), String.valueOf(newUser));

        // 4. 返回新用户的用户名
        return newUser;
    }
}
