package com.example.airecruitmentbackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 求职者信息实体类
 * 存储求职者的详细个人信息
 */
@Data
@TableName("job_seeker")
public class JobSeeker {
    /**
     * 求职者ID（主键，自增）
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联用户ID
     */
    private Long userId;

    /**
     * 姓名
     */
    private String name;

    /**
     * 性别：0-未知，1-男，2-女
     */
    private Integer gender;

    /**
     * 头像URL
     */
    private String avatar;

    /**
     * 邮箱地址
     */
    private String email;

    /**
     * 年龄
     */
    private Integer age;

    /**
     * 学历层次：1-高中及以下，2-大专，3-本科，4-硕士，5-博士
     */
    private Integer educationLevel;

    /**
     * 毕业院校
     */
    private String graduateSchool;

    /**
     * 专业
     */
    private String major;

    /**
     * 工作年限（年）
     */
    private Integer workYears;

    /**
     * 当前薪资（万元/年）
     */
    private BigDecimal currentSalary;

    /**
     * 期望薪资（万元/年）
     */
    private BigDecimal expectedSalary;

    /**
     * 当前状态：1-在职，2-离职，3-在读学生
     */
    private Integer currentStatus;

    /**
     * 所在城市
     */
    private String city;

    /**
     * 详细地址
     */
    private String address;

    /**
     * 个人简介
     */
    private String introduction;

    /**
     * 技能标签（JSON数组格式）
     */
    private String skills;

    /**
     * 简历附件URL
     */
    private String resumeUrl;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
