package com.example.airecruitmentbackend.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 请求追踪Filter
 * 记录所有进入应用的HTTP请求
 * Order设置为最低优先级，确保在其他Filter之后执行
 */
@Slf4j
@Component
@Order(Ordered.LOWEST_PRECEDENCE - 100)  // 很低优先级，但在FilterChain最后之前
public class RequestTraceFilter implements Filter {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        String uri = request.getRequestURI();
        String method = request.getMethod();
        
        //log.info("【RequestTraceFilter-开始】{} {}，线程：{}", method, uri, Thread.currentThread().getName());
        
        try {
            chain.doFilter(request, servletResponse);
        } finally {
            //log.info("【RequestTraceFilter-结束】{} {}，线程：{}", method, uri, Thread.currentThread().getName());
        }
    }
}
