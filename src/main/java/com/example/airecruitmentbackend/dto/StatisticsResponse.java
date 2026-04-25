package com.example.airecruitmentbackend.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 统计数据响应DTO
 */
@Data
public class StatisticsResponse {

    /**
     * 总职位数
     */
    private Long totalPositions;

    /**
     * 今日新增职位数
     */
    private Long todayNewPositions;

    /**
     * 总投递数
     */
    private Long totalApplications;

    /**
     * 今日新增投递数
     */
    private Long todayNewApplications;

    /**
     * 热门城市分布
     */
    private List<CityDistribution> cityDistribution;

    /**
     * 薪资分布
     */
    private List<SalaryDistribution> salaryDistribution;

    /**
     * 职位类别分布
     */
    private List<CategoryDistribution> categoryDistribution;

    /**
     * 投递状态分布
     */
    private Map<Integer, Long> applicationStatusDistribution;

    /**
     * 城市分布内部类
     */
    @Data
    public static class CityDistribution {
        private String city;
        private Long count;
        private Double percentage;
    }

    /**
     * 薪资分布内部类
     */
    @Data
    public static class SalaryDistribution {
        private String range;
        private Long count;
        private Double percentage;
    }

    /**
     * 类别分布内部类
     */
    @Data
    public static class CategoryDistribution {
        private String category;
        private Long count;
        private Double percentage;
    }

    /**
     * 求职者端统计响应
     */
    @Data
    public static class SeekerStatistics {
        /**
         * 总职位数
         */
        private Long totalPositions;

        /**
         * 今日新增职位数
         */
        private Long todayNewPositions;

        /**
         * 我的投递数
         */
        private Long myApplications;

        /**
         * 我的面试邀请数
         */
        private Long myInterviews;

        /**
         * 热门城市TOP5
         */
        private List<CityDistribution> hotCities;

        /**
         * 热门职位类别TOP5
         */
        private List<CategoryDistribution> hotCategories;

        /**
         * 平均薪资（高薪职位占比）
         */
        private BigDecimal highSalaryPercentage;

        /**
         * 求职竞争指数（平均投递数/职位数）
         */
        private BigDecimal competitionIndex;
    }

    /**
     * HR端统计响应
     */
    @Data
    public static class BossStatistics {
        /**
         * 我发布的职位数
         */
        private Long myPositions;

        /**
         * 我收到的投递数
         */
        private Long myApplications;

        /**
         * 待处理投递数
         */
        private Long pendingApplications;

        /**
         * 面试中数
         */
        private Long interviewingCount;

        /**
         * 已录用数
         */
        private Long hiredCount;

        /**
         * 不合适数
         */
        private Long rejectedCount;

        /**
         * 投递转化率（录用/投递）
         */
        private BigDecimal conversionRate;

        /**
         * 各职位投递情况
         */
        private List<PositionApplicationStat> positionStats;
    }

    /**
     * 职位投递统计内部类
     */
    @Data
    public static class PositionApplicationStat {
        private Long positionId;
        private String positionTitle;
        private Long applicationCount;
        private BigDecimal conversionRate;
    }

    /**
     * 词云数据项
     */
    @Data
    public static class WordCloudItem {
        private String name;
        private Integer value;
    }

    /**
     * 词云数据响应
     */
    @Data
    public static class WordCloudResponse {
        private List<WordCloudItem> skills;
        private List<WordCloudItem> positions;
        private List<WordCloudItem> requirements;
    }
}
