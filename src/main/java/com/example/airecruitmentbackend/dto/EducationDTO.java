package com.example.airecruitmentbackend.dto;

import com.example.airecruitmentbackend.entity.Education;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/**
 * 教育经历DTO - 接收前端年月格式数据
 */
@Data
public class EducationDTO {
    private Long id;

    /**
     * 求职者ID
     */
    private Long jobSeekerId;

    /**
     * 学校名称
     */
    @NotBlank(message = "学校名称不能为空")
    private String schoolName;

    /**
     * 专业
     */
    private String major;

    /**
     * 学历：1-高中及以下，2-大专，3-本科，4-硕士，5-博士
     */
    @NotNull(message = "学历不能为空")
    private Integer educationLevel;

    /**
     * 入学时间（格式：yyyy-MM-dd）
     */
    private LocalDate startDate;

    /**
     * 毕业时间（格式：yyyy-MM-dd）
     */
    private LocalDate endDate;

    /**
     * 在校表现/主要课程描述
     */
    private String description;

    /**
     * 转换为Education实体
     */
    public Education toEntity() {
        Education education = new Education();
        education.setId(this.id);
        education.setJobSeekerId(this.jobSeekerId);
        education.setSchoolName(this.schoolName);
        education.setMajor(this.major);
        education.setEducationLevel(this.educationLevel);
        education.setStartDate(this.startDate);
        education.setEndDate(this.endDate);
        education.setDescription(this.description);
        return education;
    }
}
