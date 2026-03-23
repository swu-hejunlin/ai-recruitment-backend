package com.example.airecruitmentbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文件上传响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UploadResponse {
    /**
     * 文件访问URL
     */
    private String fileUrl;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;
}
