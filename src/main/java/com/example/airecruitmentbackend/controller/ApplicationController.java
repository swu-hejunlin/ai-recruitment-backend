package com.example.airecruitmentbackend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.airecruitmentbackend.common.Result;
import com.example.airecruitmentbackend.dto.ApplicationDetailDTO;
import com.example.airecruitmentbackend.dto.JobSeekerSimpleDTO;
import com.example.airecruitmentbackend.entity.Application;
import com.example.airecruitmentbackend.entity.Company;
import com.example.airecruitmentbackend.entity.JobSeeker;
import com.example.airecruitmentbackend.entity.Position;
import com.example.airecruitmentbackend.entity.User;
import com.example.airecruitmentbackend.exception.BusinessException;
import com.example.airecruitmentbackend.mapper.CompanyMapper;
import com.example.airecruitmentbackend.mapper.JobSeekerMapper;
import com.example.airecruitmentbackend.mapper.PositionMapper;
import com.example.airecruitmentbackend.mapper.UserMapper;
import com.example.airecruitmentbackend.service.ApplicationService;
import com.example.airecruitmentbackend.service.JobSeekerService;
import com.example.airecruitmentbackend.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 投递记录控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/application")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;
    private final JobSeekerService jobSeekerService;
    private final JwtUtil jwtUtil;
    private final UserMapper userMapper;
    private final JobSeekerMapper jobSeekerMapper;
    private final CompanyMapper companyMapper;
    private final PositionMapper positionMapper;

    /**
     * 投递简历
     */
    @PostMapping("/apply")
    public Result<Void> apply(HttpServletRequest request,
                              @RequestParam("positionId") Long positionId) {
        Long userId = jwtUtil.getUserIdFromToken(request);
        // 校验是否为求职者角色
        validateJobSeekerRole(userId);
        // 获取jobSeekerId
        var jobSeeker = jobSeekerService.getByUserId(userId);
        if (jobSeeker == null) {
            throw new BusinessException("请先完善求职者信息");
        }
        applicationService.apply(jobSeeker.getId(), positionId);
        return Result.success("投递成功", null);
    }

    /**
     * Boss查询收到的投递列表
     */
    @GetMapping("/boss/list")
    public Result<Page<Application>> getBossApplications(
            HttpServletRequest request,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        Long userId = jwtUtil.getUserIdFromToken(request);
        validateBossRole(userId);
        Page<Application> page = applicationService.getApplicationsByBoss(userId, status, pageNum, pageSize);
        return Result.success("查询成功", page);
    }

    /**
     * 求职者查询自己的投递列表
     */
    @GetMapping("/seeker/list")
    public Result<Page<Application>> getSeekerApplications(
            HttpServletRequest request,
            @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        Long userId = jwtUtil.getUserIdFromToken(request);
        validateJobSeekerRole(userId);
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
    public Result<ApplicationDetailDTO> getApplicationDetail(
            HttpServletRequest request,
            @RequestParam("id") Long id) {
        Long userId = jwtUtil.getUserIdFromToken(request);
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
        User user = userMapper.selectById(userId);
        if (user != null && user.getRole() == 2 && application.getBossId().equals(userId)) {
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
     * Boss查看求职者信息（简历）
     */
    @GetMapping("/job-seeker")
    public Result<JobSeekerSimpleDTO> getJobSeekerByApplication(
            HttpServletRequest request,
            @RequestParam("id") Long id) {
        Long userId = jwtUtil.getUserIdFromToken(request);
        validateBossRole(userId);

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
     * 查看简历（标记为已查看）
     */
    @PutMapping("/read")
    public Result<Void> readApplication(HttpServletRequest request,
                                         @RequestParam("id") Long id) {
        Long userId = jwtUtil.getUserIdFromToken(request);
        validateBossRole(userId);
        applicationService.readApplication(id, userId);
        return Result.success("已查看", null);
    }

    /**
     * 更新投递状态
     */
    @PutMapping("/status")
    public Result<Void> updateStatus(HttpServletRequest request,
                                      @RequestParam("id") Long id,
                                      @RequestParam("status") Integer status) {
        Long userId = jwtUtil.getUserIdFromToken(request);
        validateBossRole(userId);
        applicationService.updateStatus(id, status, userId);
        return Result.success("更新成功", null);
    }

    /**
     * 校验是否为Boss角色
     */
    private void validateBossRole(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (user.getRole() != 2) {
            throw new BusinessException("只有Boss角色才能执行此操作");
        }
    }

    /**
     * 校验是否为求职者角色
     */
    private void validateJobSeekerRole(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (user.getRole() != 1) {
            throw new BusinessException("只有求职者才能执行此操作");
        }
    }
}
