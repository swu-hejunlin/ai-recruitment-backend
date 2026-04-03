package com.example.airecruitmentbackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.airecruitmentbackend.entity.Experience;

import java.util.List;

/**
 * 工作经验/实习经历 Service
 */
public interface ExperienceService extends IService<Experience> {

    /**
     * 根据求职者ID获取所有经历
     */
    List<Experience> getByJobSeekerId(Long jobSeekerId);

    /**
     * 新增经历
     */
    boolean addExperience(Experience experience);

    /**
     * 更新经历
     */
    boolean updateExperience(Experience experience);

    /**
     * 删除经历
     */
    boolean deleteExperience(Long id, Long jobSeekerId);
}