package com.example.yoyo_data.service;

import com.example.yoyo_data.common.Result;
import com.example.yoyo_data.common.dto.response.FileUploadResponse;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传服务接口
 */
public interface FileUploadService {
    /**
     * 上传文件
     *
     * @param file 文件
     * @param bucketName 存储桶名称
     * @param objectName 对象名称
     * @return 上传结果，包含文件URL等信息
     */
    Result<FileUploadResponse> uploadFile(MultipartFile file, String bucketName, String objectName);

    /**
     * 删除文件
     *
     * @param bucketName 存储桶名称
     * @param objectName 对象名称
     * @return 删除结果
     */
    Result<?> deleteFile(String bucketName, String objectName);

    /**
     * 获取文件URL
     *
     * @param bucketName 存储桶名称
     * @param objectName 对象名称
     * @param expiry 过期时间（秒）
     * @return 文件URL
     */
    Result<FileUploadResponse> getFileUrl(String bucketName, String objectName, Integer expiry);
}