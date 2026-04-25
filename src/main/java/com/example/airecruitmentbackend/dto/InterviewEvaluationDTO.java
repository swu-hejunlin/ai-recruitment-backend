package com.example.airecruitmentbackend.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 面试评估结果DTO
 */
@Data
public class InterviewEvaluationDTO {

    /**
     * 评估ID
     */
    private Long id;

    /**
     * 面试ID
     */
    private Long interviewId;

    /**
     * 评估分数(0-100)
     */
    private Double score;

    /**
     * 评估文本(优势+不足)
     */
    private String evaluationText;

    /**
     * 语言表达分数
     */
    private Double languageScore;

    /**
     * 逻辑思维分数
     */
    private Double logicScore;

    /**
     * 专业能力分数
     */
    private Double professionalScore;

    /**
     * 改进建议
     */
    private String suggestions;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}