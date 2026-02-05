package com.example.yoyo_data.service.impl;

import com.alibaba.fastjson.JSON;
import com.example.yoyo_data.infrastructure.cache.RedisService;
import com.example.yoyo_data.common.Result;
import com.example.yoyo_data.common.dto.request.CreatePostRequest;
import com.example.yoyo_data.common.dto.request.UpdatePostRequest;
import com.example.yoyo_data.common.pojo.Post;
import com.example.yoyo_data.common.pojo.PostTag;
import com.example.yoyo_data.common.pojo.Tag;
import com.example.yoyo_data.infrastructure.repository.PostMapper;
import com.example.yoyo_data.infrastructure.repository.PostTagMapper;
import com.example.yoyo_data.infrastructure.repository.TagMapper;
import com.example.yoyo_data.service.PostService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 帖子服务实现类
 */
@Slf4j
@Service
public class PostServiceImpl implements PostService {

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
                Map<String, Object> result = JSON.parseObject(cachedPostList, Map.class);
                log.info("从缓存获取帖子列表成功: page={}, size={}, category={}", page, size, category);
                return Result.success(result);
            }

            // 计算分页参数
            int offset = (page - 1) * size;

            // 构建查询条件
            com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Post> queryWrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
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
                            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Post>().eq(Post::getCategory, category) :
                            null
            );

            // 构建响应数据
            List<Map<String, Object>> postList = new ArrayList<>();
            for (Post post : posts) {
                Map<String, Object> postInfo = new HashMap<>();
                postInfo.put("id", post.getId());
                postInfo.put("userId", post.getUserId());
                postInfo.put("title", post.getTitle());
                postInfo.put("content", post.getContent());
                postInfo.put("category", post.getCategory());
                postInfo.put("likeCount", post.getLikeCount());
                postInfo.put("commentCount", post.getCommentCount());
                postInfo.put("viewCount", post.getViewCount());
                postInfo.put("createdAt", post.getCreatedAt());
                postInfo.put("updatedAt", post.getUpdatedAt());

                // 获取帖子标签
                List<Tag> tags = tagMapper.selectList(
                        new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Tag>()
                                .inSql(Tag::getId, "SELECT tag_id FROM post_tag WHERE post_id = " + post.getId())
                );
                List<String> tagNames = new ArrayList<>();
                for (Tag tag : tags) {
                    tagNames.add(tag.getName());
                }
                postInfo.put("tags", tagNames);

                postList.add(postInfo);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("postList", postList);
            result.put("total", total);
            result.put("page", page);
            result.put("size", size);
            result.put("category", category);

            // 存入缓存，设置过期时间为10分钟
            redisService.stringSetString(cacheKey, JSON.toJSONString(result), 600L);

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
            Map<String, Object> postDetail = new HashMap<>();
            postDetail.put("id", post.getId());
            postDetail.put("userId", post.getUserId());
            postDetail.put("title", post.getTitle());
            postDetail.put("content", post.getContent());
            postDetail.put("category", post.getCategory());
            postDetail.put("likeCount", post.getLikeCount());
            postDetail.put("commentCount", post.getCommentCount());
            postDetail.put("viewCount", post.getViewCount());
            postDetail.put("createdAt", post.getCreatedAt());
            postDetail.put("updatedAt", post.getUpdatedAt());

            // 获取帖子标签
            List<Tag> tags = tagMapper.selectList(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Tag>()
                            .inSql(Tag::getId, "SELECT tag_id FROM post_tag WHERE post_id = " + postId)
            );
            List<String> tagNames = new ArrayList<>();
            for (Tag tag : tags) {
                tagNames.add(tag.getName());
            }
            postDetail.put("tags", tagNames);

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
            Post post = new Post();
            post.setUserId(userId);
            post.setTitle(request.getTitle());
            post.setContent(request.getContent());
            post.setCategory(request.getCategory());
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
            Map<String, Object> result = new HashMap<>();
            result.put("id", post.getId());
            result.put("userId", post.getUserId());
            result.put("title", post.getTitle());
            result.put("content", post.getContent());
            result.put("category", post.getCategory());
            result.put("tags", tags);
            result.put("likeCount", post.getLikeCount());
            result.put("commentCount", post.getCommentCount());
            result.put("viewCount", post.getViewCount());
            result.put("createdAt", post.getCreatedAt());
            result.put("updatedAt", post.getUpdatedAt());

            // 清除帖子列表缓存
            redisService.delByKeyPrefix("post:list:");

            log.info("创建帖子成功: userId={}, title={}", userId, request.getTitle());
            return Result.success(result);

        } catch (Exception e) {
            log.error("创建帖子失败", e);
            return Result.error("创建帖子失败: " + e.getMessage());
        }
    }

    @Override
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
            Map<String, Object> result = new HashMap<>();
            result.put("id", post.getId());
            result.put("userId", post.getUserId());
            result.put("title", post.getTitle());
            result.put("content", post.getContent());
            result.put("category", post.getCategory());
            result.put("tags", tags);
            result.put("updatedAt", post.getUpdatedAt());

            // 清除帖子详情缓存
            redisService.delete("post:detail:" + postId);
            // 清除帖子列表缓存
            redisService.delByKeyPrefix("post:list:");

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
            redisService.delete("post:detail:" + postId);
            // 清除帖子列表缓存
            redisService.delByKeyPrefix("post:list:");

            log.info("删除帖子成功: postId={}, userId={}", postId, userId);
            return Result.success("删除帖子成功");

        } catch (Exception e) {
            log.error("删除帖子失败", e);
            return Result.error("删除帖子失败: " + e.getMessage());
        }
    }
}