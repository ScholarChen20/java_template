package com.example.yoyo_data.controller;

import com.example.yoyo_data.base.BaseTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证控制器测试类
 * 验证认证模块的接口功能
 */
public class AuthControllerTest extends BaseTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 测试用户注册接口
     */
    @Test
    public void testRegister() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("username", "testuser");
        params.put("email", "testuser@example.com");
        params.put("password", "123456");
        params.put("phone", "13800138000");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(params)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(200))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("success"));
    }

    /**
     * 测试用户登录接口
     */
    @Test
    public void testLogin() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("username", "testuser");
        params.put("password", "123456");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(params)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(200))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("success"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.token").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.user").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.expires_at").exists());
    }

    /**
     * 测试发送验证码接口
     */
    @Test
    public void testSendCode() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("email", "testuser@example.com");
        params.put("type", "verify_email");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/send-code")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(params)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(200))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("success"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.email").value("testuser@example.com"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.expires_in").value(300));
    }
}
