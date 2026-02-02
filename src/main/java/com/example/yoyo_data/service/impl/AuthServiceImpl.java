package com.example.yoyo_data.service.impl;

import com.example.yoyo_data.common.Result;
import com.example.yoyo_data.common.dto.JwtUserDTO;
import com.example.yoyo_data.common.vo.UserVO;
import com.example.yoyo_data.service.AuthService;
import com.example.yoyo_data.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 认证服务实现类
 * 实现用户登录、注册、刷新token等认证相关业务逻辑
 */
@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 用户注册
     *
     * @param params 注册参数，包含username、email、password、phone
     * @return 注册结果
     */
    @Override
    public Result<Map<String, Object>> register(Map<String, Object> params) {
        // 模拟注册逻辑
        // 实际项目中需要：
        // 1. 验证参数
        // 2. 检查用户名/邮箱是否已存在
        // 3. 对密码进行加密
        // 4. 保存用户信息到数据库
        
        String username = (String) params.get("username");
        String email = (String) params.get("email");
        String password = (String) params.get("password");
        String phone = (String) params.get("phone");

        // 模拟注册成功
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", 1L);
        userInfo.put("username", username);
        userInfo.put("email", email);
        userInfo.put("phone", phone);
        userInfo.put("role", "user");
        userInfo.put("is_active", true);
        userInfo.put("is_verified", false);
        userInfo.put("created_at", new Date());

        return Result.success(userInfo);
    }

    /**
     * 用户登录
     *
     * @param params 登录参数，包含username、password
     * @return 登录结果，包含token、用户信息、过期时间
     */
    @Override
    public Result<Map<String, Object>> login(Map<String, Object> params) {
        // 模拟登录逻辑
        // 实际项目中需要：
        // 1. 验证参数
        // 2. 根据用户名查询用户信息
        // 3. 验证密码
        // 4. 生成token
        // 5. 缓存用户信息
        
        String username = (String) params.get("username");
        String password = (String) params.get("password");

        // 模拟登录成功
        JwtUserDTO jwtUser = new JwtUserDTO();
        jwtUser.setId(1L);
        jwtUser.setUsername(username);
        jwtUser.setEmail(username + "@example.com");
        jwtUser.setPhone("13800138000");

        UserVO userVO = new UserVO();
        userVO.setId(1L);
        userVO.setUsername(username);
        userVO.setEmail(username + "@example.com");
        userVO.setPhone("13800138000");
        userVO.setStatus(1);

        jwtUser.setUser(userVO);

        // 生成token
        String token = jwtUtils.generateToken(jwtUser);

        // 计算过期时间
        Date expireDate = new Date(System.currentTimeMillis() + 7200000); // 2小时

        // 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("user", userVO);
        result.put("expires_at", expireDate);

        return Result.success(result);
    }

    /**
     * 刷新Token
     *
     * @param token 当前token
     * @return 刷新结果，包含新token和过期时间
     */
    @Override
    public Result<Map<String, Object>> refreshToken(String token) {
        // 模拟刷新token逻辑
        // 实际项目中需要：
        // 1. 验证当前token
        // 2. 从token中获取用户信息
        // 3. 生成新token
        // 4. 更新缓存
        
        if (token == null || !jwtUtils.validateToken(token)) {
            return Result.unauthorized("Token无效或已过期");
        }

        // 生成新token
        String newToken = jwtUtils.refreshToken(token);

        // 计算过期时间
        Date expireDate = new Date(System.currentTimeMillis() + 7200000); // 2小时

        // 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("token", newToken);
        result.put("expires_at", expireDate);

        return Result.success(result);
    }

    /**
     * 用户登出
     *
     * @param token 当前token
     * @return 登出结果
     */
    @Override
    public Result<Void> logout(String token) {
        // 模拟登出逻辑
        // 实际项目中需要：
        // 1. 验证当前token
        // 2. 从缓存中删除用户信息
        // 3. 将token加入黑名单
        
        if (token != null) {
            // 将token加入黑名单，设置过期时间为token的剩余有效期
            redisTemplate.opsForValue().set("blacklist:" + token, "1", 7200, TimeUnit.SECONDS);
        }

        return Result.success();
    }

    /**
     * 发送验证码
     *
     * @param params 发送验证码参数，包含email、type
     * @return 发送结果
     */
    @Override
    public Result<Map<String, Object>> sendCode(Map<String, Object> params) {
        // 模拟发送验证码逻辑
        // 实际项目中需要：
        // 1. 验证参数
        // 2. 生成验证码
        // 3. 缓存验证码
        // 4. 发送邮件
        
        String email = (String) params.get("email");
        String type = (String) params.get("type");

        // 生成验证码
        String code = String.format("%06d", (int) (Math.random() * 1000000));

        // 缓存验证码，设置5分钟过期
        redisTemplate.opsForValue().set("verify_code:" + email + ":" + type, code, 300, TimeUnit.SECONDS);

        // 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("email", email);
        result.put("expires_in", 300);

        return Result.success(result);
    }

    /**
     * 验证邮箱
     *
     * @param params 验证邮箱参数，包含email、code
     * @return 验证结果
     */
    @Override
    public Result<Map<String, Object>> verifyEmail(Map<String, Object> params) {
        // 模拟验证邮箱逻辑
        // 实际项目中需要：
        // 1. 验证参数
        // 2. 从缓存中获取验证码
        // 3. 验证验证码
        // 4. 更新用户邮箱验证状态
        
        String email = (String) params.get("email");
        String code = (String) params.get("code");

        // 从缓存中获取验证码
        String cachedCode = (String) redisTemplate.opsForValue().get("verify_code:" + email + ":verify_email");

        if (cachedCode == null || !cachedCode.equals(code)) {
            return Result.badRequest("验证码无效或已过期");
        }

        // 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("email", email);
        result.put("is_verified", true);

        return Result.success(result);
    }
}
