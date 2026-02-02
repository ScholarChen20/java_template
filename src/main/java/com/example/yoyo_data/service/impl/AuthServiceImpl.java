package com.example.yoyo_data.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.yoyo_data.common.Result;
import com.example.yoyo_data.common.dto.JwtUserDTO;
import com.example.yoyo_data.common.dto.request.*;
import com.example.yoyo_data.common.dto.response.*;
import com.example.yoyo_data.common.vo.UserVO;
import com.example.yoyo_data.mapper.UserMapper;
import com.example.yoyo_data.common.pojo.Users;
import com.example.yoyo_data.service.AuthService;
import com.example.yoyo_data.support.exception.BusinessException;
import com.example.yoyo_data.support.exception.SystemException;
import com.example.yoyo_data.utils.HashUtils;
import com.example.yoyo_data.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * 认证服务实现类
 * 实现用户登录、注册、刷新token等认证相关业务逻辑
 *
 * @author yoyo_data
 * @version 1.0
 */
@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 用户注册
     *
     * @param request 注册请求
     * @return 注册结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<UserVO> register(RegisterRequest request) {
        try {
            // 1. 检查用户名是否已存在
            Users existingUser = userMapper.selectOne(
                    new LambdaQueryWrapper<Users>()
                            .eq(Users::getUserName, request.getUsername())
            );
            if (existingUser != null) {
                throw new BusinessException("用户名已存在");
            }

            // 2. 检查邮箱是否已存在
            existingUser = userMapper.selectOne(
                    new LambdaQueryWrapper<Users>()
                            .eq(Users::getEmail, request.getEmail())
            );
            if (existingUser != null) {
                throw new BusinessException("邮箱已被注册");
            }

            // 3. 创建新用户
            Users user = Users.builder()
                    .userName(request.getUsername())
                    .email(request.getEmail())
                    .passwordHash(passwordEncoder.encode(request.getPassword()))
                    .phone(request.getPhone())
                    .role("user")
                    .isActive(true)
                    .isVerified(false)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            // 4. 保存到数据库
            int result = userMapper.insert(user);
            if (result <= 0) {
                throw new SystemException("注册失败，请稍后重试");
            }

            // 5. 转换为VO返回
            UserVO userVO = convertToUserVO(user);
            log.info("用户注册成功: userId={}, username={}", user.getId(), user.getUserName());
            return Result.success(userVO);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("用户注册失败: username={}", request.getUsername(), e);
            throw new SystemException("注册失败，请稍后重试", e);
        }
    }

    /**
     * 用户登录
     *
     * @param request 登录请求
     * @return 登录结果，包含token、用户信息、过期时间
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<LoginResponse> login(LoginRequest request) {
        try {
            // 1. 根据用户名查询用户
            Users user = userMapper.selectOne(
                    new LambdaQueryWrapper<Users>()
                            .eq(Users::getUserName, request.getUsername())
            );

            if (user == null) {
                throw new BusinessException("用户名或密码错误");
            }

            // 2. 验证密码
            boolean passwordMatch = false;
            String passwordHash = user.getPasswordHash();
            
            // 检查密码是否是BCrypt格式
            if (passwordHash != null && passwordHash.startsWith("$2a$")) {
                // 使用BCrypt验证密码
                passwordMatch = passwordEncoder.matches(request.getPassword(), passwordHash);
            } else {
                // 直接比较明文
                passwordMatch = request.getPassword().equals(passwordHash);
            }
            
            if (!passwordMatch) {
                log.info("用户名或密码错误,请求密码：{}, 用户密码：{}", request.getPassword(), passwordHash);
                throw new BusinessException("用户名或密码错误");
            }

            // 3. 检查用户状态
            if (!user.getIsActive()) {
                throw new BusinessException("账户已被禁用，请联系管理员");
            }

            // 4. 生成JWT Token
            JwtUserDTO jwtUser = new JwtUserDTO();
            jwtUser.setId(user.getId().longValue());
            jwtUser.setUsername(user.getUserName());
            jwtUser.setEmail(user.getEmail());
            jwtUser.setPhone(user.getPhone());

            String token = jwtUtils.generateToken(jwtUser);

            // 5. 更新最后登录时间
            user.setLastLoginAt(LocalDateTime.now());
            userMapper.updateById(user);

            // 6. 缓存用户信息到Redis（2小时过期）
            String cacheKey = "user:token:" + token;
            redisTemplate.opsForValue().set(cacheKey, jwtUser, 7, TimeUnit.DAYS);

            // 7. 构建响应
            UserVO userVO = convertToUserVO(user);
            LoginResponse response = LoginResponse.builder()
                    .token(token)
                    .user(userVO)
                    .expiresAt(new Date(System.currentTimeMillis() + 7200000))
                    .build();

            log.info("用户登录成功: userId={}, userName={}", user.getId(), user.getUserName());
            return Result.success(response);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("用户登录失败: userName={}", request.getUsername(), e);
            throw new SystemException("登录失败，请稍后重试", e);
        }
    }

    /**
     * 刷新Token
     *
     * @param token 当前token
     * @return 刷新结果，包含新token和过期时间
     */
    @Override
    public Result<TokenResponse> refreshToken(String token) {
        try {
            // 1. 验证当前token
            if (token == null || !jwtUtils.validateToken(token)) {
                throw new BusinessException(401, "Token无效或已过期");
            }

            // 2. 从Redis缓存中获取用户信息
            String cacheKey = "user:token:" + token;
            JwtUserDTO jwtUser = (JwtUserDTO) redisTemplate.opsForValue().get(cacheKey);
            
            // 如果缓存中没有，从token中获取用户名并查询数据库
            if (jwtUser == null) {
                String username = jwtUtils.getUsernameFromToken(token);
                if (username == null) {
                    throw new BusinessException(401, "无法获取用户信息");
                }
                
                // 从数据库查询用户
                Users user = userMapper.selectOne(
                        new LambdaQueryWrapper<Users>()
                                .eq(Users::getUserName, username)
                );
                
                if (user == null) {
                    throw new BusinessException(401, "用户不存在");
                }
                
                // 创建JwtUserDTO
                jwtUser = new JwtUserDTO();
                jwtUser.setId(user.getId().longValue());
                jwtUser.setUsername(user.getUserName());
                jwtUser.setEmail(user.getEmail());
                jwtUser.setPhone(user.getPhone());
            }

            // 3. 生成新token
            String newToken = jwtUtils.generateToken(jwtUser);

            // 4. 更新缓存
            String oldCacheKey = "user:token:" + token;
            String newCacheKey = "user:token:" + newToken;
            redisTemplate.delete(oldCacheKey);
            redisTemplate.opsForValue().set(newCacheKey, jwtUser, 2, TimeUnit.HOURS);

            // 5. 构建响应
            TokenResponse response = TokenResponse.builder()
                    .token(newToken)
                    .expiresAt(new Date(System.currentTimeMillis() + 7200000))
                    .build();

            log.info("Token刷新成功: userId={}", jwtUser.getId());
            return Result.success(response);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Token刷新失败", e);
            throw new SystemException("Token刷新失败，请重新登录", e);
        }
    }

    /**
     * 用户登出
     *
     * @param token 当前token
     * @return 登出结果
     */
    @Override
    public Result<Void> logout(String token) {
        try {
            if (token != null) {
                // 1. 从缓存中删除用户信息
                String cacheKey = "user:token:" + token;
                redisTemplate.delete(cacheKey);

                // 2. 将token加入黑名单（设置过期时间为token的剩余有效期）
                String blacklistKey = "blacklist:token:" + token;
                redisTemplate.opsForValue().set(blacklistKey, "1", 7200, TimeUnit.SECONDS);

                log.info("用户登出成功");
            }

            return Result.success();

        } catch (Exception e) {
            log.error("用户登出失败", e);
            throw new SystemException("登出失败，请稍后重试", e);
        }
    }

    /**
     * 发送验证码
     *
     * @param request 发送验证码请求
     * @return 发送结果
     */
    @Override
    public Result<SendCodeResponse> sendCode(SendCodeRequest request) {
        try {
            // 1. 生成6位数字验证码
            String code = String.format("%06d", (int) (Math.random() * 1000000));

            // 2. 缓存验证码（5分钟过期）
            String cacheKey = "verify:code:" + request.getEmail() + ":" + request.getType();
            redisTemplate.opsForValue().set(cacheKey, code, 300, TimeUnit.SECONDS);

            // TODO: 3. 发送邮件（需要集成邮件服务）
            log.info("验证码已生成: email={}, type={}, code={}", request.getEmail(), request.getType(), code);

            // 4. 构建响应
            SendCodeResponse response = SendCodeResponse.builder()
                    .email(request.getEmail())
                    .expiresIn(300)
                    .timestamp(System.currentTimeMillis())
                    .build();

            return Result.success(response);

        } catch (Exception e) {
            log.error("发送验证码失败: email={}", request.getEmail(), e);
            throw new SystemException("发送验证码失败，请稍后重试", e);
        }
    }

    /**
     * 验证邮箱
     *
     * @param request 验证邮箱请求
     * @return 验证结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<VerifyEmailResponse> verifyEmail(VerifyEmailRequest request) {
        try {
            // 1. 从缓存中获取验证码
            String cacheKey = "verify:code:" + request.getEmail() + ":verify_email";
            String cachedCode = (String) redisTemplate.opsForValue().get(cacheKey);

            if (cachedCode == null) {
                throw new BusinessException("验证码已过期，请重新获取");
            }

            if (!cachedCode.equals(request.getCode())) {
                throw new BusinessException("验证码错误");
            }

            // 2. 更新用户邮箱验证状态
            Users user = userMapper.selectOne(
                    new LambdaQueryWrapper<Users>()
                            .eq(Users::getEmail, request.getEmail())
            );

            if (user == null) {
                throw new BusinessException("用户不存在");
            }

            user.setIsVerified(true);
            user.setUpdatedAt(LocalDateTime.now());
            userMapper.updateById(user);

            // 3. 删除验证码缓存
            redisTemplate.delete(cacheKey);

            // 4. 构建响应
            VerifyEmailResponse response = VerifyEmailResponse.builder()
                    .email(request.getEmail())
                    .isVerified(true)
                    .timestamp(System.currentTimeMillis())
                    .build();

            log.info("邮箱验证成功: email={}, userId={}", request.getEmail(), user.getId());
            return Result.success(response);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("邮箱验证失败: email={}", request.getEmail(), e);
            throw new SystemException("邮箱验证失败，请稍后重试", e);
        }
    }

    /**
     * 转换Users实体为UserVO
     *
     * @param user Users实体
     * @return UserVO
     */
    private UserVO convertToUserVO(Users user) {
        return UserVO.builder()
                .id(user.getId().longValue())
                .username(user.getUserName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .avatar(user.getAvatarUrl())
                .bio(user.getBio())
                .role(user.getRole())
                .status(user.getIsActive()? 1 : 0)
                .isActive(user.getIsActive())
                .isVerified(user.getIsVerified())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .lastLoginAt(user.getLastLoginAt())
                .build();
    }
}
