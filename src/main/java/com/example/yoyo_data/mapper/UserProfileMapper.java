package com.example.yoyo_data.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.yoyo_data.common.pojo.UserProfile;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserProfileMapper extends BaseMapper<UserProfile> {
}
