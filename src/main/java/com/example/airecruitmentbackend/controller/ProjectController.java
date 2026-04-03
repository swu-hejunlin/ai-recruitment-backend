package com.example.airecruitmentbackend.controller;

import com.example.airecruitmentbackend.common.Result;
import com.example.airecruitmentbackend.entity.Project;
import com.example.airecruitmentbackend.exception.BusinessException;
import com.example.airecruitmentbackend.service.JobSeekerService;
import com.example.airecruitmentbackend.service.ProjectService;
import com.example.airecruitmentbackend.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 项目经历控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/project")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;
    private final JobSeekerService jobSeekerService;
    private final JwtUtil jwtUtil;

    /**
     * 获取当前用户的所有项目经历
     */
    @GetMapping("/list")
    public Result<List<Project>> getProjects(HttpServletRequest request) {
        Long userId = jwtUtil.getUserIdFromToken(request);
        log.info("获取项目经历列表请求，userId：{}", userId);

        Long jobSeekerId = getJobSeekerId(userId);
        List<Project> projects = projectService.getByJobSeekerId(jobSeekerId);

        return Result.success("获取项目经历成功", projects);
    }

    /**
     * 新增项目经历
     */
    @PostMapping("/add")
    public Result<Void> addProject(HttpServletRequest request,
                                    @Valid @RequestBody Project project) {
        Long userId = jwtUtil.getUserIdFromToken(request);
        log.info("新增项目经历请求，userId：{}", userId);

        Long jobSeekerId = getJobSeekerId(userId);
        project.setJobSeekerId(jobSeekerId);
        projectService.addProject(project);

        return Result.success("新增项目经历成功", null);
    }

    /**
     * 更新项目经历
     */
    @PutMapping("/update")
    public Result<Void> updateProject(@Valid @RequestBody Project project) {
        log.info("更新项目经历请求，id：{}", project.getId());

        projectService.updateProject(project);

        return Result.success("更新项目经历成功", null);
    }

    /**
     * 删除项目经历
     */
    @DeleteMapping("/delete")
    public Result<Void> deleteProject(HttpServletRequest request,
                                       @RequestParam("id") Long id) {
        Long userId = jwtUtil.getUserIdFromToken(request);
        log.info("删除项目经历请求，userId：{}，id：{}", userId, id);

        Long jobSeekerId = getJobSeekerId(userId);
        projectService.deleteProject(id, jobSeekerId);

        return Result.success("删除项目经历成功", null);
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