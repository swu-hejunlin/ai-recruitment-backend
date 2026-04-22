package com.example.airecruitmentbackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 职位发布实体类
 */
@Data
@TableName("position")
public class Position {
    /**
     * 职位ID（主键，自增）
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 所属企业ID
     */
    private Long companyId;

    /**
     * 发布者(Boss/HR)ID
     */
    private Long bossId;

    /**
     * 职位名称
     */
    private String title;

    /**
     * 职位类别（如：后端开发）
     */
    private String category;

    /**
     * 工作城市
     */
    private String city;

    /**
     * 详细工作地址
     */
    private String address;

    /**
     * 最低薪资（K）
     */
    private Integer salaryMin;

    /**
     * 最高薪资（K）
     */
    private Integer salaryMax;

    /**
     * 最低学历要求：1-5
     */
    private Integer educationMin;

    /**
     * 最低工作年限要求
     */
    private Integer workYearsMin;

    /**
     * 岗位职责
     */
    private String description;

    /**
     * 任职要求
     */
    private String requirement;

    /**
     * 状态：1-招聘中，0-已关闭
     */
    private Integer status;

    /**
     * 职位福利标签（JSON数组）
     */
    private String tags;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 企业Logo（冗余字段，方便前端展示）
     */
    private String companyLogo;

    /**
     * 企业名称（冗余字段，方便前端展示）
     */
    private String companyName;

    /**
     * 是否已生成画像：0-否，1-是
     */
    private Integer profileGenerated;
}
