package com.example.airecruitmentbackend.config;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 阿里云OSS配置类
 * 配置OSS客户端和相关属性
 */
@Configuration
@ConfigurationProperties(prefix = "aliyun.oss")
@Data
public class OssConfig {

    /**
     * OSS服务的Endpoint
     * 不同地域的Endpoint不同，例如：oss-cn-hangzhou.aliyuncs.com
     */
    private String endpoint;

    /**
     * 阿里云账号AccessKey ID
     */
    private String accessKeyId;

    /**
     * 阿里云账号AccessKey Secret
     */
    private String accessKeySecret;

    /**
     * OSS存储空间名称
     */
    private String bucketName;

    /**
     * 创建OSS客户端Bean
     * OSSClient是线程安全的，可以单例使用
     *
     * @return OSS客户端实例
     */
    @Bean
    public OSS ossClient() {
        return new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
    }
}
