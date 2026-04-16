package com.example.airecruitmentbackend.controller;

import com.example.airecruitmentbackend.common.Result;
import com.example.airecruitmentbackend.dto.InterviewRequest;
import com.example.airecruitmentbackend.dto.InterviewDetailDTO;
import com.example.airecruitmentbackend.service.InterviewService;
import com.example.airecruitmentbackend.service.CompanyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

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
}
