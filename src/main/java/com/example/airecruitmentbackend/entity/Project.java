package com.example.airecruitmentbackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;

/**
 * 项目经历实体类
 */
@Data
@TableName("project")
public class Project {
    /**
     * 项目ID（主键，自增）
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 求职者ID（外键）
     */
    private Long jobSeekerId;

    /**
     * 项目名称
     */
    private String projectName;

    /**
     * 项目角色
     */
    private String projectRole;

    /**
     * 项目开始时间
     */
    private LocalDate startDate;

    /**
     * 项目结束时间
     */
    private LocalDate endDate;

    /**
     * 项目描述
     */
    private String description;

    /**
     * 创建时间
     */
    private java.time.LocalDateTime createTime;

    /**
     * 更新时间
     */
    private java.time.LocalDateTime updateTime;
}