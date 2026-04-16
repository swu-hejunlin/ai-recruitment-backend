package com.example.airecruitmentbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.airecruitmentbackend.entity.Experience;
import com.example.airecruitmentbackend.exception.BusinessException;
import com.example.airecruitmentbackend.exception.ForbiddenException;
import com.example.airecruitmentbackend.mapper.ExperienceMapper;
import com.example.airecruitmentbackend.service.ExperienceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 工作经验/实习经历 Service实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExperienceServiceImpl extends ServiceImpl<ExperienceMapper, Experience> implements ExperienceService {

    @Override
    public List<Experience> getByJobSeekerId(Long jobSeekerId) {
        if (jobSeekerId == null) {
            throw new BusinessException("求职者ID不能为空");
        }
        LambdaQueryWrapper<Experience> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Experience::getJobSeekerId, jobSeekerId)
                .orderByDesc(Experience::getStartDate);
        return list(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addExperience(Experience experience) {
        if (experience == null || experience.getJobSeekerId() == null) {
            throw new BusinessException("经历信息或求职者ID不能为空");
        }
        experience.setCreateTime(LocalDateTime.now());
        experience.setUpdateTime(LocalDateTime.now());
        boolean success = save(experience);
        log.info("新增工作/实习经历成功：id={}, jobSeekerId={}", experience.getId(), experience.getJobSeekerId());
        return success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateExperience(Experience experience) {
        if (experience == null || experience.getId() == null) {
            throw new BusinessException("经历ID不能为空");
        }
        Experience existing = getById(experience.getId());
        if (existing == null) {
            throw new BusinessException("经历不存在");
        }
        experience.setUpdateTime(LocalDateTime.now());
        boolean success = updateById(experience);
        log.info("更新工作/实习经历成功：id={}", experience.getId());
        return success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteExperience(Long id, Long jobSeekerId) {
        if (id == null) {
            throw new BusinessException("经历ID不能为空");
        }
        Experience experience = getById(id);
        if (experience == null) {
            throw new BusinessException("经历不存在");
        }
        if (jobSeekerId != null && !experience.getJobSeekerId().equals(jobSeekerId)) {
            throw new ForbiddenException("无权删除他人的经历");
        }
        boolean success = removeById(id);
        log.info("删除工作/实习经历成功：id={}", id);
        return success;
    }
}