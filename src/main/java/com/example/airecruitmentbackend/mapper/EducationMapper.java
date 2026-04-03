package com.example.airecruitmentbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.airecruitmentbackend.entity.Education;
import org.apache.ibatis.annotations.Mapper;

/**
 * 教育经历 Mapper
 */
@Mapper
public interface EducationMapper extends BaseMapper<Education> {
}