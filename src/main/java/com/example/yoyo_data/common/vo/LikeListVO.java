package com.example.yoyo_data.common.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 点赞列表响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LikeListVO {
    /**
     * 点赞用户列表
     */
    private List<LikeUserInfoVO> likeList;

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 当前页码
     */
    private Integer page;

    /**
     * 每页大小
     */
    private Integer size;
}
