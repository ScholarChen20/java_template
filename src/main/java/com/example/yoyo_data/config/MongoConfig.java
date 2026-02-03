package com.example.yoyo_data.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

/**
 * MongoDB配置类
 */
@Configuration
@EnableMongoRepositories(basePackages = "com.example.yoyo_data.repository")
public class MongoConfig {
}
