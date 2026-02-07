package com.example.yoyo_data.service;

import com.example.yoyo_data.common.Result;
import com.example.yoyo_data.common.vo.LikeListVO;
import com.example.yoyo_data.common.vo.LikeToggleVO;
import com.example.yoyo_data.common.vo.LikeTopVO;

import java.util.List;

/**
 * 点赞服务接口
 */
public interface LikeService {
    /**
     * 点赞/取消点赞
     *
     * @param userId 用户ID
     * @param targetId 目标ID（帖子或评论）
     * @param targetType 目标类型（post或comment）
     * @return 操作结果
     */
    Result<LikeToggleVO> toggleLike(Long userId, Long targetId, String targetType);

    /**
     * 获取点赞状态
     *
     * @param userId 用户ID
     * @param targetId 目标ID
     * @param targetType 目标类型
     * @return 点赞状态
     */
    Result<LikeToggleVO> getLikeStatus(Long userId, Long targetId, String targetType);

    /**
     * 获取点赞列表
     *
     * @param targetId 目标ID
     * @param targetType 目标类型
     * @param page 页码
     * @param size 每页大小
     * @return 点赞列表
     */
    Result<LikeListVO> getLikeList(Long targetId, String targetType, Integer page, Integer size);

    /**
     * 获取点赞数top N 的帖子
     * @param top
     * @return
     */
    Result<List<LikeTopVO>> getLikeRank(Integer top);
}
