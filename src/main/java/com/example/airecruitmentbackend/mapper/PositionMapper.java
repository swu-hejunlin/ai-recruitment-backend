package com.example.airecruitmentbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.airecruitmentbackend.entity.Position;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 职位Mapper
 */
@Mapper
public interface PositionMapper extends BaseMapper<Position> {

    /**
     * 按城市分组统计职位数量
     */
    @Select("SELECT city, COUNT(*) as count FROM position WHERE status = 1 AND city IS NOT NULL GROUP BY city ORDER BY count DESC")
    List<Map<String, Object>> groupByCity();

    /**
     * 按类别分组统计职位数量
     */
    @Select("SELECT category, COUNT(*) as count FROM position WHERE status = 1 AND category IS NOT NULL GROUP BY category ORDER BY count DESC")
    List<Map<String, Object>> groupByCategory();

    /**
     * 按薪资范围分组统计职位数量
     */
    @Select("SELECT " +
            "CASE " +
            "  WHEN salary_max >= 50 THEN '50K以上' " +
            "  WHEN salary_max >= 30 THEN '30-50K' " +
            "  WHEN salary_max >= 20 THEN '20-30K' " +
            "  WHEN salary_max >= 10 THEN '10-20K' " +
            "  WHEN salary_max >= 5 THEN '5-10K' " +
            "  ELSE '5K以下' " +
            "END as salary_range, " +
            "COUNT(*) as count " +
            "FROM position " +
            "WHERE status = 1 " +
            "GROUP BY salary_range " +
            "ORDER BY MAX(salary_max) DESC")
    List<Map<String, Object>> groupBySalaryRange();
}
