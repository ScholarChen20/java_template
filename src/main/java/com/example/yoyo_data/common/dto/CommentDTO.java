package com.example.yoyo_data.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentDTO {
    /**
     * 帖子ID
     */
    private Long postId;
    /**
     * 评论内容
     */
    private String content;
    /**
     * 父评论ID
     */
    private Long parentId;
}
