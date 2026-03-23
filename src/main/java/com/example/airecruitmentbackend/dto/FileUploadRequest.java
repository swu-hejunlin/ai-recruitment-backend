package com.example.airecruitmentbackend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 文件上传请求DTO
 */
@Data
public class FileUploadRequest {

    /**
     * 文件类型
     */
    @NotBlank(message = "文件类型不能为空")
    private String fileType;

    /**
     * 文件类型说明：
     * - avatar: 头像
     * - resume: 简历
     * - logo: 企业logo
     * - license: 营业执照
     */
}
