package com.example.airecruitmentbackend.service;

import com.example.airecruitmentbackend.dto.TalentRecommendResponse;
import com.example.airecruitmentbackend.entity.TalentMatchRecord;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface TalentRecommendService extends IService<TalentMatchRecord> {
    List<TalentRecommendResponse> getTalentRecommendations(Long bossId, Long positionId, Integer limit);
    TalentRecommendResponse getMatchDetails(Long bossId, Long jobSeekerId, Long positionId);
    int batchGenerateMatchRecords(Long bossId, Long positionId);
    boolean markAsViewed(Long recordId);
}
