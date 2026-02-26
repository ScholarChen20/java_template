package com.example.yoyo_data.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.yoyo_data.common.Result;
import com.example.yoyo_data.common.dto.request.ApplyRefundRequest;
import com.example.yoyo_data.common.vo.RefundPolicyVO;
import com.example.yoyo_data.common.vo.RefundRecordVO;
import com.example.yoyo_data.service.RefundService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

/**
 * 退票退款控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/ticket")
@Api(tags = "退票退款", description = "退票政策查询、申请退款、退款进度查询")
public class RefundController {

    @Autowired
    private RefundService refundService;

    @GetMapping("/shows/{showEventId}/refund-policy")
    @ApiOperation(value = "查询退票政策", notes = "查询指定演出的退票规则")
    public Result<RefundPolicyVO> getRefundPolicy(
            @ApiParam(value = "演出活动ID", required = true) @PathVariable Long showEventId
    ) {
        log.info("查询退票政策: showEventId={}", showEventId);
        return refundService.getRefundPolicy(showEventId);
    }

    @PostMapping("/refund")
    @ApiOperation(value = "申请退款", notes = "对已支付订单申请退款。需要JWT认证")
    public Result<RefundRecordVO> applyRefund(
            @Valid @RequestBody ApplyRefundRequest req,
            HttpServletRequest request
    ) {
        String token = extractToken(request);
        log.info("申请退款: orderId={}, reason={}", req.getOrderId(), req.getReason());
        return refundService.applyRefund(req, token);
    }

    @GetMapping("/refund/{orderId}")
    @ApiOperation(value = "查询退款进度", notes = "查询指定订单的退款进度（含时间线）。需要JWT认证")
    public Result<RefundRecordVO> getRefundProgress(
            @ApiParam(value = "订单ID", required = true) @PathVariable Long orderId,
            HttpServletRequest request
    ) {
        String token = extractToken(request);
        log.info("查询退款进度: orderId={}", orderId);
        return refundService.getRefundProgress(orderId, token);
    }

    @GetMapping("/refunds")
    @ApiOperation(value = "查询我的退款列表", notes = "分页查询当前用户所有退款记录。需要JWT认证")
    public Result<Page<RefundRecordVO>> getMyRefundList(
            @ApiParam(value = "页码", defaultValue = "1") @RequestParam(defaultValue = "1") Integer page,
            @ApiParam(value = "每页大小", defaultValue = "10") @RequestParam(defaultValue = "10") Integer size,
            HttpServletRequest request
    ) {
        String token = extractToken(request);
        return refundService.getMyRefundList(token, page, size);
    }

    private String extractToken(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return token;
    }
}
