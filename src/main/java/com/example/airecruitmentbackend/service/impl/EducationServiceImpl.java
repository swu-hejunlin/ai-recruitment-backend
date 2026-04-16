package com.example.airecruitmentbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.airecruitmentbackend.entity.Education;
import com.example.airecruitmentbackend.exception.BusinessException;
import com.example.airecruitmentbackend.exception.ForbiddenException;
import com.example.airecruitmentbackend.mapper.EducationMapper;
import com.example.airecruitmentbackend.service.EducationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 教育经历 Service实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EducationServiceImpl extends ServiceImpl<EducationMapper, Education> implements EducationService {

    @Override
    public List<Education> getByJobSeekerId(Long jobSeekerId) {
        if (jobSeekerId == null) {
            throw new BusinessException("求职者ID不能为空");
        }
        LambdaQueryWrapper<Education> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Education::getJobSeekerId, jobSeekerId)
                .orderByDesc(Education::getStartDate);
        return list(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addEducation(Education education) {
        if (education == null || education.getJobSeekerId() == null) {
            throw new BusinessException("教育经历信息或求职者ID不能为空");
        }
        education.setCreateTime(LocalDateTime.now());
        education.setUpdateTime(LocalDateTime.now());
        boolean success = save(education);
        log.info("新增教育经历成功：id={}, jobSeekerId={}", education.getId(), education.getJobSeekerId());
        return success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateEducation(Education education) {
        if (education == null || education.getId() == null) {
            throw new BusinessException("教育经历ID不能为空");
        }
        Education existing = getById(education.getId());
        if (existing == null) {
            throw new BusinessException("教育经历不存在");
        }
        education.setUpdateTime(LocalDateTime.now());
        boolean success = updateById(education);
        log.info("更新教育经历成功：id={}", education.getId());
        return success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteEducation(Long id, Long jobSeekerId) {
        if (id == null) {
            throw new BusinessException("教育经历ID不能为空");
        }
        Education education = getById(id);
        if (education == null) {
            throw new BusinessException("教育经历不存在");
        }
        if (jobSeekerId != null && !education.getJobSeekerId().equals(jobSeekerId)) {
            throw new ForbiddenException("无权删除他人的教育经历");
        }
        boolean success = removeById(id);
        log.info("删除教育经历成功：id={}", id);
        return success;
    }
}