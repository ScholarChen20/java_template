package com.example.yoyo_data.infrastructure.message.consumer;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.yoyo_data.common.constant.KafkaTopic;
import com.example.yoyo_data.common.entity.Comment;
import com.example.yoyo_data.common.entity.Post;
import com.example.yoyo_data.infrastructure.message.KafkaConsumerTemplate;
import com.example.yoyo_data.infrastructure.message.MessageEvent;
import com.example.yoyo_data.infrastructure.repository.CommentMapper;
import com.example.yoyo_data.infrastructure.repository.PostMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
public class CommentEventConsumer extends KafkaConsumerTemplate {
    @Autowired
    private CommentMapper commentMapper;
    @Autowired
    private PostMapper postMapper;

    /**
     * 监听评论删除事件
     */
    @KafkaListener(topics = KafkaTopic.COMMENT_DELETE, groupId = "comment-group")
    public void listenCommentDeleteEvent(ConsumerRecord<String, String> record) {
        String message = record.value();
        log.info("接收到对话归档事件: topic={}, partition={}, offset={}",
                record.topic(), record.partition(), record.offset());

        if (!validateMessage(message)) {
            return;
        }

        MessageEvent event = parseEvent(message);
        if (!validateEvent(event)) {
            return;
        }

        // 解析事件数据
        Map<String, Object> eventData = JSON.parseObject(event.getData(), Map.class);
        Long postId = ((Number) eventData.get("postId")).longValue();
        Long userId = ((Number) eventData.get("userId")).longValue();
        Long commentId = ((Number) eventData.get("commentId")).longValue();
        log.info("处理评论删除事件: postId={}, userId={}, commentId={}", postId, userId, commentId);

        // 处理业务逻辑,可能存在该评论是其他评论的父评论，一并删除;
        commentMapper.deleteById(commentId);
        log.info("删除了评论");

        List<Comment> comments = commentMapper.selectList(new LambdaQueryWrapper<Comment>()
                .eq(Comment::getPostId, postId).eq(Comment::getParentId, commentId));
        List<Long> commentIds = comments.stream().map(Comment::getId).collect(Collectors.toList());
        commentMapper.deleteBatchIds(commentIds);
        log.info("删除了{}个子评论", commentIds.size());

        event.markAsProcessed("处理完成");


    }
    @KafkaListener(topics = KafkaTopic.COMMENT_CREATE, groupId = "comment-group")
    public void listenCommentCreateEvent(ConsumerRecord<String, String> record) {
        String message = record.value();
        log.info("接收到对话归档事件: topic={}, partition={}, offset={}",
                record.topic(), record.partition(), record.offset());
        if (!validateMessage(message)) {
            return;
        }
        MessageEvent event = parseEvent(message);
        if (!validateEvent(event)) {
            return;
        }
        Map<String, Object> eventData = JSON.parseObject(event.getData(), Map.class);
        Comment comment = JSON.parseObject(eventData.get("commentObj").toString(), Comment.class);
        Long postId = ((Number) eventData.get("postId")).longValue();
        Long userId = ((Number) eventData.get("userId")).longValue();

        commentMapper.insert(comment);

        // 更新帖子评论数
        Post post = postMapper.selectById(postId);
        post.setCommentCount(post.getCommentCount() + 1);
        postMapper.updateById(post);

        log.info("处理评论创建事件: postId={}, userId={}, commentId={}", postId, userId, comment.getId());

    }

}
