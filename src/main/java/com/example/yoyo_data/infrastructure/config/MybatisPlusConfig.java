package com.example.yoyo_data.infrastructure.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus配置 - 集成MyBatis-Plus框架配置
 * 配置分页插件、性能监控等
 *
 * @author Template Framework
 * @version 1.0
 */
@Configuration
public class MybatisPlusConfig {

    /**
     * MyBatis-Plus拦截器配置
     * 包含分页插件配置
     *
     * @return MybatisPlusInterceptor
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // 添加分页插件
        PaginationInnerInterceptor paginationInnerInterceptor = new PaginationInnerInterceptor();
        // 设置数据库方言为MySQL
        paginationInnerInterceptor.setDialect("mysql");
        // 设置单页最大条数为500
        paginationInnerInterceptor.setMaxLimit(500L);
        // 设置分页的第一页从1开始
        paginationInnerInterceptor.setOptimizeJoin(true);
        interceptor.addInnerInterceptor(paginationInnerInterceptor);

        return interceptor;
    }
}
