package com.example.yoyo_data.infrastructure.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Kafka配置属性 - 从application.yml读取Kafka配置
 *
 * @author Template Framework
 * @version 1.0
 */
@Data
@Component
@ConfigurationProperties(prefix = "spring.kafka")
public class KafkaProperties {

    /**
     * Kafka服务器地址（Bootstrap Servers）
     * 格式: host1:port1,host2:port2
     */
    private String bootstrapServers;

    /**
     * Producer配置
     */
    private Producer producer = new Producer();

    /**
     * Consumer配置
     */
    private Consumer consumer = new Consumer();

    /**
     * Producer配置
     */
    @Data
    public static class Producer {
        /**
         * 批处理大小
         */
        private int batchSize = 16384;

        /**
         * 缓冲大小
         */
        private long bufferMemory = 33554432;

        /**
         * 压缩算法
         */
        private String compressionType = "snappy";

        /**
         * 确认级别（all, 1, 0）
         */
        private String acks = "all";

        /**
         * 重试次数
         */
        private int retries = 3;

        /**
         * 发送超时时间
         */
        private int requestTimeoutMs = 30000;
    }

    /**
     * Consumer配置
     */
    @Data
    public static class Consumer {
        /**
         * 消费者分组ID
         */
        private String groupId;

        /**
         * 自动提交偏移量
         */
        private boolean enableAutoCommit = true;

        /**
         * 自动提交间隔
         */
        private long autoCommitIntervalMs = 1000;

        /**
         * 最大poll记录数
         */
        private int maxPollRecords = 500;

        /**
         * Session超时时间
         */
        private int sessionTimeoutMs = 10000;

        /**
         * 心跳间隔
         */
        private int heartbeatIntervalMs = 3000;
    }
}
