package com.example.yoyo_data.infrastructure.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * 异步任务配置 - 配置线程池用于异步任务处理
 * 使用@Async注解实现异步方法调用
 *
 * @author Template Framework
 * @version 1.0
 */
@Slf4j
@Configuration
@EnableAsync
public class AsyncTaskConfig {

    /**
     * 通用异步任务执行器
     *
     * @return ThreadPoolTaskExecutor
     */
    @Bean(name = "asyncExecutor")
    public Executor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 核心线程数
        executor.setCorePoolSize(10);

        // 最大线程数
        executor.setMaxPoolSize(50);

        // 队列容量
        executor.setQueueCapacity(1000);

        // 线程名前缀
        executor.setThreadNamePrefix("async-task-");

        // 拒绝策略：等待队列可用
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());

        // 线程池关闭时的等待时间
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);

        executor.initialize();

        log.info("异步任务线程池初始化成功: corePoolSize=10, maxPoolSize=50, queueCapacity=1000");

        return executor;
    }

    /**
     * 长任务执行器（用于长时间运行的任务）
     *
     * @return ThreadPoolTaskExecutor
     */
    @Bean(name = "longTaskExecutor")
    public Executor longTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 核心线程数（较小）
        executor.setCorePoolSize(5);

        // 最大线程数（较小）
        executor.setMaxPoolSize(20);

        // 队列容量（较小）
        executor.setQueueCapacity(200);

        // 线程名前缀
        executor.setThreadNamePrefix("long-task-");

        // 拒绝策略
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.AbortPolicy());

        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(120);

        executor.initialize();

        log.info("长任务线程池初始化成功: corePoolSize=5, maxPoolSize=20, queueCapacity=200");

        return executor;
    }

    /**
     * IO密集型任务执行器
     *
     * @return ThreadPoolTaskExecutor
     */
    @Bean(name = "ioTaskExecutor")
    public Executor ioTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // CPU核心数 * 2（适合IO密集型）
        int cpuCores = Runtime.getRuntime().availableProcessors();
        int corePoolSize = cpuCores * 2;
        int maxPoolSize = cpuCores * 4;

        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(2000);
        executor.setThreadNamePrefix("io-task-");
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);

        executor.initialize();

        log.info("IO密集型任务线程池初始化成功: corePoolSize={}, maxPoolSize={}, queueCapacity=2000",
                corePoolSize, maxPoolSize);

        return executor;
    }

    /**
     * CPU密集型任务执行器
     *
     * @return ThreadPoolTaskExecutor
     */
    @Bean(name = "cpuTaskExecutor")
    public Executor cpuTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // CPU核心数 + 1（适合CPU密集型）
        int cpuCores = Runtime.getRuntime().availableProcessors();
        int poolSize = cpuCores + 1;

        executor.setCorePoolSize(poolSize);
        executor.setMaxPoolSize(poolSize);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("cpu-task-");
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.AbortPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);

        executor.initialize();

        log.info("CPU密集型任务线程池初始化成功: corePoolSize={}, maxPoolSize={}, queueCapacity=500",
                poolSize, poolSize);

        return executor;
    }
}
