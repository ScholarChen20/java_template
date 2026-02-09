package com.example.yoyo_data.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.yoyo_data.common.entity.Tag;

import java.util.List;

/**
 * 标签服务接口
 */
public interface TagService extends IService<Tag> {
    /**
     * postId获取标签名列表
     */
    List<String> getTagNamesByPostId(Long postId);

}
