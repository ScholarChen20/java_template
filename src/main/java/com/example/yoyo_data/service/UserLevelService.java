package com.example.yoyo_data.service;

import com.example.yoyo_data.common.enums.UserLevel;
import com.example.yoyo_data.common.pojo.UserLevelInfo;

public interface UserLevelService {

    
    /**
     * 获取用户的粉丝数
     * @param userId 用户ID
     * @return 粉丝数
     */
    int getFollowerCount(Long userId);
    
    /**
     * 获取用户的等级
     * @param userId 用户ID
     * @return 用户等级
     */
    UserLevel getUserLevel(Long userId);
    
    /**
     * 获取用户的等级信息
     * @param userId 用户ID
     * @return 用户等级信息
     */
    UserLevelInfo getUserLevelInfo(Long userId);

}
