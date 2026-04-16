package com.example.airecruitmentbackend.exception;

import lombok.Getter;

/**
 * 自定义业务异常
 * 用于抛出业务逻辑错误，由全局异常处理器统一处理
 */
@Getter
public class BusinessException extends RuntimeException {
    /**
     * 错误状态码
     */
    private final Integer code;

    /**
     * 错误消息
     */
    private final String message;

    public BusinessException(String message) {
        super(message);
        this.code = 400;
        this.message = message;
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }
}
