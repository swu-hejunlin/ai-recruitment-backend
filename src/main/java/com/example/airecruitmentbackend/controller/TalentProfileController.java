package com.example.airecruitmentbackend.controller;

import com.example.airecruitmentbackend.common.Result;
import com.example.airecruitmentbackend.dto.TalentProfileResponse;
import com.example.airecruitmentbackend.service.TalentProfileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 人才画像Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/talent-profile")
public class TalentProfileController extends BaseController {

    @Autowired
    private TalentProfileService talentProfileService;

    /**
     * 生成人才画像
     */
    @PostMapping("/generate")
    public Result<TalentProfileResponse> generateTalentProfile() {
        Long userId = getCurrentUserId();
        log.info("生成人才画像，用户ID：{}", userId);
        try {
            TalentProfileResponse response = talentProfileService.generateTalentProfile(userId);
            return Result.success(response);
        } catch (Exception e) {
            log.error("生成人才画像失败，用户ID：{}", userId, e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取人才画像
     */
    @GetMapping
    public Result<TalentProfileResponse> getTalentProfile() {
        Long userId = getCurrentUserId();
        log.info("获取人才画像，用户ID：{}", userId);
        try {
            TalentProfileResponse response = talentProfileService.getTalentProfile(userId);
            return Result.success(response);
        } catch (Exception e) {
            log.error("获取人才画像失败，用户ID：{}", userId, e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新人才画像
     */
    @PutMapping("/update")
    public Result<TalentProfileResponse> updateTalentProfile() {
        Long userId = getCurrentUserId();
        log.info("更新人才画像，用户ID：{}", userId);
        try {
            TalentProfileResponse response = talentProfileService.updateTalentProfile(userId);
            return Result.success(response);
        } catch (Exception e) {
            log.error("更新人才画像失败，用户ID：{}", userId, e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除人才画像
     */
    @DeleteMapping
    public Result<Boolean> deleteTalentProfile() {
        Long userId = getCurrentUserId();
        log.info("删除人才画像，用户ID：{}", userId);
        try {
            boolean result = talentProfileService.deleteTalentProfile(userId);
            return Result.success(result);
        } catch (Exception e) {
            log.error("删除人才画像失败，用户ID：{}", userId, e);
            return Result.error(e.getMessage());
        }
    }
}
