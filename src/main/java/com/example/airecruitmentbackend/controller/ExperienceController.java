package com.example.airecruitmentbackend.controller;

import com.example.airecruitmentbackend.common.Result;
import com.example.airecruitmentbackend.entity.Experience;
import com.example.airecruitmentbackend.exception.BusinessException;
import com.example.airecruitmentbackend.service.ExperienceService;
import com.example.airecruitmentbackend.service.JobSeekerService;
import com.example.airecruitmentbackend.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
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
public class ExperienceController {

    private final ExperienceService experienceService;
    private final JobSeekerService jobSeekerService;
    private final JwtUtil jwtUtil;

    /**
     * 获取当前用户的所有工作/实习经历
     */
    @GetMapping("/list")
    public Result<List<Experience>> getExperiences(HttpServletRequest request) {
        Long userId = jwtUtil.getUserIdFromToken(request);
        log.info("获取工作/实习经历列表请求，userId：{}", userId);

        Long jobSeekerId = getJobSeekerId(userId);
        List<Experience> experiences = experienceService.getByJobSeekerId(jobSeekerId);

        return Result.success("获取工作/实习经历成功", experiences);
    }

    /**
     * 新增工作/实习经历
     */
    @PostMapping("/add")
    public Result<Void> addExperience(HttpServletRequest request,
                                      @Valid @RequestBody Experience experience) {
        Long userId = jwtUtil.getUserIdFromToken(request);
        log.info("新增工作/实习经历请求，userId：{}", userId);

        Long jobSeekerId = getJobSeekerId(userId);
        experience.setJobSeekerId(jobSeekerId);
        experienceService.addExperience(experience);

        return Result.success("新增工作/实习经历成功", null);
    }

    /**
     * 更新工作/实习经历
     */
    @PutMapping("/update")
    public Result<Void> updateExperience(@Valid @RequestBody Experience experience) {
        log.info("更新工作/实习经历请求，id：{}", experience.getId());

        experienceService.updateExperience(experience);

        return Result.success("更新工作/实习经历成功", null);
    }

    /**
     * 删除工作/实习经历
     */
    @DeleteMapping("/delete")
    public Result<Void> deleteExperience(HttpServletRequest request,
                                         @RequestParam("id") Long id) {
        Long userId = jwtUtil.getUserIdFromToken(request);
        log.info("删除工作/实习经历请求，userId：{}，id：{}", userId, id);

        Long jobSeekerId = getJobSeekerId(userId);
        experienceService.deleteExperience(id, jobSeekerId);

        return Result.success("删除工作/实习经历成功", null);
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