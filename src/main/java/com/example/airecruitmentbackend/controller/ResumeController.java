package com.example.airecruitmentbackend.controller;

import com.example.airecruitmentbackend.common.Result;
import com.example.airecruitmentbackend.dto.ResumeAnalysisRequest;
import com.example.airecruitmentbackend.dto.ResumeAnalysisResponse;
import com.example.airecruitmentbackend.dto.ResumeSmartFillResponse;
import com.example.airecruitmentbackend.service.ResumeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 简历分析控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/resume")
@RequiredArgsConstructor
public class ResumeController extends BaseController {

    private final ResumeService resumeService;

    /**
     * 分析简历
     */
    @PostMapping("/analyze")
    public Result<ResumeAnalysisResponse> analyzeResume(@RequestBody ResumeAnalysisRequest request) {
        Long userId = getCurrentUserId();
        log.info("收到简历分析请求，userId：{}", userId);

        try {
            ResumeAnalysisResponse response = resumeService.analyzeResume(request);
            log.info("简历分析完成，userId：{}", userId);
            return Result.success("简历分析成功", response);
        } catch (Exception e) {
            log.error("简历分析失败，userId：{}", userId, e);
            return Result.error("简历分析失败：" + e.getMessage());
        }
    }

    /**
     * 上传并分析简历
     */
    @PostMapping("/upload-and-analyze")
    public Result<ResumeAnalysisResponse> uploadAndAnalyzeResume(@RequestParam("file") MultipartFile file) {
        Long userId = getCurrentUserId();
        log.info("收到上传并分析简历请求，userId：{}，fileName：{}", userId, file.getOriginalFilename());

        try {
            ResumeAnalysisResponse response = resumeService.uploadAndAnalyzeResume(file);
            log.info("上传并分析简历完成，userId：{}", userId);
            return Result.success("上传并分析简历成功", response);
        } catch (Exception e) {
            log.error("上传并分析简历失败，userId：{}", userId, e);
            return Result.error("上传并分析简历失败：" + e.getMessage());
        }
    }

    /**
     * 智能填充简历信息
     */
    @PostMapping("/smart-fill")
    public Result<ResumeSmartFillResponse> smartFillResume(@RequestParam("file") MultipartFile file) {
        Long userId = getCurrentUserId();
        log.info("收到智能填充简历请求，userId：{}，fileName：{}", userId, file.getOriginalFilename());

        try {
            ResumeSmartFillResponse response = resumeService.smartFillResume(file);
            log.info("智能填充简历完成，userId：{}", userId);
            return Result.success("智能填充简历成功", response);
        } catch (Exception e) {
            log.error("智能填充简历失败，userId：{}", userId, e);
            return Result.error("智能填充简历失败：" + e.getMessage());
        }
    }
}