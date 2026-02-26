package com.example.yoyo_data.controller;

import com.example.yoyo_data.common.Result;
import com.example.yoyo_data.common.dto.request.ContactRequest;
import com.example.yoyo_data.common.dto.request.UpdateViewerRequest;
import com.example.yoyo_data.common.vo.ViewerVO;
import com.example.yoyo_data.service.ViewerService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

/**
 * 观演人管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/user/viewers")
@Api(tags = "观演人管理", description = "观演人的增删改查操作")
public class ViewerController {

    @Autowired
    private ViewerService viewerService;

    @GetMapping
    @ApiOperation(value = "获取观演人列表", notes = "获取当前用户的所有观演人。需要JWT认证")
    public Result<List<ViewerVO>> getViewerList(HttpServletRequest request) {
        String token = extractToken(request);
        return viewerService.getViewerList(token);
    }

    @PostMapping
    @ApiOperation(value = "添加观演人", notes = "添加新的观演人。需要JWT认证")
    public Result<ViewerVO> addViewer(
            @Valid @RequestBody ContactRequest req,
            HttpServletRequest request
    ) {
        String token = extractToken(request);
        return viewerService.addViewer(req, token);
    }

    @PutMapping("/{viewerId}")
    @ApiOperation(value = "更新观演人", notes = "更新观演人手机号或默认标记。需要JWT认证")
    public Result<String> updateViewer(
            @ApiParam(value = "观演人ID", required = true) @PathVariable("viewerId") Long viewerId,
            @Valid @RequestBody UpdateViewerRequest req,
            HttpServletRequest request
    ) {
        String token = extractToken(request);
        return viewerService.updateViewer(viewerId, req, token);
    }

    @DeleteMapping("/{viewerId}")
    @ApiOperation(value = "删除观演人", notes = "软删除观演人。需要JWT认证")
    public Result<String> deleteViewer(
            @ApiParam(value = "观演人ID", required = true) @PathVariable("viewerId") Long viewerId,
            HttpServletRequest request
    ) {
        String token = extractToken(request);
        return viewerService.deleteViewer(viewerId, token);
    }

    @PutMapping("/{viewerId}/default")
    @ApiOperation(value = "设为默认观演人", notes = "将指定观演人设为默认。需要JWT认证")
    public Result<String> setDefaultViewer(
            @ApiParam(value = "观演人ID", required = true) @PathVariable("viewerId") Long viewerId,
            HttpServletRequest request
    ) {
        String token = extractToken(request);
        return viewerService.setDefaultViewer(viewerId, token);
    }

    private String extractToken(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return token;
    }
}
