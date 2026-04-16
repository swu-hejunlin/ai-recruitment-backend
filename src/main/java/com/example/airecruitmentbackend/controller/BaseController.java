package com.example.airecruitmentbackend.controller;

import com.example.airecruitmentbackend.entity.User;
import com.example.airecruitmentbackend.exception.BusinessException;
import com.example.airecruitmentbackend.exception.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Controller 基类
 * 提供通用的工具方法
 */
@Slf4j
public abstract class BaseController {

    /**
     * 从请求属性中获取当前登录用户ID
     * 注意：需要在 JwtInterceptor 之后执行
     */
    protected Long getCurrentUserId() {
        HttpServletRequest request = getRequest();
        Object userId = request.getAttribute("userId");
        if (userId == null) {
            log.warn("【BaseController】获取用户ID失败！请求路径：{}，线程：{}", 
                    request.getRequestURI(), Thread.currentThread().getName());
            throw new BusinessException(401, "请先登录");
        }
        return (Long) userId;
    }

    /**
     * 从请求属性中获取当前登录用户角色
     */
    protected Integer getCurrentUserRole() {
        HttpServletRequest request = getRequest();
        Object role = request.getAttribute("role");
        if (role == null) {
            throw new UnauthorizedException("用户未登录");
        }
        return (Integer) role;
    }

    /**
     * 从请求属性中获取当前登录用户信息
     */
    protected User getCurrentUser() {
        HttpServletRequest request = getRequest();
        Object user = request.getAttribute("user");
        if (user == null) {
            throw new UnauthorizedException("用户未登录");
        }
        return (User) user;
    }

    /**
     * 获取当前请求对象
     */
    protected HttpServletRequest getRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            throw new BusinessException("无法获取请求上下文");
        }
        return attributes.getRequest();
    }
}
