package com.example.yoyo_data.service.impl;

import com.example.yoyo_data.common.Result;
import com.example.yoyo_data.common.dto.response.FileUploadResponse;
import com.example.yoyo_data.infrastructure.config.properties.MinIOProperties;
import com.example.yoyo_data.service.FileUploadService;
import io.minio.MinioClient;
import io.minio.PutObjectOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
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
    public Result<FileUploadResponse> uploadFile(MultipartFile file, String bucketName, String objectName) {
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
                // 使用MinIO客户端7.x版本的API
                // 创建PutObjectOptions对象
                PutObjectOptions options = new PutObjectOptions(file.getSize(), -1);
                if (file.getContentType() != null) {
                    options.setContentType(file.getContentType());
                }
                
                minioClient.putObject(
                        bucketName,
                        objectName,
                        inputStream,
                        options
                );
            }

            // 5. 生成预签名URL
            // 使用MinIO客户端7.x版本的API，生成7天有效期的预签名URL
            String presignedUrl = minioClient.presignedGetObject(bucketName, objectName, 7 * 24 * 3600);

            // 6. 构建响应
            FileUploadResponse response = new FileUploadResponse();
            response.setBucketName(bucketName);
            response.setObjectName(objectName);
            response.setFileUrl(presignedUrl);
            response.setFileSize(file.getSize());
            response.setContentType(file.getContentType());

            log.info("文件上传成功: bucketName={}, objectName={}, fileSize={}", bucketName, objectName, file.getSize());
            return Result.success(response);

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
            // 使用MinIO客户端7.x版本的API
            minioClient.statObject(bucketName, objectName);

            // 3. 删除文件
            // 使用MinIO客户端7.x版本的API
            minioClient.removeObject(bucketName, objectName);

            log.info("文件删除成功: bucketName={}, objectName={}", bucketName, objectName);
            return Result.success("文件删除成功");

        } catch (Exception e) {
            log.error("文件删除失败", e);
            return Result.error("文件删除失败: " + e.getMessage());
        }
    }

    @Override
    public Result<FileUploadResponse> getFileUrl(String bucketName, String objectName, Integer expiry) {
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
            // 使用MinIO客户端7.x版本的API
            minioClient.statObject(bucketName, objectName);

            // 3. 生成预签名URL
            // 使用MinIO客户端7.x版本的API
            String presignedUrl = minioClient.presignedGetObject(bucketName, objectName, expiry);

            // 4. 构建响应
            FileUploadResponse response = new FileUploadResponse();
            response.setBucketName(bucketName);
            response.setObjectName(objectName);
            response.setFileUrl(presignedUrl);
            response.setExpiry(expiry);

            log.info("获取文件URL成功: bucketName={}, objectName={}, expiry={}", bucketName, objectName, expiry);
            return Result.success(response);

        } catch (Exception e) {
            log.error("获取文件URL失败", e);
            return Result.error("获取文件URL失败: " + e.getMessage());
        }
    }
}