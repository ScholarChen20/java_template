package com.example.yoyo_data.infrastructure.repository;

import com.example.yoyo_data.domain.entity.TravelPlan;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 旅行计划Repository
 */
@Repository
public interface TravelPlanRepository extends MongoRepository<TravelPlan, String> {

    /**
     * 根据用户ID查找旅行计划列表
     */
    @Query("{ 'user_id': ?0 }")
    List<TravelPlan> findByUserId(Long userId);

    /**
     * 根据计划ID和用户ID查找旅行计划
     */
    @Query("{ '_id': ?0, 'user_id': ?1 }")
    Optional<TravelPlan> findByIdAndUserId(String id, Long userId);

    /**
     * 根据目的地查找旅行计划
     */
    @Query("{ 'user_id': ?0, 'destination': ?1 }")
    List<TravelPlan> findByUserIdAndDestination(Long userId, String destination);

    /**
     * 根据状态查找旅行计划
     */
    @Query("{ 'user_id': ?0, 'status': ?1 }")
    List<TravelPlan> findByUserIdAndStatus(Long userId, String status);
}
