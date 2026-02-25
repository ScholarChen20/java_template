package com.example.yoyo_data.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.yoyo_data.common.Result;
import com.example.yoyo_data.common.dto.request.RealNameVerifyRequest;
import com.example.yoyo_data.common.entity.UserProfile;
import com.example.yoyo_data.common.entity.Users;
import com.example.yoyo_data.common.vo.RealNameVerifyVO;
import com.example.yoyo_data.infrastructure.repository.UserMapper;
import com.example.yoyo_data.infrastructure.repository.UserProfileMapper;
import com.example.yoyo_data.service.UserVerifyService;
import com.example.yoyo_data.util.jwt.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 实名认证服务实现
 *
 * 真实场景：调用公安部/商业第三方（如银联、芝麻信用）接口核验姓名与身份证是否匹配。
 * 本实现通过简单格式校验模拟认证通过，并将结果写入数据库。
 */
@Slf4j
@Service
public class UserVerifyServiceImpl implements UserVerifyService {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserProfileMapper userProfileMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<RealNameVerifyVO> verifyRealName(RealNameVerifyRequest request, String token) {
        // 1. Token 校验
        if (token == null || !jwtUtils.validateToken(token)) {
            return Result.unauthorized("Token无效或已过期，请重新登录");
        }
        Long userId = jwtUtils.getUserIdFromToken(token);

        // 2. 查询用户
        Users user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }

        // 3. 已认证则直接返回，无需重复认证
        if (Boolean.TRUE.equals(user.getIsVerified())) {
            log.info("用户已完成实名认证: userId={}", userId);
            return Result.success(buildVerifyVO(true, request.getRealName(), "已完成实名认证"));
        }

        // 4. 模拟调用第三方身份核验接口
        //    真实场景：调用 HTTP 接口传入姓名+身份证，返回核验结果
        boolean verifyPassed = mockThirdPartyVerify(request.getRealName(), request.getIdCard());
        if (!verifyPassed) {
            log.warn("实名认证失败: userId={}, realName={}", userId, maskName(request.getRealName()));
            return Result.error("实名认证失败：姓名与身份证号不匹配，请检查后重试");
        }

        // 5. 更新 users.is_verified = true
        userMapper.update(null, new LambdaUpdateWrapper<Users>()
                .eq(Users::getId, userId)
                .set(Users::getIsVerified, true));

        // 6. 同步更新 user_profiles.full_name（若档案已存在）
        LambdaQueryWrapper<UserProfile> profileQuery = new LambdaQueryWrapper<>();
        profileQuery.eq(UserProfile::getUserId, userId);
        UserProfile profile = userProfileMapper.selectOne(profileQuery);
        if (profile != null) {
            userProfileMapper.update(null, new LambdaUpdateWrapper<UserProfile>()
                    .eq(UserProfile::getUserId, userId)
                    .set(UserProfile::getFullName, request.getRealName()));
        }

        log.info("实名认证成功: userId={}", userId);
        return Result.success(buildVerifyVO(true, request.getRealName(), "实名认证成功"));
    }

    @Override
    public Result<RealNameVerifyVO> getVerifyStatus(String token) {
        if (token == null || !jwtUtils.validateToken(token)) {
            return Result.unauthorized("Token无效或已过期，请重新登录");
        }
        Long userId = jwtUtils.getUserIdFromToken(token);

        Users user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }

        boolean verified = Boolean.TRUE.equals(user.getIsVerified());
        String msg = verified ? "已完成实名认证" : "尚未实名认证，请前往认证后再抢票";

        // 已认证时，从 user_profiles 读取姓名
        String realName = null;
        if (verified) {
            LambdaQueryWrapper<UserProfile> q = new LambdaQueryWrapper<>();
            q.eq(UserProfile::getUserId, userId);
            UserProfile profile = userProfileMapper.selectOne(q);
            if (profile != null) {
                realName = profile.getFullName();
            }
        }

        return Result.success(buildVerifyVO(verified, realName, msg));
    }

    /**
     * 模拟第三方身份核验
     * 生产环境替换为真实 HTTP 调用
     */
    private boolean mockThirdPartyVerify(String realName, String idCard) {
        // 简单校验：姓名非空、身份证格式合法即视为通过（格式已由请求DTO的@Pattern保证）
        return realName != null  && idCard != null && idCard.length() == 18;
    }

    /**
     * 构建实名认证结果VO（姓名脱敏）
     */
    private RealNameVerifyVO buildVerifyVO(boolean verified, String realName, String message) {
        return RealNameVerifyVO.builder()
                .verified(verified)
                .maskedName(maskName(realName))
                .verifiedAt(verified ? LocalDateTime.now() : null)
                .message(message)
                .build();
    }

    /**
     * 姓名脱敏：张三 → 张*，欧阳修 → 欧**
     */
    private String maskName(String name) {
        if (name == null || name.length() < 2) {
            return name;
        }
        // 获取第一个字
        char firstChar = name.charAt(0);
        // 计算剩余长度
        int remainingLength = name.length() - 1;

        // 构建结果：首字 + (长度-1)个星号
        StringBuilder sb = new StringBuilder();
        sb.append(firstChar);
        for (int i = 0; i < remainingLength; i++) {
            sb.append('*');
        }
        return sb.toString();
    }
}
