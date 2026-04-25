package com.example.airecruitmentbackend.controller;

import com.example.airecruitmentbackend.common.Result;
import com.example.airecruitmentbackend.dto.StatisticsResponse;
import com.example.airecruitmentbackend.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 数据统计控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticsController extends BaseController {

    private final StatisticsService statisticsService;

    /**
     * 获取求职者端统计数据（需登录）
     */
    @GetMapping("/seeker")
    public Result<StatisticsResponse.SeekerStatistics> getSeekerStatistics() {
        Long userId = getCurrentUserId();
        log.info("获取求职者端统计数据，用户ID：{}", userId);
        StatisticsResponse.SeekerStatistics stats = statisticsService.getSeekerStatistics(userId);
        return Result.success("获取成功", stats);
    }

    /**
     * 获取HR端统计数据（需登录，仅HR）
     */
    @GetMapping("/boss")
    public Result<StatisticsResponse.BossStatistics> getBossStatistics() {
        Integer role = getCurrentUserRole();
        if (role != 2) {
            return Result.error("只有企业HR才能查看此统计数据");
        }

        Long userId = getCurrentUserId();
        log.info("获取HR端统计数据，用户ID：{}", userId);
        StatisticsResponse.BossStatistics stats = statisticsService.getBossStatistics(userId);
        return Result.success("获取成功", stats);
    }

    /**
     * 获取词云数据
     */
    @GetMapping("/wordcloud")
    public Result<StatisticsResponse.WordCloudResponse> getWordCloud() {
        log.info("获取词云数据");
        StatisticsResponse.WordCloudResponse data = statisticsService.getWordCloudData();
        return Result.success("获取成功", data);
    }
}
