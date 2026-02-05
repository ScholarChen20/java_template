package com.example.yoyo_data.infrastructure.repository.mongodb;

import com.example.yoyo_data.common.document.HotNewsMain;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HotNewsRepository extends MongoRepository<HotNewsMain, String> {
    List<HotNewsMain> findByTypeOrderByCreatedAtDesc(String type);
    HotNewsMain findTopByTypeOrderByCreatedAtDesc(String type);
}