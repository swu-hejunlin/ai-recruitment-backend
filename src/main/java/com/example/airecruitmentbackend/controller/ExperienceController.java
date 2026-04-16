package com.example.airecruitmentbackend.controller;

import com.example.airecruitmentbackend.common.Result;
import com.example.airecruitmentbackend.entity.Experience;
import com.example.airecruitmentbackend.exception.BusinessException;
import com.example.airecruitmentbackend.exception.ForbiddenException;
import com.example.airecruitmentbackend.service.ExperienceService;
import com.example.airecruitmentbackend.service.JobSeekerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 工作经验/实习经历控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/experience")
@RequiredArgsConstructor
public class ExperienceController extends BaseController {

    private final ExperienceService experienceService;
    private final JobSeekerService jobSeekerService;

    /**
     * 获取当前用户的工作/实习经历列表（需登录）
     */
    @GetMapping("/list")
    public Result<List<Experience>> getExperiences() {
        // 获取当前登录用户的求职者ID
        Long userId = getCurrentUserId();
        Long jobSeekerId = getJobSeekerId(userId);
        List<Experience> experiences = experienceService.getByJobSeekerId(jobSeekerId);
        return Result.success("获取工作/实习经历成功", experiences);
    }

    /**
     * 新增工作/实习经历（仅求职者）
     */
    @PostMapping("/add")
    public Result<Void> addExperience(@Valid @RequestBody Experience experience) {
        Long userId = getCurrentUserId();
        Long jobSeekerId = getJobSeekerId(userId);
        experience.setJobSeekerId(jobSeekerId);
        experienceService.addExperience(experience);
        return Result.success("新增工作/实习经历成功", null);
    }

    /**
     * 更新工作/实习经历（需校验归属权）
     */
    @PutMapping("/update")
    public Result<Void> updateExperience(@Valid @RequestBody Experience experience) {
        Long userId = getCurrentUserId();
        Long jobSeekerId = getJobSeekerId(userId);
        
        // 校验归属权：确保只能修改自己的经历
        Experience existing = experienceService.getById(experience.getId());
        if (existing == null) {
            throw new BusinessException("经历记录不存在");
        }
        if (!existing.getJobSeekerId().equals(jobSeekerId)) {
            throw new ForbiddenException("无权修改他人的工作经历");
        }
        
        experience.setJobSeekerId(jobSeekerId);
        experienceService.updateExperience(experience);
        return Result.success("更新工作/实习经历成功", null);
    }

    /**
     * 删除工作/实习经历（需校验归属权）
     */
    @DeleteMapping("/delete")
    public Result<Void> deleteExperience(@RequestParam("id") Long id) {
        Long userId = getCurrentUserId();
        Long jobSeekerId = getJobSeekerId(userId);
        experienceService.deleteExperience(id, jobSeekerId);
        return Result.success("删除工作/实习经历成功", null);
    }

    private Long getJobSeekerId(Long userId) {
        var jobSeeker = jobSeekerService.getByUserId(userId);
        if (jobSeeker == null) {
            throw new BusinessException("求职者信息不存在");
        }
        return jobSeeker.getId();
    }
}
