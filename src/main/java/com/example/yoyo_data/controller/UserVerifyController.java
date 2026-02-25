package com.example.yoyo_data.controller;

import com.example.yoyo_data.common.Result;
import com.example.yoyo_data.common.dto.request.RealNameVerifyRequest;
import com.example.yoyo_data.common.vo.RealNameVerifyVO;
import com.example.yoyo_data.service.UserVerifyService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

/**
 * 实名认证控制器
 *
 * 在抢票流程中的位置：
 *   Step 4: POST /api/user/verify/real-name  - 提交实名认证（如未认证）
 *           GET  /api/user/verify/real-name  - 查询当前认证状态
 *
 * 大麦网参考逻辑：
 *   - 用户首次购票前必须完成实名认证
 *   - 认证信息（姓名+身份证）与公安系统核验一致后标记为已认证
 *   - 已认证用户可直接跳过此步骤进入验证码环节
 */
@Slf4j
@RestController
@RequestMapping("/api/user/verify")
@Api(tags = "实名认证模块", description = "抢票前的实名认证，验证用户身份真实性")
public class UserVerifyController {

    @Autowired
    private UserVerifyService userVerifyService;

    /**
     * Step 4a：提交实名认证
     * 前端应在进入抢票流程前调用此接口校验用户认证状态（GET），
     * 若未认证则跳转认证页面，用户填写姓名与身份证后提交本接口（POST）。
     */
    @PostMapping("/real-name")
    @ApiOperation(value = "提交实名认证",
            notes = "传入真实姓名和身份证号进行身份核验。核验通过后用户账号标记为已认证（is_verified=true），之后抢票无需重复认证。")
    public Result<RealNameVerifyVO> verifyRealName(
            @Valid @RequestBody RealNameVerifyRequest request,
            HttpServletRequest httpRequest
    ) {
        String token = extractToken(httpRequest);
        log.info("实名认证请求: realName={}", maskName(request.getRealName()));
        return userVerifyService.verifyRealName(request, token);
    }

    /**
     * Step 4b：查询实名认证状态
     * 前端进入抢票流程时调用，判断是否需要展示认证页面。
     */
    @GetMapping("/real-name")
    @ApiOperation(value = "查询实名认证状态",
            notes = "查询当前登录用户的实名认证状态。已认证返回认证信息，未认证返回提示信息引导用户认证。")
    public Result<RealNameVerifyVO> getVerifyStatus(HttpServletRequest httpRequest) {
        String token = extractToken(httpRequest);
        return userVerifyService.getVerifyStatus(token);
    }

    private String extractToken(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return token;
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
