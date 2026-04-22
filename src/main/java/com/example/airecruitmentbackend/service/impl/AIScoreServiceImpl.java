package com.example.airecruitmentbackend.service.impl;

import com.example.airecruitmentbackend.entity.JobProfile;
import com.example.airecruitmentbackend.entity.JobSeeker;
import com.example.airecruitmentbackend.entity.TalentProfile;
import com.example.airecruitmentbackend.mapper.JobProfileMapper;
import com.example.airecruitmentbackend.mapper.JobSeekerMapper;
import com.example.airecruitmentbackend.mapper.TalentProfileMapper;
import com.example.airecruitmentbackend.service.AIScoreService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * AI评分服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AIScoreServiceImpl implements AIScoreService {

    private final TalentProfileMapper talentProfileMapper;
    private final JobProfileMapper jobProfileMapper;
    private final JobSeekerMapper jobSeekerMapper;
    private final ObjectMapper objectMapper;

    private static final BigDecimal SKILL_WEIGHT = new BigDecimal("0.3");
    private static final BigDecimal EXPERIENCE_WEIGHT = new BigDecimal("0.3");
    private static final BigDecimal EDUCATION_WEIGHT = new BigDecimal("0.3");
    private static final BigDecimal SALARY_WEIGHT = new BigDecimal("0.1");

    @Override
    public BigDecimal calculateApplicationScore(Long jobSeekerId, Long positionId) {
        log.info("开始计算AI评分，jobSeekerId：{}，positionId：{}", jobSeekerId, positionId);

        // 先获取JobSeeker，获取其userId
        JobSeeker jobSeeker = jobSeekerMapper.selectById(jobSeekerId);
        if (jobSeeker == null) {
            log.warn("求职者不存在，jobSeekerId：{}", jobSeekerId);
            return BigDecimal.ZERO;
        }

        // 获取求职者画像（使用userId查询）
        TalentProfile talentProfile = talentProfileMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<TalentProfile>()
                        .eq(TalentProfile::getUserId, jobSeeker.getUserId())
        );

        if (talentProfile == null) {
            log.warn("求职者画像不存在，userId：{}", jobSeeker.getUserId());
            return BigDecimal.ZERO;
        }

        // 获取岗位画像
        JobProfile jobProfile = jobProfileMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<JobProfile>()
                        .eq(JobProfile::getPositionId, positionId)
        );

        if (jobProfile == null) {
            log.warn("岗位画像不存在，positionId：{}", positionId);
            return BigDecimal.ZERO;
        }

        // 计算各维度匹配度
        BigDecimal skillMatchRate = calculateSkillMatchRate(talentProfile, jobProfile);
        BigDecimal experienceMatchRate = calculateExperienceMatchRate(talentProfile, jobProfile);
        BigDecimal educationMatchRate = calculateEducationMatchRate(talentProfile, jobProfile);
        BigDecimal salaryMatchRate = calculateSalaryMatchRate(talentProfile, jobProfile);

        // 计算综合评分
        BigDecimal matchScore = skillMatchRate.multiply(SKILL_WEIGHT)
                .add(experienceMatchRate.multiply(EXPERIENCE_WEIGHT))
                .add(educationMatchRate.multiply(EDUCATION_WEIGHT))
                .add(salaryMatchRate.multiply(SALARY_WEIGHT))
                .setScale(2, RoundingMode.HALF_UP);

        log.info("AI评分计算完成，jobSeekerId：{}，positionId：{}，score：{}", jobSeekerId, positionId, matchScore);
        return matchScore;
    }

    private BigDecimal calculateSkillMatchRate(TalentProfile talentProfile, JobProfile jobProfile) {
        List<String> talentSkills = parseJsonToList(talentProfile.getSkills());
        List<String> jobSkills = parseJsonToList(jobProfile.getSkills());

        if (jobSkills == null || jobSkills.isEmpty()) {
            return new BigDecimal("100");
        }

        if (talentSkills == null || talentSkills.isEmpty()) {
            return BigDecimal.ZERO;
        }

        Set<String> talentSkillSet = talentSkills.stream()
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        long matchedCount = jobSkills.stream()
                .filter(skill -> talentSkillSet.contains(skill.toLowerCase()))
                .count();

        return BigDecimal.valueOf(matchedCount)
                .multiply(new BigDecimal("100"))
                .divide(new BigDecimal(jobSkills.size()), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateExperienceMatchRate(TalentProfile talentProfile, JobProfile jobProfile) {
        if (jobProfile.getExperienceRequire() == null) {
            return new BigDecimal("100");
        }

        int requiredLevel = Integer.parseInt(jobProfile.getExperienceRequire());
        int talentYears = talentProfile.getWorkYears() != null ? talentProfile.getWorkYears() : 0;

        if (requiredLevel == 1) {
            return new BigDecimal("100");
        }

        int requiredYears = switch (requiredLevel) {
            case 2 -> 0;
            case 3 -> 1;
            case 4 -> 3;
            case 5 -> 5;
            case 6 -> 10;
            default -> 0;
        };

        if (talentYears >= requiredYears) {
            return new BigDecimal("100");
        }

        return BigDecimal.valueOf(talentYears)
                .multiply(new BigDecimal("100"))
                .divide(new BigDecimal(requiredYears), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateEducationMatchRate(TalentProfile talentProfile, JobProfile jobProfile) {
        if (jobProfile.getEducationRequire() == null) {
            return new BigDecimal("100");
        }

        int requiredLevel = Integer.parseInt(jobProfile.getEducationRequire());
        int talentLevel = talentProfile.getEducation() != null ? Integer.parseInt(talentProfile.getEducation()) : 0;

        if (talentLevel >= requiredLevel) {
            return new BigDecimal("100");
        }

        return BigDecimal.valueOf(talentLevel)
                .multiply(new BigDecimal("100"))
                .divide(new BigDecimal(requiredLevel), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateSalaryMatchRate(TalentProfile talentProfile, JobProfile jobProfile) {
        if (jobProfile.getSalaryMin() == null || jobProfile.getSalaryMax() == null || talentProfile.getSalaryExpectation() == null) {
            return new BigDecimal("100");
        }

        BigDecimal expectation = talentProfile.getSalaryExpectation();

        if (expectation.compareTo(jobProfile.getSalaryMin()) >= 0 && expectation.compareTo(jobProfile.getSalaryMax()) <= 0) {
            return new BigDecimal("100");
        }

        if (expectation.compareTo(jobProfile.getSalaryMax()) > 0) {
            return BigDecimal.valueOf(50);
        }

        return BigDecimal.valueOf(75);
    }

    private List<String> parseJsonToList(String json) {
        if (json == null || json.isEmpty()) {
            return Collections.emptyList();
        }

        try {
            return objectMapper.readValue(json, List.class);
        } catch (Exception e) {
            log.error("解析JSON列表失败：{}", json, e);
            return Collections.emptyList();
        }
    }
}
