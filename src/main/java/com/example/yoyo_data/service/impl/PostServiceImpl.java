package com.example.yoyo_data.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.yoyo_data.common.dto.JwtUserDTO;
import com.example.yoyo_data.common.dto.PostPageDTO;
import com.example.yoyo_data.infrastructure.cache.RedisService;
import com.example.yoyo_data.common.Result;
import com.example.yoyo_data.common.dto.request.CreatePostRequest;
import com.example.yoyo_data.common.dto.request.UpdatePostRequest;
import com.example.yoyo_data.common.entity.Post;
import com.example.yoyo_data.common.entity.PostTag;
import com.example.yoyo_data.common.entity.Tag;
import com.example.yoyo_data.infrastructure.message.KafkaProducerTemplate;
import com.example.yoyo_data.infrastructure.message.post.PostMessageEvent;
import com.example.yoyo_data.infrastructure.repository.PostMapper;
import com.example.yoyo_data.infrastructure.repository.PostTagMapper;
import com.example.yoyo_data.infrastructure.repository.TagMapper;
import com.example.yoyo_data.service.PostService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.*;

import static com.example.yoyo_data.infrastructure.cache.CacheKeyManager.*;

/**
 * 帖子服务实现类
 */
@Slf4j
@Service
public class PostServiceImpl implements PostService {
    @Autowired
    private KafkaProducerTemplate  kafkaProducerTemplate;

    @Autowired
    private RedisService redisService;

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private TagMapper tagMapper;

    @Autowired
    private PostTagMapper postTagMapper;

    @Override
    public Result<?> getPostList(Integer page, Integer size, String category) {
        try {
            // 缓存键
            String cacheKey = "post:list:" + page + ":" + size + ":" + (category != null ? category : "all");

            // 尝试从缓存获取
            String cachedPostList = redisService.stringGetString(cacheKey);
            if (cachedPostList != null) {
                Post result = JSON.parseObject(cachedPostList, Post.class);
                log.info("从缓存获取帖子列表成功: page={}, size={}, category={}", page, size, category);
                return Result.success(result);
            }

            // 计算分页参数
            int offset = (page - 1) * size;

            // 构建查询条件
            LambdaQueryWrapper<Post> queryWrapper = new LambdaQueryWrapper<>();
            if (category != null && !category.isEmpty()) {
                queryWrapper.eq(Post::getCategory, category);
            }
            queryWrapper.orderByDesc(Post::getCreatedAt);
            queryWrapper.last("LIMIT " + offset + ", " + size);

            // 从数据库获取帖子列表
            List<Post> posts = postMapper.selectList(queryWrapper);

            // 计算总数
            Long total = postMapper.selectCount(
                    category != null && !category.isEmpty() ?
                            new LambdaQueryWrapper<Post>().eq(Post::getCategory, category) :
                            null
            );

            // 构建响应数据
            List<Post> postList = new ArrayList<>();
            for (Post post : posts) {
                Post postInfo = getPost(post);

                // 获取帖子标签
                List<Tag> tags = tagMapper.selectList(
                        new LambdaQueryWrapper<Tag>()
                                .inSql(Tag::getId, "SELECT tag_id FROM post_tags WHERE post_id = " + post.getId())
                );
                List<String> tagNames = new ArrayList<>();
                for (Tag tag : tags) {
                    tagNames.add(tag.getName());
                }
                postList.add(postInfo);
            }

            PostPageDTO postPageDTO = new PostPageDTO();
            postPageDTO.setPostList(postList);
            postPageDTO.setTotal(total);
            postPageDTO.setPage(page);
            postPageDTO.setSize(size);
            postPageDTO.setCategory(category);

            // 存入缓存，设置过期时间为10分钟
            redisService.stringSetString(cacheKey, JSON.toJSONString(postPageDTO), 3600L);

            log.info("获取帖子列表成功: page={}, size={}, category={}", page, size, category);
            return Result.success(postPageDTO);

        } catch (Exception e) {
            log.error("获取帖子列表失败", e);
            return Result.error("获取帖子列表失败: " + e.getMessage());
        }
    }

    private static Post getPost(Post post) {
        Post postInfo = new Post();
        postInfo.setId(post.getId());
        postInfo.setUserId(post.getUserId());
        postInfo.setTitle(post.getTitle());
        postInfo.setContent(post.getContent());
        postInfo.setCategory(post.getCategory());
        postInfo.setLikeCount(post.getLikeCount());
        postInfo.setCommentCount(post.getCommentCount());
        postInfo.setViewCount(post.getViewCount());
        postInfo.setCreatedAt(post.getCreatedAt());
        postInfo.setUpdatedAt(post.getUpdatedAt());
        return postInfo;
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
            Post postDetail = getPost(post);

            // 获取帖子标签
            List<Tag> tags = tagMapper.selectList(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Tag>()
                            .inSql(Tag::getId, "SELECT tag_id FROM post_tags WHERE post_id = " + postId)
            );
            List<String> tagNames = new ArrayList<>();
            for (Tag tag : tags) {
                tagNames.add(tag.getName());
            }
            postDetail.setTags(tagNames);

            // 存入缓存，设置过期时间为30分钟
            redisService.stringSetString(cacheKey, JSON.toJSONString(postDetail), 1800L);

            log.info("获取帖子详情成功: postId={}", postId);
            return Result.success(postDetail);

        } catch (Exception e) {
            log.error("获取帖子详情失败", e);
            return Result.error("获取帖子详情失败: " + e.getMessage());
        }
    }

    @Override
    public Result<?> createPost(Long userId, CreatePostRequest request) {
        try {
            // 创建帖子对象
            Post post = BeanUtil.copyProperties(request, Post.class);
            post.setUserId(userId);
            post.setLikeCount(0);
            post.setCommentCount(0);
            post.setViewCount(0);
            post.setCreatedAt(java.time.LocalDateTime.now());
            post.setUpdatedAt(java.time.LocalDateTime.now());

            // 保存帖子到数据库
            postMapper.insert(post);

            // 处理标签
            List<String> tags = request.getTags();
            if (tags != null && !tags.isEmpty()) {
                for (String tagName : tags) {
                    // 检查标签是否存在
                    Tag tag = tagMapper.selectOne(
                            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Tag>()
                                    .eq(Tag::getName, tagName)
                    );
                    
                    if (tag == null) {
                        // 创建新标签
                        tag = new Tag();
                        tag.setCategory(post.getCategory());
                        tag.setName(tagName);
                        tag.setCreatedAt(LocalDateTime.now());
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
            Post result = getPost(post);

            // 清除帖子列表缓存
            redisService.delByKeyPrefix(POST_LIST_PREFIX);

            log.info("创建帖子成功: userId={}, title={}", userId, request.getTitle());
            return Result.success(result);

        } catch (Exception e) {
            log.error("创建帖子失败", e);
            return Result.error("创建帖子失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<?> updatePost(Long postId, Long userId, UpdatePostRequest request) {
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
            post.setTitle(request.getTitle());
            post.setContent(request.getContent());
            post.setCategory(request.getCategory());
            post.setUpdatedAt(java.time.LocalDateTime.now());

            // 保存更新
            postMapper.updateById(post);

            // 删除旧的帖子标签关联
            postTagMapper.delete(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PostTag>()
                            .eq(PostTag::getPostId, postId)
            );

            // 处理新标签
            List<String> tags = request.getTags();
            if (tags != null && !tags.isEmpty()) {
                for (String tagName : tags) {
                    // 检查标签是否存在
                    Tag tag = tagMapper.selectOne(
                            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Tag>()
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
            Post result = getPost(post);

            // 清除帖子详情缓存
            redisService.delete(POST_DETAIL_PREFIX + postId);
            // 清除帖子列表缓存
            redisService.delByKeyPrefix(POST_LIST_PREFIX);

            log.info("更新帖子成功: postId={}, userId={}", postId, userId);
            return Result.success(result);

        } catch (Exception e) {
            log.error("更新帖子失败", e);
            return Result.error("更新帖子失败: " + e.getMessage());
        }
    }

    @Override
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
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PostTag>()
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

    @Override
    public Result<?> sendCreatePostMsg(CreatePostRequest request, String token) {
        try {
            // 1. 从token中取出用户id
            JwtUserDTO jwtUserDTO = redisService.getCacheObject(USER_TOKEN_PREFIX + token);
            if(jwtUserDTO == null){
                return Result.error("用户未登录或token已过期");
            }

            Long userId = jwtUserDTO.getId();

            // 构建帖子创建事件
            String data = JSON.toJSONString(request);
            PostMessageEvent event = PostMessageEvent.buildCreateEvent(userId, data, request.getTitle(), request.getCategory());

            // 发送到Kafka队列
            boolean sent = kafkaProducerTemplate.sendEvent("posts-create", event);

            if (sent) {
                log.info("帖子创建请求已发送到队列: title={}, category={}", request.getTitle(), request.getCategory());
                // 立即返回"帖子创建中"响应
                return Result.success();
            } else {
                log.error("帖子创建请求发送到队列失败: title={}, category={}", request.getTitle(), request.getCategory());
                return Result.error("请求发送失败，请稍后重试");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}