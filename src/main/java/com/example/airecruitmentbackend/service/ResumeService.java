package com.example.airecruitmentbackend.service;

import com.example.airecruitmentbackend.dto.ResumeAnalysisRequest;
import com.example.airecruitmentbackend.dto.ResumeAnalysisResponse;
import com.example.airecruitmentbackend.dto.ResumeSmartFillResponse;
import org.springframework.web.multipart.MultipartFile;

/**
 * 简历分析服务接口
 */
public interface ResumeService {
    /**
     * 分析简历
     * @param request 简历分析请求
     * @return 简历分析响应
     */
    ResumeAnalysisResponse analyzeResume(ResumeAnalysisRequest request);
    
    /**
     * 上传并分析简历
     * @param file 简历文件
     * @return 简历分析响应
     */
    ResumeAnalysisResponse uploadAndAnalyzeResume(MultipartFile file);
    
    /**
     * 智能填充简历信息
     * @param file 简历文件
     * @return 智能填充响应
     */
    ResumeSmartFillResponse smartFillResume(MultipartFile file);
}