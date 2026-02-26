package com.example.yoyo_data.common.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 城市视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CityVO {
    /**
     * 城市名称
     */
    private String city;
    /**
     * 显示次数
     */
    private Integer showCount;
    /**
     * 热门城市
     */
    private Integer hotCount;
}
