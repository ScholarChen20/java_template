package com.example.yoyo_data.infrastructure.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.yoyo_data.common.entity.ShowReview;
import com.example.yoyo_data.common.vo.ShowRatingVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface ShowReviewMapper extends BaseMapper<ShowReview> {
    /**
     * 根据演出ID查询标签
     * @param showEventId
     * @return
     */

    List<String> selectTagsByShowEventId(@Param("showEventId") Long showEventId);

    /**
     * 根据演出ID查询评分分布
     * @param showEventId
     * @return
     */
    List<ShowRatingVO> selectRatingDistribution(@Param("showEventId") Long showEventId);
}
