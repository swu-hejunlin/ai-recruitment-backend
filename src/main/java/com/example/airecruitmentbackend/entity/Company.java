package com.example.airecruitmentbackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 企业信息实体类
 * 存储企业的详细信息
 */
@Data
@TableName("company")
public class Company {
    /**
     * 企业ID（主键，自增）
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联用户ID（企业HR）
     */
    private Long userId;

    /**
     * 企业名称
     */
    private String companyName;

    /**
     * 企业logo URL
     */
    private String logo;

    /**
     * 法人代表
     */
    private String legalPerson;

    /**
     * 所属行业
     */
    private String industry;

    /**
     * 企业规模：1-0-20人，2-20-99人，3-100-499人，4-500-999人，5-1000-9999人，6-10000人以上
     */
    private Integer scale;

    /**
     * 融资阶段：1-未融资，2-天使轮，3-A轮，4-B轮，5-C轮，6-D轮及以上，7-已上市，8-不需要融资
     */
    private Integer financingStage;

    /**
     * 所在城市
     */
    private String city;

    /**
     * 详细地址
     */
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
    private String website;

    /**
     * 企业简介
     */
    private String description;

    /**
     * 福利待遇（JSON数组格式）
     */
    private String welfare;

    /**
     * 营业执照URL
     */
    private String businessLicense;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
