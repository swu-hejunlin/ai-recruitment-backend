package com.example.airecruitmentbackend.controller;

import com.example.airecruitmentbackend.common.Result;
import com.example.airecruitmentbackend.dto.InterviewEvaluationDTO;
import com.example.airecruitmentbackend.dto.InterviewRequest;
import com.example.airecruitmentbackend.dto.InterviewDetailDTO;
import com.example.airecruitmentbackend.service.InterviewService;
import com.example.airecruitmentbackend.service.CompanyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 面试控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/interview")
@RequiredArgsConstructor
public class InterviewController extends BaseController {

    private final InterviewService interviewService;
    private final CompanyService companyService;

    /**
     * 创建面试邀请
     */
    @PostMapping("/create")
    public Result<Long> createInterview(@RequestBody InterviewRequest request) {
        Long bossId = getCurrentUserId();
        log.info("收到创建面试邀请请求，bossId：{}，applicationId：{}", bossId, request.getApplicationId());

        try {
            Long interviewId = interviewService.createInterview(request, bossId);
            log.info("创建面试邀请成功，interviewId：{}", interviewId);
            return Result.success("创建面试邀请成功", interviewId);
        } catch (Exception e) {
            log.error("创建面试邀请失败：{}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新面试状态
     */
    @PutMapping("/{id}/status")
    public Result<Void> updateInterviewStatus(@PathVariable Long id, @RequestParam Integer status) {
        Long userId = getCurrentUserId();
        log.info("收到更新面试状态请求，userId：{}，interviewId：{}，status：{}", userId, id, status);

        try {
            boolean result = interviewService.updateInterviewStatus(id, status);
            if (result) {
                log.info("更新面试状态成功，interviewId：{}，status：{}", id, status);
                return Result.success("更新面试状态成功", null);
            } else {
                return Result.error("更新面试状态失败");
            }
        } catch (Exception e) {
            log.error("更新面试状态失败：{}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取企业HR的面试列表
     */
    @GetMapping("/company/list")
    public Result<List<InterviewDetailDTO>> getCompanyInterviews() {
        Long bossId = getCurrentUserId();
        log.info("收到获取企业面试列表请求，bossId：{}", bossId);

        try {
            // 获取企业ID
            Long companyId = companyService.getByUserId(bossId).getId();
            List<InterviewDetailDTO> interviews = interviewService.getCompanyInterviews(companyId);
            log.info("获取企业面试列表成功，companyId：{}，数量：{}", companyId, interviews.size());
            return Result.success("获取面试列表成功", interviews);
        } catch (Exception e) {
            log.error("获取企业面试列表失败：{}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取求职者的面试列表
     */
    @GetMapping("/job-seeker/list")
    public Result<List<InterviewDetailDTO>> getJobSeekerInterviews() {
        Long jobSeekerId = getCurrentUserId();
        log.info("收到获取求职者面试列表请求，jobSeekerId：{}", jobSeekerId);

        try {
            List<InterviewDetailDTO> interviews = interviewService.getJobSeekerInterviews(jobSeekerId);
            log.info("获取求职者面试列表成功，jobSeekerId：{}，数量：{}", jobSeekerId, interviews.size());
            return Result.success("获取面试列表成功", interviews);
        } catch (Exception e) {
            log.error("获取求职者面试列表失败：{}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取面试详情
     */
    @GetMapping("/{id}")
    public Result<InterviewDetailDTO> getInterviewDetail(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        log.info("收到获取面试详情请求，userId：{}，interviewId：{}", userId, id);

        try {
            InterviewDetailDTO detail = interviewService.getInterviewDetail(id);
            log.info("获取面试详情成功，interviewId：{}", id);
            return Result.success("获取面试详情成功", detail);
        } catch (Exception e) {
            log.error("获取面试详情失败：{}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除面试
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteInterview(@PathVariable Long id) {
        Long bossId = getCurrentUserId();
        log.info("收到删除面试请求，bossId：{}，interviewId：{}", bossId, id);

        try {
            boolean result = interviewService.deleteInterview(id, bossId);
            if (result) {
                log.info("删除面试成功，interviewId：{}", id);
                return Result.success("删除面试成功", null);
            } else {
                return Result.error("删除面试失败");
            }
        } catch (Exception e) {
            log.error("删除面试失败：{}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 处理模拟面试
     */
    @PostMapping("/mock")
    public Result<Object> submitMockInterview(@RequestParam("video") MultipartFile video, 
                                             @RequestParam(value = "interviewId", required = false) Long interviewId,
                                             @RequestParam(value = "sessionKey", required = false) String sessionKey) {
        Long userId = getCurrentUserId();
        log.info("收到模拟面试视频，userId：{}，视频大小：{}，interviewId：{}，sessionKey：{}", userId, video.getSize(), interviewId, sessionKey);

        try {
            Object evaluationResult = interviewService.processMockInterview(video, userId, interviewId, sessionKey);
            log.info("模拟面试处理完成，userId：{}", userId);
            return Result.success("模拟面试评估完成", evaluationResult);
        } catch (Exception e) {
            log.error("处理模拟面试失败：{}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 生成模拟面试题
     */
    @GetMapping("/mock/questions")
    public Result<Object> generateMockInterviewQuestions(
            @RequestParam(value = "interviewId", required = false) Long interviewId,
            @RequestParam(value = "positionName", required = false) String positionName,
            @RequestParam(value = "positionCategory", required = false) String positionCategory,
            @RequestParam(value = "city", required = false) String city,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "requirement", required = false) String requirement) {
        Long userId = getCurrentUserId();
        log.info("生成模拟面试题，userId：{}，interviewId：{}，positionName：{}", userId, interviewId, positionName);

        try {
            Object questions = interviewService.generateMockInterviewQuestions(userId, interviewId, 
                    positionName, positionCategory, city, description, requirement);
            log.info("模拟面试题生成完成，userId：{}", userId);
            return Result.success("模拟面试题生成成功", questions);
        } catch (Exception e) {
            log.error("生成模拟面试题失败：{}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 结束真实AI面试（异步处理评估）
     */
    @PostMapping("/finish")
    public Result<Void> finishInterview(@RequestParam("video") MultipartFile video, @RequestParam("interviewId") Long interviewId) {
        Long userId = getCurrentUserId();
        log.info("收到结束面试请求，interviewId：{}，videoSize：{}", interviewId, video.getSize());

        try {
            // 异步处理，不等待评估完成
            interviewService.finishRealInterview(interviewId, video);
            log.info("结束面试请求已接收，interviewId：{}", interviewId);
            return Result.success("面试已结束", null);
        } catch (Exception e) {
            log.error("结束面试失败：{}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取面试评估结果（供BOSS端使用）
     */
    @GetMapping("/evaluation/{interviewId}")
    public Result<InterviewEvaluationDTO> getInterviewEvaluation(@PathVariable Long interviewId) {
        Long userId = getCurrentUserId();
        log.info("获取面试评估结果，userId：{}，interviewId：{}", userId, interviewId);

        try {
            InterviewEvaluationDTO evaluation = interviewService.getInterviewEvaluation(interviewId);
            if (evaluation == null) {
                return Result.error("评估结果不存在");
            }
            return Result.success("获取评估结果成功", evaluation);
        } catch (Exception e) {
            log.error("获取评估结果失败：{}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 检查面试评估结果是否存在
     */
    @GetMapping("/evaluation/{interviewId}/exists")
    public Result<Boolean> checkEvaluationExists(@PathVariable Long interviewId) {
        log.info("检查评估结果是否存在，interviewId：{}", interviewId);

        try {
            boolean exists = interviewService.hasEvaluation(interviewId);
            return Result.success("检查完成", exists);
        } catch (Exception e) {
            log.error("检查评估结果失败：{}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }
}
