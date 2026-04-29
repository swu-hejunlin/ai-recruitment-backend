package com.example.airecruitmentbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.airecruitmentbackend.dto.TalentRecommendResponse;
import com.example.airecruitmentbackend.entity.*;
import com.example.airecruitmentbackend.mapper.*;
import com.example.airecruitmentbackend.service.EmbeddingService;
import com.example.airecruitmentbackend.service.TalentRecommendService;
import com.example.airecruitmentbackend.utils.VectorCalculator;
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

@Slf4j
@Service
public class TalentRecommendServiceImpl extends ServiceImpl<TalentMatchRecordMapper, TalentMatchRecord> implements TalentRecommendService {

    @Autowired
    private JobSeekerMapper jobSeekerMapper;
    @Autowired
    private TalentProfileMapper talentProfileMapper;
    @Autowired
    private JobProfileMapper jobProfileMapper;
    @Autowired
    private PositionMapper positionMapper;
    @Autowired
    private TalentMatchRecordMapper talentMatchRecordMapper;
    @Autowired
    private EmbeddingService embeddingService;
    @Autowired
    private ObjectMapper objectMapper;

    private static final BigDecimal SKILL_WEIGHT = new BigDecimal("0.25");
    private static final BigDecimal EXPERIENCE_WEIGHT = new BigDecimal("0.25");
    private static final BigDecimal EDUCATION_WEIGHT = new BigDecimal("0.25");
    private static final BigDecimal SALARY_WEIGHT = new BigDecimal("0.1");
    private static final BigDecimal SEMANTIC_WEIGHT = new BigDecimal("0.15");

    @Override
    public List<TalentRecommendResponse> getTalentRecommendations(Long bossId, Long positionId, Integer limit) {
        log.info("获取人才推荐，bossId：{}，positionId：{}，limit：{}", bossId, positionId, limit);
        if (limit == null || limit <= 0) {
            limit = 10;
        }

        JobProfile jobProfile;
        if (positionId != null) {
            jobProfile = getJobProfile(positionId);
            if (jobProfile == null) {
                log.warn("岗位画像不存在，positionId：{}", positionId);
                return Collections.emptyList();
            }
        } else {
            List<Position> positions = positionMapper.selectList(new LambdaQueryWrapper<Position>()
                    .eq(Position::getBossId, bossId).eq(Position::getStatus, 1)
                    .orderByDesc(Position::getCreateTime).last("LIMIT 1"));
            if (positions.isEmpty()) {
                log.warn("HR没有招聘中的职位，bossId：{}", bossId);
                return Collections.emptyList();
            }
            positionId = positions.get(0).getId();
            jobProfile = getJobProfile(positionId);
            if (jobProfile == null) {
                log.warn("岗位画像不存在，positionId：{}", positionId);
                return Collections.emptyList();
            }
        }

        List<TalentProfile> allTalentProfiles = talentProfileMapper.selectList(null);

        List<TalentMatchRecord> matchRecords = new ArrayList<>();
        for (TalentProfile talentProfile : allTalentProfiles) {
            TalentMatchRecord record = calculateMatchScore(bossId, talentProfile.getUserId(), positionId, talentProfile, jobProfile);
            if (record != null) {
                matchRecords.add(record);
            }
        }

        matchRecords.sort((a, b) -> b.getMatchScore().compareTo(a.getMatchScore()));

        final Long finalPositionId = positionId;
        return matchRecords.stream()
                .limit(limit)
                .map(record -> buildRecommendResponse(record, finalPositionId))
                .collect(Collectors.toList());
    }

    @Override
    public TalentRecommendResponse getMatchDetails(Long bossId, Long jobSeekerId, Long positionId) {
        log.info("获取人才匹配详情，bossId：{}，jobSeekerId：{}，positionId：{}", bossId, jobSeekerId, positionId);

        TalentProfile talentProfile = talentProfileMapper.selectOne(
                new LambdaQueryWrapper<TalentProfile>().eq(TalentProfile::getUserId, jobSeekerId));
        JobProfile jobProfile = getJobProfile(positionId);

        if (talentProfile == null || jobProfile == null) {
            throw new RuntimeException("人才画像或岗位画像不存在");
        }

        TalentMatchRecord record = calculateMatchScore(bossId, jobSeekerId, positionId, talentProfile, jobProfile);
        return buildRecommendResponse(record, positionId);
    }

    @Override
    @Transactional
    public int batchGenerateMatchRecords(Long bossId, Long positionId) {
        log.info("批量生成人才匹配记录，bossId：{}，positionId：{}", bossId, positionId);

        JobProfile jobProfile = getJobProfile(positionId);
        if (jobProfile == null) {
            log.warn("岗位画像不存在，positionId：{}", positionId);
            return 0;
        }

        List<TalentProfile> allTalentProfiles = talentProfileMapper.selectList(null);
        int count = 0;

        for (TalentProfile talentProfile : allTalentProfiles) {
            TalentMatchRecord record = calculateMatchScore(bossId, talentProfile.getUserId(), positionId, talentProfile, jobProfile);
            if (record != null) {
                TalentMatchRecord existing = talentMatchRecordMapper.selectOne(
                        new LambdaQueryWrapper<TalentMatchRecord>()
                                .eq(TalentMatchRecord::getBossId, bossId)
                                .eq(TalentMatchRecord::getJobSeekerId, record.getJobSeekerId())
                                .eq(TalentMatchRecord::getPositionId, positionId));

                if (existing != null) {
                    record.setId(existing.getId());
                    record.setIsViewed(existing.getIsViewed());
                    talentMatchRecordMapper.updateById(record);
                } else {
                    talentMatchRecordMapper.insert(record);
                }
                count++;
            }
        }

        log.info("批量生成人才匹配记录完成，生成数量：{}", count);
        return count;
    }

    @Override
    public boolean markAsViewed(Long recordId) {
        TalentMatchRecord record = talentMatchRecordMapper.selectById(recordId);
        if (record == null) {
            return false;
        }
        record.setIsViewed(1);
        return talentMatchRecordMapper.updateById(record) > 0;
    }

    private JobProfile getJobProfile(Long positionId) {
        return jobProfileMapper.selectOne(
                new LambdaQueryWrapper<JobProfile>().eq(JobProfile::getPositionId, positionId));
    }

    private TalentMatchRecord calculateMatchScore(Long bossId, Long jobSeekerUserId, Long positionId, TalentProfile talentProfile, JobProfile jobProfile) {
        JobSeeker jobSeeker = jobSeekerMapper.selectOne(
                new LambdaQueryWrapper<JobSeeker>().eq(JobSeeker::getUserId, jobSeekerUserId));
        if (jobSeeker == null) {
            return null;
        }

        Position position = positionMapper.selectById(positionId);
        if (position == null || position.getStatus() != 1) {
            return null;
        }

        if (jobSeekerUserId.equals(position.getBossId())) {
            return null;
        }

        TalentMatchRecord record = new TalentMatchRecord();
        record.setBossId(bossId);
        record.setJobSeekerId(jobSeeker.getId());
        record.setPositionId(positionId);
        record.setCreatedAt(LocalDateTime.now());
        record.setUpdatedAt(LocalDateTime.now());

        BigDecimal skillMatchRate = calculateSkillMatchRate(talentProfile, jobProfile);
        BigDecimal experienceMatchRate = calculateExperienceMatchRate(talentProfile, jobProfile);
        BigDecimal educationMatchRate = calculateEducationMatchRate(talentProfile, jobProfile);
        BigDecimal salaryMatchRate = calculateSalaryMatchRate(talentProfile, jobProfile);

        BigDecimal semanticMatchRate = calculateSemanticMatchRate(talentProfile, jobProfile);

        BigDecimal matchScore = skillMatchRate.multiply(SKILL_WEIGHT)
                .add(experienceMatchRate.multiply(EXPERIENCE_WEIGHT))
                .add(educationMatchRate.multiply(EDUCATION_WEIGHT))
                .add(salaryMatchRate.multiply(SALARY_WEIGHT))
                .add(semanticMatchRate.multiply(SEMANTIC_WEIGHT))
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
                .map(String::toLowerCase).collect(Collectors.toSet()));

        long matchedCount = jobSkills.stream()
                .filter(skill -> talentSkillSet.contains(skill.toLowerCase())).count();

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
        if (requiredYears == 0) {
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
        if (requiredLevel == 0) {
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

    private BigDecimal calculateSemanticMatchRate(TalentProfile talentProfile, JobProfile jobProfile) {
        try {
            List<Float> talentVector = VectorCalculator.parseFromJson(talentProfile.getEmbeddingVector());
            List<Float> jobVector = VectorCalculator.parseFromJson(jobProfile.getEmbeddingVector());

            if (!VectorCalculator.isValidVector(talentVector) || !VectorCalculator.isValidVector(jobVector)) {
                log.debug("向量无效，使用默认语义匹配分数100");
                return new BigDecimal("100");
            }

            double similarity = VectorCalculator.cosineSimilarity(talentVector, jobVector);
            return BigDecimal.valueOf(VectorCalculator.similarityToPercent(similarity));
        } catch (Exception e) {
            log.warn("计算语义匹配率失败: {}", e.getMessage());
            return new BigDecimal("100");
        }
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
        Set<String> talentSkillSet = talentSkills.stream().map(String::toLowerCase).collect(Collectors.toSet());
        return jobSkills.stream().filter(skill -> talentSkillSet.contains(skill.toLowerCase())).collect(Collectors.toList());
    }

    private List<String> getMissingSkills(TalentProfile talentProfile, JobProfile jobProfile) {
        List<String> talentSkills = parseJsonToList(talentProfile.getSkills());
        List<String> jobSkills = parseJsonToList(jobProfile.getSkills());
        Set<String> talentSkillSet = talentSkills.stream().map(String::toLowerCase).collect(Collectors.toSet());
        return jobSkills.stream().filter(skill -> !talentSkillSet.contains(skill.toLowerCase())).collect(Collectors.toList());
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

    private TalentRecommendResponse buildRecommendResponse(TalentMatchRecord record, Long positionId) {
        TalentRecommendResponse response = new TalentRecommendResponse();

        JobSeeker jobSeeker = jobSeekerMapper.selectById(record.getJobSeekerId());
        if (jobSeeker != null) {
            response.setJobSeekerId(jobSeeker.getId());
            response.setUserId(jobSeeker.getUserId());
            response.setName(jobSeeker.getName());
            response.setAvatarUrl(jobSeeker.getAvatar());
            response.setWorkYears(jobSeeker.getWorkYears());
            response.setSelfIntroduction(jobSeeker.getIntroduction());
            response.setExpectedCity(jobSeeker.getCity());

            String[] statusNames = {"", "在职-暂不考虑", "离职-随时到岗", "在职-考虑机会"};
            if (jobSeeker.getCurrentStatus() != null && jobSeeker.getCurrentStatus() >= 1 && jobSeeker.getCurrentStatus() <= 3) {
                response.setCurrentStatus(statusNames[jobSeeker.getCurrentStatus()]);
            }
        }

        TalentProfile talentProfile = talentProfileMapper.selectOne(
                new LambdaQueryWrapper<TalentProfile>().eq(TalentProfile::getUserId, record.getJobSeekerId()));
        if (talentProfile != null) {
            response.setSkills(talentProfile.getSkills());
            response.setSalaryExpectation(talentProfile.getSalaryExpectation());
            response.setStrengthsSummary(talentProfile.getStrengthsSummary());
            response.setExpectedPosition(talentProfile.getCareerGoals());

            String[] educationNames = {"", "高中及以下", "大专", "本科", "硕士", "博士"};
            try {
                int eduLevel = Integer.parseInt(talentProfile.getEducation());
                if (eduLevel >= 1 && eduLevel <= 5) {
                    response.setEducation(educationNames[eduLevel]);
                }
            } catch (Exception ignored) {
            }
        }

        Position position = positionMapper.selectById(positionId);
        if (position != null) {
            response.setPositionId(positionId);
            response.setPositionTitle(position.getTitle());
        }

        response.setMatchScore(record.getMatchScore());
        response.setSkillMatchRate(record.getSkillMatchRate());
        response.setExperienceMatchRate(record.getExperienceMatchRate());
        response.setEducationMatchRate(record.getEducationMatchRate());
        response.setSalaryMatchRate(record.getSalaryMatchRate());

        try {
            if (record.getMatchDetails() != null) {
                Map<String, Object> details = objectMapper.readValue(record.getMatchDetails(), Map.class);
                TalentRecommendResponse.MatchDetails matchDetails = new TalentRecommendResponse.MatchDetails();
                matchDetails.setMatchedSkills((List<String>) details.get("matchedSkills"));
                matchDetails.setMissingSkills((List<String>) details.get("missingSkills"));
                matchDetails.setMatchDescription((String) details.get("matchDescription"));
                response.setMatchDetails(matchDetails);
            }
        } catch (Exception e) {
            log.error("解析匹配详情失败", e);
        }

        return response;
    }
}
