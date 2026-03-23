package com.example.airecruitmentbackend.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * 企业信息更新请求DTO
 */
@Data
public class CompanyUpdateRequest {

    /**
     * 企业ID
     */
    @NotNull(message = "企业ID不能为空")
    private Long id;

    /**
     * 企业名称
     */
    @NotBlank(message = "企业名称不能为空")
    @Size(max = 100, message = "企业名称长度不能超过100个字符")
    private String companyName;

    /**
     * 法人代表
     */
    @Size(max = 50, message = "法人代表长度不能超过50个字符")
    private String legalPerson;

    /**
     * 所属行业
     */
    private String industry;

    /**
     * 企业规模：1-0-20人，2-20-99人，3-100-499人，4-500-999人，5-1000-9999人，6-10000人以上
     */
    @Min(value = 1, message = "企业规模值不正确")
    @Max(value = 6, message = "企业规模值不正确")
    private Integer scale;

    /**
     * 融资阶段：1-未融资，2-天使轮，3-A轮，4-B轮，5-C轮，6-D轮及以上，7-已上市，8-不需要融资
     */
    @Min(value = 1, message = "融资阶段值不正确")
    @Max(value = 8, message = "融资阶段值不正确")
    private Integer financingStage;

    /**
     * 所在城市
     */
    private String city;

    /**
     * 详细地址
     */
    @Size(max = 200, message = "地址长度不能超过200个字符")
    private String address;

    /**
     * 企业邮箱
     */
    private String email;

    /**
     * 企业联系电话
     */
    private String phone;

    /**
     * 企业官网
     */
    @Size(max = 200, message = "官网地址长度不能超过200个字符")
    private String website;

    /**
     * 企业简介
     */
    private String description;

    /**
     * 福利待遇（JSON数组格式）
     */
    private String welfare;
}
