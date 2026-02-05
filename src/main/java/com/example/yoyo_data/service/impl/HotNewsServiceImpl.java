package com.example.yoyo_data.service.impl;

import com.example.yoyo_data.common.Result;
import com.example.yoyo_data.common.document.HotNewsDetail;
import com.example.yoyo_data.common.document.HotNewsMain;
import com.example.yoyo_data.infrastructure.repository.mongodb.HotNewsRepository;
import com.example.yoyo_data.service.DataDistributionService;
import com.example.yoyo_data.service.HotNewsCacheService;
import com.example.yoyo_data.service.HotNewsService;
import com.example.yoyo_data.service.HotNewsStreamService;
import com.example.yoyo_data.utils.HotNewsApiUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class HotNewsServiceImpl implements HotNewsService {
    @Autowired
    private HotNewsStreamService hotNewsStreamService;
    @Autowired
    private HotNewsCacheService hotNewsCacheService;
    
    @Autowired
    private HotNewsRepository hotNewsRepository;
    
    @Autowired
    private HotNewsApiUtil hotNewsApiUtil;
    
    @Autowired
    private DataDistributionService dataDistributionService;
    
    @Override
    public boolean saveHotNews(HotNewsMain hotNewsMain) {
        try {
            // 设置创建时间
            hotNewsMain.setCreatedAt(LocalDateTime.now());
            
            // 保存到数据库
            hotNewsRepository.save(hotNewsMain);
            log.info("保存热点数据成功，类型: {}, 数据条数: {}", hotNewsMain.getType(), hotNewsMain.getData().size());
            return true;
        } catch (Exception e) {
            log.error("保存热点数据失败", e);
            return false;
        }
    }
    
    @Override
    public List<HotNewsDetail> getLatestHotNews(String type, Integer count) {
        try {
            // 1. 尝试从Redis缓存获取
            if (hotNewsCacheService.hasHotNewsCache(type)) {
                log.info("从Redis缓存获取热点排行数据，类型: {}, 数量: {}", type, count);
                List<HotNewsDetail> hotNewsDetails = hotNewsCacheService.getHotNewsFromRedisZSet(type, count);
                if (!hotNewsDetails.isEmpty()) {
                    return hotNewsDetails;
                }
            }

            // 2. 尝试从数据库获取
            HotNewsMain hotNewsMain = hotNewsRepository.findTopByTypeOrderByCreatedAtDesc(type);
            log.info("获取最新热点数据成功，类型: {}, 是否找到: {}", type, hotNewsMain != null);
            if (hotNewsMain != null && hotNewsMain.getData() != null && !hotNewsMain.getData().isEmpty()) {
                List<HotNewsDetail> hotNewsDetails = hotNewsMain.getData();

                // 3. 存入Redis缓存
                hotNewsCacheService.saveHotNewsToRedisZSet(type, hotNewsDetails);

                // 4. 截取指定数量
                if (hotNewsDetails.size() > count) {
                    hotNewsDetails = hotNewsDetails.subList(0, count);
                }
                return hotNewsDetails;
            }else {
                log.warn("从数据库获取最新热点数据失败，类型: {}", type);
                return null;
            }
        } catch (Exception e) {
            log.error("获取最新热点数据失败", e);
            return null;
        }
    }
    
    @Override
    public HotNewsMain getLatestHotNews(String type) {
        try {
            HotNewsMain hotNewsMain = hotNewsRepository.findTopByTypeOrderByCreatedAtDesc(type);
            log.info("获取最新热点数据完整对象成功，类型: {}, 是否找到: {}", type, hotNewsMain != null);
            return hotNewsMain;
        } catch (Exception e) {
            log.error("获取最新热点数据完整对象失败", e);
            return null;
        }
    }
    
    @Override
    public HotNewsMain fetchAndSaveHotNews(String type) {
        try {
            // 1. 从第三方接口获取热点数据
            HotNewsMain hotNewsMain = hotNewsApiUtil.getHotNews(type);
            
            if (hotNewsMain != null && hotNewsMain.isSuccess()) {
                // 2. 保存到数据库
                saveHotNews(hotNewsMain);
                log.info("从接口获取并保存热点数据成功，类型: {}", type);

                // 3. 存入Redis缓存
                hotNewsCacheService.saveHotNewsToRedisZSet(type, hotNewsMain.getData());
                
                // 4. 发送到Stream
                hotNewsStreamService.publishHotNewsToStream(type, hotNewsMain.getData());
                
                // 5. 分发热点数据到不同等级的用户
                dataDistributionService.distributeHotNews(type, hotNewsMain.getData());
                
                return hotNewsMain;
            } else {
                log.warn("从接口获取热点数据失败或数据无效，类型: {}", type);
                return null;
            }
        } catch (Exception e) {
            log.error("获取并保存热点数据失败", e);
            return null;
        }
    }
}