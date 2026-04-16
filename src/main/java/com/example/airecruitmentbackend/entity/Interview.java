package com.example.airecruitmentbackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 面试记录表
 */
@Data
@TableName("interview")
public class Interview {

    /**
     * 面试ID
     */
    @TableId(type = IdType.AUTO)
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
     * 职位ID
     */
    private Long positionId;

    /**
     * 企业ID
     */
    private Long companyId;

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
     * 面试状态：1-待确认，2-已确认，3-已拒绝，4-已完成
     */
    private Integer status;

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
