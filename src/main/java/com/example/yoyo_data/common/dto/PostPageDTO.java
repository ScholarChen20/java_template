package com.example.yoyo_data.common.dto;

import com.example.yoyo_data.common.pojo.Post;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PostPageDTO {
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
     * 帖子列表
     */
    private Long total;
    private List<Post> postList;

}
