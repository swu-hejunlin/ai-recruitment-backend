package com.example.airecruitmentbackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.airecruitmentbackend.entity.Education;

import java.util.List;

/**
 * 教育经历 Service
 */
public interface EducationService extends IService<Education> {

    /**
     * 根据求职者ID获取所有教育经历
     */
    List<Education> getByJobSeekerId(Long jobSeekerId);

    /**
     * 新增教育经历
     */
    boolean addEducation(Education education);

    /**
     * 更新教育经历
     */
    boolean updateEducation(Education education);

    /**
     * 删除教育经历
     */
    boolean deleteEducation(Long id, Long jobSeekerId);
}