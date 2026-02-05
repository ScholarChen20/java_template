package com.example.yoyo_data.utils;

import com.example.yoyo_data.common.document.HotNewsMain;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Slf4j
@Component
public class HotNewsApiUtil {
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private static final String API_URL = "https://dabenshi.cn/other/api/hot.php";
    
    /**
     * 调用第三方接口获取热点数据
     * @param type 热点类型，如：douyinhot
     * @return HotNewsMain 热点数据
     */
    public HotNewsMain getHotNews(String type) {
        try {
            // 构建请求URL
            URI uri = UriComponentsBuilder.fromUriString(API_URL)
                    .queryParam("type", type)
                    .build()
                    .toUri();
            
            log.info("调用热点数据接口: {}", uri);
            
            // 调用接口获取响应
            String response = restTemplate.getForObject(uri, String.class);
            log.info("接口响应数据: {}", response);
            
            // 解析响应数据
            HotNewsMain hotNewsMain = objectMapper.readValue(response, HotNewsMain.class);
            hotNewsMain.setType(type);
            
            log.info("解析热点数据成功，类型: {}, 数据条数: {}", type, hotNewsMain.getData().size());
            return hotNewsMain;
            
        } catch (Exception e) {
            log.error("获取热点数据失败", e);
            return null;
        }
    }
}