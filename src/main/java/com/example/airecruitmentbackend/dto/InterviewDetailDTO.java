package com.example.airecruitmentbackend.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 面试详情DTO
 */
@Data
public class InterviewDetailDTO {

    /**
     * 面试ID
     */
    private Long id;

    /**
     * 关联投递ID
     */
    private Long applicationId;

    /**
     * 求职者ID
     */
    private Long jobSeekerId;

    /**
     * 求职者姓名
     */
    private String jobSeekerName;

    /**
     * 职位ID
     */
    private Long positionId;

    /**
     * 职位名称
     */
    private String positionTitle;

    /**
     * 企业ID
     */
    private Long companyId;

    /**
     * 企业名称
     */
    private String companyName;

    /**
     * 面试时间
     */
    private LocalDateTime interviewTime;

    /**
     * 面试类型：1-线下，2-线上，3-AI面试
     */
    private Integer interviewType;

    /**
     * 面试类型名称
     */
    private String interviewTypeName;

    /**
     * 面试地址（线下）
     */
    private String interviewAddress;

    /**
     * 面试链接（线上）
     */
    private String interviewLink;

    /**
     * 面试状态：1-待确认，2-已确认，3-已拒绝，4-已完成
     */
    private Integer status;

    /**
     * 面试状态名称
     */
    private String statusName;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
