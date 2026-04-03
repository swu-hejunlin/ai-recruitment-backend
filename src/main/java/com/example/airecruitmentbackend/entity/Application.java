package com.example.airecruitmentbackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 投递记录实体类
 */
@Data
@TableName("application")
public class Application {
    /**
     * 投递ID（主键，自增）
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 求职者ID
     */
    private Long jobSeekerId;

    /**
     * 职位ID
     */
    private Long positionId;

    /**
     * 所属企业ID（冗余）
     */
    private Long companyId;

    /**
     * 接收投递的Boss/HR ID（冗余）
     */
    private Long bossId;

    /**
     * 投递状态：1-待查看，2-已查看，3-面试中，4-不合适，5-录用
     */
    private Integer status;

    /**
     * AI匹配分（0-100）
     */
    private BigDecimal aiScore;

    /**
     * 投递时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
