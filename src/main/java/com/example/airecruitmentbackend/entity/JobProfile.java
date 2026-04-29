package com.example.airecruitmentbackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 岗位画像实体类
 * 存储岗位的结构化画像信息
 */
@Data
@TableName("job_profile")
public class JobProfile {
    /**
     * 主键ID（自增）
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 岗位ID（对应数据库position_id字段）
     */
    private Long positionId;

    /**
     * 技能标签列表（JSON格式）
     */
    private String skills;

    /**
     * 学历要求：1-高中及以下，2-大专，3-本科，4-硕士，5-博士
     */
    private String educationRequire;

    /**
     * 经验要求：1-不限，2-1年以下，3-1-3年，4-3-5年，5-5-10年，6-10年以上
     */
    private String experienceRequire;

    /**
     * 最低薪资（K/月）
     */
    private BigDecimal salaryMin;

    /**
     * 最高薪资（K/月）
     */
    private BigDecimal salaryMax;

    /**
     * 岗位标签列表（JSON格式）
     */
    private String jobTags;

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
     * 匹配关键词（JSON格式）
     */
    private String matchKeywords;

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
