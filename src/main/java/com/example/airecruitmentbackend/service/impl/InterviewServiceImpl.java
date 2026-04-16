package com.example.airecruitmentbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.airecruitmentbackend.dto.InterviewRequest;
import com.example.airecruitmentbackend.dto.InterviewDetailDTO;
import com.example.airecruitmentbackend.entity.Interview;
import com.example.airecruitmentbackend.entity.Application;
import com.example.airecruitmentbackend.entity.JobSeeker;
import com.example.airecruitmentbackend.entity.Position;
import com.example.airecruitmentbackend.entity.Company;
import com.example.airecruitmentbackend.entity.User;
import com.example.airecruitmentbackend.exception.BusinessException;
import com.example.airecruitmentbackend.mapper.InterviewMapper;
import com.example.airecruitmentbackend.mapper.ApplicationMapper;
import com.example.airecruitmentbackend.mapper.JobSeekerMapper;
import com.example.airecruitmentbackend.mapper.PositionMapper;
import com.example.airecruitmentbackend.mapper.CompanyMapper;
import com.example.airecruitmentbackend.mapper.UserMapper;
import com.example.airecruitmentbackend.service.InterviewService;
import com.example.airecruitmentbackend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 面试服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InterviewServiceImpl extends ServiceImpl<InterviewMapper, Interview> implements InterviewService {

    private final InterviewMapper interviewMapper;
    private final ApplicationMapper applicationMapper;
    private final JobSeekerMapper jobSeekerMapper;
    private final PositionMapper positionMapper;
    private final CompanyMapper companyMapper;
    private final UserMapper userMapper;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public Long createInterview(InterviewRequest request, Long bossId) {
        // 1. 验证投递记录是否存在
        Application application = applicationMapper.selectById(request.getApplicationId());
        if (application == null) {
            throw new BusinessException(400, "投递记录不存在");
        }

        // 2. 验证企业HR是否有权限
        Company company = companyMapper.selectById(application.getCompanyId());
        if (company == null || !company.getUserId().equals(bossId)) {
            throw new BusinessException(403, "无权限操作此面试");
        }

        // 3. 检查是否已存在面试记录
        Interview existingInterview = interviewMapper.selectOne(new LambdaQueryWrapper<Interview>()
                .eq(Interview::getApplicationId, request.getApplicationId()));
        if (existingInterview != null) {
            throw new BusinessException(400, "该投递已存在面试记录");
        }

        // 4. 创建面试记录
        Interview interview = new Interview();
        interview.setApplicationId(request.getApplicationId());
        interview.setJobSeekerId(application.getJobSeekerId());
        interview.setPositionId(application.getPositionId());
        interview.setCompanyId(application.getCompanyId());
        interview.setInterviewTime(request.getInterviewTime());
        interview.setInterviewType(request.getInterviewType());
        interview.setInterviewAddress(request.getInterviewAddress());
        interview.setInterviewLink(request.getInterviewLink());
        interview.setStatus(1); // 待确认
        interview.setRemark(request.getRemark());

        interviewMapper.insert(interview);
        log.info("创建面试邀请成功：interviewId={}, applicationId={}, bossId={}",
                interview.getId(), request.getApplicationId(), bossId);

        // 5. 更新投递状态为面试中
        application.setStatus(3); // 面试中
        applicationMapper.updateById(application);

        // 6. 发送面试邀请通知给求职者
        JobSeeker jobSeeker = jobSeekerMapper.selectById(application.getJobSeekerId());
        Position position = positionMapper.selectById(application.getPositionId());

        if (jobSeeker != null && position != null && company != null) {
            // 获取求职者的用户ID
            User jobSeekerUser = userMapper.selectOne(new LambdaQueryWrapper<User>()
                    .eq(User::getId, application.getJobSeekerId()));
            if (jobSeekerUser != null) {
                String title = "面试邀请通知";
                String content = String.format("您投递的职位【%s】已收到面试邀请，面试时间：%s，面试类型：%s",
                        position.getTitle(), interview.getInterviewTime(), getInterviewTypeName(interview.getInterviewType()));
                notificationService.sendNotification(jobSeekerUser.getId(), 3, title, content, interview.getId());
            }
        }

        return interview.getId();
    }

    @Override
    @Transactional
    public boolean updateInterviewStatus(Long interviewId, Integer status) {
        // 验证状态值
        if (!List.of(2, 3, 4).contains(status)) {
            throw new BusinessException(400, "无效的面试状态");
        }

        Interview interview = interviewMapper.selectById(interviewId);
        if (interview == null) {
            throw new BusinessException(400, "面试记录不存在");
        }

        // 更新面试状态
        interview.setStatus(status);
        int result = interviewMapper.updateById(interview);

        // 如果面试完成，更新投递状态
        if (status == 4) { // 已完成
            Application application = applicationMapper.selectById(interview.getApplicationId());
            if (application != null) {
                application.setStatus(5); // 录用
                applicationMapper.updateById(application);
            }
        }

        // 发送面试状态更新通知
        Application application = applicationMapper.selectById(interview.getApplicationId());
        Position position = positionMapper.selectById(interview.getPositionId());
        Company company = companyMapper.selectById(interview.getCompanyId());

        if (application != null && position != null && company != null) {
            // 通知求职者
            User jobSeekerUser = userMapper.selectOne(new LambdaQueryWrapper<User>()
                    .eq(User::getId, interview.getJobSeekerId()));
            if (jobSeekerUser != null) {
                String title = "面试状态更新通知";
                String content = String.format("您的面试【%s】状态已更新为：%s",
                        position.getTitle(), getStatusName(status));
                notificationService.sendNotification(jobSeekerUser.getId(), 2, title, content, interview.getId());
            }

            // 通知企业HR
            User bossUser = userMapper.selectOne(new LambdaQueryWrapper<User>()
                    .eq(User::getId, company.getUserId()));
            if (bossUser != null) {
                JobSeeker jobSeeker = jobSeekerMapper.selectById(interview.getJobSeekerId());
                String title = "面试状态更新通知";
                String content = String.format("求职者【%s】的面试状态已更新为：%s",
                        jobSeeker != null ? jobSeeker.getName() : "未知", getStatusName(status));
                notificationService.sendNotification(bossUser.getId(), 2, title, content, interview.getId());
            }
        }

        log.info("更新面试状态成功：interviewId={}, status={}", interviewId, status);
        return result > 0;
    }

    @Override
    public List<InterviewDetailDTO> getCompanyInterviews(Long companyId) {
        List<Interview> interviews = interviewMapper.selectList(new LambdaQueryWrapper<Interview>()
                .eq(Interview::getCompanyId, companyId)
                .orderByDesc(Interview::getCreateTime));

        return interviews.stream()
                .map(this::convertToDetailDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<InterviewDetailDTO> getJobSeekerInterviews(Long jobSeekerId) {
        List<Interview> interviews = interviewMapper.selectList(new LambdaQueryWrapper<Interview>()
                .eq(Interview::getJobSeekerId, jobSeekerId)
                .orderByDesc(Interview::getCreateTime));

        return interviews.stream()
                .map(this::convertToDetailDTO)
                .collect(Collectors.toList());
    }

    @Override
    public InterviewDetailDTO getInterviewDetail(Long interviewId) {
        Interview interview = interviewMapper.selectById(interviewId);
        if (interview == null) {
            throw new BusinessException(400, "面试记录不存在");
        }

        return convertToDetailDTO(interview);
    }

    @Override
    @Transactional
    public boolean deleteInterview(Long interviewId, Long bossId) {
        Interview interview = interviewMapper.selectById(interviewId);
        if (interview == null) {
            throw new BusinessException(400, "面试记录不存在");
        }

        // 验证企业HR是否有权限
        Company company = companyMapper.selectById(interview.getCompanyId());
        if (company == null || !company.getUserId().equals(bossId)) {
            throw new BusinessException(403, "无权限删除此面试");
        }

        // 删除面试记录
        int result = interviewMapper.deleteById(interviewId);

        // 恢复投递状态
        Application application = applicationMapper.selectById(interview.getApplicationId());
        if (application != null) {
            application.setStatus(2); // 已查看
            applicationMapper.updateById(application);
        }

        log.info("删除面试记录成功：interviewId={}, bossId={}", interviewId, bossId);
        return result > 0;
    }

    /**
     * 转换为面试详情DTO
     */
    private InterviewDetailDTO convertToDetailDTO(Interview interview) {
        InterviewDetailDTO dto = new InterviewDetailDTO();
        dto.setId(interview.getId());
        dto.setApplicationId(interview.getApplicationId());
        dto.setJobSeekerId(interview.getJobSeekerId());
        dto.setPositionId(interview.getPositionId());
        dto.setCompanyId(interview.getCompanyId());
        dto.setInterviewTime(interview.getInterviewTime());
        dto.setInterviewType(interview.getInterviewType());
        dto.setInterviewAddress(interview.getInterviewAddress());
        dto.setInterviewLink(interview.getInterviewLink());
        dto.setStatus(interview.getStatus());
        dto.setRemark(interview.getRemark());
        dto.setCreateTime(interview.getCreateTime());
        dto.setUpdateTime(interview.getUpdateTime());

        // 获取求职者信息
        JobSeeker jobSeeker = jobSeekerMapper.selectById(interview.getJobSeekerId());
        if (jobSeeker != null) {
            dto.setJobSeekerName(jobSeeker.getName());
        }

        // 获取职位信息
        Position position = positionMapper.selectById(interview.getPositionId());
        if (position != null) {
            dto.setPositionTitle(position.getTitle());
        }

        // 获取企业信息
        Company company = companyMapper.selectById(interview.getCompanyId());
        if (company != null) {
            dto.setCompanyName(company.getCompanyName());
        }

        // 设置面试类型名称
        dto.setInterviewTypeName(getInterviewTypeName(interview.getInterviewType()));

        // 设置面试状态名称
        dto.setStatusName(getStatusName(interview.getStatus()));

        return dto;
    }

    /**
     * 获取面试类型名称
     */
    private String getInterviewTypeName(Integer type) {
        return switch (type) {
            case 1 -> "线下面试";
            case 2 -> "线上面试";
            case 3 -> "AI面试";
            default -> "未知类型";
        };
    }

    /**
     * 获取面试状态名称
     */
    private String getStatusName(Integer status) {
        return switch (status) {
            case 1 -> "待确认";
            case 2 -> "已确认";
            case 3 -> "已拒绝";
            case 4 -> "已完成";
            default -> "未知状态";
        };
    }
}
