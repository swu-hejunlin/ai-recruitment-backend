package com.example.airecruitmentbackend.dto;

import com.example.airecruitmentbackend.entity.Education;
import com.example.airecruitmentbackend.entity.Experience;
import com.example.airecruitmentbackend.entity.JobSeeker;
import com.example.airecruitmentbackend.entity.Project;
import lombok.Data;

import java.util.List;

/**
 * 求职者完整信息DTO
 * 包含基本信息、教育经历、工作/实习经历、项目经历
 */
@Data
public class JobSeekerDetailDTO {
    /**
     * 求职者基本信息
     */
    private JobSeeker jobSeeker;

    /**
     * 教育经历列表
     */
    private List<Education> educations;

    /**
     * 工作/实习经历列表
     */
    private List<Experience> experiences;

    /**
     * 项目经历列表
     */
    private List<Project> projects;
}