package com.example.airecruitmentbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.airecruitmentbackend.entity.Interview;
import org.apache.ibatis.annotations.Mapper;

/**
 * 面试Mapper
 */
@Mapper
public interface InterviewMapper extends BaseMapper<Interview> {
}
