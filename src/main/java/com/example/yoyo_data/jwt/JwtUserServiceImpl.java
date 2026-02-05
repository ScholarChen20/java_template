package com.example.yoyo_data.jwt;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.example.yoyo_data.infrastructure.cache.RedisService;
import com.example.yoyo_data.common.constant.CacheKey;
import com.example.yoyo_data.common.dto.JwtUserDTO;
import com.example.yoyo_data.common.vo.UserVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class JwtUserServiceImpl implements JwtUserService {
    private static final Logger log = LoggerFactory.getLogger(JwtUserServiceImpl.class);

    @Autowired
    private RedisService redisService;

    @Override
    public JwtUserDTO getByUserName(String userName) {
        if (StrUtil.isBlank(userName)) {
            return null;
        }
        String str = redisService.stringGetString(CacheKey.JWT_USER_NAME + userName);
        if (str == null) {
            return null;
        }
        return JSON.parseObject(str, JwtUserDTO.class);
    }

    @Override
    public void putByUserName(String userName, JwtUserDTO jwtUserDto) {
        redisService.stringSetString(CacheKey.JWT_USER_NAME + userName, JSON.toJSONString(jwtUserDto), 3 * 60 * 60 * 1000L);
    }

    @Override
    public void removeByUserName(String userName) {
        JwtUserDTO jwtUserDto = this.getByUserName(userName);
        if (jwtUserDto != null) {
            UserVO user = jwtUserDto.getUser();
            if (user != null) {
                Long userId = user.getId();
                redisService.delete(CacheKey.JWT_USER_NAME + userName);
                redisService.delete(CacheKey.MENU_USER_ID + userId);
                redisService.delete(CacheKey.PERMISSION_DATA_RULE_USER_ID + userId);
                redisService.delete(CacheKey.PERMISSION_DATA_FIELD_USER_ID + userId);
            }
        }
    }
}
