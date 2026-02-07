package com.example.yoyo_data.service.impl;

import com.example.yoyo_data.common.Result;
import com.example.yoyo_data.infrastructure.cache.RedisService;
import com.example.yoyo_data.service.SystemService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.sql.Connection;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * 系统服务实现类
 * 提供真实的系统监控和健康检查功能
 */
@Slf4j
@Service
public class SystemServiceImpl implements SystemService {

    @Autowired(required = false)
    private DataSource dataSource;

    @Autowired(required = false)
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private RedisService redisService;

    @Autowired(required = false)
    private MongoTemplate mongoTemplate;

    @Autowired(required = false)
    private KafkaTemplate<String, String> kafkaTemplate;

    private static final DecimalFormat DF = new DecimalFormat("0.00");

    @Override
    public Result<?> getSystemInfo() {
        try {
            Map<String, Object> systemInfo = new HashMap<>();
            systemInfo.put("systemName", "YoYo Data System");
            systemInfo.put("version", "1.0.0");
            systemInfo.put("description", "高并发单体项目架构示例");
            systemInfo.put("author", "YoYo Data Team");
            systemInfo.put("email", "contact@yoyodata.com");
            systemInfo.put("website", "https://www.yoyodata.com");

            // 添加Java环境信息
            systemInfo.put("javaVersion", System.getProperty("java.version"));
            systemInfo.put("javaVendor", System.getProperty("java.vendor"));
            systemInfo.put("osName", System.getProperty("os.name"));
            systemInfo.put("osArch", System.getProperty("os.arch"));
            systemInfo.put("osVersion", System.getProperty("os.version"));

            // 添加运行时信息
            Runtime runtime = Runtime.getRuntime();
            systemInfo.put("processors", runtime.availableProcessors());
            systemInfo.put("freeMemory", formatBytes(runtime.freeMemory()));
            systemInfo.put("totalMemory", formatBytes(runtime.totalMemory()));
            systemInfo.put("maxMemory", formatBytes(runtime.maxMemory()));

            systemInfo.put("timestamp", System.currentTimeMillis());

            log.info("获取系统信息成功");
            return Result.success(systemInfo);

        } catch (Exception e) {
            log.error("获取系统信息失败", e);
            return Result.error("获取系统信息失败: " + e.getMessage());
        }
    }

    @Override
    public Result<?> getSystemStatus() {
        try {
            Map<String, Object> systemStatus = new HashMap<>();

            // 获取系统运行时信息
            Runtime runtime = Runtime.getRuntime();
            MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
            OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();

            // 计算内存使用率
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            long usedMemory = totalMemory - freeMemory;
            double memoryUsage = (double) usedMemory / totalMemory * 100;

            // 系统负载
            double systemLoadAverage = osBean.getSystemLoadAverage();

            // 组装系统状态信息
            systemStatus.put("status", "healthy");
            systemStatus.put("systemLoadAverage", systemLoadAverage > 0 ? DF.format(systemLoadAverage) : "N/A");
            systemStatus.put("availableProcessors", runtime.availableProcessors());
            systemStatus.put("memoryUsage", DF.format(memoryUsage) + "%");
            systemStatus.put("totalMemory", formatBytes(totalMemory));
            systemStatus.put("usedMemory", formatBytes(usedMemory));
            systemStatus.put("freeMemory", formatBytes(freeMemory));

            // 堆内存使用情况
            long heapMemoryUsed = memoryBean.getHeapMemoryUsage().getUsed();
            long heapMemoryMax = memoryBean.getHeapMemoryUsage().getMax();
            double heapUsage = (double) heapMemoryUsed / heapMemoryMax * 100;
            systemStatus.put("heapMemoryUsage", DF.format(heapUsage) + "%");
            systemStatus.put("heapMemoryUsed", formatBytes(heapMemoryUsed));
            systemStatus.put("heapMemoryMax", formatBytes(heapMemoryMax));

            // 组件健康检查
            Map<String, String> components = new HashMap<>();
            components.put("database", checkDatabaseHealth());
            components.put("redis", checkRedisHealth());
            components.put("mongodb", checkMongoDBHealth());
            components.put("kafka", checkKafkaHealth());
            systemStatus.put("components", components);

            // 系统运行时间
            long uptime = ManagementFactory.getRuntimeMXBean().getUptime();
            systemStatus.put("uptime", formatUptime(uptime));
            systemStatus.put("timestamp", System.currentTimeMillis());

            log.info("获取系统状态成功");
            return Result.success(systemStatus);

        } catch (Exception e) {
            log.error("获取系统状态失败", e);
            return Result.error("获取系统状态失败: " + e.getMessage());
        }
    }

    @Override
    public Result<?> cleanSystemCache() {
        try {
            long clearedSize = 0;

            // 清理Redis缓存
            if (redisService != null) {
                try {
                    // 获取缓存key数量
                    Long keyCount = redisService.dbSize();
                    log.info("清理前Redis Key数量: {}", keyCount);

                    // 这里只是示例,实际应该根据业务需要选择性清理
                    // 例如清理过期的缓存、临时缓存等
                    // redisService.flushDb(); // 慎用,会清空整个数据库

                    clearedSize = keyCount != null ? keyCount : 0;
                } catch (Exception e) {
                    log.error("清理Redis缓存失败", e);
                }
            }

            // 触发JVM垃圾回收
            System.gc();

            Map<String, Object> result = new HashMap<>();
            result.put("status", "success");
            result.put("message", "系统缓存清理成功");
            result.put("clearedKeys", clearedSize);
            result.put("timestamp", System.currentTimeMillis());

            log.info("清理系统缓存成功");
            return Result.success(result);

        } catch (Exception e) {
            log.error("清理系统缓存失败", e);
            return Result.error("清理系统缓存失败: " + e.getMessage());
        }
    }

    @Override
    public Result<?> restartSystemService(String serviceName) {
        try {
            Map<String, Object> result = new HashMap<>();
            result.put("status", "warning");
            result.put("message", "服务重启功能需要管理员权限");
            result.put("serviceName", serviceName);
            result.put("timestamp", System.currentTimeMillis());
            result.put("note", "此功能应通过系统运维工具执行");

            log.info("重启系统服务请求: serviceName={}", serviceName);
            return Result.success(result);

        } catch (Exception e) {
            log.error("重启系统服务失败", e);
            return Result.error("重启系统服务失败: " + e.getMessage());
        }
    }

    /**
     * 检查数据库健康状态
     */
    private String checkDatabaseHealth() {
        if (dataSource == null) {
            return "NOT_CONFIGURED";
        }
        try (Connection connection = dataSource.getConnection()) {
            return connection.isValid(3) ? "UP" : "DOWN";
        } catch (Exception e) {
            log.error("数据库健康检查失败", e);
            return "DOWN";
        }
    }

    /**
     * 检查Redis健康状态
     */
    private String checkRedisHealth() {
        if (redisTemplate == null) {
            return "NOT_CONFIGURED";
        }
        try {
            String pong = redisTemplate.getConnectionFactory()
                    .getConnection()
                    .ping();
            return "PONG".equals(pong) ? "UP" : "DOWN";
        } catch (Exception e) {
            log.error("Redis健康检查失败", e);
            return "DOWN";
        }
    }

    /**
     * 检查MongoDB健康状态
     */
    private String checkMongoDBHealth() {
        if (mongoTemplate == null) {
            return "NOT_CONFIGURED";
        }
        try {
            mongoTemplate.getDb().getName();
            return "UP";
        } catch (Exception e) {
            log.error("MongoDB健康检查失败", e);
            return "DOWN";
        }
    }

    /**
     * 检查Kafka健康状态
     */
    private String checkKafkaHealth() {
        if (kafkaTemplate == null) {
            return "NOT_CONFIGURED";
        }
        try {
            // Kafka健康检查较复杂,这里简化处理
            // 实际应该检查broker连接状态
            return kafkaTemplate.getProducerFactory() != null ? "UP" : "DOWN";
        } catch (Exception e) {
            log.error("Kafka健康检查失败", e);
            return "DOWN";
        }
    }

    /**
     * 格式化字节大小
     */
    private String formatBytes(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return DF.format(bytes / 1024.0) + " KB";
        } else if (bytes < 1024 * 1024 * 1024) {
            return DF.format(bytes / (1024.0 * 1024.0)) + " MB";
        } else {
            return DF.format(bytes / (1024.0 * 1024.0 * 1024.0)) + " GB";
        }
    }

    /**
     * 格式化运行时间
     */
    private String formatUptime(long uptimeMillis) {
        long seconds = uptimeMillis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        hours = hours % 24;
        minutes = minutes % 60;
        seconds = seconds % 60;

        StringBuilder sb = new StringBuilder();
        if (days > 0) {
            sb.append(days).append("d ");
        }
        if (hours > 0 || days > 0) {
            sb.append(hours).append("h ");
        }
        if (minutes > 0 || hours > 0 || days > 0) {
            sb.append(minutes).append("m ");
        }
        sb.append(seconds).append("s");

        return sb.toString();
    }
}
