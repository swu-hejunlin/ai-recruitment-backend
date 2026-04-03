package com.example.airecruitmentbackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;

/**
 * 工作经验/实习经历实体类
 */
@Data
@TableName("experience")
public class Experience {
    /**
     * 经历ID（主键，自增）
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 求职者ID（外键）
     */
    private Long jobSeekerId;

    /**
     * 公司名称
     */
    private String companyName;

    /**
     * 公司所属行业
     */
    private String companyIndustry;

    /**
     * 职位
     */
    private String position;

    /**
     * 开始时间
     */
    private LocalDate startDate;

    /**
     * 结束时间（离职/结束时间）
     */
    private LocalDate endDate;

    /**
     * 工作/实习内容描述
     */
    private String description;

    /**
     * 是否为实习：0-否（正式工作），1-是
     */
    private Integer isInternship;

    /**
     * 创建时间
     */
    private java.time.LocalDateTime createTime;

    /**
     * 更新时间
     */
    private java.time.LocalDateTime updateTime;
}