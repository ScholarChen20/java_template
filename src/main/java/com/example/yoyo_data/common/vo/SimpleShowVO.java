package com.example.yoyo_data.common.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 演出简要信息视图对象（用于场馆详情中的演出列表）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SimpleShowVO {

    private Long id;
    private String showName;
    private String showTime;
    private String status;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
}
