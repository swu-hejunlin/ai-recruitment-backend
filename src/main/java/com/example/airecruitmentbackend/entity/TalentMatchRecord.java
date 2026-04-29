package com.example.airecruitmentbackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("talent_match_record")
public class TalentMatchRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long bossId;
    private Long jobSeekerId;
    private Long positionId;
    private BigDecimal matchScore;
    private BigDecimal skillMatchRate;
    private BigDecimal experienceMatchRate;
    private BigDecimal educationMatchRate;
    private BigDecimal salaryMatchRate;
    private String matchDetails;
    private Integer isViewed;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
