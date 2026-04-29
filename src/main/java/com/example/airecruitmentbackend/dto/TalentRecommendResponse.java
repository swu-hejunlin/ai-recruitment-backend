package com.example.airecruitmentbackend.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class TalentRecommendResponse {
    private Long jobSeekerId;
    private Long userId;
    private String name;
    private String avatarUrl;
    private Integer workYears;
    private String education;
    private String skills;
    private BigDecimal salaryExpectation;
    private String currentStatus;
    private String expectedPosition;
    private String expectedCity;
    private String selfIntroduction;
    private String strengthsSummary;
    private BigDecimal matchScore;
    private BigDecimal skillMatchRate;
    private BigDecimal experienceMatchRate;
    private BigDecimal educationMatchRate;
    private BigDecimal salaryMatchRate;
    private MatchDetails matchDetails;
    private Long positionId;
    private String positionTitle;

    @Data
    public static class MatchDetails {
        private List<String> matchedSkills;
        private List<String> missingSkills;
        private String matchDescription;
    }
}
