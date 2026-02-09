package com.example.yoyo_data.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.yoyo_data.common.entity.Post;
import com.example.yoyo_data.common.entity.Tag;
import com.example.yoyo_data.infrastructure.repository.PostMapper;
import com.example.yoyo_data.infrastructure.repository.TagMapper;
import com.example.yoyo_data.service.PostService;
import com.example.yoyo_data.service.TagService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


@Service
@Slf4j
public class TagServiceImpl extends ServiceImpl<TagMapper,  Tag> implements TagService {
    @Autowired
    private TagMapper tagMapper;
    @Autowired
    private PostService postService;

    @Override
    public List<String> getTagNamesByPostId(Long postId) {
        // 获取帖子标签
        List<Tag> tags = tagMapper.selectList(
                new LambdaQueryWrapper<Tag>()
                        .inSql(Tag::getId, "SELECT tag_id FROM post_tags WHERE post_id = " + postId)
        );
        List<String> tagNames = new ArrayList<>();
        for (Tag tag : tags) {
            tagNames.add(tag.getName());
        }
        return tagNames;
    }
}
