package com.example.yoyo_data.jwt;

import com.example.yoyo_data.common.dto.JwtUserDTO;

public interface JwtUserService {
    /**
     * 根据用户名获取用户信息
     * @param userName
     * @return
     */
    JwtUserDTO getByUserName(String userName);

    /**
     *  缓存用户信息
     * @param userName
     * @param jwtUserDto
     */
    void putByUserName(String userName, JwtUserDTO jwtUserDto);

    /**
     *  删除用户信息
     * @param userName
     */
    void removeByUserName(String userName);
}
