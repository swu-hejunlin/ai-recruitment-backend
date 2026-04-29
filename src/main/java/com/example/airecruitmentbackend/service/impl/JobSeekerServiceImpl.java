package com.example.airecruitmentbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.airecruitmentbackend.entity.Application;
import com.example.airecruitmentbackend.entity.JobSeeker;
import com.example.airecruitmentbackend.exception.BusinessException;
import com.example.airecruitmentbackend.mapper.ApplicationMapper;
import com.example.airecruitmentbackend.mapper.JobSeekerMapper;
import com.example.airecruitmentbackend.service.JobSeekerService;
import com.example.airecruitmentbackend.service.TalentProfileService;
import com.example.airecruitmentbackend.utils.OssUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
    private final ApplicationMapper applicationMapper;

    @Autowired
    OssUtil ossUtil;

    @Autowired
    TalentProfileService talentProfileService;

    @Override
    public JobSeeker getByUserId(Long userId) {
        if (userId == null) {
            throw new BusinessException("用户ID不能为空");
        }
        return jobSeekerMapper.selectOne(new LambdaQueryWrapper<JobSeeker>()
                .eq(JobSeeker::getUserId, userId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateJobSeeker(JobSeeker jobSeeker) {
        if (jobSeeker == null || jobSeeker.getId() == null) {
            throw new BusinessException("求职者信息或ID不能为空");
        }

        // 查询原信息，用于判断姓名是否变更
        JobSeeker original = jobSeekerMapper.selectById(jobSeeker.getId());
        boolean nameChanged = original != null
                && jobSeeker.getName() != null
                && !jobSeeker.getName().equals(original.getName());

        int rows = jobSeekerMapper.updateById(jobSeeker);
        if (rows == 0) {
            throw new BusinessException("更新求职者信息失败");
        }

        // 同步更新投递记录中的冗余姓名
        if (nameChanged) {
            applicationMapper.update(null, new LambdaUpdateWrapper<Application>()
                    .eq(Application::getJobSeekerId, jobSeeker.getId())
                    .set(Application::getJobSeekerName, jobSeeker.getName()));
            log.info("同步更新投递记录中的求职者姓名：jobSeekerId={}, name={}", jobSeeker.getId(), jobSeeker.getName());
        }

        log.info("更新求职者信息成功：id={}", jobSeeker.getId());

        // 异步生成人才画像
        if (original != null && original.getUserId() != null) {
            log.info("开始异步生成人才画像，用户ID：{}", original.getUserId());
            talentProfileService.generateTalentProfileAsync(original.getUserId())
                    .thenAccept(result -> log.info("异步生成人才画像完成，用户ID：{}", original.getUserId()))
                    .exceptionally(e -> {
                        log.error("异步生成人才画像失败，用户ID：{}", original.getUserId(), e);
                        return null;
                    });
        }

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

        // 异步生成人才画像
        log.info("开始异步生成人才画像，用户ID：{}", userId);
        talentProfileService.generateTalentProfileAsync(userId)
                .thenAccept(result -> log.info("异步生成人才画像完成，用户ID：{}", userId))
                .exceptionally(e -> {
                    log.error("异步生成人才画像失败，用户ID：{}", userId, e);
                    return null;
                });

        return true;
    }

    @Override
    public String getAvatarUrl(Long userId) {
        JobSeeker jobSeeker = getByUserId(userId);
        if (jobSeeker == null) {
            return null;
        }
        return jobSeeker.getAvatar();
    }

    @Override
    public String getResumeUrl(Long userId) {
        JobSeeker jobSeeker = getByUserId(userId);
        if (jobSeeker == null) {
            return null;
        }

        return ossUtil.generatePresignedUrl(jobSeeker.getResumeUrl(),3600 * 24);
    }
}