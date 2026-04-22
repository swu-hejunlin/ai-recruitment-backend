package com.example.airecruitmentbackend.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 岗位画像提取DTO（用于GLM模型返回结果）
 */
@Data
public class JobProfileExtractDTO {
    /**
     * 技能标签列表
     */
    private List<String> skills;

    /**
     * 学历要求：1-高中及以下，2-大专，3-本科，4-硕士，5-博士
     */
    private Integer educationLevel;

    /**
     * 经验要求：1-不限，2-1年以下，3-1-3年，4-3-5年，5-5-10年，6-10年以上
     */
    private Integer experienceLevel;

    /**
     * 最低薪资（K/月）
     */
    private BigDecimal salaryMin;

    /**
     * 最高薪资（K/月）
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
}
