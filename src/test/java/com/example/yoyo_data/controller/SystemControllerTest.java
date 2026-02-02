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
 * 系统控制器测试类
 * 验证系统模块的接口功能
 */
public class SystemControllerTest extends BaseTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 测试获取系统状态接口
     */
    @Test
    public void testGetSystemStatus() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/system/status"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(200))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("success"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.status").value("healthy"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.services").exists());
    }

    /**
     * 测试获取系统版本接口
     */
    @Test
    public void testGetSystemVersion() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/system/version"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(200))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("success"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.version").value("1.0.0"));
    }

    /**
     * 测试生成验证码接口
     */
    @Test
    public void testGenerateCaptcha() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("type", "numeric");
        params.put("length", 6);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/system/captcha")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(params)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(200))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("success"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.captcha_id").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.expires_in").value(300));
    }

    /**
     * 测试获取系统配置接口
     */
    @Test
    public void testGetSystemConfig() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/system/config/max_upload_size"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(200))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("success"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.key").value("max_upload_size"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.value").value(104857600));
    }

    /**
     * 测试获取系统公告接口
     */
    @Test
    public void testGetSystemAnnouncements() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/system/announcements")
                .param("page", "1")
                .param("size", "10"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(200))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("success"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.total").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.items").isArray());
    }
}
