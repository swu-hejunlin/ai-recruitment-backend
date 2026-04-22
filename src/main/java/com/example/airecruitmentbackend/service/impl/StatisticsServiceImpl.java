package com.example.airecruitmentbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.airecruitmentbackend.dto.StatisticsResponse;
import com.example.airecruitmentbackend.entity.Application;
import com.example.airecruitmentbackend.entity.Position;
import com.example.airecruitmentbackend.entity.JobSeeker;
import com.example.airecruitmentbackend.mapper.ApplicationMapper;
import com.example.airecruitmentbackend.mapper.PositionMapper;
import com.example.airecruitmentbackend.mapper.JobSeekerMapper;
import com.example.airecruitmentbackend.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 数据统计Service实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final PositionMapper positionMapper;
    private final ApplicationMapper applicationMapper;
    private final JobSeekerMapper jobSeekerMapper;

    @Override
    public StatisticsResponse.SeekerStatistics getSeekerStatistics(Long userId) {
        log.info("获取求职者端统计数据，用户ID：{}", userId);

        StatisticsResponse.SeekerStatistics stats = new StatisticsResponse.SeekerStatistics();

        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime todayEnd = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);

        // 总职位数
        Long totalPositions = positionMapper.selectCount(new LambdaQueryWrapper<Position>()
                .eq(Position::getStatus, 1));
        stats.setTotalPositions(totalPositions);

        // 今日新增职位数
        Long todayNewPositions = positionMapper.selectCount(new LambdaQueryWrapper<Position>()
                .eq(Position::getStatus, 1)
                .ge(Position::getCreateTime, todayStart)
                .le(Position::getCreateTime, todayEnd));
        stats.setTodayNewPositions(todayNewPositions);

        // 热门城市分布TOP5
        List<StatisticsResponse.CityDistribution> hotCities = getHotCities(5);
        stats.setHotCities(hotCities);

        // 热门职位类别TOP5
        List<StatisticsResponse.CategoryDistribution> hotCategories = getHotCategories(5);
        stats.setHotCategories(hotCategories);

        // 如果用户已登录，获取其投递信息
        if (userId != null) {
            JobSeeker jobSeeker = jobSeekerMapper.selectOne(new LambdaQueryWrapper<JobSeeker>()
                    .eq(JobSeeker::getUserId, userId));

            if (jobSeeker != null) {
                // 我的投递数
                Long myApplications = applicationMapper.selectCount(new LambdaQueryWrapper<Application>()
                        .eq(Application::getJobSeekerId, jobSeeker.getId()));
                stats.setMyApplications(myApplications);

                // 我的面试邀请数
                Long myInterviews = applicationMapper.selectCount(new LambdaQueryWrapper<Application>()
                        .eq(Application::getJobSeekerId, jobSeeker.getId())
                        .eq(Application::getStatus, 3));
                stats.setMyInterviews(myInterviews);
            }
        }

        // 高薪职位占比（薪资>=20K的职位占比）
        Long highSalaryCount = positionMapper.selectCount(new LambdaQueryWrapper<Position>()
                .eq(Position::getStatus, 1)
                .ge(Position::getSalaryMin, 20));
        if (totalPositions != null && totalPositions > 0 && highSalaryCount != null) {
            BigDecimal highSalaryPercentage = BigDecimal.valueOf(highSalaryCount)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(totalPositions), 2, RoundingMode.HALF_UP);
            stats.setHighSalaryPercentage(highSalaryPercentage);
        } else {
            stats.setHighSalaryPercentage(BigDecimal.ZERO);
        }

        // 求职竞争指数（平均每个职位的投递数）
        Long totalApplications = applicationMapper.selectCount(null);
        if (totalPositions != null && totalPositions > 0 && totalApplications != null) {
            BigDecimal competitionIndex = BigDecimal.valueOf(totalApplications)
                    .divide(BigDecimal.valueOf(totalPositions), 2, RoundingMode.HALF_UP);
            stats.setCompetitionIndex(competitionIndex);
        } else {
            stats.setCompetitionIndex(BigDecimal.ZERO);
        }

        log.info("求职者统计数据获取成功，用户ID：{}", userId);
        return stats;
    }

    @Override
    public StatisticsResponse.BossStatistics getBossStatistics(Long userId) {
        log.info("获取HR端统计数据，用户ID：{}", userId);

        StatisticsResponse.BossStatistics stats = new StatisticsResponse.BossStatistics();

        // 我发布的职位数
        Long myPositions = positionMapper.selectCount(new LambdaQueryWrapper<Position>()
                .eq(Position::getBossId, userId));
        stats.setMyPositions(myPositions);

        // 查询我发布的职位ID列表
        List<Position> myPositionList = positionMapper.selectList(new LambdaQueryWrapper<Position>()
                .eq(Position::getBossId, userId)
                .select(Position::getId));
        List<Long> myPositionIds = myPositionList.stream()
                .map(Position::getId)
                .collect(Collectors.toList());

        if (myPositionIds.isEmpty()) {
            stats.setMyApplications(0L);
            stats.setPendingApplications(0L);
            stats.setInterviewingCount(0L);
            stats.setHiredCount(0L);
            stats.setRejectedCount(0L);
            stats.setConversionRate(BigDecimal.ZERO);
            stats.setPositionStats(Collections.emptyList());
            return stats;
        }

        // 我收到的投递数
        Long myApplications = applicationMapper.selectCount(new LambdaQueryWrapper<Application>()
                .in(Application::getPositionId, myPositionIds));
        stats.setMyApplications(myApplications);

        // 待处理投递数（状态1-待查看）
        Long pendingApplications = applicationMapper.selectCount(new LambdaQueryWrapper<Application>()
                .in(Application::getPositionId, myPositionIds)
                .eq(Application::getStatus, 1));
        stats.setPendingApplications(pendingApplications);

        // 面试中数（状态3）
        Long interviewingCount = applicationMapper.selectCount(new LambdaQueryWrapper<Application>()
                .in(Application::getPositionId, myPositionIds)
                .eq(Application::getStatus, 3));
        stats.setInterviewingCount(interviewingCount);

        // 已录用数（状态5）
        Long hiredCount = applicationMapper.selectCount(new LambdaQueryWrapper<Application>()
                .in(Application::getPositionId, myPositionIds)
                .eq(Application::getStatus, 5));
        stats.setHiredCount(hiredCount);

        // 不合适数（状态4）
        Long rejectedCount = applicationMapper.selectCount(new LambdaQueryWrapper<Application>()
                .in(Application::getPositionId, myPositionIds)
                .eq(Application::getStatus, 4));
        stats.setRejectedCount(rejectedCount);

        // 投递转化率（已录用/总投递）
        if (myApplications != null && myApplications > 0 && hiredCount != null) {
            BigDecimal conversionRate = BigDecimal.valueOf(hiredCount)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(myApplications), 2, RoundingMode.HALF_UP);
            stats.setConversionRate(conversionRate);
        } else {
            stats.setConversionRate(BigDecimal.ZERO);
        }

        // 各职位投递情况
        List<StatisticsResponse.PositionApplicationStat> positionStats = new ArrayList<>();
        for (Position position : myPositionList) {
            StatisticsResponse.PositionApplicationStat stat = new StatisticsResponse.PositionApplicationStat();
            stat.setPositionId(position.getId());
            stat.setPositionTitle(position.getTitle());

            Long appCount = applicationMapper.selectCount(new LambdaQueryWrapper<Application>()
                    .eq(Application::getPositionId, position.getId()));
            stat.setApplicationCount(appCount != null ? appCount : 0L);

            // 投递转化率
            if (appCount != null && appCount > 0) {
                Long hiredForPosition = applicationMapper.selectCount(new LambdaQueryWrapper<Application>()
                        .eq(Application::getPositionId, position.getId())
                        .eq(Application::getStatus, 5));
                if (hiredForPosition != null) {
                    BigDecimal posConvRate = BigDecimal.valueOf(hiredForPosition)
                            .multiply(BigDecimal.valueOf(100))
                            .divide(BigDecimal.valueOf(appCount), 2, RoundingMode.HALF_UP);
                    stat.setConversionRate(posConvRate);
                } else {
                    stat.setConversionRate(BigDecimal.ZERO);
                }
            } else {
                stat.setConversionRate(BigDecimal.ZERO);
            }

            positionStats.add(stat);
        }

        // 按投递数排序
        positionStats.sort((a, b) -> b.getApplicationCount().compareTo(a.getApplicationCount()));
        stats.setPositionStats(positionStats);

        log.info("HR统计数据获取成功，用户ID：{}", userId);
        return stats;
    }

    /**
     * 获取热门城市分布
     */
    private List<StatisticsResponse.CityDistribution> getHotCities(int limit) {
        List<Map<String, Object>> cityStats = positionMapper.groupByCity();

        Long total = positionMapper.selectCount(new LambdaQueryWrapper<Position>()
                .eq(Position::getStatus, 1));

        List<StatisticsResponse.CityDistribution> result = new ArrayList<>();
        for (Map<String, Object> stat : cityStats) {
            String city = (String) stat.get("city");
            Long count = ((Number) stat.get("count")).longValue();

            StatisticsResponse.CityDistribution dist = new StatisticsResponse.CityDistribution();
            dist.setCity(city);
            dist.setCount(count);
            if (total != null && total > 0) {
                dist.setPercentage(BigDecimal.valueOf(count)
                        .multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP)
                        .doubleValue());
            } else {
                dist.setPercentage(0.0);
            }
            result.add(dist);
        }

        return result.stream().limit(limit).collect(Collectors.toList());
    }

    /**
     * 获取热门职位类别分布
     */
    private List<StatisticsResponse.CategoryDistribution> getHotCategories(int limit) {
        List<Map<String, Object>> categoryStats = positionMapper.groupByCategory();

        Long total = positionMapper.selectCount(new LambdaQueryWrapper<Position>()
                .eq(Position::getStatus, 1));

        List<StatisticsResponse.CategoryDistribution> result = new ArrayList<>();
        for (Map<String, Object> stat : categoryStats) {
            String category = (String) stat.get("category");
            Long count = ((Number) stat.get("count")).longValue();

            StatisticsResponse.CategoryDistribution dist = new StatisticsResponse.CategoryDistribution();
            dist.setCategory(category);
            dist.setCount(count);
            if (total != null && total > 0) {
                dist.setPercentage(BigDecimal.valueOf(count)
                        .multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP)
                        .doubleValue());
            } else {
                dist.setPercentage(0.0);
            }
            result.add(dist);
        }

        return result.stream().limit(limit).collect(Collectors.toList());
    }
}
