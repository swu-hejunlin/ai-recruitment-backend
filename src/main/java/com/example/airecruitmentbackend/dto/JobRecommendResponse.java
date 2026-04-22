package com.example.airecruitmentbackend.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 岗位推荐响应DTO
 */
@Data
public class JobRecommendResponse {
    /**
     * 岗位ID
     */
    private Long positionId;

    /**
     * 岗位ID（与职位列表接口兼容）
     */
    private Long id;

    /**
     * 公司ID
     */
    private Long companyId;

    /**
     * 岗位名称
     */
    private String jobName;

    /**
     * 职位名称（与职位列表接口兼容）
     */
    private String title;

    /**
     * 公司名称
     */
    private String companyName;

    /**
     * 公司Logo
     */
    private String companyLogo;

    /**
     * 工作地点
     */
    private String city;

    /**
     * 详细工作地址
     */
    private String address;

    /**
     * 薪资范围
     */
    private String salaryRange;

    /**
     * 最低薪资（K）
     */
    private Integer salaryMin;

    /**
     * 最高薪资（K）
     */
    private Integer salaryMax;

    /**
     * 学历要求
     */
    private String educationRequire;

    /**
     * 最低学历要求
     */
    private Integer educationMin;

    /**
     * 经验要求
     */
    private String experienceRequire;

    /**
     * 最低工作年限要求
     */
    private Integer workYearsMin;

    /**
     * 职位类别
     */
    private String category;

    /**
     * 岗位标签
     */
    private List<String> jobTags;

    /**
     * 福利标签（JSON数组格式）
     */
    private String tags;

    /**
     * 岗位职责
     */
    private String description;

    /**
     * 任职要求
     */
    private String requirement;

    /**
     * 职位状态
     */
    private Integer status;

    /**
     * 匹配分数（0-100）
     */
    private BigDecimal matchScore;

    /**
     * 技能匹配率
     */
    private BigDecimal skillMatchRate;

    /**
     * 经验匹配率
     */
    private BigDecimal experienceMatchRate;

    /**
     * 学历匹配率
     */
    private BigDecimal educationMatchRate;

    /**
     * 薪资匹配率
     */
    private BigDecimal salaryMatchRate;

    /**
     * 匹配详情
     */
    private MatchDetails matchDetails;

    /**
     * 岗位描述摘要
     */
    private String descriptionSummary;

    /**
     * 是否已收藏
     */
    private Boolean isFavorite;

    /**
     * 创建时间
     */
    private String createdAt;

    /**
     * 创建时间（与职位列表接口兼容）
     */
    private String createTime;

    /**
     * 更新时间
     */
    private String updatedAt;

    /**
     * 匹配详情内部类
     */
    @Data
    public static class MatchDetails {
        /**
         * 匹配的技能列表
         */
        private List<String> matchedSkills;

        /**
         * 缺失的技能列表
         */
        private List<String> missingSkills;

        /**
         * 匹配说明
         */
        private String matchDescription;
    }
}
