package com.example.yoyo_data.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.yoyo_data.common.Result;
import com.example.yoyo_data.common.constant.RemindStatus;
import com.example.yoyo_data.common.entity.ShowEvent;
import com.example.yoyo_data.common.entity.ShowRemind;
import com.example.yoyo_data.common.entity.ShowWish;
import com.example.yoyo_data.common.vo.ShowEventVO;
import com.example.yoyo_data.common.vo.ShowRemindVO;
import com.example.yoyo_data.common.vo.ShowWishVO;
import com.example.yoyo_data.infrastructure.repository.ShowEventMapper;
import com.example.yoyo_data.infrastructure.repository.ShowRemindMapper;
import com.example.yoyo_data.infrastructure.repository.ShowWishMapper;
import com.example.yoyo_data.service.ShowRemindService;
import com.example.yoyo_data.util.jwt.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 开票提醒与想看服务实现类
 */
@Slf4j
@Service
public class ShowRemindServiceImpl implements ShowRemindService {

    @Autowired
    private ShowRemindMapper showRemindMapper;

    @Autowired
    private ShowWishMapper showWishMapper;

    @Autowired
    private ShowEventMapper showEventMapper;

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    public Result<ShowRemindVO> setRemind(Long showEventId, String token) {
        Long userId = getUserId(token);
        if (userId == null) return Result.unauthorized("请先登录");

        ShowEvent showEvent = showEventMapper.selectById(showEventId);
        if (showEvent == null) return Result.error(404, "演出不存在");

        // 幂等处理
        LambdaQueryWrapper<ShowRemind> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShowRemind::getUserId, userId).eq(ShowRemind::getShowEventId, showEventId);
        ShowRemind existing = showRemindMapper.selectOne(wrapper);

        if (existing == null) {
            ShowRemind remind = ShowRemind.builder()
                    .userId(userId)
                    .showEventId(showEventId)
                    .remindStatus(RemindStatus.PENDING)
                    .createdAt(LocalDateTime.now())
                    .build();
            showRemindMapper.insert(remind);
            existing = remind;
        }

        // 统计提醒人数
        long remindCount = showRemindMapper.selectCount(
                new LambdaQueryWrapper<ShowRemind>().eq(ShowRemind::getShowEventId, showEventId));

        return Result.success(buildRemindVO(showEvent, existing, (int) remindCount));
    }

    @Override
    public Result<String> cancelRemind(Long showEventId, String token) {
        Long userId = getUserId(token);
        if (userId == null) return Result.unauthorized("请先登录");

        LambdaQueryWrapper<ShowRemind> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShowRemind::getUserId, userId).eq(ShowRemind::getShowEventId, showEventId);
        showRemindMapper.delete(wrapper);

        return Result.success("取消提醒成功");
    }

    @Override
    public Result<ShowRemindVO> getRemindStatus(Long showEventId, String token) {
        Long userId = getUserId(token);
        if (userId == null) return Result.unauthorized("请先登录");

        ShowEvent showEvent = showEventMapper.selectById(showEventId);
        if (showEvent == null) return Result.error(404, "演出不存在");

        LambdaQueryWrapper<ShowRemind> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShowRemind::getUserId, userId).eq(ShowRemind::getShowEventId, showEventId);
        ShowRemind remind = showRemindMapper.selectOne(wrapper);

        long remindCount = showRemindMapper.selectCount(
                new LambdaQueryWrapper<ShowRemind>().eq(ShowRemind::getShowEventId, showEventId));

        if (remind == null) {
            return Result.success(ShowRemindVO.builder()
                    .showEventId(showEventId)
                    .showName(showEvent.getShowName())
                    .saleStartTime(showEvent.getSaleStartTime())
                    .remindCount((int) remindCount)
                    .isReminded(false)
                    .build());
        }

        return Result.success(buildRemindVO(showEvent, remind, (int) remindCount));
    }

    @Override
    public Result<Page<ShowEventVO>> getMyRemindList(String token, Integer page, Integer size) {
        Long userId = getUserId(token);
        if (userId == null) return Result.unauthorized("请先登录");

        LambdaQueryWrapper<ShowRemind> remindWrapper = new LambdaQueryWrapper<>();
        remindWrapper.eq(ShowRemind::getUserId, userId).orderByDesc(ShowRemind::getCreatedAt);
        Page<ShowRemind> remindPage = showRemindMapper.selectPage(new Page<>(page, size), remindWrapper);

        List<Long> showIds = remindPage.getRecords().stream()
                .map(ShowRemind::getShowEventId).collect(Collectors.toList());

        Page<ShowEventVO> voPage = new Page<>(remindPage.getCurrent(), remindPage.getSize(), remindPage.getTotal());
        if (showIds.isEmpty()) {
            voPage.setRecords(java.util.Collections.emptyList());
            return Result.success(voPage);
        }

        List<ShowEvent> shows = showEventMapper.selectBatchIds(showIds);
        List<ShowEventVO> voList = shows.stream().map(s -> {
            ShowEventVO vo = new ShowEventVO();
            BeanUtils.copyProperties(s, vo);
            vo.setIsReminded(true);
            return vo;
        }).collect(Collectors.toList());
        voPage.setRecords(voList);

        return Result.success(voPage);
    }

    @Override
    public Result<ShowWishVO> setWish(Long showEventId, String token) {
        Long userId = getUserId(token);
        if (userId == null) return Result.unauthorized("请先登录");

        ShowEvent showEvent = showEventMapper.selectById(showEventId);
        if (showEvent == null) return Result.error(404, "演出不存在");

        // 幂等处理
        LambdaQueryWrapper<ShowWish> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShowWish::getUserId, userId).eq(ShowWish::getShowEventId, showEventId);
        ShowWish existing = showWishMapper.selectOne(wrapper);

        if (existing == null) {
            ShowWish wish = ShowWish.builder()
                    .userId(userId)
                    .showEventId(showEventId)
                    .createdAt(LocalDateTime.now())
                    .build();
            showWishMapper.insert(wish);

            // 更新 hot_score（+1）
            LambdaUpdateWrapper<ShowEvent> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(ShowEvent::getId, showEventId)
                    .setSql("wish_count = wish_count + 1, hot_score = hot_score + 1");
            showEventMapper.update(null, updateWrapper);
        }

        long wishCount = showWishMapper.selectCount(
                new LambdaQueryWrapper<ShowWish>().eq(ShowWish::getShowEventId, showEventId));

        return Result.success(ShowWishVO.builder()
                .showEventId(showEventId)
                .wishCount((int) wishCount)
                .build());
    }

    @Override
    public Result<String> cancelWish(Long showEventId, String token) {
        Long userId = getUserId(token);
        if (userId == null) return Result.unauthorized("请先登录");

        LambdaQueryWrapper<ShowWish> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShowWish::getUserId, userId).eq(ShowWish::getShowEventId, showEventId);
        int deleted = showWishMapper.delete(wrapper);

        if (deleted > 0) {
            LambdaUpdateWrapper<ShowEvent> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(ShowEvent::getId, showEventId)
                    .setSql("wish_count = GREATEST(wish_count - 1, 0), hot_score = GREATEST(hot_score - 1, 0)");
            showEventMapper.update(null, updateWrapper);
        }

        return Result.success("取消想看成功");
    }

    @Override
    public Result<Page<ShowEventVO>> getMyWishList(String token, String status, Integer page, Integer size) {
        Long userId = getUserId(token);
        if (userId == null) return Result.unauthorized("请先登录");

        LambdaQueryWrapper<ShowWish> wishWrapper = new LambdaQueryWrapper<>();
        wishWrapper.eq(ShowWish::getUserId, userId).orderByDesc(ShowWish::getCreatedAt);
        Page<ShowWish> wishPage = showWishMapper.selectPage(new Page<>(page, size), wishWrapper);

        List<Long> showIds = wishPage.getRecords().stream()
                .map(ShowWish::getShowEventId).collect(Collectors.toList());

        Page<ShowEventVO> voPage = new Page<>(wishPage.getCurrent(), wishPage.getSize(), wishPage.getTotal());
        if (showIds.isEmpty()) {
            voPage.setRecords(java.util.Collections.emptyList());
            return Result.success(voPage);
        }

        LambdaQueryWrapper<ShowEvent> showWrapper = new LambdaQueryWrapper<>();
        showWrapper.in(ShowEvent::getId, showIds);
        if (StringUtils.hasText(status)) {
            showWrapper.eq(ShowEvent::getStatus, status);
        }
        List<ShowEvent> shows = showEventMapper.selectList(showWrapper);

        List<ShowEventVO> voList = shows.stream().map(s -> {
            ShowEventVO vo = new ShowEventVO();
            BeanUtils.copyProperties(s, vo);
            vo.setIsWished(true);
            return vo;
        }).collect(Collectors.toList());
        voPage.setRecords(voList);

        return Result.success(voPage);
    }

    private ShowRemindVO buildRemindVO(ShowEvent showEvent, ShowRemind remind, int remindCount) {
        return ShowRemindVO.builder()
                .showEventId(showEvent.getId())
                .showName(showEvent.getShowName())
                .saleStartTime(showEvent.getSaleStartTime())
                .remindTime(remind.getCreatedAt())
                .remindCount(remindCount)
                .isReminded(true)
                .remindStatus(remind.getRemindStatus())
                .build();
    }

    private Long getUserId(String token) {
        if (!StringUtils.hasText(token)) return null;
        if (!Boolean.TRUE.equals(jwtUtils.validateToken(token))) return null;
        return jwtUtils.getUserIdFromToken(token);
    }
}
