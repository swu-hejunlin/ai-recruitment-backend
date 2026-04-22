package com.example.airecruitmentbackend.service;

import com.example.airecruitmentbackend.dto.JobProfileResponse;
import com.example.airecruitmentbackend.entity.JobProfile;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.concurrent.CompletableFuture;

/**
 * 岗位画像Service接口
 */
public interface JobProfileService extends IService<JobProfile> {
    /**
     * 生成岗位画像
     * @param positionId 岗位ID
     * @return 生成的画像响应
     */
    JobProfileResponse generateJobProfile(Long positionId);

    /**
     * 异步生成岗位画像
     * @param positionId 岗位ID
     * @return 异步任务
     */
    CompletableFuture<JobProfileResponse> generateJobProfileAsync(Long positionId);

    /**
     * 获取岗位画像
     * @param positionId 岗位ID
     * @return 岗位画像响应
     */
    JobProfileResponse getJobProfile(Long positionId);

    /**
     * 更新岗位画像
     * @param positionId 岗位ID
     * @return 更新后的画像响应
     */
    JobProfileResponse updateJobProfile(Long positionId);

    /**
     * 异步更新岗位画像
     * @param positionId 岗位ID
     * @return 异步任务
     */
    CompletableFuture<JobProfileResponse> updateJobProfileAsync(Long positionId);

    /**
     * 删除岗位画像
     * @param positionId 岗位ID
     * @return 是否删除成功
     */
    boolean deleteJobProfile(Long positionId);
}
