package com.example.airecruitmentbackend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 求职者信息更新请求DTO
 */
@Data
public class JobSeekerUpdateRequest {

    /**
     * 求职者ID
     */
    @NotNull(message = "求职者ID不能为空")
    private Long id;

    /**
     * 姓名
     */
    @NotBlank(message = "姓名不能为空")
    private String name;

    /**
     * 性别：0-未知，1-男，2-女
     */
    @Min(value = 0, message = "性别值不正确")
    @Max(value = 2, message = "性别值不正确")
    private Integer gender;

    /**
     * 邮箱地址
     */
    private String email;

    /**
     * 年龄
     */
    @Min(value = 16, message = "年龄不能小于16岁")
    @Max(value = 100, message = "年龄不能大于100岁")
    private Integer age;

    /**
     * 学历层次：1-高中及以下，2-大专，3-本科，4-硕士，5-博士
     */
    @Min(value = 1, message = "学历层次值不正确")
    @Max(value = 5, message = "学历层次值不正确")
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
    @Min(value = 0, message = "工作年限不能为负数")
    private Integer workYears;

    /**
     * 当前薪资（万元/年）
     */
    @Min(value = 0, message = "当前薪资不能为负数")
    private BigDecimal currentSalary;

    /**
     * 期望薪资（万元/年）
     */
    @Min(value = 0, message = "期望薪资不能为负数")
    private BigDecimal expectedSalary;

    /**
     * 当前状态：1-在职，2-离职，3-在读学生
     */
    @Min(value = 1, message = "当前状态值不正确")
    @Max(value = 3, message = "当前状态值不正确")
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
}
