package com.example.yoyo_data.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.yoyo_data.common.Result;
import com.example.yoyo_data.common.dto.request.ContactRequest;
import com.example.yoyo_data.common.dto.request.UpdateViewerRequest;
import com.example.yoyo_data.common.entity.Viewer;
import com.example.yoyo_data.common.vo.ViewerVO;
import com.example.yoyo_data.infrastructure.repository.ViewerMapper;
import com.example.yoyo_data.service.ViewerService;
import com.example.yoyo_data.util.jwt.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 观演人服务实现类
 */
@Slf4j
@Service
public class ViewerServiceImpl implements ViewerService {

    @Autowired
    private ViewerMapper viewerMapper;

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    public Result<List<ViewerVO>> getViewerList(String token) {
        Long userId = getUserId(token);
        if (userId == null) return Result.unauthorized("请先登录");

        LambdaQueryWrapper<Viewer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Viewer::getUserId, userId)
                .eq(Viewer::getIsDeleted, false)
                .orderByDesc(Viewer::getIsDefault)
                .orderByDesc(Viewer::getCreatedAt);

        List<Viewer> viewers = viewerMapper.selectList(wrapper);
        List<ViewerVO> voList = viewers.stream().map(this::toVO).collect(Collectors.toList());

        return Result.success(voList);
    }

    @Override
    @Transactional
    public Result<ViewerVO> addViewer(ContactRequest req, String token) {
        Long userId = getUserId(token);
        if (userId == null) return Result.unauthorized("请先登录");

        // 若设为默认，先清除其他默认
        if (Boolean.TRUE.equals(req.getIsDefault())) {
            clearDefault(userId);
        }

        Viewer viewer = Viewer.builder()
                .userId(userId)
                .name(req.getName())
                .idCard(req.getIdCard())
                .phone(req.getPhone())
                .isDefault(req.getIsDefault() != null && req.getIsDefault())
                .isVerified(false)
                .isDeleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        viewerMapper.insert(viewer);
        return Result.success(toVO(viewer));
    }

    @Override
    @Transactional
    public Result<String> updateViewer(Long viewerId, UpdateViewerRequest req, String token) {
        Long userId = getUserId(token);
        if (userId == null) return Result.unauthorized("请先登录");

        Viewer viewer = viewerMapper.selectById(viewerId);
        if (viewer == null || !viewer.getUserId().equals(userId) || Boolean.TRUE.equals(viewer.getIsDeleted())) {
            return Result.error(404, "观演人不存在");
        }

        // 若设为默认，先清除其他默认
        if (Boolean.TRUE.equals(req.getIsDefault())) {
            clearDefault(userId);
        }

        LambdaUpdateWrapper<Viewer> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Viewer::getId, viewerId);
        if (StringUtils.hasText(req.getPhone())) {
            updateWrapper.set(Viewer::getPhone, req.getPhone());
        }
        if (req.getIsDefault() != null) {
            updateWrapper.set(Viewer::getIsDefault, req.getIsDefault());
        }
        updateWrapper.set(Viewer::getUpdatedAt, LocalDateTime.now());
        viewerMapper.update(null, updateWrapper);

        return Result.success("更新成功");
    }

    @Override
    public Result<String> deleteViewer(Long viewerId, String token) {
        Long userId = getUserId(token);
        if (userId == null) return Result.unauthorized("请先登录");

        Viewer viewer = viewerMapper.selectById(viewerId);
        if (viewer == null || !viewer.getUserId().equals(userId)) {
            return Result.error(404, "观演人不存在");
        }

        LambdaUpdateWrapper<Viewer> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Viewer::getId, viewerId)
                .set(Viewer::getIsDeleted, true)
                .set(Viewer::getUpdatedAt, LocalDateTime.now());
        viewerMapper.update(null, updateWrapper);

        return Result.success("删除成功");
    }

    @Override
    @Transactional
    public Result<String> setDefaultViewer(Long viewerId, String token) {
        Long userId = getUserId(token);
        if (userId == null) return Result.unauthorized("请先登录");

        Viewer viewer = viewerMapper.selectById(viewerId);
        if (viewer == null || !viewer.getUserId().equals(userId) || Boolean.TRUE.equals(viewer.getIsDeleted())) {
            return Result.error(404, "观演人不存在");
        }

        clearDefault(userId);

        LambdaUpdateWrapper<Viewer> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Viewer::getId, viewerId)
                .set(Viewer::getIsDefault, true)
                .set(Viewer::getUpdatedAt, LocalDateTime.now());
        viewerMapper.update(null, updateWrapper);

        return Result.success("设置成功");
    }

    private void clearDefault(Long userId) {
        LambdaUpdateWrapper<Viewer> clearWrapper = new LambdaUpdateWrapper<>();
        clearWrapper.eq(Viewer::getUserId, userId)
                .eq(Viewer::getIsDefault, true)
                .set(Viewer::getIsDefault, false);
        viewerMapper.update(null, clearWrapper);
    }

    private ViewerVO toVO(Viewer viewer) {
        return ViewerVO.builder()
                .id(viewer.getId())
                .name(viewer.getName())
                .idCardMasked(maskIdCard(viewer.getIdCard()))
                .phone(viewer.getPhone())
                .isDefault(viewer.getIsDefault())
                .isVerified(viewer.getIsVerified())
                .createdAt(viewer.getCreatedAt())
                .build();
    }

    private String maskIdCard(String idCard) {
        if (idCard == null || idCard.length() < 8) return idCard;
        return idCard.substring(0, 4) + "****" + idCard.substring(idCard.length() - 4);
    }

    private Long getUserId(String token) {
        if (!StringUtils.hasText(token)) return null;
        if (!Boolean.TRUE.equals(jwtUtils.validateToken(token))) return null;
        return jwtUtils.getUserIdFromToken(token);
    }
}
