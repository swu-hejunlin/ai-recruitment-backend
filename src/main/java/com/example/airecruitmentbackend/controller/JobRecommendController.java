package com.example.airecruitmentbackend.controller;

import com.example.airecruitmentbackend.common.Result;
import com.example.airecruitmentbackend.dto.JobRecommendResponse;
import com.example.airecruitmentbackend.service.JobRecommendService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 岗位推荐Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/job-recommend")
public class JobRecommendController extends BaseController {

    @Autowired
    private JobRecommendService jobRecommendService;

    /**
     * 获取岗位推荐列表
     */
    @GetMapping
    public Result<List<JobRecommendResponse>> getJobRecommendations(
            @RequestParam(required = false) Integer limit) {
        Long userId = getCurrentUserId();
        log.info("获取岗位推荐列表，用户ID：{}，推荐数量：{}", userId, limit);
        try {
            List<JobRecommendResponse> recommendations = jobRecommendService.getJobRecommendations(userId, limit);
            return Result.success(recommendations);
        } catch (Exception e) {
            log.error("获取岗位推荐列表失败，用户ID：{}", userId, e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取匹配度详情
     */
    @GetMapping("/match/{positionId}")
    public Result<JobRecommendResponse> getMatchDetails(@PathVariable Long positionId) {
        Long userId = getCurrentUserId();
        log.info("获取匹配度详情，用户ID：{}，岗位ID：{}", userId, positionId);
        try {
            JobRecommendResponse response = jobRecommendService.getMatchDetails(userId, positionId);
            return Result.success(response);
        } catch (Exception e) {
            log.error("获取匹配度详情失败，用户ID：{}，岗位ID：{}", userId, positionId, e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 批量生成匹配记录
     */
    @PostMapping("/batch-generate")
    public Result<Integer> batchGenerateMatchRecords() {
        Long userId = getCurrentUserId();
        log.info("批量生成匹配记录，用户ID：{}", userId);
        try {
            int count = jobRecommendService.batchGenerateMatchRecords(userId);
            return Result.success(count);
        } catch (Exception e) {
            log.error("批量生成匹配记录失败，用户ID：{}", userId, e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 标记为已查看
     */
    @PutMapping("/viewed/{recordId}")
    public Result<Boolean> markAsViewed(@PathVariable Long recordId) {
        log.info("标记匹配记录为已查看，记录ID：{}", recordId);
        try {
            boolean result = jobRecommendService.markAsViewed(recordId);
            return Result.success(result);
        } catch (Exception e) {
            log.error("标记已查看失败，记录ID：{}", recordId, e);
            return Result.error(e.getMessage());
        }
    }
}
