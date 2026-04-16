package com.example.airecruitmentbackend.controller;

import com.example.airecruitmentbackend.common.Result;
import com.example.airecruitmentbackend.entity.Project;
import com.example.airecruitmentbackend.exception.BusinessException;
import com.example.airecruitmentbackend.exception.ForbiddenException;
import com.example.airecruitmentbackend.service.JobSeekerService;
import com.example.airecruitmentbackend.service.ProjectService;
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
public class ProjectController extends BaseController {

    private final ProjectService projectService;
    private final JobSeekerService jobSeekerService;

    /**
     * 获取当前用户的项目经历列表（需登录）
     */
    @GetMapping("/list")
    public Result<List<Project>> getProjects() {
        // 获取当前登录用户的求职者ID
        Long userId = getCurrentUserId();
        Long jobSeekerId = getJobSeekerId(userId);
        List<Project> projects = projectService.getByJobSeekerId(jobSeekerId);
        return Result.success("获取项目经历成功", projects);
    }

    /**
     * 新增项目经历（仅求职者）
     */
    @PostMapping("/add")
    public Result<Void> addProject(@Valid @RequestBody Project project) {
        Long userId = getCurrentUserId();
        Long jobSeekerId = getJobSeekerId(userId);
        project.setJobSeekerId(jobSeekerId);
        projectService.addProject(project);
        return Result.success("新增项目经历成功", null);
    }

    /**
     * 更新项目经历（需校验归属权）
     */
    @PutMapping("/update")
    public Result<Void> updateProject(@Valid @RequestBody Project project) {
        Long userId = getCurrentUserId();
        Long jobSeekerId = getJobSeekerId(userId);
        
        // 校验归属权：确保只能修改自己的项目
        Project existing = projectService.getById(project.getId());
        if (existing == null) {
            throw new BusinessException("项目记录不存在");
        }
        if (!existing.getJobSeekerId().equals(jobSeekerId)) {
            throw new ForbiddenException("无权修改他人的项目经历");
        }
        
        project.setJobSeekerId(jobSeekerId);
        projectService.updateProject(project);
        return Result.success("更新项目经历成功", null);
    }

    /**
     * 删除项目经历（需校验归属权）
     */
    @DeleteMapping("/delete")
    public Result<Void> deleteProject(@RequestParam("id") Long id) {
        Long userId = getCurrentUserId();
        Long jobSeekerId = getJobSeekerId(userId);
        projectService.deleteProject(id, jobSeekerId);
        return Result.success("删除项目经历成功", null);
    }

    private Long getJobSeekerId(Long userId) {
        var jobSeeker = jobSeekerService.getByUserId(userId);
        if (jobSeeker == null) {
            throw new BusinessException("求职者信息不存在");
        }
        return jobSeeker.getId();
    }
}
