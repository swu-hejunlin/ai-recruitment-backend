package com.example.airecruitmentbackend.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

/**
 * 简历智能填充响应
 */
@Data
public class ResumeSmartFillResponse {
    /**
     * 操作结果状态
     */
    private boolean success;
    
    /**
     * 错误信息（如果操作失败）
     */
    private String errorMessage;
    
    // 基本信息
    /**
     * 姓名
     */
    private String name;
    
    /**
     * 性别：1-男，2-女
     */
    private Integer gender;
    
    /**
     * 年龄
     */
    private Integer age;
    
    /**
     * 手机号
     */
    private String phone;
    
    /**
     * 邮箱
     */
    private String email;
    
    /**
     * 所在城市
     */
    private String city;
    
    /**
     * 详细地址
     */
    private String address;
    
    // 工作信息
    /**
     * 工作年限
     */
    private Integer workYears;
    
    /**
     * 当前薪资（单位：K/月）
     */
    private BigDecimal currentSalary;
    
    /**
     * 期望薪资（单位：K/月）
     */
    private BigDecimal expectedSalary;
    
    /**
     * 当前状态：1-在职，2-离职，3-在读学生
     */
    private Integer currentStatus;
    
    // 技能
    /**
     * 技能标签列表
     */
    private List<String> skills;
    
    // 个人简介
    /**
     * 个人简介或自我评价
     */
    private String introduction;
    
    // 教育经历（可能返回多个）
    /**
     * 教育经历列表
     */
    private List<EducationItem> educations;
    
    // 工作经历（可能返回多个）
    /**
     * 工作经历列表
     */
    private List<ExperienceItem> experiences;
    
    // 项目经历（可能返回多个）
    /**
     * 项目经历列表
     */
    private List<ProjectItem> projects;
    
    // 解析置信度（用于提示用户）
    /**
     * 解析置信度（0-1）
     */
    private Double confidence;
    
    // 未解析到的字段（提示用户手动填写）
    /**
     * 未解析到的字段列表
     */
    private List<String> unfilledFields;
    
    /**
     * 教育经历项
     */
    @Data
    public static class EducationItem {
        /**
         * 学校名称
         */
        private String schoolName;
        
        /**
         * 专业
         */
        private String major;
        
        /**
         * 学历：1-高中及以下，2-大专，3-本科，4-硕士，5-博士
         */
        private Integer educationLevel;
        
        /**
         * 开始时间（YYYY-MM）
         */
        private String startDate;
        
        /**
         * 结束时间（YYYY-MM）
         */
        private String endDate;
        
        /**
         * 在校表现/主修课程（可选）
         */
        private String description;
    }
    
    /**
     * 工作经历项
     */
    @Data
    public static class ExperienceItem {
        /**
         * 公司名称
         */
        private String companyName;
        
        /**
         * 公司所属行业（可选）
         */
        private String companyIndustry;
        
        /**
         * 职位名称
         */
        private String position;
        
        /**
         * 开始时间（YYYY-MM）
         */
        private String startDate;
        
        /**
         * 结束时间（YYYY-MM或至今）
         */
        private String endDate;
        
        /**
         * 工作内容（可选）
         */
        private String description;
        
        /**
         * 是否为实习：0-否，1-是
         */
        private Integer isInternship;
    }
    
    /**
     * 项目经历项
     */
    @Data
    public static class ProjectItem {
        /**
         * 项目名称
         */
        private String projectName;
        
        /**
         * 项目角色（可选）
         */
        private String projectRole;
        
        /**
         * 开始时间（YYYY-MM）
         */
        private String startDate;
        
        /**
         * 结束时间（YYYY-MM）
         */
        private String endDate;
        
        /**
         * 项目描述（可选）
         */
        private String description;
    }
}
