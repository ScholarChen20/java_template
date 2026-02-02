package com.example.yoyo_data.controller;

import com.example.yoyo_data.base.BaseTest;
import com.example.yoyo_data.utils.JwtUtils;
import com.example.yoyo_data.common.dto.JwtUserDTO;
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
 * 用户控制器测试类
 * 验证用户模块的接口功能
 */
public class UserControllerTest extends BaseTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtUtils jwtUtils;

    /**
     * 生成测试用的token
     *
     * @return token字符串
     */
    private String generateTestToken() {
        JwtUserDTO jwtUser = new JwtUserDTO();
        jwtUser.setId(1L);
        jwtUser.setUsername("testuser");
        jwtUser.setEmail("testuser@example.com");
        return jwtUtils.generateToken(jwtUser);
    }

    /**
     * 测试获取当前用户信息接口
     */
    @Test
    public void testGetCurrentUser() throws Exception {
        String token = generateTestToken();

        mockMvc.perform(MockMvcRequestBuilders.get("/api/users/me")
                .header("Authorization", "Bearer " + token))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(200))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("success"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.id").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.username").value("testuser"));
    }

    /**
     * 测试更新当前用户信息接口
     */
    @Test
    public void testUpdateCurrentUser() throws Exception {
        String token = generateTestToken();

        Map<String, Object> params = new HashMap<>();
        params.put("phone", "13900139000");
        params.put("bio", "旅行爱好者，喜欢探索新地方");

        mockMvc.perform(MockMvcRequestBuilders.put("/api/users/me")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(params)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(200))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("success"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.phone").value("13900139000"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.bio").value("旅行爱好者，喜欢探索新地方"));
    }

    /**
     * 测试获取当前用户档案接口
     */
    @Test
    public void testGetCurrentUserProfile() throws Exception {
        String token = generateTestToken();

        mockMvc.perform(MockMvcRequestBuilders.get("/api/users/me/profile")
                .header("Authorization", "Bearer " + token))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(200))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("success"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.user_id").value(1));
    }

    /**
     * 测试获取其他用户信息接口
     */
    @Test
    public void testGetUserById() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/users/2"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(200))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("success"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.id").value(2));
    }
}
