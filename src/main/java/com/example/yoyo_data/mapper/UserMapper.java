package com.example.yoyo_data.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.yoyo_data.common.pojo.Users;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<Users> {
}
