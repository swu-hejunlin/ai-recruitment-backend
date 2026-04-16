package com.example.airecruitmentbackend.controller;

import com.example.airecruitmentbackend.common.Result;
import com.example.airecruitmentbackend.dto.JobSeekerDetailDTO;
import com.example.airecruitmentbackend.dto.JobSeekerUpdateRequest;
import com.example.airecruitmentbackend.entity.JobSeeker;
import com.example.airecruitmentbackend.exception.ForbiddenException;
import com.example.airecruitmentbackend.service.EducationService;
import com.example.airecruitmentbackend.service.ExperienceService;
import com.example.airecruitmentbackend.service.JobSeekerService;
import com.example.airecruitmentbackend.service.ProjectService;
import com.example.airecruitmentbackend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

/**
 * 求职者信息控制器
 * 处理求职者个人信息相关的接口请求
 */
@Slf4j
@RestController
@RequestMapping("/api/job-seeker")
@RequiredArgsConstructor
public class JobSeekerController extends BaseController {

    private final JobSeekerService jobSeekerService;
    private final EducationService educationService;
    private final ExperienceService experienceService;
    private final ProjectService projectService;
    private final UserService userService;

    /**
     * 获取求职者完整信息（仅求职者）
     * 接口地址：GET /api/job-seeker/info
     * 返回基本信息 + 教育经历 + 工作/实习经历 + 项目经历
     */
    @GetMapping("/info")
    public Result<JobSeekerDetailDTO> getJobSeekerInfo() {
        // 角色校验：只有求职者(role=1)才能访问
        Integer role = getCurrentUserRole();
        if (role != 1) {
            throw new ForbiddenException("只有求职者才能访问简历信息");
        }
        
        Long userId = getCurrentUserId();
        log.info("获取求职者完整信息请求，userId：{}", userId);

        JobSeeker jobSeeker = jobSeekerService.getByUserId(userId);
        if (jobSeeker == null) {
            return Result.error("求职者信息不存在，请先完善个人信息");
        }

        // 构建完整信息DTO
        JobSeekerDetailDTO detailDTO = new JobSeekerDetailDTO();
        detailDTO.setJobSeeker(jobSeeker);
        detailDTO.setEducations(educationService.getByJobSeekerId(jobSeeker.getId()));
        detailDTO.setExperiences(experienceService.getByJobSeekerId(jobSeeker.getId()));
        detailDTO.setProjects(projectService.getByJobSeekerId(jobSeeker.getId()));

        return Result.success("获取求职者信息成功", detailDTO);
    }

    /**
     * 更新求职者信息（仅求职者）
     * 接口地址：PUT /api/job-seeker/update
     */
    @PutMapping("/update")
    public Result<Void> updateJobSeekerInfo(@Valid @RequestBody JobSeekerUpdateRequest updateRequest) {
        // 角色校验：只有求职者(role=1)才能更新
        Integer role = getCurrentUserRole();
        if (role != 1) {
            throw new ForbiddenException("只有求职者才能更新简历信息");
        }
        
        Long userId = getCurrentUserId();
        log.info("更新求职者信息请求，userId：{}，求职者ID：{}", userId, updateRequest.getId());

        JobSeeker jobSeeker = jobSeekerService.getById(updateRequest.getId());
        if (jobSeeker == null) {
            return Result.error("求职者信息不存在");
        }

        if (!jobSeeker.getUserId().equals(userId)) {
            throw new ForbiddenException("无权修改其他用户的信息");
        }

        BeanUtils.copyProperties(updateRequest, jobSeeker);
        jobSeekerService.updateJobSeeker(jobSeeker);

        return Result.success("更新求职者信息成功", null);
    }

    /**
     * 上传头像（仅求职者）
     * 接口地址：POST /api/job-seeker/avatar
     */
    @PostMapping("/avatar")
    public Result<Void> updateAvatar(@RequestParam("avatarUrl") String avatarUrl) {
        // 角色校验：只有求职者(role=1)才能上传
        Integer role = getCurrentUserRole();
        if (role != 1) {
            throw new ForbiddenException("只有求职者才能上传头像");
        }
        
        Long userId = getCurrentUserId();
        log.info("更新求职者头像请求，userId：{}，avatarUrl：{}", userId, avatarUrl);

        jobSeekerService.updateAvatar(userId, avatarUrl);
        return Result.success("上传头像成功", null);
    }

    /**
     * 上传简历（仅求职者）
     * 接口地址：POST /api/job-seeker/resume
     */
    @PostMapping("/resume")
    public Result<Void> updateResume(@RequestParam("resumeUrl") String resumeUrl) {
        // 角色校验：只有求职者(role=1)才能上传
        Integer role = getCurrentUserRole();
        if (role != 1) {
            throw new ForbiddenException("只有求职者才能上传简历");
        }
        
        Long userId = getCurrentUserId();
        log.info("更新求职者简历请求，userId：{}，resumeUrl：{}", userId, resumeUrl);

        jobSeekerService.updateResume(userId, resumeUrl);
        return Result.success("上传简历成功", null);
    }

    /**
     * 查看头像（仅求职者）
     * 接口地址：GET /api/job-seeker/avatar
     */
    @GetMapping("/avatar")
    public Result<String> getAvatar() {
        // 角色校验：只有求职者(role=1)才能查看
        Integer role = getCurrentUserRole();
        if (role != 1) {
            throw new ForbiddenException("只有求职者才能查看头像");
        }
        
        Long userId = getCurrentUserId();
        log.info("获取求职者头像请求，userId：{}", userId);

        String avatarUrl = jobSeekerService.getAvatarUrl(userId);
        return Result.success("获取头像成功", avatarUrl);
    }

    /**
     * 查看简历（仅求职者）
     * 接口地址：GET /api/job-seeker/resume
     */
    @GetMapping("/resume")
    public Result<String> getResume() {
        // 角色校验：只有求职者(role=1)才能查看
        Integer role = getCurrentUserRole();
        if (role != 1) {
            throw new ForbiddenException("只有求职者才能查看简历");
        }
        
        Long userId = getCurrentUserId();
        log.info("获取求职者简历请求，userId：{}", userId);

        String resumeUrl = jobSeekerService.getResumeUrl(userId);
        return Result.success("获取简历成功", resumeUrl);
    }
}
