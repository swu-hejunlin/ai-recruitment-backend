package com.example.airecruitmentbackend.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 面试请求DTO
 */
@Data
public class InterviewRequest {

    /**
     * 关联投递ID
     */
    private Long applicationId;

    /**
     * 面试时间
     */
    private LocalDateTime interviewTime;

    /**
     * 面试类型：1-线下，2-线上，3-AI面试
     */
    private Integer interviewType;

    /**
     * 面试地址（线下）
     */
    private String interviewAddress;

    /**
     * 面试链接（线上）
     */
    private String interviewLink;

    /**
     * 备注
     */
    private String remark;
}
