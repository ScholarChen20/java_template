package com.example.yoyo_data;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableMongoRepositories(basePackages = "com.example.yoyo_data.infrastructure.repository.mongodb")
public class YoyoDataApplication {

    public static void main(String[] args) {
        SpringApplication.run(YoyoDataApplication.class, args);
    }

}
