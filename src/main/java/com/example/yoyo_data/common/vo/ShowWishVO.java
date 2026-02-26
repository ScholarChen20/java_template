package com.example.yoyo_data.common.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 演出想看视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShowWishVO {

    private Long showEventId;
    private Integer wishCount;
}
