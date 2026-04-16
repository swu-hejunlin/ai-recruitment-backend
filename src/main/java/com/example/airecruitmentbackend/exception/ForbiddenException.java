package com.example.airecruitmentbackend.exception;

/**
 * 禁止访问异常
 * 用于表示用户已登录但权限不足、被拒绝访问等情况
 */
public class ForbiddenException extends RuntimeException {

    public ForbiddenException() {
        super("权限不足");
    }

    public ForbiddenException(String message) {
        super(message);
    }
}
