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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * 帖子控制器测试类
 * 验证社交模块的接口功能
 */
public class PostControllerTest extends BaseTest {

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
     * 测试创建帖子接口
     */
    @Test
    public void testCreatePost() throws Exception {
        String token = generateTestToken();

        Map<String, Object> params = new HashMap<>();
        params.put("title", "北京三日游攻略");
        params.put("content", "北京是中国的首都，有着丰富的历史文化...");
        params.put("status", "published");

        ArrayList<String> tags = new ArrayList<>();
        tags.add("北京");
        tags.add("旅行");
        tags.add("攻略");
        params.put("tags", tags);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/posts")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(params)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(200))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("success"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.title").value("北京三日游攻略"));
    }

    /**
     * 测试获取帖子列表接口
     */
    @Test
    public void testGetPostList() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/posts")
                .param("page", "1")
                .param("size", "10"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(200))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("success"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.total").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.items").isArray());
    }

    /**
     * 测试获取帖子详情接口
     */
    @Test
    public void testGetPostDetail() throws Exception {
        String token = generateTestToken();

        mockMvc.perform(MockMvcRequestBuilders.get("/api/posts/1")
                .header("Authorization", "Bearer " + token))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(200))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("success"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.id").value(1));
    }

    /**
     * 测试更新帖子接口
     */
    @Test
    public void testUpdatePost() throws Exception {
        String token = generateTestToken();

        Map<String, Object> params = new HashMap<>();
        params.put("title", "北京三日游攻略（更新版）");
        params.put("content", "北京是中国的首都，有着丰富的历史文化...（更新内容）");

        mockMvc.perform(MockMvcRequestBuilders.put("/api/posts/1")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(params)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(200))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("success"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.title").value("北京三日游攻略（更新版）"));
    }

    /**
     * 测试删除帖子接口
     */
    @Test
    public void testDeletePost() throws Exception {
        String token = generateTestToken();

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/posts/1")
                .header("Authorization", "Bearer " + token))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(200))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("success"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.status").value("deleted"));
    }
}
