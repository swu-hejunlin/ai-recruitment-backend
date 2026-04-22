package com.example.airecruitmentbackend.controller;

import com.example.airecruitmentbackend.common.Result;
import com.example.airecruitmentbackend.dto.JobProfileResponse;
import com.example.airecruitmentbackend.service.JobProfileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 岗位画像Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/job-profile")
public class JobProfileController extends BaseController {

    @Autowired
    private JobProfileService jobProfileService;

    /**
     * 生成岗位画像
     */
    @PostMapping("/generate/{positionId}")
    public Result<JobProfileResponse> generateJobProfile(@PathVariable Long positionId) {
        log.info("生成岗位画像，岗位ID：{}", positionId);
        try {
            JobProfileResponse response = jobProfileService.generateJobProfile(positionId);
            return Result.success(response);
        } catch (Exception e) {
            log.error("生成岗位画像失败，岗位ID：{}", positionId, e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取岗位画像
     */
    @GetMapping("/{positionId}")
    public Result<JobProfileResponse> getJobProfile(@PathVariable Long positionId) {
        log.info("获取岗位画像，岗位ID：{}", positionId);
        try {
            JobProfileResponse response = jobProfileService.getJobProfile(positionId);
            return Result.success(response);
        } catch (Exception e) {
            log.error("获取岗位画像失败，岗位ID：{}", positionId, e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新岗位画像
     */
    @PutMapping("/update/{positionId}")
    public Result<JobProfileResponse> updateJobProfile(@PathVariable Long positionId) {
        log.info("更新岗位画像，岗位ID：{}", positionId);
        try {
            JobProfileResponse response = jobProfileService.updateJobProfile(positionId);
            return Result.success(response);
        } catch (Exception e) {
            log.error("更新岗位画像失败，岗位ID：{}", positionId, e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除岗位画像
     */
    @DeleteMapping("/{positionId}")
    public Result<Boolean> deleteJobProfile(@PathVariable Long positionId) {
        log.info("删除岗位画像，岗位ID：{}", positionId);
        try {
            boolean result = jobProfileService.deleteJobProfile(positionId);
            return Result.success(result);
        } catch (Exception e) {
            log.error("删除岗位画像失败，岗位ID：{}", positionId, e);
            return Result.error(e.getMessage());
        }
    }
}
