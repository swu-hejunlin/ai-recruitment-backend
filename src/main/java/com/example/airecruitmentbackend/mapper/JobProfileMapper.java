package com.example.airecruitmentbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.airecruitmentbackend.entity.JobProfile;
import org.apache.ibatis.annotations.Mapper;

/**
 * 岗位画像Mapper接口
 */
@Mapper
public interface JobProfileMapper extends BaseMapper<JobProfile> {
}
