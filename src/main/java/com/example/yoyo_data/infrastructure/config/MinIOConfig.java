package com.example.yoyo_data.infrastructure.config;

import com.example.yoyo_data.infrastructure.config.properties.MinIOProperties;
import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MinIO配置 - 初始化MinIO客户端
 *
 * @author yoyo_data
 * @version 1.0
 */
@Slf4j
@Configuration
public class MinIOConfig {

    @Autowired
    private MinIOProperties minIOProperties;

    /**
     * 创建MinIO客户端实例
     *
     * @return MinioClient
     */
    @Bean
    public MinioClient minioClient() {
        try {
            // 使用MinIO客户端7.x版本的构造方式
            MinioClient minioClient = new MinioClient(
                    minIOProperties.getEndpoint(),
                    minIOProperties.getAccessKey(),
                    minIOProperties.getSecretKey()
            );
            
            // 检查默认bucket是否存在，不存在则创建
            boolean found = minioClient.bucketExists(minIOProperties.getBucketName());
            if (!found) {
                minioClient.makeBucket(minIOProperties.getBucketName());
                log.info("创建MinIO默认Bucket: {}", minIOProperties.getBucketName());
            }
            
            log.info("MinIO客户端初始化成功");
            return minioClient;
        } catch (Exception e) {
            log.error("MinIO客户端初始化失败", e);
            throw new RuntimeException("MinIO客户端初始化失败", e);
        }
    }
}
