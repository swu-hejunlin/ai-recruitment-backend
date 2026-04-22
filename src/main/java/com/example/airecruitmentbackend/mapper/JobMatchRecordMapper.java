package com.example.airecruitmentbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.airecruitmentbackend.entity.JobMatchRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 岗位匹配记录Mapper接口
 */
@Mapper
public interface JobMatchRecordMapper extends BaseMapper<JobMatchRecord> {
}
