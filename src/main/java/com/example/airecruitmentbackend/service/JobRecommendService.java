package com.example.airecruitmentbackend.service;

import com.example.airecruitmentbackend.dto.JobRecommendResponse;
import com.example.airecruitmentbackend.entity.JobMatchRecord;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 岗位推荐Service接口
 */
public interface JobRecommendService extends IService<JobMatchRecord> {
    /**
     * 获取岗位推荐列表
     * @param userId 用户ID
     * @param limit 推荐数量限制
     * @return 推荐岗位列表
     */
    List<JobRecommendResponse> getJobRecommendations(Long userId, Integer limit);

    /**
     * 获取匹配度详情
     * @param userId 用户ID
     * @param positionId 岗位ID
     * @return 匹配度详情
     */
    JobRecommendResponse getMatchDetails(Long userId, Long positionId);

    /**
     * 批量生成匹配记录
     * @param userId 用户ID
     * @return 生成的匹配记录数
     */
    int batchGenerateMatchRecords(Long userId);

    /**
     * 更新匹配记录查看状态
     * @param recordId 记录ID
     * @return 是否更新成功
     */
    boolean markAsViewed(Long recordId);
}
