package com.example.yoyo_data.infrastructure.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.yoyo_data.common.entity.Follow;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FollowMapper extends BaseMapper<Follow> {
}
