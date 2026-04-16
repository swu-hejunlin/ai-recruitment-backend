package com.example.airecruitmentbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.airecruitmentbackend.entity.Project;
import com.example.airecruitmentbackend.exception.BusinessException;
import com.example.airecruitmentbackend.exception.ForbiddenException;
import com.example.airecruitmentbackend.mapper.ProjectMapper;
import com.example.airecruitmentbackend.service.ProjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 项目经历 Service实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectServiceImpl extends ServiceImpl<ProjectMapper, Project> implements ProjectService {

    @Override
    public List<Project> getByJobSeekerId(Long jobSeekerId) {
        if (jobSeekerId == null) {
            throw new BusinessException("求职者ID不能为空");
        }
        LambdaQueryWrapper<Project> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Project::getJobSeekerId, jobSeekerId)
                .orderByDesc(Project::getStartDate);
        return list(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addProject(Project project) {
        if (project == null || project.getJobSeekerId() == null) {
            throw new BusinessException("项目信息或求职者ID不能为空");
        }
        project.setCreateTime(LocalDateTime.now());
        project.setUpdateTime(LocalDateTime.now());
        boolean success = save(project);
        log.info("新增项目经历成功：id={}, jobSeekerId={}", project.getId(), project.getJobSeekerId());
        return success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateProject(Project project) {
        if (project == null || project.getId() == null) {
            throw new BusinessException("项目ID不能为空");
        }
        Project existing = getById(project.getId());
        if (existing == null) {
            throw new BusinessException("项目不存在");
        }
        project.setUpdateTime(LocalDateTime.now());
        boolean success = updateById(project);
        log.info("更新项目经历成功：id={}", project.getId());
        return success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteProject(Long id, Long jobSeekerId) {
        if (id == null) {
            throw new BusinessException("项目ID不能为空");
        }
        Project project = getById(id);
        if (project == null) {
            throw new BusinessException("项目不存在");
        }
        if (jobSeekerId != null && !project.getJobSeekerId().equals(jobSeekerId)) {
            throw new ForbiddenException("无权删除他人的项目");
        }
        boolean success = removeById(id);
        log.info("删除项目经历成功：id={}", id);
        return success;
    }
}