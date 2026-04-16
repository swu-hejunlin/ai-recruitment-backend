package com.example.airecruitmentbackend.interceptor;

import com.example.airecruitmentbackend.common.Result;
import com.example.airecruitmentbackend.entity.User;
import com.example.airecruitmentbackend.mapper.UserMapper;
import com.example.airecruitmentbackend.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * JWT 认证拦截器
 * 统一处理接口认证和角色校验
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    private final UserMapper userMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    public void init() {
        log.info("JwtInterceptor 初始化完成，白名单路径数：{}", WHITE_LIST.size());
    }

    /**
     * 白名单：不需要认证的接口
     * 注意：路径不带 /api 前缀
     * ⚠️ WebMvcConfig 也引用此列表，保持同步！
     */
    public static final List<String> WHITE_LIST = Arrays.asList(
            // 用户模块
            "/user/login",
            "/user/register",
            "/user/send-code",
            // 职位模块（公开浏览）
            "/position/list",
            "/position/detail",
            "/position/latest",
            "/position/hot",
            "/position/company-info",
            "/position/detail-with-company",
            // 企业模块（公开浏览）
            "/company/{id}"  // 注意：PathVariable 需要特殊处理
    );

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();
        String method = request.getMethod();
        
        log.info("【JWT拦截器-preHandle】请求：{} {}，线程：{}", method, uri, Thread.currentThread().getName());
        log.info("【JWT拦截器-preHandle】handler类型：{}，handler类：{}", handler, handler != null ? handler.getClass().getName() : "null");
        
        // 放 OPTIONS 请求（跨域预检）
        if ("OPTIONS".equalsIgnoreCase(method)) {
            log.info("【JWT拦截器-preHandle】OPTIONS请求，直接放行");
            return true;
        }

        try {
            // 检查是否为白名单接口
            if (isWhiteList(uri)) {
                // 白名单接口：尝试解析token但不强制校验（可选）
                // 如果有token，解析并设置用户信息到请求属性
                String token = extractToken(request);
                if (token != null && jwtUtil.validateToken(token)) {
                    try {
                        Long userId = jwtUtil.getUserIdFromToken(token);
                        request.setAttribute("userId", userId);
                        request.setAttribute("role", jwtUtil.getRoleFromToken(token));
                    } catch (Exception e) {
                        // token解析失败，忽略（白名单接口不强制要求登录）
                    }
                }
                log.info("【JWT拦截器-preHandle】白名单接口，放行");
                return true;
            }

            // 需要认证的接口
            String token = extractToken(request);
            if (token == null) {
                log.warn("【JWT拦截器-preHandle】Token为空，拒绝访问");
                sendUnauthorized(response, "请先登录");
                return false;
            }

            if (!jwtUtil.validateToken(token)) {
                log.warn("【JWT拦截器-preHandle】Token无效，拒绝访问");
                sendUnauthorized(response, "登录已过期，请重新登录");
                return false;
            }

            Long userId = jwtUtil.getUserIdFromToken(token);
            Integer role = jwtUtil.getRoleFromToken(token);

            // 验证用户是否存在
            User user = userMapper.selectById(userId);
            if (user == null) {
                log.warn("【JWT拦截器-preHandle】用户不存在，userId：{}", userId);
                sendUnauthorized(response, "用户不存在");
                return false;
            }

            // 将用户信息设置到请求属性，供后续Controller使用
            request.setAttribute("userId", userId);
            request.setAttribute("role", role);
            request.setAttribute("user", user);
            
            log.info("【JWT拦截器-preHandle】Token验证成功，用户ID：{}，角色：{}", userId, role);

            return true;

        } catch (Exception e) {
            log.error("【JWT拦截器-preHandle】处理请求时发生异常", e);
            sendUnauthorized(response, "Token无效");
            return false;
        }
    }

    /**
     * 检查是否为白名单接口
     */
    private boolean isWhiteList(String uri) {
        // 移除API前缀进行匹配
        String path = uri.replaceFirst("^/api", "");

        for (String pattern : WHITE_LIST) {
            if (matchPath(pattern, path)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 简单的路径匹配（支持 /company/{id} 这种格式）
     */
    private boolean matchPath(String pattern, String path) {
        // 精确匹配
        if (pattern.equals(path)) {
            return true;
        }
        // 通配符匹配（只支持 {xxx} 形式的路径参数）
        if (pattern.contains("{")) {
            String regex = pattern.replaceAll("\\{[^}]+\\}", "\\\\d+");
            return path.matches(regex);
        }
        return false;
    }

    /**
     * 从请求头提取Token
     */
    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    /**
     * 发送未授权响应
     */
    private void sendUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        Result<?> result = Result.error(401, message);
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        log.info("【JWT拦截器】afterCompletion：{} {}，异常：{}", 
                request.getMethod(), request.getRequestURI(), ex != null ? ex.getMessage() : "无");
    }
}
