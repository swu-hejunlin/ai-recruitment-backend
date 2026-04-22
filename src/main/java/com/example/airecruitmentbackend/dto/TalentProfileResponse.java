package com.example.airecruitmentbackend.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 人才画像响应DTO
 */
@Data
public class TalentProfileResponse {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 技能标签列表
     */
    private List<String> skills;

    /**
     * 学历描述
     */
    private String education;

    /**
     * 学历等级
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

    /**
     * AI综合评估
     */
    private String aiEvaluation;

    /**
     * 创建时间
     */
    private String createdAt;
}
