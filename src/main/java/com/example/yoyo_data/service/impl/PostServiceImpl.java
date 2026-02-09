package com.example.yoyo_data.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.yoyo_data.common.vo.PostPageVO;
import com.example.yoyo_data.common.vo.PostVO;
import com.example.yoyo_data.infrastructure.cache.RedisService;
import com.example.yoyo_data.common.Result;
import com.example.yoyo_data.common.dto.PostDTO;
import com.example.yoyo_data.common.entity.Post;
import com.example.yoyo_data.common.entity.PostTag;
import com.example.yoyo_data.common.entity.Tag;
import com.example.yoyo_data.infrastructure.repository.PostMapper;
import com.example.yoyo_data.infrastructure.repository.PostTagMapper;
import com.example.yoyo_data.infrastructure.repository.TagMapper;
import com.example.yoyo_data.infrastructure.repository.UserProfileMapper;
import com.example.yoyo_data.infrastructure.repository.mongodb.TravelPlanRepository;
import com.example.yoyo_data.service.PostService;
import com.example.yoyo_data.service.TagService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.example.yoyo_data.infrastructure.cache.CacheKeyManager.CacheTTL.TWO_HOURS;
import static com.example.yoyo_data.infrastructure.cache.CacheKeyManager.POST_DETAIL_PREFIX;
import static com.example.yoyo_data.infrastructure.cache.CacheKeyManager.POST_LIST_PREFIX;

/**
 * 帖子服务实现类
 */
@Slf4j
@Service
public class PostServiceImpl extends ServiceImpl<PostMapper, Post> implements PostService {
    @Autowired
    private UserProfileMapper userProfileMapper;

    @Autowired
    private RedisService redisService;

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private TagMapper tagMapper;

    @Autowired
    private PostTagMapper postTagMapper;

    @Autowired
    private TravelPlanRepository travelPlanRepository;
    @Autowired
    private TagService tagService;

    @Override
    public Result<?> getPostListByCondition(Integer page, Integer size, Long userId, String title, String content, DateTime beginTime, DateTime endTime) {
        try {
            // 缓存键
            String cacheKey = POST_LIST_PREFIX + page + ":" + size + ":" + (userId != null ? userId : "all") + ":" + (title != null ? title : "") + ":" + (content != null ? content : "") + ":" + (beginTime != null ? beginTime : "") + ":" + (endTime != null ? endTime : "");
            String cachedPostList = redisService.stringGetString(cacheKey);
            if (cachedPostList != null) {
                PostPageVO result = JSON.parseObject(cachedPostList, PostPageVO.class);
                log.info("从缓存获取帖子列表成功: page={}, size={}, userId={}, title={}, content={}, beginTime={}, endTime={}", page, size, userId, title, content, beginTime, endTime);
                return Result.success(result);
            }

            // 构建查询条件
            LambdaQueryWrapper<Post> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Post::getUserId, userId);
            queryWrapper.like(StringUtils.isNotBlank(title), Post::getTitle, title);
            queryWrapper.like(StringUtils.isNotBlank(content), Post::getContent, content);
            queryWrapper.between(beginTime != null && endTime != null, Post::getPublishedAt, beginTime, endTime);

            Page<Post> pageParam = new Page<>(page, size);
            Page<Post> postList = postMapper.selectPage(pageParam, queryWrapper);

            // 构建PostVO
            List<PostVO> postVOList = postList.getRecords().stream().map(post -> PostVO.builder()
                    .id(post.getId())
                    .title(post.getTitle())
                    .content(post.getContent())
                    .category(post.getCategory())
                    .location(post.getLocation())
                    .tripPlanName(travelPlanRepository.findById(post.getTripPlanId()).isPresent() ?
                            travelPlanRepository.findById(post.getTripPlanId()).get().getTitle() : "")
                    .status(post.getStatus())
                    .viewCount(post.getViewCount())
                    .likeCount(post.getLikeCount())
                    .commentCount(post.getCommentCount())
                    .shareCount(post.getShareCount())
                    .isModerated(post.getIsModerated())
                    .moderationStatus(post.getModerationStatus())
                    .moderationReason(post.getModerationReason())
                    .createdAt(post.getCreatedAt())
                    .updatedAt(post.getUpdatedAt())
                    .publishedAt(post.getPublishedAt())
                    .tags(tagService.getTagNamesByPostId(post.getId()))
                    .nickName(userProfileMapper.selectById(post.getUserId()).getFullName())
                    .mediaUrls(post.getMediaUrls())
                    .build()
            ).collect(Collectors.toList());

            PostPageVO postPageVO = PostPageVO.builder()
                    .total(postList.getTotal())
                    .page(page)
                    .size(size)
                    .postList(postVOList)
                    .build();

            redisService.stringSetString(cacheKey, JSON.toJSONString(postPageVO), TWO_HOURS);
            log.info("缓存帖子列表成功: page={}, size={}, userId={}, title={}, content={}, beginTime={}, endTime={}", page, size, userId, title, content, beginTime, endTime);

            return Result.success(postPageVO);

        } catch (Exception e) {
            log.error("获取帖子列表失败", e);
            return Result.error("获取帖子列表失败: " + e.getMessage());
        }
    }

    @Override
    public Result<?> deletePosts(List<Long> postIds, Long userId) {
        // 1. 都是一个作者，判断与userId是否一致
        if (!postIds.stream().map(postId -> postMapper.selectById(postId).getUserId()).allMatch(userId::equals)) {
            return Result.error("帖子作者不一致");
        }

        return postMapper.deleteBatchIds(postIds) > 0 ? Result.success() : Result.error("删除帖子失败");
    }

    @Override
    public Result<?> getPostLikeTopN(Integer topN) {
        // limit topN
        topN = Math.min(topN, 10);
        log.info("获取帖子点赞数topN: topN={}", topN);
        // 1. 获取topN
        List<Post> topNPosts = postMapper.selectList(new LambdaQueryWrapper<Post>()
                .orderByDesc(Post::getLikeCount)
                .orderByDesc(Post::getPublishedAt)
                .last("LIMIT " + topN));
        List<PostVO> postVOList = topNPosts.stream().map(post -> PostVO.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .category(post.getCategory())
                .location(post.getLocation())
                .tripPlanName(travelPlanRepository.findById(post.getTripPlanId()).isPresent() ?
                        travelPlanRepository.findById(post.getTripPlanId()).get().getTitle() : "")
                .status(post.getStatus())
                .viewCount(post.getViewCount())
                .likeCount(post.getLikeCount())
                .commentCount(post.getCommentCount())
                .shareCount(post.getShareCount())
                .isModerated(post.getIsModerated())
                .moderationStatus(post.getModerationStatus())
                .moderationReason(post.getModerationReason())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .publishedAt(post.getPublishedAt())
                .tags(tagService.getTagNamesByPostId(post.getId()))
                .nickName(userProfileMapper.selectById(post.getUserId()).getFullName())
                .mediaUrls(post.getMediaUrls())
                .build()
        ).collect(Collectors.toList());
        return Result.success(postVOList);
    }

    @Override
    public Result<?> getPostList(Integer page, Integer size, String category) {
        try {
            // 缓存键
            String cacheKey = POST_LIST_PREFIX + page + ":" + size + ":" + (category != null ? category : "all");

            // 尝试从缓存获取
            String cachedPostList = redisService.stringGetString(cacheKey);
            if (cachedPostList != null) {
                Map<String, Object> result = JSON.parseObject(cachedPostList, Map.class);
                log.info("从缓存获取帖子列表成功: page={}, size={}, category={}", page, size, category);
                return Result.success(result);
            }

            // 构建查询条件
            LambdaQueryWrapper<Post> queryWrapper = new LambdaQueryWrapper<>();
            if (category != null && !category.isEmpty()) {
                queryWrapper.eq(Post::getCategory, category);
            }
            queryWrapper.orderByDesc(Post::getCreatedAt);

            // 从数据库获取帖子列表
            Page<Post> pageParam = new Page<>(page, size);
            Page<Post> postPage = postMapper.selectPage(pageParam, queryWrapper);

            // 计算总数
            Long total = postMapper.selectCount(
                    category != null && !category.isEmpty() ?
                            new LambdaQueryWrapper<Post>().eq(Post::getCategory, category) :
                            null
            );

            // 构建响应数据
            List<PostVO> postList = postPage.getRecords().stream().map(post -> PostVO.builder()
                    .id(post.getId())
                    .title(post.getTitle())
                    .content(post.getContent())
                    .category(post.getCategory())
                    .location(post.getLocation())
                    .tripPlanName(travelPlanRepository.findById(post.getTripPlanId()).isPresent() ?
                            travelPlanRepository.findById(post.getTripPlanId()).get().getTitle() : "")
                    .status(post.getStatus())
                    .viewCount(post.getViewCount())
                    .likeCount(post.getLikeCount())
                    .commentCount(post.getCommentCount())
                    .shareCount(post.getShareCount())
                    .isModerated(post.getIsModerated())
                    .moderationStatus(post.getModerationStatus())
                    .moderationReason(post.getModerationReason())
                    .createdAt(post.getCreatedAt())
                    .updatedAt(post.getUpdatedAt())
                    .publishedAt(post.getPublishedAt())
                    .tags(tagService.getTagNamesByPostId(post.getId()))
                    .nickName(userProfileMapper.selectById(post.getUserId())==null ? "": userProfileMapper.selectById(post.getUserId()).getFullName())
                    .mediaUrls(post.getMediaUrls())
                    .build()
            ).collect(Collectors.toList());

            PostPageVO result = PostPageVO.builder()
                    .page(page)
                    .size(size)
                    .category(category)
                    .total(total)
                    .postList(postList)
                    .build();

            // 存入缓存，设置过期时间为10分钟
            redisService.stringSetString(cacheKey, JSON.toJSONString(result), TWO_HOURS);

            log.info("获取帖子列表成功: page={}, size={}, category={}", page, size, category);
            return Result.success(result);

        } catch (Exception e) {
            log.error("获取帖子列表失败", e);
            return Result.error("获取帖子列表失败: " + e.getMessage());
        }
    }

    @Override
    public Result<?> getPostDetail(Long postId) {
        try {
            // 缓存键
            String cacheKey = "post:detail:" + postId;

            // 尝试从缓存获取
            String cachedPostDetail = redisService.stringGetString(cacheKey);
            if (cachedPostDetail != null) {
                Map<String, Object> postDetail = JSON.parseObject(cachedPostDetail, Map.class);
                log.info("从缓存获取帖子详情成功: postId={}", postId);
                return Result.success(postDetail);
            }

            // 从数据库获取帖子详情
            Post post = postMapper.selectById(postId);
            if (post == null) {
                return Result.error("帖子不存在");
            }

            // 构建响应数据
            Post postDetail = BeanUtil.copyProperties(post, Post.class);

            // 获取帖子标签
            List<Tag> tags = tagMapper.selectList(
                    new LambdaQueryWrapper<Tag>()
                            .inSql(Tag::getId, "SELECT tag_id FROM post_tags WHERE post_id = " + postId)
            );
            List<String> tagNames = new ArrayList<>();
            for (Tag tag : tags) {
                tagNames.add(tag.getName());
            }
            postDetail.setTags(tagNames);

            // 存入缓存，设置过期时间为30分钟
            redisService.stringSetString(cacheKey, JSON.toJSONString(postDetail), TWO_HOURS);

            log.info("获取帖子详情成功: postId={}", postId);
            return Result.success(postDetail);

        } catch (Exception e) {
            log.error("获取帖子详情失败", e);
            return Result.error("获取帖子详情失败: " + e.getMessage());
        }
    }

    @Override
    public Result<?> createPost(Long userId, PostDTO postDTO) {
        try {
            // 创建帖子对象
            Post post = BeanUtil.copyProperties(postDTO, Post.class);
            post.setUserId(userId);
            post.setLikeCount(0);
            post.setCommentCount(0);
            post.setViewCount(0);
            post.setCreatedAt(LocalDateTime.now());
            post.setUpdatedAt(LocalDateTime.now());

            // 保存帖子到数据库
            postMapper.insert(post);

            // 处理标签
            List<String> tags = postDTO.getTags();
            if (tags != null && !tags.isEmpty()) {
                for (String tagName : tags) {
                    // 检查标签是否存在
                    Tag tag = tagMapper.selectOne(
                            new LambdaQueryWrapper<Tag>()
                                    .eq(Tag::getName, tagName)
                    );
                    
                    if (tag == null) {
                        // 创建新标签
                        tag = new Tag();
                        tag.setName(tagName);
                        tag.setCreatedAt(java.time.LocalDateTime.now());
                        tagMapper.insert(tag);
                    }

                    // 创建帖子标签关联
                    PostTag postTag = new PostTag();
                    postTag.setPostId(post.getId());
                    postTag.setTagId(tag.getId());
                    postTag.setCreatedAt(java.time.LocalDateTime.now());
                    postTagMapper.insert(postTag);
                }
            }

            // 构建响应数据
            post.setTags(tags);
            // 清除帖子列表缓存
            redisService.delByKeyPrefix(POST_LIST_PREFIX);

            log.info("创建帖子成功: userId={}, title={}", userId, postDTO.getTitle());
            return Result.success(post);

        } catch (Exception e) {
            log.error("创建帖子失败", e);
            return Result.error("创建帖子失败: " + e.getMessage());
        }
    }

    @Override
    public Result<?> updatePost(Long postId, Long userId, PostDTO postDTO) {
        try {
            // 从数据库获取帖子
            Post post = postMapper.selectById(postId);
            if (post == null) {
                return Result.error("帖子不存在");
            }

            // 检查是否是帖子的作者
            if (!post.getUserId().equals(userId)) {
                return Result.error("无权限更新此帖子");
            }

            // 更新帖子信息
            post.setTitle(postDTO.getTitle());
            post.setContent(postDTO.getContent());
            post.setCategory(postDTO.getCategory());
            post.setUpdatedAt(LocalDateTime.now());
            post.setMediaUrls(postDTO.getMediaUrls());
            post.setTags(postDTO.getTags());

            // 保存更新
            postMapper.updateById(post);

            // 删除旧的帖子标签关联
            postTagMapper.delete(
                    new LambdaQueryWrapper<PostTag>()
                            .eq(PostTag::getPostId, postId)
            );

            // 处理新标签
            List<String> tags = postDTO.getTags();
            if (tags != null && !tags.isEmpty()) {
                for (String tagName : tags) {
                    // 检查标签是否存在
                    Tag tag = tagMapper.selectOne(
                            new LambdaQueryWrapper<Tag>()
                                    .eq(Tag::getName, tagName)
                    );
                    
                    if (tag == null) {
                        // 创建新标签
                        tag = new Tag();
                        tag.setName(tagName);
                        tag.setCreatedAt(java.time.LocalDateTime.now());
                        tagMapper.insert(tag);
                    }

                    // 创建帖子标签关联
                    PostTag postTag = new PostTag();
                    postTag.setPostId(post.getId());
                    postTag.setTagId(tag.getId());
                    postTag.setCreatedAt(java.time.LocalDateTime.now());
                    postTagMapper.insert(postTag);
                }
            }

            post.setTags(tags);
            post.setUpdatedAt(LocalDateTime.now());

            // 清除帖子详情缓存
            redisService.delete(POST_DETAIL_PREFIX + postId);
            // 清除帖子列表缓存
            redisService.delByKeyPrefix(POST_LIST_PREFIX);

            log.info("更新帖子成功: postId={}, userId={}", postId, userId);
            return Result.success(post);

        } catch (Exception e) {
            log.error("更新帖子失败", e);
            return Result.error("更新帖子失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public Result<?> deletePost(Long postId, Long userId) {
        try {
            // 从数据库获取帖子
            Post post = postMapper.selectById(postId);
            if (post == null) {
                return Result.error("帖子不存在");
            }

            // 检查是否是帖子的作者
            if (!post.getUserId().equals(userId)) {
                return Result.error("无权限删除此帖子");
            }

            // 删除帖子标签关联
            postTagMapper.delete(
                    new LambdaQueryWrapper<PostTag>()
                            .eq(PostTag::getPostId, postId)
            );

            // 删除帖子
            postMapper.deleteById(postId);
            // 清除帖子详情缓存
            redisService.delete(POST_DETAIL_PREFIX + postId);
            // 清除帖子列表缓存
            redisService.delByKeyPrefix(POST_LIST_PREFIX);

            log.info("删除帖子成功: postId={}, userId={}", postId, userId);
            return Result.success("删除帖子成功");

        } catch (Exception e) {
            log.error("删除帖子失败", e);
            return Result.error("删除帖子失败: " + e.getMessage());
        }
    }


}