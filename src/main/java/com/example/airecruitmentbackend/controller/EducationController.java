package com.example.airecruitmentbackend.controller;

import com.example.airecruitmentbackend.common.Result;
import com.example.airecruitmentbackend.dto.EducationDTO;
import com.example.airecruitmentbackend.entity.Education;
import com.example.airecruitmentbackend.exception.BusinessException;
import com.example.airecruitmentbackend.exception.ForbiddenException;
import com.example.airecruitmentbackend.service.EducationService;
import com.example.airecruitmentbackend.service.JobSeekerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 教育经历控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/education")
@RequiredArgsConstructor
public class EducationController extends BaseController {

    private final EducationService educationService;
    private final JobSeekerService jobSeekerService;

    /**
     * 获取当前用户的教育经历列表（需登录）
     */
    @GetMapping("/list")
    public Result<List<Education>> getEducations() {
        // 获取当前登录用户的求职者ID
        Long userId = getCurrentUserId();
        Long jobSeekerId = getJobSeekerId(userId);
        List<Education> educations = educationService.getByJobSeekerId(jobSeekerId);
        return Result.success("获取教育经历成功", educations);
    }

    /**
     * 新增教育经历（仅求职者）
     */
    @PostMapping("/add")
    public Result<Void> addEducation(@Valid @RequestBody EducationDTO dto) {
        Long userId = getCurrentUserId();
        Long jobSeekerId = getJobSeekerId(userId);
        Education education = dto.toEntity();
        education.setJobSeekerId(jobSeekerId);
        educationService.addEducation(education);
        return Result.success("新增教育经历成功", null);
    }

    /**
     * 更新教育经历（需校验归属权）
     */
    @PutMapping("/update")
    public Result<Void> updateEducation(@Valid @RequestBody EducationDTO dto) {
        Long userId = getCurrentUserId();
        Long jobSeekerId = getJobSeekerId(userId);
        
        // 校验归属权：确保只能修改自己的教育经历
        Education existing = educationService.getById(dto.getId());
        if (existing == null) {
            throw new BusinessException("教育记录不存在");
        }
        if (!existing.getJobSeekerId().equals(jobSeekerId)) {
            throw new ForbiddenException("无权修改他人的教育经历");
        }
        
        Education education = dto.toEntity();
        education.setJobSeekerId(jobSeekerId);
        educationService.updateEducation(education);
        return Result.success("更新教育经历成功", null);
    }

    /**
     * 删除教育经历（需校验归属权）
     */
    @DeleteMapping("/delete")
    public Result<Void> deleteEducation(@RequestParam("id") Long id) {
        Long userId = getCurrentUserId();
        Long jobSeekerId = getJobSeekerId(userId);
        educationService.deleteEducation(id, jobSeekerId);
        return Result.success("删除教育经历成功", null);
    }

    private Long getJobSeekerId(Long userId) {
        var jobSeeker = jobSeekerService.getByUserId(userId);
        if (jobSeeker == null) {
            throw new BusinessException("求职者信息不存在");
        }
        return jobSeeker.getId();
    }
}
