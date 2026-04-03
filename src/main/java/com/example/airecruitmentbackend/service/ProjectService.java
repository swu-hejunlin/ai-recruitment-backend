package com.example.airecruitmentbackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.airecruitmentbackend.entity.Project;

import java.util.List;

/**
 * 项目经历 Service
 */
public interface ProjectService extends IService<Project> {

    /**
     * 根据求职者ID获取所有项目经历
     */
    List<Project> getByJobSeekerId(Long jobSeekerId);

    /**
     * 新增项目经历
     */
    boolean addProject(Project project);

    /**
     * 更新项目经历
     */
    boolean updateProject(Project project);

    /**
     * 删除项目经历
     */
    boolean deleteProject(Long id, Long jobSeekerId);
}