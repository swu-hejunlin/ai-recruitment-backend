package com.example.airecruitmentbackend.service;

import com.example.airecruitmentbackend.dto.StatisticsResponse;

/**
 * 数据统计Service接口
 */
public interface StatisticsService {

    /**
     * 获取求职者端统计数据
     */
    StatisticsResponse.SeekerStatistics getSeekerStatistics(Long userId);

    /**
     * 获取HR端统计数据
     */
    StatisticsResponse.BossStatistics getBossStatistics(Long userId);
}
