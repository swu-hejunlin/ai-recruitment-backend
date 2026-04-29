package com.example.airecruitmentbackend.service;

import com.example.airecruitmentbackend.entity.*;
import com.example.airecruitmentbackend.mapper.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 画像向量服务
 * 用于生成和更新人才/岗位画像的语义向量
 */
@Slf4j
@Service
public class ProfileVectorService {

    @Autowired
    private EmbeddingService embeddingService;

    @Autowired
    private TalentProfileMapper talentProfileMapper;

    @Autowired
    private JobProfileMapper jobProfileMapper;

    @Autowired
    private JobSeekerMapper jobSeekerMapper;

    @Autowired
    private EducationMapper educationMapper;

    @Autowired
    private ExperienceMapper experienceMapper;

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 生成人才画像向量
     */
    public boolean generateTalentProfileVector(TalentProfile talentProfile) {
        try {
            String text = buildTalentProfileText(talentProfile);
            log.info("生成人才画像向量，输入文本长度: {}", text.length());

            List<Float> embedding = embeddingService.getEmbedding(text);
            if (embedding == null || embedding.isEmpty()) {
                log.error("获取向量失败");
                return false;
            }

            String vectorJson = objectMapper.writeValueAsString(embedding);
            talentProfile.setEmbeddingVector(vectorJson);
            talentProfileMapper.updateById(talentProfile);

            log.info("人才画像向量生成成功，userId: {}, 向量维度: {}", talentProfile.getUserId(), embedding.size());
            return true;
        } catch (Exception e) {
            log.error("生成人才画像向量失败: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 生成岗位画像向量
     */
    public boolean generateJobProfileVector(JobProfile jobProfile) {
        try {
            String text = buildJobProfileText(jobProfile);
            log.info("生成岗位画像向量，输入文本长度: {}", text.length());

            List<Float> embedding = embeddingService.getEmbedding(text);
            if (embedding == null || embedding.isEmpty()) {
                log.error("获取向量失败");
                return false;
            }

            String vectorJson = objectMapper.writeValueAsString(embedding);
            jobProfile.setEmbeddingVector(vectorJson);
            jobProfileMapper.updateById(jobProfile);

            log.info("岗位画像向量生成成功，positionId: {}, 向量维度: {}", jobProfile.getPositionId(), embedding.size());
            return true;
        } catch (Exception e) {
            log.error("生成岗位画像向量失败: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 根据人才画像构建完整的文本描述
     */
    private String buildTalentProfileText(TalentProfile profile) {
        StringBuilder sb = new StringBuilder();

        sb.append("【人才简历信息】");

        // 查询并添加求职者基本信息
        JobSeeker jobSeeker = jobSeekerMapper.selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<JobSeeker>()
                .eq(JobSeeker::getUserId, profile.getUserId())
        );

        if (jobSeeker != null) {
            sb.append("姓名：").append(jobSeeker.getName()).append("；");
            if (jobSeeker.getCity() != null) {
                sb.append("所在城市：").append(jobSeeker.getCity()).append("；");
            }
            if (jobSeeker.getIntroduction() != null && !jobSeeker.getIntroduction().isEmpty()) {
                sb.append("个人简介：").append(jobSeeker.getIntroduction()).append("；");
            }
        }

        // 添加技能信息
        if (profile.getSkills() != null && !profile.getSkills().isEmpty()) {
            sb.append("技能专长：").append(profile.getSkills()).append("；");
        }

        // 添加学历信息
        if (profile.getEducation() != null) {
            sb.append("学历：").append(getEducationName(profile.getEducation())).append("；");
        }

        // 添加工作年限
        if (profile.getWorkYears() != null) {
            sb.append("工作年限：").append(profile.getWorkYears()).append("年；");
        }

        // 添加薪资期望
        if (profile.getSalaryExpectation() != null) {
            sb.append("期望薪资：").append(profile.getSalaryExpectation()).append("K/月；");
        }

        // 添加人才标签
        if (profile.getTalentTags() != null && !profile.getTalentTags().isEmpty()) {
            sb.append("人才标签：").append(profile.getTalentTags()).append("；");
        }

        // 查询并添加教育经历
        List<Education> educations = educationMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Education>()
                .eq(Education::getJobSeekerId, jobSeeker != null ? jobSeeker.getId() : 0)
                .orderByDesc(Education::getEndDate)
        );
        if (educations != null && !educations.isEmpty()) {
            sb.append("【教育经历】");
            for (Education edu : educations) {
                sb.append(edu.getSchoolName());
                if (edu.getMajor() != null) {
                    sb.append(" - ").append(edu.getMajor());
                }
                if (edu.getEducationLevel() != null) {
                    sb.append(" (").append(getEducationLevelName(edu.getEducationLevel())).append(")");
                }
                sb.append("；");
            }
        }

        // 查询并添加工作/实习经历
        List<Experience> experiences = experienceMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Experience>()
                .eq(Experience::getJobSeekerId, jobSeeker != null ? jobSeeker.getId() : 0)
                .orderByDesc(Experience::getEndDate)
        );
        if (experiences != null && !experiences.isEmpty()) {
            sb.append("【工作经历】");
            for (Experience exp : experiences) {
                sb.append(exp.getCompanyName()).append(" - ").append(exp.getPosition());
                if (exp.getStartDate() != null && exp.getEndDate() != null) {
                    sb.append(" (").append(exp.getStartDate()).append("至").append(exp.getEndDate()).append(")");
                }
                if (exp.getDescription() != null && !exp.getDescription().isEmpty()) {
                    sb.append("：").append(exp.getDescription());
                }
                sb.append("；");
            }
        }

        // 查询并添加项目经历
        List<Project> projects = projectMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Project>()
                .eq(Project::getJobSeekerId, jobSeeker != null ? jobSeeker.getId() : 0)
                .orderByDesc(Project::getEndDate)
        );
        if (projects != null && !projects.isEmpty()) {
            sb.append("【项目经历】");
            for (Project proj : projects) {
                sb.append(proj.getProjectName());
                if (proj.getProjectRole() != null && !proj.getProjectRole().isEmpty()) {
                    sb.append("（").append(proj.getProjectRole()).append("）");
                }
                if (proj.getDescription() != null && !proj.getDescription().isEmpty()) {
                    sb.append("：").append(proj.getDescription());
                }
                sb.append("；");
            }
        }

        // 添加AI评估信息
        if (profile.getStrengthsSummary() != null && !profile.getStrengthsSummary().isEmpty()) {
            sb.append("【优势亮点】").append(profile.getStrengthsSummary()).append("；");
        }

        if (profile.getCareerGoals() != null && !profile.getCareerGoals().isEmpty()) {
            sb.append("【职业目标】").append(profile.getCareerGoals()).append("；");
        }

        return sb.toString();
    }

    /**
     * 根据岗位画像构建文本描述
     */
    private String buildJobProfileText(JobProfile profile) {
        StringBuilder sb = new StringBuilder();

        sb.append("【职位招聘信息】");

        if (profile.getSkills() != null && !profile.getSkills().isEmpty()) {
            sb.append("技能要求：").append(profile.getSkills()).append("；");
        }

        if (profile.getEducationRequire() != null) {
            sb.append("学历要求：").append(getEducationRequireName(profile.getEducationRequire())).append("；");
        }

        if (profile.getExperienceRequire() != null) {
            sb.append("经验要求：").append(getExperienceRequireName(profile.getExperienceRequire())).append("；");
        }

        if (profile.getSalaryMin() != null && profile.getSalaryMax() != null) {
            sb.append("薪资范围：").append(profile.getSalaryMin()).append("K-").append(profile.getSalaryMax()).append("K/月；");
        }

        if (profile.getJobTags() != null && !profile.getJobTags().isEmpty()) {
            sb.append("职位标签：").append(profile.getJobTags()).append("；");
        }

        if (profile.getDescriptionSummary() != null && !profile.getDescriptionSummary().isEmpty()) {
            sb.append("职位描述：").append(profile.getDescriptionSummary()).append("；");
        }

        if (profile.getResponsibilitiesSummary() != null && !profile.getResponsibilitiesSummary().isEmpty()) {
            sb.append("工作职责：").append(profile.getResponsibilitiesSummary()).append("；");
        }

        if (profile.getRequirementsSummary() != null && !profile.getRequirementsSummary().isEmpty()) {
            sb.append("任职要求：").append(profile.getRequirementsSummary()).append("；");
        }

        if (profile.getCompanyBenefits() != null && !profile.getCompanyBenefits().isEmpty()) {
            sb.append("公司福利：").append(profile.getCompanyBenefits()).append("；");
        }

        return sb.toString();
    }

    private String getEducationName(String education) {
        return switch (education) {
            case "1" -> "高中及以下";
            case "2" -> "大专";
            case "3" -> "本科";
            case "4" -> "硕士";
            case "5" -> "博士";
            default -> "未知";
        };
    }

    private String getEducationLevelName(Integer level) {
        if (level == null) return "未知";
        return switch (level) {
            case 1 -> "高中及以下";
            case 2 -> "大专";
            case 3 -> "本科";
            case 4 -> "硕士";
            case 5 -> "博士";
            default -> "未知";
        };
    }

    private String getEducationRequireName(String education) {
        return switch (education) {
            case "1" -> "不限";
            case "2" -> "高中及以下";
            case "3" -> "大专";
            case "4" -> "本科";
            case "5" -> "硕士";
            case "6" -> "博士";
            default -> "未知";
        };
    }

    private String getExperienceRequireName(String experience) {
        return switch (experience) {
            case "1" -> "不限";
            case "2" -> "1年以下";
            case "3" -> "1-3年";
            case "4" -> "3-5年";
            case "5" -> "5-10年";
            case "6" -> "10年以上";
            default -> "未知";
        };
    }
}