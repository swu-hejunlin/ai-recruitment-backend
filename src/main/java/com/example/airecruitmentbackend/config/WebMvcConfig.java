package com.example.airecruitmentbackend.config;

import com.example.airecruitmentbackend.interceptor.JwtInterceptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置类
 * 配置拦截器、CORS等
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {
    
    /**
     * CORS允许的来源域名列表
     * 从配置文件读取，支持多个域名（逗号分隔）
     */
    @Value("${cors.allowed-origins:http://localhost:5173,http://localhost:5174,http://127.0.0.1:5173}")
    private String allowedOrigins;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        log.info("【WebMvcConfig】使用JwtAuthenticationFilter进行认证，不再使用Interceptor");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 解析允许的域名列表
        String[] origins = allowedOrigins.split(",");
        
        registry.addMapping("/**")
                .allowedOriginPatterns(origins)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
