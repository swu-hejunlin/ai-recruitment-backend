package com.example.airecruitmentbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.airecruitmentbackend.entity.Project;
import org.apache.ibatis.annotations.Mapper;

/**
 * 项目经历 Mapper
 */
@Mapper
public interface ProjectMapper extends BaseMapper<Project> {
}