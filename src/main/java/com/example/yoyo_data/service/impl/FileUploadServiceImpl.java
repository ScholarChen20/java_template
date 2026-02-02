package com.example.yoyo_data.service.impl;

import com.example.yoyo_data.common.Result;
import com.example.yoyo_data.infrastructure.config.properties.MinIOProperties;
import com.example.yoyo_data.service.FileUploadService;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.StatObjectArgs;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 文件上传服务实现类
 */
@Slf4j
@Service
public class FileUploadServiceImpl implements FileUploadService {

    @Autowired
    private MinioClient minioClient;

    @Autowired
    private MinIOProperties minIOProperties;

    @Override
    public Result<Map<String, Object>> uploadFile(MultipartFile file, String bucketName, String objectName) {
        try {
            // 1. 检查参数
            if (file == null || file.isEmpty()) {
                return Result.error("文件不能为空");
            }

            // 2. 确定存储桶名称
            if (bucketName == null || bucketName.isEmpty()) {
                bucketName = minIOProperties.getBucketName();
            }

            // 3. 生成对象名称
            if (objectName == null || objectName.isEmpty()) {
                String originalFilename = file.getOriginalFilename();
                String suffix = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf(".")) : ".bin";
                objectName = UUID.randomUUID().toString() + suffix;
            }

            // 4. 上传文件
            try (InputStream inputStream = file.getInputStream()) {
                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(bucketName)
                                .object(objectName)
                                .stream(inputStream, file.getSize(), -1)
                                .contentType(file.getContentType())
                                .build()
                );
            }

            // 5. 生成文件URL
            String fileUrl = minIOProperties.getEndpoint() + "/" + bucketName + "/" + objectName;

            // 6. 构建响应
            Map<String, Object> result = new HashMap<>();
            result.put("bucketName", bucketName);
            result.put("objectName", objectName);
            result.put("fileUrl", fileUrl);
            result.put("fileSize", file.getSize());
            result.put("contentType", file.getContentType());

            log.info("文件上传成功: bucketName={}, objectName={}, fileSize={}", bucketName, objectName, file.getSize());
            return Result.success(result);

        } catch (Exception e) {
            log.error("文件上传失败", e);
            return Result.error("文件上传失败: " + e.getMessage());
        }
    }

    @Override
    public Result<?> deleteFile(String bucketName, String objectName) {
        try {
            // 1. 检查参数
            if (bucketName == null || bucketName.isEmpty()) {
                return Result.error("存储桶名称不能为空");
            }
            if (objectName == null || objectName.isEmpty()) {
                return Result.error("对象名称不能为空");
            }

            // 2. 检查文件是否存在
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );

            // 3. 删除文件
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );

            log.info("文件删除成功: bucketName={}, objectName={}", bucketName, objectName);
            return Result.success("文件删除成功");

        } catch (Exception e) {
            log.error("文件删除失败", e);
            return Result.error("文件删除失败: " + e.getMessage());
        }
    }

    @Override
    public Result<Map<String, Object>> getFileUrl(String bucketName, String objectName, Integer expiry) {
        try {
            // 1. 检查参数
            if (bucketName == null || bucketName.isEmpty()) {
                bucketName = minIOProperties.getBucketName();
            }
            if (objectName == null || objectName.isEmpty()) {
                return Result.error("对象名称不能为空");
            }
            if (expiry == null || expiry <= 0) {
                expiry = 3600; // 默认1小时
            }

            // 2. 检查文件是否存在
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );

            // 3. 生成预签名URL
            String presignedUrl = minioClient.getPresignedObjectUrl(
                    io.minio.GetPresignedObjectUrlArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .expiry(expiry)
                            .build()
            );

            // 4. 构建响应
            Map<String, Object> result = new HashMap<>();
            result.put("bucketName", bucketName);
            result.put("objectName", objectName);
            result.put("presignedUrl", presignedUrl);
            result.put("expiry", expiry);

            log.info("获取文件URL成功: bucketName={}, objectName={}, expiry={}", bucketName, objectName, expiry);
            return Result.success(result);

        } catch (Exception e) {
            log.error("获取文件URL失败", e);
            return Result.error("获取文件URL失败: " + e.getMessage());
        }
    }
}