package com.example.airecruitmentbackend.dto;

import lombok.Data;
import java.util.List;

/**
 * 简历分析响应
 */
@Data
public class ResumeAnalysisResponse {
    /**
     * 分析结果状态
     */
    private boolean success;
    
    /**
     * 错误信息（如果分析失败）
     */
    private String errorMessage;
    
    /**
     * 简历整体评级（1-5，5为最高）
     */
    private int overallRating;
    
    /**
     * 评级描述
     */
    private String ratingDescription;
    
    /**
     * 个人亮点
     */
    private List<String> highlights;
    
    /**
     * 存在的不足
     */
    private List<String> weaknesses;
    
    /**
     * 改进建议
     */
    private List<String> suggestions;
    
    /**
     * 技能评估
     */
    private List<SkillEvaluation> skillEvaluations;
    
    /**
     * 技能评估类
     */
    @Data
    public static class SkillEvaluation {
        private String skillName;
        private int proficiencyLevel; // 1-5，5为最高
        private String evaluation;
    }
}