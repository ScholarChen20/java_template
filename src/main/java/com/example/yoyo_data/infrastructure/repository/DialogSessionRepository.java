package com.example.yoyo_data.infrastructure.repository;

import com.example.yoyo_data.domain.entity.DialogSession;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 对话会话Repository
 */
@Repository
public interface DialogSessionRepository extends MongoRepository<DialogSession, String> {

    /**
     * 根据用户ID查找对话列表
     */
    @Query("{ 'user_id': ?0 }")
    List<DialogSession> findByUserId(Long userId);

    /**
     * 根据用户ID和对方ID查找对话
     */
    @Query("{ 'user_id': ?0, 'recipient_id': ?1 }")
    Optional<DialogSession> findByUserIdAndRecipientId(Long userId, Long recipientId);

    /**
     * 根据对话ID和用户ID查找对话
     */
    @Query("{ '_id': ?0, 'user_id': ?1 }")
    Optional<DialogSession> findByIdAndUserId(String id, Long userId);

    /**
     * 根据对话类型查找对话
     */
    @Query("{ 'user_id': ?0, 'type': ?1 }")
    List<DialogSession> findByUserIdAndType(Long userId, String type);

    /**
     * 根据对话状态查找对话
     */
    @Query("{ 'user_id': ?0, 'status': ?1 }")
    List<DialogSession> findByUserIdAndStatus(Long userId, String status);
}
