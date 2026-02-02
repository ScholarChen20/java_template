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
            MinioClient minioClient = MinioClient.builder()
                    .endpoint(minIOProperties.getEndpoint())
                    .credentials(minIOProperties.getAccessKey(), minIOProperties.getSecretKey())
                    .build();
            
            // 检查默认bucket是否存在，不存在则创建
            boolean found = minioClient.bucketExists(
                    io.minio.BucketExistsArgs.builder()
                            .bucket(minIOProperties.getBucketName())
                            .build()
            );
            if (!found) {
                minioClient.makeBucket(
                        io.minio.MakeBucketArgs.builder()
                                .bucket(minIOProperties.getBucketName())
                                .build()
                );
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
