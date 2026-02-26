package com.example.yoyo_data.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.yoyo_data.common.Result;
import com.example.yoyo_data.common.constant.OrderStatus;
import com.example.yoyo_data.common.constant.RefundStatus;
import com.example.yoyo_data.common.dto.request.ApplyRefundRequest;
import com.example.yoyo_data.common.entity.RefundRecord;
import com.example.yoyo_data.common.entity.ShowEvent;
import com.example.yoyo_data.common.entity.TicketOrder;
import com.example.yoyo_data.common.vo.RefundPolicyVO;
import com.example.yoyo_data.common.vo.RefundRecordVO;
import com.example.yoyo_data.common.vo.TimelineItem;
import com.example.yoyo_data.infrastructure.repository.RefundRecordMapper;
import com.example.yoyo_data.infrastructure.repository.ShowEventMapper;
import com.example.yoyo_data.infrastructure.repository.TicketOrderMapper;
import com.example.yoyo_data.service.RefundService;
import com.example.yoyo_data.util.jwt.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 退票退款服务实现类
 */
@Slf4j
@Service
public class RefundServiceImpl implements RefundService {

    @Autowired
    private RefundRecordMapper refundRecordMapper;

    @Autowired
    private TicketOrderMapper ticketOrderMapper;

    @Autowired
    private ShowEventMapper showEventMapper;

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    public Result<RefundPolicyVO> getRefundPolicy(Long showEventId) {
        ShowEvent showEvent = showEventMapper.selectById(showEventId);
        if (showEvent == null) return Result.error(404, "演出不存在");

        // 演出48小时前可退票，扣10%手续费（模拟大麦网退票政策）
        RefundPolicyVO policy = RefundPolicyVO.builder()
                .showEventId(showEventId)
                .showName(showEvent.getShowName())
                .allowRefund(true)
                .refundDeadline("演出前48小时")
                .handlingFeeRate(new BigDecimal("0.10"))
                .description("距演出48小时前可申请退款，手续费10%。演出前48小时内不支持退款。")
                .build();

        return Result.success(policy);
    }

    @Override
    @Transactional
    public Result<RefundRecordVO> applyRefund(ApplyRefundRequest req, String token) {
        Long userId = getUserId(token);
        if (userId == null) return Result.unauthorized("请先登录");

        TicketOrder order = ticketOrderMapper.selectById(req.getOrderId());
        if (order == null || !order.getUserId().equals(userId)) {
            return Result.error(404, "订单不存在");
        }
        if (!OrderStatus.PAID.equals(order.getStatus())) {
            return Result.error(400, "只有已支付的订单才能申请退款");
        }

        // 检查是否已有退款申请
        LambdaQueryWrapper<RefundRecord> existWrapper = new LambdaQueryWrapper<>();
        existWrapper.eq(RefundRecord::getOrderId, req.getOrderId())
                .ne(RefundRecord::getRefundStatus, RefundStatus.REJECTED);
        if (refundRecordMapper.selectOne(existWrapper) != null) {
            return Result.error(400, "该订单已有退款申请");
        }

        // 计算退款金额（手续费10%）
        BigDecimal originalAmount = order.getTotalAmount();
        BigDecimal handlingFee = originalAmount.multiply(new BigDecimal("0.10"))
                .setScale(2, java.math.RoundingMode.HALF_UP);
        BigDecimal refundAmount = originalAmount.subtract(handlingFee);

        // 生成退款单号
        String refundNo = "RF" + System.currentTimeMillis() + UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();

        RefundRecord record = RefundRecord.builder()
                .refundNo(refundNo)
                .orderId(order.getId())
                .orderNo(order.getOrderNo())
                .userId(userId)
                .showEventId(order.getShowEventId())
                .originalAmount(originalAmount)
                .handlingFee(handlingFee)
                .refundAmount(refundAmount)
                .reason(req.getReason())
                .remark(req.getRemark())
                .refundStatus(RefundStatus.PENDING)
                .payType(order.getPayType())
                .transactionId(order.getTransactionId())
                .applyTime(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        refundRecordMapper.insert(record);

        // 更新订单状态为退款中
        LambdaUpdateWrapper<TicketOrder> orderUpdate = new LambdaUpdateWrapper<>();
        orderUpdate.eq(TicketOrder::getId, order.getId())
                .set(TicketOrder::getStatus, OrderStatus.REFUNDING)
                .set(TicketOrder::getRefundId, record.getId());
        ticketOrderMapper.update(null, orderUpdate);

        ShowEvent showEvent = showEventMapper.selectById(order.getShowEventId());
        return Result.success(toVO(record, showEvent != null ? showEvent.getShowName() : null, true));
    }

    @Override
    public Result<RefundRecordVO> getRefundProgress(Long orderId, String token) {
        Long userId = getUserId(token);
        if (userId == null) return Result.unauthorized("请先登录");

        LambdaQueryWrapper<RefundRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RefundRecord::getOrderId, orderId)
                .eq(RefundRecord::getUserId, userId)
                .orderByDesc(RefundRecord::getApplyTime)
                .last("LIMIT 1");
        RefundRecord record = refundRecordMapper.selectOne(wrapper);

        if (record == null) return Result.error(404, "退款记录不存在");

        ShowEvent showEvent = showEventMapper.selectById(record.getShowEventId());
        return Result.success(toVO(record, showEvent != null ? showEvent.getShowName() : null, true));
    }

    @Override
    public Result<Page<RefundRecordVO>> getMyRefundList(String token, Integer page, Integer size) {
        Long userId = getUserId(token);
        if (userId == null) return Result.unauthorized("请先登录");

        LambdaQueryWrapper<RefundRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RefundRecord::getUserId, userId)
                .orderByDesc(RefundRecord::getApplyTime);

        Page<RefundRecord> recordPage = refundRecordMapper.selectPage(new Page<>(page, size), wrapper);
        Page<RefundRecordVO> voPage = new Page<>(recordPage.getCurrent(), recordPage.getSize(), recordPage.getTotal());

        List<RefundRecordVO> voList = recordPage.getRecords().stream().map(r -> {
            ShowEvent showEvent = showEventMapper.selectById(r.getShowEventId());
            return toVO(r, showEvent != null ? showEvent.getShowName() : null, false);
        }).collect(Collectors.toList());
        voPage.setRecords(voList);

        return Result.success(voPage);
    }

    private RefundRecordVO toVO(RefundRecord record, String showName, boolean withTimeline) {
        RefundRecordVO vo = RefundRecordVO.builder()
                .refundId(record.getId())
                .orderId(record.getOrderId())
                .orderNo(record.getOrderNo())
                .showName(showName)
                .originalAmount(record.getOriginalAmount())
                .handlingFee(record.getHandlingFee())
                .refundAmount(record.getRefundAmount())
                .refundStatus(record.getRefundStatus())
                .payType(record.getPayType())
                .transactionId(record.getTransactionId())
                .applyTime(record.getApplyTime())
                .refundTime(record.getRefundTime())
                .build();

        if (withTimeline) {
            List<TimelineItem> timeline = new ArrayList<>();
            timeline.add(TimelineItem.builder()
                    .status("APPLY")
                    .title("申请退款")
                    .description("退款申请已提交，等待审核")
                    .time(record.getApplyTime())
                    .build());
            if (RefundStatus.APPROVED.equals(record.getRefundStatus())
                    || RefundStatus.REFUNDED.equals(record.getRefundStatus())) {
                timeline.add(TimelineItem.builder()
                        .status("APPROVED")
                        .title("审核通过")
                        .description("退款申请已审核通过")
                        .time(record.getUpdatedAt())
                        .build());
            }
            if (RefundStatus.REFUNDED.equals(record.getRefundStatus())) {
                timeline.add(TimelineItem.builder()
                        .status("REFUNDED")
                        .title("退款成功")
                        .description("退款已到账，请注意查收")
                        .time(record.getRefundTime())
                        .build());
            }
            if (RefundStatus.REJECTED.equals(record.getRefundStatus())) {
                timeline.add(TimelineItem.builder()
                        .status("REJECTED")
                        .title("审核拒绝")
                        .description("退款申请被拒绝")
                        .time(record.getUpdatedAt())
                        .build());
            }
            vo.setTimeline(timeline);
        }

        return vo;
    }

    private Long getUserId(String token) {
        if (!StringUtils.hasText(token)) return null;
        if (!Boolean.TRUE.equals(jwtUtils.validateToken(token))) return null;
        return jwtUtils.getUserIdFromToken(token);
    }
}
