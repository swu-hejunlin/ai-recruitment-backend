package com.example.airecruitmentbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.airecruitmentbackend.entity.JobSeeker;
import org.apache.ibatis.annotations.Mapper;

/**
 * 求职者信息Mapper接口
 */
@Mapper
public interface JobSeekerMapper extends BaseMapper<JobSeeker> {
}
