package com.example.airecruitmentbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.airecruitmentbackend.entity.Application;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 投递记录Mapper
 */
@Mapper
public interface ApplicationMapper extends BaseMapper<Application> {

    /**
     * 查询Boss收到的投递列表
     */
    Page<Application> selectByBossId(Page<Application> page, @Param("bossId") Long bossId);

    /**
     * 查询求职者投递列表
     */
    Page<Application> selectByJobSeekerId(Page<Application> page, @Param("jobSeekerId") Long jobSeekerId);
}
