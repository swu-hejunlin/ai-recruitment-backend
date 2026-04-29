package com.example.airecruitmentbackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 人才画像实体类
 * 存储人才的结构化画像信息
 */
@Data
@TableName("talent_profile")
public class TalentProfile {
    /**
     * 主键ID（自增）
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 技能标签列表（JSON格式）
     */
    private String skills;

    /**
     * 学历：1-高中及以下，2-大专，3-本科，4-硕士，5-博士
     */
    private String education;

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
     * 人才标签列表（JSON格式）
     */
    private String talentTags;

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
     * 匹配关键词（JSON格式）
     */
    private String matchKeywords;

    /**
     * AI综合评估
     */
    private String aiEvaluation;

    /**
     * 文本嵌入向量（JSON格式，用于语义相似度计算）
     */
    private String embeddingVector;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
