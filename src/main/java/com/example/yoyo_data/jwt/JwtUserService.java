package com.example.yoyo_data.jwt;

import com.example.yoyo_data.common.dto.JwtUserDTO;

public interface JwtUserService {
    JwtUserDTO getByUserName(String userName);

    void putByUserName(String userName, JwtUserDTO jwtUserDto);

    void removeByUserName(String userName);
}
