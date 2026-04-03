package com.example.airecruitmentbackend.controller;

import com.example.airecruitmentbackend.common.Result;
import com.example.airecruitmentbackend.dto.JobSeekerDetailDTO;
import com.example.airecruitmentbackend.entity.JobSeeker;
import com.example.airecruitmentbackend.dto.JobSeekerUpdateRequest;
import com.example.airecruitmentbackend.service.EducationService;
import com.example.airecruitmentbackend.service.ExperienceService;
import com.example.airecruitmentbackend.service.JobSeekerService;
import com.example.airecruitmentbackend.service.ProjectService;
import com.example.airecruitmentbackend.service.UserService;
import com.example.airecruitmentbackend.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
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
public class JobSeekerController {

    private final JobSeekerService jobSeekerService;
    private final EducationService educationService;
    private final ExperienceService experienceService;
    private final ProjectService projectService;
    private final UserService userService;
    private final JwtUtil jwtUtil;

    /**
     * 获取求职者完整信息
     * 接口地址：GET /api/job-seeker/info
     * 返回基本信息 + 教育经历 + 工作/实习经历 + 项目经历
     *
     * @param request HTTP请求（从请求头中获取JWT令牌）
     * @return 求职者完整信息
     */
    @GetMapping("/info")
    public Result<JobSeekerDetailDTO> getJobSeekerInfo(HttpServletRequest request) {
        Long userId = jwtUtil.getUserIdFromToken(request);
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
     * 更新求职者信息
     * 接口地址：PUT /api/job-seeker/update
     *
     * @param request HTTP请求（从请求头中获取JWT令牌）
     * @param updateRequest 求职者信息更新请求
     * @return 操作结果
     */
    @PutMapping("/update")
    public Result<Void> updateJobSeekerInfo(HttpServletRequest request,
                                            @Valid @RequestBody JobSeekerUpdateRequest updateRequest) {
        Long userId = jwtUtil.getUserIdFromToken(request);
        log.info("更新求职者信息请求，userId：{}，求职者ID：{}", userId, updateRequest.getId());

        JobSeeker jobSeeker = jobSeekerService.getById(updateRequest.getId());
        if (jobSeeker == null) {
            return Result.error("求职者信息不存在");
        }

        if (!jobSeeker.getUserId().equals(userId)) {
            return Result.error("无权修改其他用户的信息");
        }

        BeanUtils.copyProperties(updateRequest, jobSeeker);
        jobSeekerService.updateJobSeeker(jobSeeker);

        return Result.success("更新求职者信息成功", null);
    }

    /**
     * 上传头像
     * 接口地址：POST /api/job-seeker/avatar
     *
     * @param request HTTP请求（从请求头中获取JWT令牌）
     * @param avatarUrl 头像URL
     * @return 操作结果
     */
    @PostMapping("/avatar")
    public Result<Void> updateAvatar(HttpServletRequest request,
                                    @RequestParam("avatarUrl") String avatarUrl) {
        Long userId = jwtUtil.getUserIdFromToken(request);
        log.info("更新求职者头像请求，userId：{}，avatarUrl：{}", userId, avatarUrl);

        jobSeekerService.updateAvatar(userId, avatarUrl);
        return Result.success("上传头像成功", null);
    }

    /**
     * 上传简历
     * 接口地址：POST /api/job-seeker/resume
     *
     * @param request HTTP请求（从请求头中获取JWT令牌）
     * @param resumeUrl 简历URL
     * @return 操作结果
     */
    @PostMapping("/resume")
    public Result<Void> updateResume(HttpServletRequest request,
                                     @RequestParam("resumeUrl") String resumeUrl) {
        Long userId = jwtUtil.getUserIdFromToken(request);
        log.info("更新求职者简历请求，userId：{}，resumeUrl：{}", userId, resumeUrl);

        jobSeekerService.updateResume(userId, resumeUrl);
        return Result.success("上传简历成功", null);
    }

    /**
     * 查看头像
     * 接口地址：GET /api/job-seeker/avatar
     *
     * @param request HTTP请求（从请求头中获取JWT令牌）
     * @return 头像URL
     */
    @GetMapping("/avatar")
    public Result<String> getAvatar(HttpServletRequest request) {
        Long userId = jwtUtil.getUserIdFromToken(request);
        log.info("获取求职者头像请求，userId：{}", userId);

        String avatarUrl = jobSeekerService.getAvatarUrl(userId);
        return Result.success("获取头像成功", avatarUrl);
    }

    /**
     * 查看简历
     * 接口地址：GET /api/job-seeker/resume
     *
     * @param request HTTP请求（从请求头中获取JWT令牌）
     * @return 简历URL
     */
    @GetMapping("/resume")
    public Result<String> getResume(HttpServletRequest request) {
        Long userId = jwtUtil.getUserIdFromToken(request);
        log.info("获取求职者简历请求，userId：{}", userId);

        String resumeUrl = jobSeekerService.getResumeUrl(userId);
        return Result.success("获取简历成功", resumeUrl);
    }
}
