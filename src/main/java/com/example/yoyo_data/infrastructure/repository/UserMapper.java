package com.example.yoyo_data.infrastructure.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.yoyo_data.common.entity.Users;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserMapper extends BaseMapper<Users> {
    Page<Users> getUserPage(@Param("id") Integer id, IPage<Users> page);

    @Select("select is_active from users where id = #{id}")
    boolean isActive(Long id);

    @Update("update users set is_active = 1 where id = #{id}")
    boolean activeUser(Long id);
}
