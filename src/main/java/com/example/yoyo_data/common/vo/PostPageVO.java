package com.example.yoyo_data.common.vo;

import com.example.yoyo_data.common.entity.Post;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PostPageVO {
    /**
     * 页码
     */
    private Integer page;
    /**
     *  大小
     */
    private Integer size;
    /**
     * 类别
     */
    private String category;
    /**
     * 总数
     */
    private Long total;
    /**
     * 帖子列表
     */
    private List<PostVO> postList;

}
