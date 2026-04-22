package com.example.airecruitmentbackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.airecruitmentbackend.dto.JobRecommendResponse;
import com.example.airecruitmentbackend.entity.*;
import com.example.airecruitmentbackend.mapper.*;
import com.example.airecruitmentbackend.service.JobRecommendService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 岗位推荐Service实现类
 */
@Slf4j
@Service
public class JobRecommendServiceImpl extends ServiceImpl<JobMatchRecordMapper, JobMatchRecord> implements JobRecommendService {

    @Autowired
    private PositionMapper positionMapper;

    @Autowired
    private JobProfileMapper jobProfileMapper;

    @Autowired
    private TalentProfileMapper talentProfileMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JobMatchRecordMapper jobMatchRecordMapper;

    @Autowired
    private ObjectMapper objectMapper;

    private static final BigDecimal SKILL_WEIGHT = new BigDecimal("0.3");
    private static final BigDecimal EXPERIENCE_WEIGHT = new BigDecimal("0.3");
    private static final BigDecimal EDUCATION_WEIGHT = new BigDecimal("0.3");
    private static final BigDecimal SALARY_WEIGHT = new BigDecimal("0.1");

    @Override
    public List<JobRecommendResponse> getJobRecommendations(Long userId, Integer limit) {
        log.info("获取岗位推荐，用户ID：{}，推荐数量：{}", userId, limit);

        if (limit == null || limit <= 0) {
            limit = 10;
        }

        TalentProfile talentProfile = getTalentProfile(userId);
        if (talentProfile == null) {
            log.warn("用户画像不存在，无法推荐，用户ID：{}", userId);
            return Collections.emptyList();
        }

        List<JobProfile> allJobProfiles = jobProfileMapper.selectList(null);

        List<JobMatchRecord> matchRecords = new ArrayList<>();

        for (JobProfile jobProfile : allJobProfiles) {
            JobMatchRecord record = calculateMatchScore(userId, jobProfile.getPositionId(), talentProfile, jobProfile);
            if (record != null) {
                matchRecords.add(record);
            }
        }

        matchRecords.sort((a, b) -> b.getMatchScore().compareTo(a.getMatchScore()));

        int finalLimit = limit;
        return matchRecords.stream()
            .limit(finalLimit)
            .map(this::buildRecommendResponse)
            .collect(Collectors.toList());
    }

    @Override
    public JobRecommendResponse getMatchDetails(Long userId, Long positionId) {
        log.info("获取匹配度详情，用户ID：{}，岗位ID：{}", userId, positionId);

        TalentProfile talentProfile = getTalentProfile(userId);
        JobProfile jobProfile = getJobProfile(positionId);

        if (talentProfile == null || jobProfile == null) {
            throw new RuntimeException("用户画像或岗位画像不存在");
        }

        JobMatchRecord record = calculateMatchScore(userId, positionId, talentProfile, jobProfile);
        return buildRecommendResponse(record);
    }

    @Override
    @Transactional
    public int batchGenerateMatchRecords(Long userId) {
        log.info("批量生成匹配记录，用户ID：{}", userId);

        TalentProfile talentProfile = getTalentProfile(userId);
        if (talentProfile == null) {
            log.warn("用户画像不存在，无法生成匹配记录，用户ID：{}", userId);
            return 0;
        }

        List<JobProfile> allJobProfiles = jobProfileMapper.selectList(null);
        int count = 0;

        for (JobProfile jobProfile : allJobProfiles) {
            JobMatchRecord record = calculateMatchScore(userId, jobProfile.getPositionId(), talentProfile, jobProfile);
            if (record != null) {
                JobMatchRecord existing = jobMatchRecordMapper.selectOne(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<JobMatchRecord>()
                        .eq(JobMatchRecord::getUserId, userId)
                        .eq(JobMatchRecord::getPositionId, jobProfile.getPositionId())
                );

                if (existing != null) {
                    record.setId(existing.getId());
                    record.setIsViewed(existing.getIsViewed());
                    record.setIsApplied(existing.getIsApplied());
                    jobMatchRecordMapper.updateById(record);
                } else {
                    jobMatchRecordMapper.insert(record);
                }
                count++;
            }
        }

        log.info("批量生成匹配记录完成，生成数量：{}", count);
        return count;
    }

    @Override
    public boolean markAsViewed(Long recordId) {
        log.info("标记匹配记录为已查看，记录ID：{}", recordId);

        JobMatchRecord record = jobMatchRecordMapper.selectById(recordId);
        if (record == null) {
            return false;
        }

        record.setIsViewed(1);
        return jobMatchRecordMapper.updateById(record) > 0;
    }

    private TalentProfile getTalentProfile(Long userId) {
        return talentProfileMapper.selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<TalentProfile>()
                .eq(TalentProfile::getUserId, userId)
        );
    }

    private JobProfile getJobProfile(Long positionId) {
        return jobProfileMapper.selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<JobProfile>()
                .eq(JobProfile::getPositionId, positionId)
        );
    }

    private JobMatchRecord calculateMatchScore(Long userId, Long positionId, TalentProfile talentProfile, JobProfile jobProfile) {
        Position position = positionMapper.selectById(positionId);
        if (position == null || position.getStatus() != 1) {
            return null;
        }

        JobMatchRecord record = new JobMatchRecord();
        record.setUserId(userId);
        record.setPositionId(positionId);
        record.setCreatedAt(LocalDateTime.now());
        record.setUpdatedAt(LocalDateTime.now());

        BigDecimal skillMatchRate = calculateSkillMatchRate(talentProfile, jobProfile);
        BigDecimal experienceMatchRate = calculateExperienceMatchRate(talentProfile, jobProfile);
        BigDecimal educationMatchRate = calculateEducationMatchRate(talentProfile, jobProfile);
        BigDecimal salaryMatchRate = calculateSalaryMatchRate(talentProfile, jobProfile);

        BigDecimal matchScore = skillMatchRate.multiply(SKILL_WEIGHT)
            .add(experienceMatchRate.multiply(EXPERIENCE_WEIGHT))
            .add(educationMatchRate.multiply(EDUCATION_WEIGHT))
            .add(salaryMatchRate.multiply(SALARY_WEIGHT))
            .setScale(2, RoundingMode.HALF_UP);

        record.setMatchScore(matchScore);
        record.setSkillMatchRate(skillMatchRate);
        record.setExperienceMatchRate(experienceMatchRate);
        record.setEducationMatchRate(educationMatchRate);
        record.setSalaryMatchRate(salaryMatchRate);

        Map<String, Object> matchDetails = new HashMap<>();
        matchDetails.put("matchedSkills", getMatchedSkills(talentProfile, jobProfile));
        matchDetails.put("missingSkills", getMissingSkills(talentProfile, jobProfile));
        matchDetails.put("matchDescription", generateMatchDescription(skillMatchRate, experienceMatchRate, educationMatchRate));

        try {
            record.setMatchDetails(objectMapper.writeValueAsString(matchDetails));
        } catch (Exception e) {
            log.error("序列化匹配详情失败", e);
        }

        return record;
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

        Set<String> talentSkillSet = new HashSet<>(talentSkills.stream()
            .map(String::toLowerCase)
            .collect(Collectors.toSet()));

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

    private List<String> getMatchedSkills(TalentProfile talentProfile, JobProfile jobProfile) {
        List<String> talentSkills = parseJsonToList(talentProfile.getSkills());
        List<String> jobSkills = parseJsonToList(jobProfile.getSkills());

        Set<String> talentSkillSet = talentSkills.stream()
            .map(String::toLowerCase)
            .collect(Collectors.toSet());

        return jobSkills.stream()
            .filter(skill -> talentSkillSet.contains(skill.toLowerCase()))
            .collect(Collectors.toList());
    }

    private List<String> getMissingSkills(TalentProfile talentProfile, JobProfile jobProfile) {
        List<String> talentSkills = parseJsonToList(talentProfile.getSkills());
        List<String> jobSkills = parseJsonToList(jobProfile.getSkills());

        Set<String> talentSkillSet = talentSkills.stream()
            .map(String::toLowerCase)
            .collect(Collectors.toSet());

        return jobSkills.stream()
            .filter(skill -> !talentSkillSet.contains(skill.toLowerCase()))
            .collect(Collectors.toList());
    }

    private String generateMatchDescription(BigDecimal skillRate, BigDecimal experienceRate, BigDecimal educationRate) {
        StringBuilder sb = new StringBuilder();

        if (skillRate.compareTo(new BigDecimal("80")) >= 0) {
            sb.append("技能匹配度较高");
        } else if (skillRate.compareTo(new BigDecimal("50")) >= 0) {
            sb.append("技能匹配度一般");
        } else {
            sb.append("技能匹配度较低");
        }

        if (experienceRate.compareTo(new BigDecimal("80")) >= 0) {
            sb.append("，经验要求满足");
        } else {
            sb.append("，经验要求略有差距");
        }

        return sb.toString();
    }

    private JobRecommendResponse buildRecommendResponse(JobMatchRecord record) {
        JobRecommendResponse response = new JobRecommendResponse();
        Position position = positionMapper.selectById(record.getPositionId());

        if (position != null) {
            // 基本信息
            response.setPositionId(position.getId());
            response.setId(position.getId()); // 与职位列表接口兼容
            response.setCompanyId(position.getCompanyId());
            response.setJobName(position.getTitle());
            response.setTitle(position.getTitle()); // 与职位列表接口兼容
            response.setCompanyName(position.getCompanyName());
            response.setCompanyLogo(position.getCompanyLogo());
            response.setCity(position.getCity());
            response.setAddress(position.getAddress());
            
            // 薪资信息
            response.setSalaryMin(position.getSalaryMin());
            response.setSalaryMax(position.getSalaryMax());
            if (position.getSalaryMin() != null && position.getSalaryMax() != null) {
                response.setSalaryRange(position.getSalaryMin() + "-" + position.getSalaryMax() + "K/月");
            } else {
                response.setSalaryRange("面议");
            }
            
            // 学历和经验要求
            response.setEducationMin(position.getEducationMin());
            response.setWorkYearsMin(position.getWorkYearsMin());
            response.setEducationRequire(position.getEducationMin() != null ? position.getEducationMin().toString() : "不限");
            response.setExperienceRequire(position.getWorkYearsMin() != null ? position.getWorkYearsMin().toString() : "不限");
            
            // 职位信息
            response.setCategory(position.getCategory());
            response.setTags(position.getTags());
            response.setDescription(position.getDescription());
            response.setRequirement(position.getRequirement());
            response.setStatus(position.getStatus());
            response.setDescriptionSummary(position.getDescription());
        }

        // 匹配信息（虽然前端不显示，但保留用于后端逻辑）
        response.setMatchScore(record.getMatchScore());
        response.setSkillMatchRate(record.getSkillMatchRate());
        response.setExperienceMatchRate(record.getExperienceMatchRate());
        response.setEducationMatchRate(record.getEducationMatchRate());
        response.setSalaryMatchRate(record.getSalaryMatchRate());
        response.setIsFavorite(false);

        try {
            if (record.getMatchDetails() != null) {
                Map<String, Object> details = objectMapper.readValue(record.getMatchDetails(), Map.class);

                JobRecommendResponse.MatchDetails matchDetails = new JobRecommendResponse.MatchDetails();
                matchDetails.setMatchedSkills((List<String>) details.get("matchedSkills"));
                matchDetails.setMissingSkills((List<String>) details.get("missingSkills"));
                matchDetails.setMatchDescription((String) details.get("matchDescription"));
                response.setMatchDetails(matchDetails);
            }
        } catch (Exception e) {
            log.error("解析匹配详情失败", e);
        }

        // 优先使用职位的创建时间（发布时间），如果没有则使用匹配记录的创建时间
        if (position != null && position.getCreateTime() != null) {
            response.setCreatedAt(position.getCreateTime().toString());
            response.setCreateTime(position.getCreateTime().toString());
        } else if (record.getCreatedAt() != null) {
            response.setCreatedAt(record.getCreatedAt().toString());
            response.setCreateTime(record.getCreatedAt().toString());
        }

        return response;
    }
}