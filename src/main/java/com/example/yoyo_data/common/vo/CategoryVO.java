package com.example.yoyo_data.common.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 演出分类视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryVO {

    private String code;
    private String name;
    private String iconUrl;
    private Integer showCount;
}
