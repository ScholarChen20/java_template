package com.example.yoyo_data.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.yoyo_data.common.Result;
import com.example.yoyo_data.common.vo.ShowEventVO;
import com.example.yoyo_data.common.vo.VenueVO;

/**
 * 场馆服务接口
 */
public interface VenueService {

    Result<Page<VenueVO>> getVenueList(String city, Integer page, Integer size);

    Result<VenueVO> getVenueDetail(Long venueId);

    Result<Page<ShowEventVO>> getShowsByVenue(Long venueId, String status, Integer page, Integer size);
}
