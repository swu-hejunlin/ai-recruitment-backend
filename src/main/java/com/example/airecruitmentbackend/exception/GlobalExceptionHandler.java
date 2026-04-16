package com.example.airecruitmentbackend.exception;

import com.example.airecruitmentbackend.common.Result;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 * 统一处理应用中的各种异常，返回标准的JSON格式
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理未授权异常（401）
     * 包括：用户未登录、Token无效、Token过期等
     */
    @ExceptionHandler(UnauthorizedException.class)
    public Result<?> handleUnauthorizedException(UnauthorizedException e, HttpServletResponse response) {
        log.warn("未授权：{}", e.getMessage());
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        return Result.error(401, e.getMessage());
    }

    /**
     * 处理禁止访问异常（403）
     * 包括：权限不足、无权操作他人资源等
     */
    @ExceptionHandler(ForbiddenException.class)
    public Result<?> handleForbiddenException(ForbiddenException e, HttpServletResponse response) {
        log.warn("禁止访问：{}", e.getMessage());
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        return Result.error(403, e.getMessage());
    }

    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException(BusinessException e) {
        log.error("业务异常：{}", e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * 处理参数校验异常（@Valid注解触发）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> handleValidationException(MethodArgumentNotValidException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String message = fieldError != null ? fieldError.getDefaultMessage() : "参数校验失败";
        log.error("参数校验异常：{}", message);
        return Result.error(400, message);
    }

    /**
     * 处理参数绑定异常
     */
    @ExceptionHandler(BindException.class)
    public Result<?> handleBindException(BindException e) {
        FieldError fieldError = e.getFieldError();
        String message = fieldError != null ? fieldError.getDefaultMessage() : "参数绑定失败";
        log.error("参数绑定异常：{}", message);
        return Result.error(400, message);
    }

    /**
     * 处理文件上传异常
     */
    @ExceptionHandler(FileUploadException.class)
    public Result<?> handleFileUploadException(FileUploadException e) {
        log.error("文件上传异常：{}", e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(JwtException.class)
    public Result<?> handleJwtException(JwtException e, HttpServletResponse response) {
        log.error("身份验证失败：{}", e.getMessage());
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        return Result.error(401, "登录已过期，请重新登录");
    }

    /**
     * 处理其他未知异常
     */
    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        log.error("系统异常：", e);
        return Result.error("系统繁忙，请稍后重试");
    }
}
