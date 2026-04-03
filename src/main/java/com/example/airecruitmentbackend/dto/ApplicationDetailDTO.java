package com.example.airecruitmentbackend.dto;

import com.example.airecruitmentbackend.entity.Application;
import com.example.airecruitmentbackend.entity.Company;
import com.example.airecruitmentbackend.entity.JobSeeker;
import com.example.airecruitmentbackend.entity.Position;
import lombok.Data;

import java.util.List;

/**
 * 投递详情DTO
 * 包含投递记录、职位信息、公司信息、求职者信息
 */
@Data
public class ApplicationDetailDTO {
    /**
     * 投递记录
     */
    private Application application;

    /**
     * 职位信息
     */
    private Position position;

    /**
     * 公司信息
     */
    private Company company;

    /**
     * 求职者信息（Boss查看时返回）
     */
    private JobSeekerSimpleDTO jobSeeker;
}
