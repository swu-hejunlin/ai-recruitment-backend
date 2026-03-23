package com.example.airecruitmentbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.airecruitmentbackend.entity.JobSeeker;
import com.example.airecruitmentbackend.exception.BusinessException;
import com.example.airecruitmentbackend.mapper.JobSeekerMapper;
import com.example.airecruitmentbackend.service.JobSeekerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 求职者信息服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JobSeekerServiceImpl extends ServiceImpl<JobSeekerMapper, JobSeeker> implements JobSeekerService {

    private final JobSeekerMapper jobSeekerMapper;

    @Override
    public JobSeeker getByUserId(Long userId) {
        if (userId == null) {
            throw new BusinessException("用户ID不能为空");
        }

        LambdaQueryWrapper<JobSeeker> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(JobSeeker::getUserId, userId);
        JobSeeker jobSeeker = jobSeekerMapper.selectOne(wrapper);

        return jobSeeker;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateJobSeeker(JobSeeker jobSeeker) {
        if (jobSeeker == null || jobSeeker.getId() == null) {
            throw new BusinessException("求职者信息或ID不能为空");
        }

        int rows = jobSeekerMapper.updateById(jobSeeker);
        if (rows == 0) {
            throw new BusinessException("更新求职者信息失败");
        }

        log.info("更新求职者信息成功：id={}", jobSeeker.getId());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateAvatar(Long userId, String avatarUrl) {
        if (userId == null) {
            throw new BusinessException("用户ID不能为空");
        }
        if (avatarUrl == null || avatarUrl.trim().isEmpty()) {
            throw new BusinessException("头像URL不能为空");
        }

        JobSeeker jobSeeker = getByUserId(userId);
        if (jobSeeker == null) {
            throw new BusinessException("求职者信息不存在");
        }

        jobSeeker.setAvatar(avatarUrl);
        int rows = jobSeekerMapper.updateById(jobSeeker);
        if (rows == 0) {
            throw new BusinessException("更新头像失败");
        }

        log.info("更新求职者头像成功：userId={}, avatarUrl={}", userId, avatarUrl);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateResume(Long userId, String resumeUrl) {
        if (userId == null) {
            throw new BusinessException("用户ID不能为空");
        }
        if (resumeUrl == null || resumeUrl.trim().isEmpty()) {
            throw new BusinessException("简历URL不能为空");
        }

        JobSeeker jobSeeker = getByUserId(userId);
        if (jobSeeker == null) {
            throw new BusinessException("求职者信息不存在");
        }

        jobSeeker.setResumeUrl(resumeUrl);
        int rows = jobSeekerMapper.updateById(jobSeeker);
        if (rows == 0) {
            throw new BusinessException("更新简历失败");
        }

        log.info("更新求职者简历成功：userId={}, resumeUrl={}", userId, resumeUrl);
        return true;
    }
}
