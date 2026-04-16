package com.example.airecruitmentbackend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.airecruitmentbackend.common.Result;
import com.example.airecruitmentbackend.dto.ApplicationDetailDTO;
import com.example.airecruitmentbackend.dto.JobSeekerDetailDTO;
import com.example.airecruitmentbackend.dto.JobSeekerSimpleDTO;
import com.example.airecruitmentbackend.entity.Application;
import com.example.airecruitmentbackend.entity.Company;
import com.example.airecruitmentbackend.entity.Education;
import com.example.airecruitmentbackend.entity.Experience;
import com.example.airecruitmentbackend.entity.JobSeeker;
import com.example.airecruitmentbackend.entity.Position;
import com.example.airecruitmentbackend.entity.Project;
import com.example.airecruitmentbackend.exception.BusinessException;
import com.example.airecruitmentbackend.exception.ForbiddenException;
import com.example.airecruitmentbackend.mapper.CompanyMapper;
import com.example.airecruitmentbackend.mapper.EducationMapper;
import com.example.airecruitmentbackend.mapper.ExperienceMapper;
import com.example.airecruitmentbackend.mapper.JobSeekerMapper;
import com.example.airecruitmentbackend.mapper.PositionMapper;
import com.example.airecruitmentbackend.mapper.ProjectMapper;
import com.example.airecruitmentbackend.service.ApplicationService;
import com.example.airecruitmentbackend.service.JobSeekerService;
import com.example.airecruitmentbackend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 投递记录控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/application")
@RequiredArgsConstructor
public class ApplicationController extends BaseController {

    private final ApplicationService applicationService;
    private final JobSeekerService jobSeekerService;
    private final NotificationService notificationService;
    private final JobSeekerMapper jobSeekerMapper;
    private final CompanyMapper companyMapper;
    private final PositionMapper positionMapper;
    private final EducationMapper educationMapper;
    private final ExperienceMapper experienceMapper;
    private final ProjectMapper projectMapper;

    /**
     * 投递简历（仅求职者）
     */
    @PostMapping("/apply")
    public Result<Void> apply(@RequestParam("positionId") Long positionId) {
        Long userId = getCurrentUserId();
        
        // 角色校验：只有求职者(role=1)才能投递简历
        Integer role = getCurrentUserRole();
        if (role != 1) {
            throw new ForbiddenException("只有求职者才能投递简历");
        }
        
        // 获取jobSeekerId
        var jobSeeker = jobSeekerService.getByUserId(userId);
        if (jobSeeker == null) {
            throw new BusinessException("请先完善求职者信息");
        }
        applicationService.apply(jobSeeker.getId(), positionId);
        return Result.success("投递成功", null);
    }

    /**
     * Boss查询收到的投递列表（仅企业HR）
     */
    @GetMapping("/boss/list")
    public Result<Page<Application>> getBossApplications(
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        Long userId = getCurrentUserId();
        
        // 角色校验：只有企业HR(role=2)才能查询投递列表
        Integer role = getCurrentUserRole();
        if (role != 2) {
            throw new ForbiddenException("只有企业HR才能查看投递列表");
        }
        
        Page<Application> page = applicationService.getApplicationsByBoss(userId, status, pageNum, pageSize);
        return Result.success("查询成功", page);
    }

    /**
     * 求职者查询自己的投递列表（仅求职者）
     */
    @GetMapping("/seeker/list")
    public Result<Page<Application>> getSeekerApplications(
            @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        Long userId = getCurrentUserId();
        
        // 角色校验：只有求职者(role=1)才能查看自己的投递
        Integer role = getCurrentUserRole();
        if (role != 1) {
            throw new ForbiddenException("只有求职者才能查看投递记录");
        }
        
        var jobSeeker = jobSeekerService.getByUserId(userId);
        if (jobSeeker == null) {
            throw new BusinessException("请先完善求职者信息");
        }
        Page<Application> page = applicationService.getApplicationsByJobSeeker(jobSeeker.getId(), pageNum, pageSize);
        return Result.success("查询成功", page);
    }

    /**
     * 查看投递详情（含职位、公司信息）
     */
    @GetMapping("/detail")
    public Result<ApplicationDetailDTO> getApplicationDetail(@RequestParam("id") Long id) {
        Long userId = getCurrentUserId();
        Application application = applicationService.getById(id);
        if (application == null) {
            throw new BusinessException("投递记录不存在");
        }

        // 构建详情DTO
        ApplicationDetailDTO detailDTO = new ApplicationDetailDTO();
        detailDTO.setApplication(application);

        // 查询职位信息
        Position position = positionMapper.selectById(application.getPositionId());
        detailDTO.setPosition(position);

        // 查询公司信息
        Company company = companyMapper.selectById(application.getCompanyId());
        detailDTO.setCompany(company);

        // Boss查看时更新状态
        Integer role = getCurrentUserRole();
        if (role == 2 && application.getBossId().equals(userId)) {
            applicationService.readApplication(id, userId);
            application.setStatus(2); // 更新为已查看
        }

        return Result.success("查询成功", detailDTO);
    }

    /**
     * 根据投递记录查看职位信息
     */
    @GetMapping("/position")
    public Result<Position> getPositionByApplication(@RequestParam("id") Long id) {
        Application application = applicationService.getById(id);
        if (application == null) {
            throw new BusinessException("投递记录不存在");
        }
        Position position = positionMapper.selectById(application.getPositionId());
        if (position == null) {
            throw new BusinessException("职位不存在");
        }
        return Result.success("查询成功", position);
    }

    /**
     * 根据投递记录查看公司信息
     */
    @GetMapping("/company")
    public Result<Company> getCompanyByApplication(@RequestParam("id") Long id) {
        Application application = applicationService.getById(id);
        if (application == null) {
            throw new BusinessException("投递记录不存在");
        }
        Company company = companyMapper.selectById(application.getCompanyId());
        if (company == null) {
            throw new BusinessException("公司不存在");
        }
        return Result.success("查询成功", company);
    }

    /**
     * Boss查看求职者简要信息（简历）
     */
    @GetMapping("/job-seeker/simple")
    public Result<JobSeekerSimpleDTO> getJobSeekerSimpleByApplication(@RequestParam("id") Long id) {
        Long userId = getCurrentUserId();

        Application application = applicationService.getById(id);
        if (application == null) {
            throw new BusinessException("投递记录不存在");
        }
        // 校验是否是自己的投递
        if (!application.getBossId().equals(userId)) {
            throw new BusinessException("无权查看此投递的求职者信息");
        }

        JobSeeker jobSeeker = jobSeekerMapper.selectById(application.getJobSeekerId());
        if (jobSeeker == null) {
            throw new BusinessException("求职者不存在");
        }

        // 转换为简要DTO
        JobSeekerSimpleDTO dto = new JobSeekerSimpleDTO();
        dto.setId(jobSeeker.getId());
        dto.setName(jobSeeker.getName());
        dto.setGender(jobSeeker.getGender());
        dto.setAvatar(jobSeeker.getAvatar());
        dto.setPhone(jobSeeker.getPhone());
        dto.setEmail(jobSeeker.getEmail());
        dto.setAge(jobSeeker.getAge());
        dto.setWorkYears(jobSeeker.getWorkYears());
        dto.setCity(jobSeeker.getCity());
        dto.setIntroduction(jobSeeker.getIntroduction());
        dto.setSkills(jobSeeker.getSkills());
        dto.setResumeUrl(jobSeeker.getResumeUrl());
        dto.setCreateTime(jobSeeker.getCreateTime());

        return Result.success("查询成功", dto);
    }

    /**
     * Boss查看求职者完整在线简历（包含教育经历、工作经历、项目经历）
     */
    @GetMapping("/job-seeker/resume")
    public Result<JobSeekerDetailDTO> getJobSeekerResumeByApplication(@RequestParam("id") Long id) {
        Long userId = getCurrentUserId();

        Application application = applicationService.getById(id);
        if (application == null) {
            throw new BusinessException("投递记录不存在");
        }
        // 校验是否是自己的投递
        if (!application.getBossId().equals(userId)) {
            throw new ForbiddenException("无权查看此投递的求职者信息");
        }

        JobSeeker jobSeeker = jobSeekerMapper.selectById(application.getJobSeekerId());
        if (jobSeeker == null) {
            throw new BusinessException("求职者不存在");
        }

        // 构建完整简历DTO
        JobSeekerDetailDTO dto = new JobSeekerDetailDTO();
        dto.setJobSeeker(jobSeeker);

        // 查询教育经历
        List<Education> educations = educationMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Education>()
                        .eq(Education::getJobSeekerId, jobSeeker.getId())
                        .orderByDesc(Education::getEndDate));
        dto.setEducations(educations);

        // 查询工作/实习经历
        List<Experience> experiences = experienceMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Experience>()
                        .eq(Experience::getJobSeekerId, jobSeeker.getId())
                        .orderByDesc(Experience::getEndDate));
        dto.setExperiences(experiences);

        // 查询项目经历
        List<Project> projects = projectMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Project>()
                        .eq(Project::getJobSeekerId, jobSeeker.getId())
                        .orderByDesc(Project::getEndDate));
        dto.setProjects(projects);

        // 标记简历为已查看
        applicationService.readApplication(id, userId);

        return Result.success("查询成功", dto);
    }

    /**
     * 标记简历为已查看（仅Boss）
     */
    @PutMapping("/read")
    public Result<Void> readApplication(@RequestParam("id") Long id) {
        Long userId = getCurrentUserId();
        applicationService.readApplication(id, userId);
        return Result.success("已查看", null);
    }

    /**
     * 更新投递状态（仅Boss）
     */
    @PutMapping("/status")
    public Result<Void> updateStatus(@RequestParam("id") Long id, @RequestParam("status") Integer status) {
        Long userId = getCurrentUserId();
        applicationService.updateStatus(id, status, userId);
        return Result.success("更新成功", null);
    }
}
