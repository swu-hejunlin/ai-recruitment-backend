package com.example.airecruitmentbackend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.airecruitmentbackend.entity.Application;

/**
 * 投递记录Service接口
 */
public interface ApplicationService {

    /**
     * 投递简历
     */
    void apply(Long jobSeekerId, Long positionId);

    /**
     * 根据ID查询
     */
    Application getById(Long id);

    /**
     * Boss查询收到的投递列表
     */
    Page<Application> getApplicationsByBoss(Long bossId, Integer status, int pageNum, int pageSize);

    /**
     * 求职者查询投递列表
     */
    Page<Application> getApplicationsByJobSeeker(Long jobSeekerId, int pageNum, int pageSize);

    /**
     * 查看简历（更新状态为已查看）
     */
    void readApplication(Long applicationId, Long bossId);

    /**
     * 更新投递状态
     */
    void updateStatus(Long applicationId, Integer status, Long bossId);

    /**
     * 检查是否已投递
     */
    boolean hasApplied(Long jobSeekerId, Long positionId);
}
