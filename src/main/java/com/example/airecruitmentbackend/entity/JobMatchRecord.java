package com.example.airecruitmentbackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 岗位匹配记录实体类
 * 存储岗位与人才的匹配记录
 */
@Data
@TableName("job_match_record")
public class JobMatchRecord {
    /**
     * 主键ID（自增）
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 求职者ID
     */
    private Long userId;

    /**
     * 岗位ID（对应数据库position_id字段）
     */
    private Long positionId;

    /**
     * 匹配分数（0-100）
     */
    private BigDecimal matchScore;

    /**
     * 技能匹配率（0-100）
     */
    private BigDecimal skillMatchRate;

    /**
     * 经验匹配率（0-100）
     */
    private BigDecimal experienceMatchRate;

    /**
     * 学历匹配率（0-100）
     */
    private BigDecimal educationMatchRate;

    /**
     * 薪资匹配率（0-100）
     */
    private BigDecimal salaryMatchRate;

    /**
     * 匹配详情（JSON格式）
     */
    private String matchDetails;

    /**
     * 是否查看：0-否，1-是
     */
    private Integer isViewed;

    /**
     * 是否申请：0-否，1-是
     */
    private Integer isApplied;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
