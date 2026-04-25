package com.example.airecruitmentbackend.filter;

import com.example.airecruitmentbackend.entity.User;
import com.example.airecruitmentbackend.mapper.UserMapper;
import com.example.airecruitmentbackend.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * JWT认证Filter
 * 基于Filter实现，替代HandlerInterceptor
 * 注意：Filter顺序必须在RequestTraceFilter之前，但在Spring的RequestContextFilter之后
 */
@Slf4j
@Component
@Order(Ordered.LOWEST_PRECEDENCE / 2)  // 在RequestTraceFilter之前，但在Spring Filter之后
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements Filter {

    private final JwtUtil jwtUtil;
    private final UserMapper userMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 白名单：不需要认证的接口
     */
    private static final List<String> WHITE_LIST = Arrays.asList(
            "/api/user/login",
            "/api/user/register",
            "/api/user/send-code",
            "/api/user/switch-role",
            "/api/position/list",
            "/api/position/detail",
            "/api/position/latest",
            "/api/position/hot",
            "/api/position/company-info",
            "/api/position/detail-with-company"
    );

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        
        // 简单设置 CORS 响应头，确保 401/403 等错误能正常被前端捕获
        String origin = request.getHeader("Origin");
        if (origin != null) {
            response.setHeader("Access-Control-Allow-Origin", origin);
            response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            response.setHeader("Access-Control-Allow-Headers", "*");
            response.setHeader("Access-Control-Allow-Credentials", "true");
            response.setHeader("Access-Control-Max-Age", "3600");
        }

        String uri = request.getRequestURI();
        String method = request.getMethod();
        
        log.debug("【JwtFilter】收到请求：{} {}，线程：{}", method, uri, Thread.currentThread().getName());

        // 放行 OPTIONS 请求
        if ("OPTIONS".equalsIgnoreCase(method)) {
            log.debug("【JwtFilter】OPTIONS请求，直接放行");
            return;
        }

        // 检查白名单
        if (isWhiteList(uri)) {
            // 白名单接口：如果有token也解析
            String token = extractToken(request);
            if (token != null && jwtUtil.validateToken(token)) {
                try {
                    Long userId = jwtUtil.getUserIdFromToken(token);
                    request.setAttribute("userId", userId);
                    request.setAttribute("role", jwtUtil.getRoleFromToken(token));
                } catch (Exception e) {
                    // token解析失败，忽略
                }
            }
            log.debug("【JwtFilter】白名单接口，放行");
            chain.doFilter(request, response);
            return;
        }

        // 需要认证的接口
        String token = extractToken(request);
        if (token == null) {
            log.warn("【JwtFilter】Token为空，拒绝访问：{}", uri);
            sendUnauthorized(response, "请先登录");
            return;
        }

        if (!jwtUtil.validateToken(token)) {
            log.warn("【JwtFilter】Token无效，拒绝访问：{}", uri);
            sendUnauthorized(response, "登录已过期，请重新登录");
            return;
        }

        try {
            Long userId = jwtUtil.getUserIdFromToken(token);
            Integer role = jwtUtil.getRoleFromToken(token);

            // 验证用户是否存在
            User user = userMapper.selectById(userId);
            if (user == null) {
                log.warn("【JwtFilter】用户不存在，userId：{}", userId);
                sendUnauthorized(response, "用户不存在");
                return;
            }

            // 将用户信息设置到请求属性
            request.setAttribute("userId", userId);
            request.setAttribute("role", role);
            request.setAttribute("user", user);
            
            //log.info("【JwtFilter】Token验证成功，用户ID：{}，角色：{}", userId, role);

            chain.doFilter(request, response);

        } catch (Exception e) {
            log.error("【JwtFilter】处理请求时发生异常", e);
            sendUnauthorized(response, "Token无效");
        }
    }

    private boolean isWhiteList(String uri) {
        for (String pattern : WHITE_LIST) {
            if (uri.equals(pattern) || uri.matches(pattern.replace("*", ".*"))) {
                return true;
            }
        }
        return false;
    }

    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    private void sendUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":401,\"message\":\"" + message + "\"}");
    }
}
