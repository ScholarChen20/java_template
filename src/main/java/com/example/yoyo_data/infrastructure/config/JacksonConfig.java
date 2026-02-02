package com.example.yoyo_data.infrastructure.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.text.SimpleDateFormat;

/**
 * Jackson配置 - 统一JSON序列化/反序列化配置
 * 配置ObjectMapper以支持LocalDateTime、BigDecimal等特殊类型的序列化
 *
 * @author Template Framework
 * @version 1.0
 */
@Configuration
public class JacksonConfig {

    /**
     * 配置ObjectMapper
     * - 支持Java 8 Time API (LocalDateTime等)
     * - 忽略序列化时的空值
     * - 禁用失败的属性会导致异常（允许忽略未知属性）
     * - 格式化日期为 yyyy-MM-dd HH:mm:ss
     *
     * @return 配置后的ObjectMapper
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // 注册Java 8 Time模块
        mapper.registerModule(new JavaTimeModule());

        // 配置序列化选项
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));

        // 配置反序列化选项
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);

        return mapper;
    }
}
