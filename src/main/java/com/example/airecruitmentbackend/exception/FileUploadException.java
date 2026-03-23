package com.example.airecruitmentbackend.exception;

import lombok.Getter;

/**
 * 文件上传异常
 * 用于处理文件上传相关的业务异常
 */
@Getter
public class FileUploadException extends RuntimeException {
    /**
     * 错误状态码
     */
    private final Integer code;

    /**
     * 错误消息
     */
    private final String message;

    public FileUploadException(String message) {
        super(message);
        this.code = 400;
        this.message = message;
    }

    public FileUploadException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }
}
