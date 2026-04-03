package com.example.airecruitmentbackend.controller;

import com.example.airecruitmentbackend.common.Result;
import com.example.airecruitmentbackend.dto.EducationDTO;
import com.example.airecruitmentbackend.entity.Education;
import com.example.airecruitmentbackend.exception.BusinessException;
import com.example.airecruitmentbackend.service.EducationService;
import com.example.airecruitmentbackend.service.JobSeekerService;
import com.example.airecruitmentbackend.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
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
public class EducationController {

    private final EducationService educationService;
    private final JobSeekerService jobSeekerService;
    private final JwtUtil jwtUtil;

    /**
     * 获取当前用户的所有教育经历
     */
    @GetMapping("/list")
    public Result<List<Education>> getEducations(HttpServletRequest request) {
        Long userId = jwtUtil.getUserIdFromToken(request);
        log.info("获取教育经历列表请求，userId：{}", userId);

        Long jobSeekerId = getJobSeekerId(userId);
        List<Education> educations = educationService.getByJobSeekerId(jobSeekerId);

        return Result.success("获取教育经历成功", educations);
    }

    /**
     * 新增教育经历
     */
    @PostMapping("/add")
    public Result<Void> addEducation(HttpServletRequest request,
                                      @Valid @RequestBody EducationDTO dto) {
        Long userId = jwtUtil.getUserIdFromToken(request);
        log.info("新增教育经历请求，userId：{}", userId);

        Long jobSeekerId = getJobSeekerId(userId);
        Education education = dto.toEntity();
        education.setJobSeekerId(jobSeekerId);
        educationService.addEducation(education);

        return Result.success("新增教育经历成功", null);
    }

    /**
     * 更新教育经历
     */
    @PutMapping("/update")
    public Result<Void> updateEducation(@Valid @RequestBody EducationDTO dto) {
        log.info("更新教育经历请求，id：{}", dto.getId());

        Education education = dto.toEntity();
        educationService.updateEducation(education);

        return Result.success("更新教育经历成功", null);
    }

    /**
     * 删除教育经历
     */
    @DeleteMapping("/delete")
    public Result<Void> deleteEducation(HttpServletRequest request,
                                         @RequestParam("id") Long id) {
        Long userId = jwtUtil.getUserIdFromToken(request);
        log.info("删除教育经历请求，userId：{}，id：{}", userId, id);

        Long jobSeekerId = getJobSeekerId(userId);
        educationService.deleteEducation(id, jobSeekerId);

        return Result.success("删除教育经历成功", null);
    }

    /**
     * 获取当前用户的 jobSeekerId
     */
    private Long getJobSeekerId(Long userId) {
        var jobSeeker = jobSeekerService.getByUserId(userId);
        if (jobSeeker == null) {
            throw new BusinessException("求职者信息不存在");
        }
        return jobSeeker.getId();
    }
}