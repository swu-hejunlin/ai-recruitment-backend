package com.example.airecruitmentbackend.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 岗位画像响应DTO
 */
@Data
public class JobProfileResponse {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 岗位ID
     */
    private Long positionId;

    /**
     * 岗位名称
     */
    private String jobName;

    /**
     * 公司名称
     */
    private String companyName;

    /**
     * 技能标签列表
     */
    private List<String> skills;

    /**
     * 学历要求描述
     */
    private String educationRequire;

    /**
     * 学历要求等级
     */
    private Integer educationLevel;

    /**
     * 经验要求描述
     */
    private String experienceRequire;

    /**
     * 经验要求等级
     */
    private Integer experienceLevel;

    /**
     * 薪资范围
     */
    private String salaryRange;

    /**
     * 最低薪资
     */
    private BigDecimal salaryMin;

    /**
     * 最高薪资
     */
    private BigDecimal salaryMax;

    /**
     * 岗位标签列表
     */
    private List<String> jobTags;

    /**
     * 岗位描述摘要
     */
    private String descriptionSummary;

    /**
     * 工作职责摘要
     */
    private String responsibilitiesSummary;

    /**
     * 任职要求摘要
     */
    private String requirementsSummary;

    /**
     * 公司福利
     */
    private String companyBenefits;

    /**
     * 匹配关键词
     */
    private List<String> matchKeywords;

    /**
     * 创建时间
     */
    private String createdAt;
}
