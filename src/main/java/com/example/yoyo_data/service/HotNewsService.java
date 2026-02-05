package com.example.yoyo_data.service;

import com.example.yoyo_data.common.document.HotNewsDetail;
import com.example.yoyo_data.common.document.HotNewsMain;

import java.util.List;

public interface HotNewsService {
    /**
     * 保存热点数据到数据库
     * @param hotNewsMain 热点数据
     * @return 保存结果
     */
    boolean saveHotNews(HotNewsMain hotNewsMain);
    
    /**
     * 获取最新的热点数据
     * @param type 热点类型
     * @return 热点数据
     */
    List<HotNewsDetail> getLatestHotNews(String type, Integer count);
    
    /**
     * 从第三方接口获取并保存热点数据
     * @param type 热点类型
     * @return 热点数据
     */
    HotNewsMain fetchAndSaveHotNews(String type);
}