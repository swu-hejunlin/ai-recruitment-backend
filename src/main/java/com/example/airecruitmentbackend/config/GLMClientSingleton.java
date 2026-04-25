package com.example.airecruitmentbackend.config;

import ai.z.openapi.ZhipuAiClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * GLM AI客户端单例配置
 * 避免频繁创建和销毁Client导致TCP连接问题
 */
@Slf4j
@Component
public class GLMClientSingleton {

    private static volatile ZhipuAiClient instance;

    /**
     * 获取GLM AI客户端实例（单例）
     * @return ZhipuAiClient实例
     */
    public static ZhipuAiClient getInstance() {
        if (instance == null) {
            synchronized (GLMClientSingleton.class) {
                if (instance == null) {
                    String apiKey = System.getenv("ZAI_API_KEY");
                    if (apiKey == null || apiKey.isEmpty()) {
                        throw new RuntimeException("ZAI_API_KEY环境变量未设置");
                    }
                    instance = ZhipuAiClient.builder().ofZHIPU()
                            .apiKey(apiKey)
                            .build();
                    log.info("GLM AI客户端单例初始化成功");
                }
            }
        }
        return instance;
    }

    /**
     * 重新初始化客户端（当API Key变更时使用）
     * @param apiKey 新的API Key
     */
    public static void reinitialize(String apiKey) {
        synchronized (GLMClientSingleton.class) {
            if (apiKey == null || apiKey.isEmpty()) {
                throw new RuntimeException("API Key不能为空");
            }
            instance = ZhipuAiClient.builder().ofZHIPU()
                    .apiKey(apiKey)
                    .build();
            log.info("GLM AI客户端单例重新初始化成功");
        }
    }
}