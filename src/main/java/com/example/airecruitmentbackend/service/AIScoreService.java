package com.example.airecruitmentbackend.service;

import java.math.BigDecimal;

/**
 * AI评分服务接口
 */
public interface AIScoreService {
    /**
     * 计算投递的AI评分
     * @param jobSeekerId 求职者ID
     * @param positionId 职位ID
     * @return 评分结果
     */
    BigDecimal calculateApplicationScore(Long jobSeekerId, Long positionId);
}
