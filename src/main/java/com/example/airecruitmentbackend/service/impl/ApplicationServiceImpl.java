package com.example.airecruitmentbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.airecruitmentbackend.entity.Application;
import com.example.airecruitmentbackend.entity.JobSeeker;
import com.example.airecruitmentbackend.entity.Notification;
import com.example.airecruitmentbackend.entity.Position;
import com.example.airecruitmentbackend.exception.BusinessException;
import com.example.airecruitmentbackend.mapper.ApplicationMapper;
import com.example.airecruitmentbackend.mapper.JobSeekerMapper;
import com.example.airecruitmentbackend.mapper.PositionMapper;
import com.example.airecruitmentbackend.service.ApplicationService;
import com.example.airecruitmentbackend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 投递记录Service实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApplicationServiceImpl implements ApplicationService {

    private final ApplicationMapper applicationMapper;
    private final PositionMapper positionMapper;
    private final JobSeekerMapper jobSeekerMapper;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public void apply(Long jobSeekerId, Long positionId) {
        // 查询职位信息
        Position position = positionMapper.selectById(positionId);
        if (position == null) {
            throw new BusinessException("职位不存在");
        }
        // 校验职位状态
        if (position.getStatus() != 1) {
            throw new BusinessException("该职位已停止招聘");
        }
        // 校验是否已投递
        if (hasApplied(jobSeekerId, positionId)) {
            throw new BusinessException("您已投递过该职位");
        }
        // 查询求职者信息
        JobSeeker jobSeeker = jobSeekerMapper.selectById(jobSeekerId);
        if (jobSeeker == null) {
            throw new BusinessException("求职者信息不存在");
        }

        // 创建投递记录
        Application application = new Application();
        application.setJobSeekerId(jobSeekerId);
        application.setPositionId(positionId);
        application.setCompanyId(position.getCompanyId());
        application.setBossId(position.getBossId());
        application.setStatus(1); // 待查看
        application.setCreateTime(LocalDateTime.now());
        application.setUpdateTime(LocalDateTime.now());
        applicationMapper.insert(application);

        // 发送通知给Boss
        String content = String.format("您收到了来自 %s 对 %s 的新投递",
                jobSeeker.getName() != null ? jobSeeker.getName() : "求职者",
                position.getTitle());
        notificationService.sendNotification(
                position.getBossId(),
                1, // 新投递提醒
                "新投递提醒",
                content,
                application.getId()
        );

        log.info("投递成功，jobSeekerId：{}，positionId：{}，applicationId：{}",
                jobSeekerId, positionId, application.getId());
    }

    @Override
    public Application getById(Long id) {
        return applicationMapper.selectById(id);
    }

    @Override
    public Page<Application> getApplicationsByBoss(Long bossId, Integer status, int pageNum, int pageSize) {
        Page<Application> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Application> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Application::getBossId, bossId);
        if (status != null) {
            wrapper.eq(Application::getStatus, status);
        }
        wrapper.orderByDesc(Application::getCreateTime);
        return applicationMapper.selectPage(page, wrapper);
    }

    @Override
    public Page<Application> getApplicationsByJobSeeker(Long jobSeekerId, int pageNum, int pageSize) {
        Page<Application> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Application> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Application::getJobSeekerId, jobSeekerId)
               .orderByDesc(Application::getCreateTime);
        return applicationMapper.selectPage(page, wrapper);
    }

    @Override
    @Transactional
    public void readApplication(Long applicationId, Long bossId) {
        Application application = applicationMapper.selectById(applicationId);
        if (application == null) {
            throw new BusinessException("投递记录不存在");
        }
        if (!application.getBossId().equals(bossId)) {
            throw new BusinessException("无权查看此投递");
        }
        // 更新为已查看状态
        application.setStatus(2);
        application.setUpdateTime(LocalDateTime.now());
        applicationMapper.updateById(application);

        // 标记相关通知为已读
        notificationService.markAsReadByBusinessId(applicationId, bossId);

        log.info("查看简历成功，applicationId：{}", applicationId);
    }

    @Override
    @Transactional
    public void updateStatus(Long applicationId, Integer status, Long bossId) {
        Application application = applicationMapper.selectById(applicationId);
        if (application == null) {
            throw new BusinessException("投递记录不存在");
        }
        if (!application.getBossId().equals(bossId)) {
            throw new BusinessException("无权更新此投递状态");
        }
        application.setStatus(status);
        application.setUpdateTime(LocalDateTime.now());
        applicationMapper.updateById(application);

        // 发送状态变更通知给求职者
        String[] statusNames = {"", "待查看", "已查看", "面试中", "不合适", "录用"};
        String title = "投递状态更新";
        String content = String.format("您投递的职位状态已更新为：%s",
                status >= 1 && status <= 5 ? statusNames[status] : "未知状态");
        notificationService.sendNotification(
                application.getJobSeekerId(),
                2, // 面试状态变更
                title,
                content,
                applicationId
        );

        log.info("更新投递状态成功，applicationId：{}，status：{}", applicationId, status);
    }

    @Override
    public boolean hasApplied(Long jobSeekerId, Long positionId) {
        LambdaQueryWrapper<Application> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Application::getJobSeekerId, jobSeekerId)
               .eq(Application::getPositionId, positionId);
        return applicationMapper.selectCount(wrapper) > 0;
    }
}
