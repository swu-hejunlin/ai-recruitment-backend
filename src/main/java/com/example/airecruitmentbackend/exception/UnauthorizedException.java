package com.example.airecruitmentbackend.exception;

/**
 * 未授权异常
 * 用于表示用户未登录、Token无效、权限不足等认证/授权问题
 */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException() {
        super("未授权");
    }

    public UnauthorizedException(String message) {
        super(message);
    }

    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }
}
