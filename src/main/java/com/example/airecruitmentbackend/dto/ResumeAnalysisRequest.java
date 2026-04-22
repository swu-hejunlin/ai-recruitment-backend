package com.example.airecruitmentbackend.dto;

import lombok.Data;

/**
 * 简历分析请求
 */
@Data
public class ResumeAnalysisRequest {
    /**
     * 简历文件的URL（存储在OSS上的地址）
     */
    private String resumeUrl;
    
    /**
     * 简历文件的Base64编码（如果直接上传文件）
     */
    private String resumeBase64;
    
    /**
     * 简历文件类型（pdf, doc, docx等）
     */
    private String fileType;
}