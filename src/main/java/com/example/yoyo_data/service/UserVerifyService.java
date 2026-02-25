package com.example.yoyo_data.service;

import com.example.yoyo_data.common.Result;
import com.example.yoyo_data.common.dto.request.RealNameVerifyRequest;
import com.example.yoyo_data.common.vo.RealNameVerifyVO;

/**
 * 实名认证服务接口
 * 对应抢票流程 Step 4：POST /api/user/verify/real-name
 */
public interface UserVerifyService {

    /**
     * 提交实名认证
     * 模拟调用第三方权威身份认证接口（如公安部身份证核验），
     * 认证通过后将 users.is_verified 置为 true，并在 user_profiles 更新真实姓名。
     *
     * @param request 实名认证请求（realName + idCard）
     * @param token   JWT Token（用于获取当前登录用户）
     * @return 认证结果VO（是否认证、脱敏姓名、认证时间）
     */
    Result<RealNameVerifyVO> verifyRealName(RealNameVerifyRequest request, String token);

    /**
     * 查询当前用户实名认证状态
     *
     * @param token JWT Token
     * @return 认证结果VO
     */
    Result<RealNameVerifyVO> getVerifyStatus(String token);
}
