package com.example.yoyo_data.common.Enum;

import lombok.Getter;

/**
 * 用户等级枚举
 */
@Getter
public enum UserLevel {
    LEVEL_A(1, "A级", 0, 1000),
    LEVEL_B(2, "B级", 1000, 10000),
    LEVEL_C(3, "C级", 10000, Integer.MAX_VALUE);

    private final int level;
    private final String name;
    private final int minFollowers;
    private final int maxFollowers;

    UserLevel(int level, String name, int minFollowers, int maxFollowers) {
        this.level = level;
        this.name = name;
        this.minFollowers = minFollowers;
        this.maxFollowers = maxFollowers;
    }

    public static UserLevel fromFollowerCount(int followerCount) {
        if (followerCount < LEVEL_A.maxFollowers) {
            return LEVEL_A;
        } else if (followerCount < LEVEL_B.maxFollowers) {
            return LEVEL_B;
        } else {
            return LEVEL_C;
        }
    }
}