package com.example.airecruitmentbackend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 线程池配置
 */
@Configuration
public class ThreadPoolConfig {

    /**
     * 岗位画像线程池
     */
    @Bean(name = "jobProfileExecutor")
    public ExecutorService jobProfileExecutor() {
        return new ThreadPoolExecutor(
            5, // 核心线程数
            10, // 最大线程数
            60L, TimeUnit.SECONDS, // 空闲线程存活时间
            new LinkedBlockingQueue<>(100), // 队列容量
            new ThreadPoolExecutor.CallerRunsPolicy() // 拒绝策略
        );
    }

    /**
     * 通用异步任务线程池
     */
    @Bean(name = "asyncExecutor")
    public ExecutorService asyncExecutor() {
        return new ThreadPoolExecutor(
            3, // 核心线程数
            8, // 最大线程数
            60L, TimeUnit.SECONDS, // 空闲线程存活时间
            new LinkedBlockingQueue<>(50), // 队列容量
            new ThreadPoolExecutor.CallerRunsPolicy() // 拒绝策略
        );
    }
}