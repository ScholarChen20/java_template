package com.example.yoyo_data.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.yoyo_data.common.Result;
import com.example.yoyo_data.common.dto.request.ApplyRefundRequest;
import com.example.yoyo_data.common.vo.RefundPolicyVO;
import com.example.yoyo_data.common.vo.RefundRecordVO;

/**
 * 退票退款服务接口
 */
public interface RefundService {

    Result<RefundPolicyVO> getRefundPolicy(Long showEventId);

    Result<RefundRecordVO> applyRefund(ApplyRefundRequest req, String token);

    Result<RefundRecordVO> getRefundProgress(Long orderId, String token);

    Result<Page<RefundRecordVO>> getMyRefundList(String token, Integer page, Integer size);
}
