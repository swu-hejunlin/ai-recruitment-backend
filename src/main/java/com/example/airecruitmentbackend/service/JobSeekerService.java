package com.example.airecruitmentbackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.airecruitmentbackend.entity.JobSeeker;

/**
 * 求职者信息服务接口
 */
public interface JobSeekerService extends IService<JobSeeker> {

    /**
     * 根据用户ID获取求职者信息
     *
     * @param userId 用户ID
     * @return 求职者信息
     */
    JobSeeker getByUserId(Long userId);

    /**
     * 更新求职者信息
     *
     * @param jobSeeker 求职者信息
     * @return 是否成功
     */
    boolean updateJobSeeker(JobSeeker jobSeeker);

    /**
     * 上传头像
     *
     * @param userId 用户ID
     * @param avatarUrl 头像URL
     * @return 是否成功
     */
    boolean updateAvatar(Long userId, String avatarUrl);

    /**
     * 上传简历附件
     *
     * @param userId 用户ID
     * @param resumeUrl 简历URL
     * @return 是否成功
     */
    boolean updateResume(Long userId, String resumeUrl);
}
