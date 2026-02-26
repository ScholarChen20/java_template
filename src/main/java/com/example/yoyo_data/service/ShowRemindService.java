package com.example.yoyo_data.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.yoyo_data.common.Result;
import com.example.yoyo_data.common.vo.ShowEventVO;
import com.example.yoyo_data.common.vo.ShowRemindVO;
import com.example.yoyo_data.common.vo.ShowWishVO;

/**
 * 开票提醒与想看服务接口
 */
public interface ShowRemindService {

    Result<ShowRemindVO> setRemind(Long showEventId, String token);

    Result<String> cancelRemind(Long showEventId, String token);

    Result<ShowRemindVO> getRemindStatus(Long showEventId, String token);

    Result<Page<ShowEventVO>> getMyRemindList(String token, Integer page, Integer size);

    Result<ShowWishVO> setWish(Long showEventId, String token);

    Result<String> cancelWish(Long showEventId, String token);

    Result<Page<ShowEventVO>> getMyWishList(String token, String status, Integer page, Integer size);
}
