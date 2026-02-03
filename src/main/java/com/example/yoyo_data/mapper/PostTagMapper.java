package com.example.yoyo_data.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.yoyo_data.common.pojo.Post;
import com.example.yoyo_data.common.pojo.PostTag;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PostTagMapper extends BaseMapper<PostTag> {
}
