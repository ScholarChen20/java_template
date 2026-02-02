package com.example.yoyo_data.controller;

import com.example.yoyo_data.common.Result;
import com.example.yoyo_data.service.FileUploadService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 文件上传控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/files")
@Api(tags = "文件上传", description = "文件上传、删除、获取URL等操作")
public class FileUploadController {

    @Autowired
    private FileUploadService fileUploadService;

    /**
     * 上传文件
     *
     * @param file 上传的文件
     * @param bucketName 存储桶名称，可选
     * @param objectName 对象名称，可选
     * @return 上传结果，包含文件URL等信息
     */
    @PostMapping("/upload")
    @ApiOperation(value = "上传文件", notes = "上传文件到MinIO对象存储服务")
    public Result<Map<String, Object>> uploadFile(
            @ApiParam(value = "上传的文件", required = true) @RequestParam("file") MultipartFile file,
            @ApiParam(value = "存储桶名称，可选") @RequestParam(value = "bucketName", required = false) String bucketName,
            @ApiParam(value = "对象名称，可选") @RequestParam(value = "objectName", required = false) String objectName
    ) {
        log.info("上传文件: fileName={}, bucketName={}, objectName={}", file.getOriginalFilename(), bucketName, objectName);
        return fileUploadService.uploadFile(file, bucketName, objectName);
    }

    /**
     * 删除文件
     *
     * @param bucketName 存储桶名称
     * @param objectName 对象名称
     * @return 删除结果
     */
    @DeleteMapping("/delete")
    @ApiOperation(value = "删除文件", notes = "从MinIO对象存储服务中删除文件")
    public Result<?> deleteFile(
            @ApiParam(value = "存储桶名称", required = true) @RequestParam("bucketName") String bucketName,
            @ApiParam(value = "对象名称", required = true) @RequestParam("objectName") String objectName
    ) {
        log.info("删除文件: bucketName={}, objectName={}", bucketName, objectName);
        return fileUploadService.deleteFile(bucketName, objectName);
    }

    /**
     * 获取文件URL
     *
     * @param bucketName 存储桶名称
     * @param objectName 对象名称
     * @param expiry 过期时间（秒）
     * @return 文件URL
     */
    @GetMapping("/url")
    @ApiOperation(value = "获取文件URL", notes = "获取MinIO对象存储服务中文件的预签名URL")
    public Result<Map<String, Object>> getFileUrl(
            @ApiParam(value = "存储桶名称", required = true) @RequestParam("bucketName") String bucketName,
            @ApiParam(value = "对象名称", required = true) @RequestParam("objectName") String objectName,
            @ApiParam(value = "过期时间（秒），默认3600秒", required = false, defaultValue = "3600") @RequestParam(value = "expiry", required = false, defaultValue = "3600") Integer expiry
    ) {
        log.info("获取文件URL: bucketName={}, objectName={}, expiry={}", bucketName, objectName, expiry);
        return fileUploadService.getFileUrl(bucketName, objectName, expiry);
    }
}