package com.example.yoyo_data.interceptor;

import com.example.yoyo_data.common.Result;
import com.example.yoyo_data.utils.JwtUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * JWT 拦截器
 * 用于验证请求中的 token
 */
@Slf4j
@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtils jwtUtils;

    /**
     * 在请求处理之前进行调用
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. 获取请求头中的 token
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        // 2. 验证 token
        if (token == null || !jwtUtils.validateToken(token)) {
            // token 无效或不存在，返回错误响应
            response.setContentType("application/json;charset=UTF-8");
            Result<?> result = Result.error(401, "未授权或登录已过期");
            response.getWriter().write(new ObjectMapper().writeValueAsString(result));
            return false;
        }

        // 3. token 有效，继续处理请求
        log.info("Token 验证成功");
        return true;
    }

    /**
     * 请求处理之后进行调用，但是在视图被渲染之前
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        // 可以在这里添加一些通用的响应处理
    }

    /**
     * 整个请求结束之后被调用
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 可以在这里添加一些资源清理的代码

    }
}