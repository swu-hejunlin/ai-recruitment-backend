package com.example.airecruitmentbackend.service;

import com.example.airecruitmentbackend.dto.TalentProfileResponse;
import com.example.airecruitmentbackend.entity.TalentProfile;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.concurrent.CompletableFuture;

/**
 * 人才画像Service接口
 */
public interface TalentProfileService extends IService<TalentProfile> {
    /**
     * 生成人才画像
     * @param userId 用户ID
     * @return 生成的人才画像响应
     */
    TalentProfileResponse generateTalentProfile(Long userId);

    /**
     * 异步生成人才画像
     * @param userId 用户ID
     * @return 异步任务
     */
    CompletableFuture<TalentProfileResponse> generateTalentProfileAsync(Long userId);

    /**
     * 获取人才画像
     * @param userId 用户ID
     * @return 人才画像响应
     */
    TalentProfileResponse getTalentProfile(Long userId);

    /**
     * 更新人才画像
     * @param userId 用户ID
     * @return 更新后的人才画像响应
     */
    TalentProfileResponse updateTalentProfile(Long userId);

    /**
     * 异步更新人才画像
     * @param userId 用户ID
     * @return 异步任务
     */
    CompletableFuture<TalentProfileResponse> updateTalentProfileAsync(Long userId);

    /**
     * 删除人才画像
     * @param userId 用户ID
     * @return 是否删除成功
     */
    boolean deleteTalentProfile(Long userId);
}