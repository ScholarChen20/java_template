package com.example.yoyo_data.common.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户分页查询参数
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserPageVO {
    /** 用户编号 **/
    private Integer id;
    /** 当前页数，从1开始 **/
    private Integer current;
    /** 每页记录数 **/
    private Integer size;
}
