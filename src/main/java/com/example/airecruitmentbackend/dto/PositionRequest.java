package com.example.airecruitmentbackend.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * 职位请求DTO
 */
@Data
public class PositionRequest {

    /**
     * 职位ID（更新时必填）
     */
    private Long id;

    /**
     * 所属企业ID
     */
    @NotNull(message = "企业ID不能为空")
    private Long companyId;

    /**
     * 职位名称
     */
    @NotBlank(message = "职位名称不能为空")
    @Size(max = 100, message = "职位名称最大100字符")
    private String title;

    /**
     * 职位类别
     */
    @NotBlank(message = "职位类别不能为空")
    @Size(max = 50, message = "职位类别最大50字符")
    private String category;

    /**
     * 工作城市
     */
    @NotBlank(message = "工作城市不能为空")
    @Size(max = 50, message = "工作城市最大50字符")
    private String city;

    /**
     * 详细工作地址
     */
    @Size(max = 200, message = "详细地址最大200字符")
    private String address;

    /**
     * 最低薪资（K）
     */
    @Min(value = 0, message = "最低薪资不能为负数")
    private Integer salaryMin;

    /**
     * 最高薪资（K）
     */
    @Min(value = 0, message = "最高薪资不能为负数")
    private Integer salaryMax;

    /**
     * 最低学历要求：1-5
     */
    @Min(value = 1, message = "学历要求值不正确")
    @Max(value = 5, message = "学历要求值不正确")
    private Integer educationMin;

    /**
     * 最低工作年限要求
     */
    @Min(value = 0, message = "工作年限要求不能为负数")
    private Integer workYearsMin;

    /**
     * 岗位职责
     */
    @NotBlank(message = "岗位职责不能为空")
    private String description;

    /**
     * 任职要求
     */
    @NotBlank(message = "任职要求不能为空")
    private String requirement;

    /**
     * 职位福利标签（JSON数组）
     */
    private String tags;
}
