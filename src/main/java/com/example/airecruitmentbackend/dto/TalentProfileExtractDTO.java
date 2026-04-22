package com.example.airecruitmentbackend.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 人才画像提取DTO（用于GLM模型返回结果）
 */
@Data
public class TalentProfileExtractDTO {
    /**
     * 技能标签列表
     */
    private List<String> skills;

    /**
     * 学历：1-高中及以下，2-大专，3-本科，4-硕士，5-博士
     */
    private Integer educationLevel;

    /**
     * 工作年限
     */
    private Integer workYears;

    /**
     * 期望薪资（K/月）
     */
    private BigDecimal salaryExpectation;

    /**
     * 当前薪资（K/月）
     */
    private BigDecimal currentSalary;

    /**
     * 人才标签列表
     */
    private List<String> talentTags;

    /**
     * 个人简介摘要
     */
    private String descriptionSummary;

    /**
     * 优势亮点摘要
     */
    private String strengthsSummary;

    /**
     * 职业目标
     */
    private String careerGoals;

    /**
     * 匹配关键词
     */
    private List<String> matchKeywords;
}
