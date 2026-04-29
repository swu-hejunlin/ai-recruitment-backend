package com.example.airecruitmentbackend.controller;

import com.example.airecruitmentbackend.common.Result;
import com.example.airecruitmentbackend.dto.TalentRecommendResponse;
import com.example.airecruitmentbackend.service.TalentRecommendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/talent-recommend")
public class TalentRecommendController extends BaseController {

    @Autowired
    private TalentRecommendService talentRecommendService;

    @GetMapping
    public Result<List<TalentRecommendResponse>> getTalentRecommendations(
            @RequestParam(required = false) Long positionId,
            @RequestParam(required = false, defaultValue = "10") Integer limit) {
        Long bossId = getCurrentUserId();
        List<TalentRecommendResponse> recommendations = talentRecommendService.getTalentRecommendations(bossId, positionId, limit);
        return Result.success(recommendations);
    }

    @GetMapping("/match")
    public Result<TalentRecommendResponse> getMatchDetails(
            @RequestParam Long jobSeekerId,
            @RequestParam Long positionId) {
        Long bossId = getCurrentUserId();
        TalentRecommendResponse details = talentRecommendService.getMatchDetails(bossId, jobSeekerId, positionId);
        return Result.success(details);
    }

    @PostMapping("/batch-generate")
    public Result<Map<String, Object>> batchGenerateMatchRecords(@RequestParam Long positionId) {
        Long bossId = getCurrentUserId();
        int count = talentRecommendService.batchGenerateMatchRecords(bossId, positionId);
        return Result.success(Map.of("count", count));
    }

    @PutMapping("/viewed/{recordId}")
    public Result<Boolean> markAsViewed(@PathVariable Long recordId) {
        boolean result = talentRecommendService.markAsViewed(recordId);
        return Result.success(result);
    }
}
