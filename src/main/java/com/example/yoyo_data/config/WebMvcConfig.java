package com.example.yoyo_data.config;

import com.example.yoyo_data.interceptor.JwtInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置类
 * 用于注册拦截器等
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private JwtInterceptor jwtInterceptor;

    /**
     * 注册拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册 JWT 拦截器
        registry.addInterceptor(jwtInterceptor)
                // 配置需要拦截的路径
                .addPathPatterns("/api/**")
                // 配置不需要拦截的路径
                .excludePathPatterns(
                        "/api/auth/login",      // 登录接口
                        "/api/auth/register",    // 注册接口
                        "/api/auth/refresh",     // 刷新 token 接口
                        "/api/auth/logout",      // 登出接口
                        "/api/common/**",        // 通用接口
                        "/swagger-ui.html",      // Swagger UI
                        "/swagger-resources/**", // Swagger 资源
                        "/v2/api-docs",         // Swagger API 文档
                        "/webjars/**"           // Webjars 资源
                );
    }
}