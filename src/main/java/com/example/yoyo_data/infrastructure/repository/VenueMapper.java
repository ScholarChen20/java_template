package com.example.yoyo_data.infrastructure.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.yoyo_data.common.entity.Venue;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface VenueMapper extends BaseMapper<Venue> {
}
