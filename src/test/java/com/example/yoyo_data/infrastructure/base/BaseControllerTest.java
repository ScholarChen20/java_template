package com.example.yoyo_data.infrastructure.base;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 * 控制器测试基类 - 所有Controller单元测试的父类
 * 提供MockMvc和通用的测试方法
 *
 * @author Template Framework
 * @version 1.0
 */
@SpringBootTest
@AutoConfigureMockMvc
public abstract class BaseControllerTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected WebApplicationContext webApplicationContext;

    @Autowired
    protected ObjectMapper objectMapper;

    /**
     * 初始化测试环境
     */
    @BeforeEach
    public void setup() {
        if (mockMvc == null) {
            mockMvc = MockMvcBuilders
                    .webAppContextSetup(webApplicationContext)
                    .build();
        }
    }

    /**
     * 将对象转换为JSON字符串
     *
     * @param object 对象
     * @return JSON字符串
     * @throws Exception 异常
     */
    public String toJsonString(Object object) throws Exception {
        return objectMapper.writeValueAsString(object);
    }

    /**
     * 将JSON字符串转换为对象
     *
     * @param jsonString JSON字符串
     * @param clazz 目标类型
     * @param <T> 泛型类型
     * @return 对象
     * @throws Exception 异常
     */
    public <T> T toObject(String jsonString, Class<T> clazz) throws Exception {
        return objectMapper.readValue(jsonString, clazz);
    }

    /**
     * 打印响应体（用于调试）
     *
     * @param content 响应体内容
     * @throws Exception 异常
     */
    public void printResponse(String content) throws Exception {
        Object json = objectMapper.readValue(content, Object.class);
        String formatted = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
        System.out.println("Response: " + formatted);
    }
}
