package com.example.airecruitmentbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.airecruitmentbackend.entity.Experience;
import org.apache.ibatis.annotations.Mapper;

/**
 * 工作经验/实习经历 Mapper
 */
@Mapper
public interface ExperienceMapper extends BaseMapper<Experience> {
}