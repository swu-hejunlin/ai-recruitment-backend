package com.example.airecruitmentbackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.airecruitmentbackend.common.AIConstant;
import com.example.airecruitmentbackend.dto.JobProfileExtractDTO;
import com.example.airecruitmentbackend.dto.JobProfileResponse;
import com.example.airecruitmentbackend.entity.Position;
import com.example.airecruitmentbackend.entity.JobProfile;
import com.example.airecruitmentbackend.entity.JobSkillTag;
import com.example.airecruitmentbackend.mapper.PositionMapper;
import com.example.airecruitmentbackend.mapper.JobProfileMapper;
import com.example.airecruitmentbackend.mapper.JobSkillTagMapper;
import com.example.airecruitmentbackend.service.JobProfileService;
import com.fasterxml.jackson.databind.ObjectMapper;
import ai.z.openapi.ZhipuAiClient;
import ai.z.openapi.service.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

/**
 * 岗位画像Service实现类
 */
@Slf4j
@Service
public class JobProfileServiceImpl extends ServiceImpl<JobProfileMapper, JobProfile> implements JobProfileService {

    @Autowired
    private PositionMapper positionMapper;

    @Autowired
    private JobSkillTagMapper jobSkillTagMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    @Qualifier("jobProfileExecutor")
    private ExecutorService jobProfileExecutor;

    @Override
    @Transactional
    public JobProfileResponse generateJobProfile(Long positionId) {
        log.info("==========================================");
        log.info("开始生成岗位画像流程，岗位ID：{}", positionId);
        log.info("==========================================");

        Position position = positionMapper.selectById(positionId);
        if (position == null) {
            log.error("岗位不存在，ID：{}", positionId);
            throw new RuntimeException("岗位不存在，ID：" + positionId);
        }
        log.info("获取岗位信息成功，职位名称：{}", position.getTitle());

        JobProfileExtractDTO extractDTO = extractJobProfileInfo(position);
        log.info("提取岗位画像信息完成，技能数量：{}，岗位标签数量：{}",
                 extractDTO.getSkills() != null ? extractDTO.getSkills().size() : 0,
                 extractDTO.getJobTags() != null ? extractDTO.getJobTags().size() : 0);

        JobProfile jobProfile = new JobProfile();
        jobProfile.setPositionId(positionId);
        try {
            jobProfile.setSkills(objectMapper.writeValueAsString(extractDTO.getSkills()));
            jobProfile.setJobTags(objectMapper.writeValueAsString(extractDTO.getJobTags()));
            jobProfile.setMatchKeywords(objectMapper.writeValueAsString(extractDTO.getMatchKeywords()));
            log.info("序列化JSON数据成功");
        } catch (Exception e) {
            log.error("序列化JSON失败", e);
        }
        jobProfile.setEducationRequire(String.valueOf(extractDTO.getEducationLevel()));
        jobProfile.setExperienceRequire(String.valueOf(extractDTO.getExperienceLevel()));
        jobProfile.setSalaryMin(extractDTO.getSalaryMin());
        jobProfile.setSalaryMax(extractDTO.getSalaryMax());
        jobProfile.setDescriptionSummary(extractDTO.getDescriptionSummary());
        jobProfile.setResponsibilitiesSummary(extractDTO.getResponsibilitiesSummary());
        jobProfile.setRequirementsSummary(extractDTO.getRequirementsSummary());
        jobProfile.setCompanyBenefits(extractDTO.getCompanyBenefits());

        JobProfile existing = baseMapper.selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<JobProfile>()
                .eq(JobProfile::getPositionId, positionId)
        );

        if (existing != null) {
            jobProfile.setId(existing.getId());
            baseMapper.updateById(jobProfile);
            log.info("更新岗位画像成功，岗位ID：{}", positionId);
        } else {
            baseMapper.insert(jobProfile);
            log.info("创建岗位画像成功，岗位ID：{}", positionId);
        }

        position.setProfileGenerated(1);
        positionMapper.updateById(position);
        log.info("更新岗位画像生成标记成功");

        saveJobSkillTags(positionId, extractDTO.getSkills());

        JobProfileResponse response = buildJobProfileResponse(jobProfile, position);
        log.info("岗位画像生成流程完成，返回响应数据");
        log.info("==========================================");
        return response;
    }

    @Override
    public CompletableFuture<JobProfileResponse> generateJobProfileAsync(Long positionId) {
        return CompletableFuture.supplyAsync(() -> {
            log.info("==========================================");
            log.info("开始异步生成岗位画像，岗位ID：{}", positionId);
            log.info("==========================================");
            try {
                JobProfileResponse response = generateJobProfile(positionId);
                log.info("异步生成岗位画像完成，岗位ID：{}", positionId);
                log.info("==========================================");
                return response;
            } catch (Exception e) {
                log.error("异步生成岗位画像失败，岗位ID：{}", positionId, e);
                throw new RuntimeException("异步生成岗位画像失败", e);
            }
        }, jobProfileExecutor);
    }

    @Override
    public JobProfileResponse getJobProfile(Long positionId) {
        log.info("==========================================");
        log.info("开始获取岗位画像，岗位ID：{}", positionId);
        log.info("==========================================");

        JobProfile jobProfile = baseMapper.selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<JobProfile>()
                .eq(JobProfile::getPositionId, positionId)
        );

        if (jobProfile == null) {
            log.info("岗位画像不存在，需要生成，岗位ID：{}", positionId);
            JobProfileResponse response = generateJobProfile(positionId);
            log.info("岗位画像生成并返回成功");
            log.info("==========================================");
            return response;
        }

        log.info("岗位画像已存在，直接返回，岗位ID：{}", positionId);
        Position position = positionMapper.selectById(positionId);
        JobProfileResponse response = buildJobProfileResponse(jobProfile, position);
        log.info("获取岗位画像完成");
        log.info("==========================================");
        return response;
    }

    @Override
    @Transactional
    public JobProfileResponse updateJobProfile(Long positionId) {
        log.info("==========================================");
        log.info("开始更新岗位画像，岗位ID：{}", positionId);
        log.info("==========================================");

        JobProfileResponse response = generateJobProfile(positionId);
        log.info("岗位画像更新完成");
        log.info("==========================================");
        return response;
    }

    @Override
    public CompletableFuture<JobProfileResponse> updateJobProfileAsync(Long positionId) {
        return CompletableFuture.supplyAsync(() -> {
            log.info("==========================================");
            log.info("开始异步更新岗位画像，岗位ID：{}", positionId);
            log.info("==========================================");
            try {
                JobProfileResponse response = updateJobProfile(positionId);
                log.info("异步更新岗位画像完成，岗位ID：{}", positionId);
                log.info("==========================================");
                return response;
            } catch (Exception e) {
                log.error("异步更新岗位画像失败，岗位ID：{}", positionId, e);
                throw new RuntimeException("异步更新岗位画像失败", e);
            }
        }, jobProfileExecutor);
    }

    @Override
    @Transactional
    public boolean deleteJobProfile(Long positionId) {
        log.info("==========================================");
        log.info("开始删除岗位画像，岗位ID：{}", positionId);
        log.info("==========================================");

        int deleted = baseMapper.delete(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<JobProfile>()
                .eq(JobProfile::getPositionId, positionId)
        );
        log.info("删除岗位画像记录，影响行数：{}", deleted);

        int skillTagsDeleted = jobSkillTagMapper.delete(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<JobSkillTag>()
                .eq(JobSkillTag::getPositionId, positionId)
        );
        log.info("删除岗位技能标签，影响行数：{}", skillTagsDeleted);

        Position position = positionMapper.selectById(positionId);
        if (position != null) {
            position.setProfileGenerated(0);
            positionMapper.updateById(position);
            log.info("更新岗位画像生成标记为0");
        } else {
            log.warn("岗位不存在，无法更新画像生成标记");
        }

        boolean success = deleted > 0;
        log.info("删除岗位画像完成，结果：{}", success ? "成功" : "失败");
        log.info("==========================================");
        return success;
    }

    private JobProfileExtractDTO extractJobProfileInfo(Position position) {
        log.info("==========================================");
        log.info("开始提取岗位画像信息，职位ID：{}", position.getId());
        log.info("职位名称：{}", position.getTitle());
        log.info("==========================================");

        try {
            // 构建岗位描述文本
            // 注意：position表中的薪资单位是K/月
            StringBuilder jobText = new StringBuilder();
            jobText.append("职位名称：").append(position.getTitle()).append("\n");
            jobText.append("职位类别：").append(position.getCategory()).append("\n");
            jobText.append("工作城市：").append(position.getCity()).append("\n");
            if (position.getSalaryMin() != null && position.getSalaryMax() != null) {
                jobText.append("薪资范围：").append(position.getSalaryMin()).append("-").append(position.getSalaryMax()).append("K/月\n");
            } else {
                jobText.append("薪资范围：面议\n");
            }
            jobText.append("学历要求：").append(position.getEducationMin() != null ? position.getEducationMin() : "不限").append("\n");
            jobText.append("工作经验：").append(position.getWorkYearsMin() != null ? position.getWorkYearsMin() : "不限").append("年\n");
            jobText.append("岗位职责：").append(position.getDescription()).append("\n");
            jobText.append("任职要求：").append(position.getRequirement()).append("\n");

            log.info("构建岗位描述文本完成，文本长度：{}", jobText.length());

            // 调用GLM模型分析
            log.info("开始调用GLM模型进行智能分析");
            String analysisResult = analyzeWithGLM(jobText.toString());
            log.info("GLM模型分析完成，分析结果长度：{}", analysisResult.length());
            log.info("开始解析GLM模型分析结果");

            // 解析分析结果
            JobProfileExtractDTO dto = parseJobProfileResult(analysisResult, position);
            log.info("岗位画像分析结果解析完成");
            log.info("技能标签数量：{}", dto.getSkills() != null ? dto.getSkills().size() : 0);
            log.info("岗位标签数量：{}", dto.getJobTags() != null ? dto.getJobTags().size() : 0);
            log.info("匹配关键词数量：{}", dto.getMatchKeywords() != null ? dto.getMatchKeywords().size() : 0);
            log.info("学历要求：{}", dto.getEducationLevel());
            log.info("经验要求：{}", dto.getExperienceLevel());
            log.info("薪资范围：{}-{}K/月", dto.getSalaryMin(), dto.getSalaryMax());
            log.info("==========================================");
            return dto;
        } catch (Exception e) {
            log.error("提取岗位画像信息失败", e);
            // 失败时返回默认值
            log.warn("使用默认岗位画像信息");
            JobProfileExtractDTO defaultDto = getDefaultJobProfileInfo(position);
            log.info("默认岗位画像信息准备完成");
            log.info("==========================================");
            return defaultDto;
        }
    }

    /**
     * 使用GLM模型分析岗位信息
     */
    private String analyzeWithGLM(String jobText) {
        // 1. 检查API密钥
        String apiKey = System.getenv("ZAI_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            throw new RuntimeException("ZAI_API_KEY环境变量未设置");
        }

        // 2. 使用glm-4.5模型
        String model = AIConstant.FLASH_FREE_MODEL;

        // 3. 创建GLM客户端
        ZhipuAiClient client = ZhipuAiClient.builder().ofZHIPU()
                .apiKey(apiKey)
                .build();

        // 4. 构建请求
        log.info("开始构建GLM模型请求");
        ChatCompletionCreateParams request = ChatCompletionCreateParams.builder()
                .model(model)
                .messages(Arrays.asList(
                        ChatMessage.builder()
                                .role(ChatMessageRole.USER.value())
                                .content("你是一个专业的招聘专家，请从以下岗位描述中提取结构化的岗位画像信息。\n\n" +
                                        "要求：\n" +
                                        "1. 只返回JSON格式，不要包含任何解释或说明\n" +
                                        "2. 严格按照以下JSON Schema返回\n" +
                                        "3. 如果某字段在岗位描述中无法确定，设置为null或空列表\n" +
                                        "4. 技能标签请提取岗位职责和任职要求中提到的核心技能\n" +
                                        "5. 岗位标签请提取行业、职位类型等标签\n" +
                                        "6. 匹配关键词请提取用于匹配人才的重要关键词\n" +
                                        "\n" +
                                        "JSON Schema:\n" +
                                        "{\n" +
                                        "  \"skills\": [\"技能1\", \"技能2\", \"技能3\"],\n" +
                                        "  \"educationLevel\": 3, // 1-高中及以下，2-大专，3-本科，4-硕士，5-博士\n" +
                                        "  \"experienceLevel\": 3, // 1-不限，2-1年以下，3-1-3年，4-3-5年，5-5-10年，6-10年以上\n" +
                                        "  \"salaryMin\": 15, // 最低薪资（K/月）\n" +
                                        "  \"salaryMax\": 30, // 最高薪资（K/月）\n" +
                                        "  \"jobTags\": [\"标签1\", \"标签2\"],\n" +
                                        "  \"descriptionSummary\": \"岗位职责摘要\",\n" +
                                        "  \"responsibilitiesSummary\": \"工作职责摘要\",\n" +
                                        "  \"requirementsSummary\": \"任职要求摘要\",\n" +
                                        "  \"companyBenefits\": \"公司福利\",\n" +
                                        "  \"matchKeywords\": [\"关键词1\", \"关键词2\"]\n" +
                                        "}\n" +
                                        "\n" +
                                        "岗位描述：\n" +
                                        jobText + "\n\n" +
                                        "请提取信息并以 JSON 格式输出。如果无法提取，对应字段设为 null。注意：你可以输出 JSON 代码块，无需额外解释。")
                                .build()
                ))
                .responseFormat(ResponseFormat.builder()
                        .type("json_object")
                        .build())
                .temperature(0.1f)
                .maxTokens(2000)
                .build();

        // 5. 调用API
        log.info("调用GLM模型API进行岗位画像分析");
        long modelStartTime = System.currentTimeMillis();
        try {
            ChatCompletionResponse response = client.chat().createChatCompletion(request);
            long modelEndTime = System.currentTimeMillis();
            log.info("GLM模型API调用完成，状态: {}, 耗时: {}ms",
                     response.isSuccess() ? "成功" : "失败",
                     (modelEndTime - modelStartTime));

            if (response.isSuccess()) {
                Object contentObj = response.getData().getChoices().get(0).getMessage().getContent();
                if (contentObj == null) {
                    log.error("GLM模型返回空内容");
                    throw new RuntimeException("GLM模型返回空内容");
                }

                String content;
                if (contentObj instanceof String) {
                    content = (String) contentObj;
                } else {
                    // 如果返回的是对象，转换为JSON字符串
                    try {
                        com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
                        content = objectMapper.writeValueAsString(contentObj);
                    } catch (Exception e) {
                        log.error("转换返回对象为JSON失败", e);
                        throw new RuntimeException("转换返回对象为JSON失败: " + e.getMessage(), e);
                    }
                }

                log.info("GLM模型分析结果长度: {}", content.length());

                if (content.isEmpty()) {
                    log.error("GLM模型返回空字符串");
                    throw new RuntimeException("GLM模型返回空字符串");
                }

                return content;
            } else {
                log.error("GLM模型分析失败，错误信息: {}", response.getMsg());
                throw new RuntimeException("GLM模型分析失败: " + response.getMsg());
            }
        } catch (Exception e) {
            long modelEndTime = System.currentTimeMillis();
            log.error("GLM模型API调用异常，耗时: {}ms", (modelEndTime - modelStartTime), e);
            throw new RuntimeException("GLM模型分析失败: " + e.getMessage(), e);
        }
    }

    /**
     * 解析岗位画像分析结果
     */
    private JobProfileExtractDTO parseJobProfileResult(String jsonResult, Position position) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();

            // 清理JSON结果，处理Markdown代码块
            String cleanedResult = jsonResult.trim();
            if (cleanedResult.startsWith("```json") || cleanedResult.startsWith("```")) {
                // 提取代码块内容
                int startIndex = cleanedResult.indexOf("\n");
                int endIndex = cleanedResult.lastIndexOf("```");
                if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
                    cleanedResult = cleanedResult.substring(startIndex + 1, endIndex).trim();
                    log.info("已从Markdown代码块中提取JSON内容");
                }
            }

            // 解析JSON
            JobProfileExtractDTO dto = objectMapper.readValue(cleanedResult, JobProfileExtractDTO.class);

            // 确保技能列表不为空
            if (dto.getSkills() == null || dto.getSkills().isEmpty()) {
                List<String> defaultSkills = new ArrayList<>();
                defaultSkills.add("Java");
                defaultSkills.add("Spring Boot");
                defaultSkills.add("MySQL");
                dto.setSkills(defaultSkills);
            }

            // 确保岗位标签不为空
            if (dto.getJobTags() == null || dto.getJobTags().isEmpty()) {
                List<String> defaultTags = new ArrayList<>();
                defaultTags.add("互联网");
                defaultTags.add("技术开发");
                dto.setJobTags(defaultTags);
            }

            // 确保匹配关键词不为空
            if (dto.getMatchKeywords() == null || dto.getMatchKeywords().isEmpty()) {
                List<String> defaultKeywords = new ArrayList<>();
                defaultKeywords.add("Java");
                defaultKeywords.add("Spring");
                defaultKeywords.add("后端开发");
                dto.setMatchKeywords(defaultKeywords);
            }

            // 如果薪资为空，使用默认值
            if (dto.getSalaryMin() == null || dto.getSalaryMax() == null) {
                if (position.getSalaryMin() != null && position.getSalaryMax() != null) {
                    dto.setSalaryMin(new BigDecimal(position.getSalaryMin()));
                    dto.setSalaryMax(new BigDecimal(position.getSalaryMax()));
                } else {
                    dto.setSalaryMin(new BigDecimal("15"));
                    dto.setSalaryMax(new BigDecimal("30"));
                }
            }

            log.info("岗位画像分析结果解析完成，技能数量：{}", dto.getSkills().size());
            return dto;
        } catch (Exception e) {
            log.error("解析岗位画像分析结果失败", e);
            // 解析失败时返回默认值
            return getDefaultJobProfileInfo(position);
        }
    }

    /**
     * 获取默认岗位画像信息（当AI分析失败时使用）
     */
    private JobProfileExtractDTO getDefaultJobProfileInfo(Position position) {
        JobProfileExtractDTO dto = new JobProfileExtractDTO();

        List<String> skills = new ArrayList<>();
        skills.add("Java");
        skills.add("Spring Boot");
        skills.add("MySQL");
        dto.setSkills(skills);

        dto.setEducationLevel(position.getEducationMin() != null ? position.getEducationMin() : 3);
        dto.setExperienceLevel(3);

        // 保持薪资单位为K/月
        if (position.getSalaryMin() != null && position.getSalaryMax() != null) {
            dto.setSalaryMin(new BigDecimal(position.getSalaryMin()));
            dto.setSalaryMax(new BigDecimal(position.getSalaryMax()));
        } else {
            dto.setSalaryMin(new BigDecimal("15"));
            dto.setSalaryMax(new BigDecimal("30"));
        }

        List<String> jobTags = new ArrayList<>();
        jobTags.add("互联网");
        jobTags.add("技术开发");
        dto.setJobTags(jobTags);

        dto.setDescriptionSummary(position.getDescription());
        dto.setResponsibilitiesSummary(position.getDescription());
        dto.setRequirementsSummary(position.getRequirement());

        List<String> matchKeywords = new ArrayList<>();
        matchKeywords.add("Java");
        matchKeywords.add("Spring");
        matchKeywords.add("后端开发");
        dto.setMatchKeywords(matchKeywords);

        return dto;
    }

    private void saveJobSkillTags(Long positionId, List<String> skills) {
        log.info("开始保存岗位技能标签，岗位ID：{}，技能数量：{}", positionId, skills != null ? skills.size() : 0);

        if (skills == null || skills.isEmpty()) {
            log.info("技能列表为空，跳过保存，岗位ID：{}", positionId);
            return;
        }

        // 删除原有技能标签
        int deleted = jobSkillTagMapper.delete(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<JobSkillTag>()
                .eq(JobSkillTag::getPositionId, positionId)
        );
        log.info("删除原有技能标签数量：{}", deleted);

        // 保存新技能标签
        int saved = 0;
        for (String skill : skills) {
            JobSkillTag skillTag = new JobSkillTag();
            skillTag.setPositionId(positionId);
            skillTag.setSkillTag(skill);
            skillTag.setSkillLevel("required");
            skillTag.setProficiencyWeight(100);
            jobSkillTagMapper.insert(skillTag);
            saved++;
        }
        log.info("保存新技能标签数量：{}", saved);
    }

    private JobProfileResponse buildJobProfileResponse(JobProfile jobProfile, Position position) {
        JobProfileResponse response = new JobProfileResponse();
        response.setId(jobProfile.getId());
        response.setPositionId(jobProfile.getPositionId());
        response.setJobName(position != null ? position.getTitle() : "");
        response.setCompanyName(position != null ? position.getCompanyName() : "");

        try {
            if (jobProfile.getSkills() != null) {
                response.setSkills(objectMapper.readValue(jobProfile.getSkills(), List.class));
            }
            if (jobProfile.getJobTags() != null) {
                response.setJobTags(objectMapper.readValue(jobProfile.getJobTags(), List.class));
            }
            if (jobProfile.getMatchKeywords() != null) {
                response.setMatchKeywords(objectMapper.readValue(jobProfile.getMatchKeywords(), List.class));
            }
        } catch (Exception e) {
            log.error("解析JSON失败", e);
        }

        if (jobProfile.getEducationRequire() != null) {
            response.setEducationLevel(Integer.parseInt(jobProfile.getEducationRequire()));
            response.setEducationRequire(getEducationText(response.getEducationLevel()));
        }

        if (jobProfile.getExperienceRequire() != null) {
            response.setExperienceLevel(Integer.parseInt(jobProfile.getExperienceRequire()));
            response.setExperienceRequire(getExperienceText(response.getExperienceLevel()));
        }

        response.setSalaryMin(jobProfile.getSalaryMin());
        response.setSalaryMax(jobProfile.getSalaryMax());
        if (jobProfile.getSalaryMin() != null && jobProfile.getSalaryMax() != null) {
            response.setSalaryRange(jobProfile.getSalaryMin() + "-" + jobProfile.getSalaryMax() + "K/月");
        }

        response.setDescriptionSummary(jobProfile.getDescriptionSummary());
        response.setResponsibilitiesSummary(jobProfile.getResponsibilitiesSummary());
        response.setRequirementsSummary(jobProfile.getRequirementsSummary());
        response.setCompanyBenefits(jobProfile.getCompanyBenefits());

        if (jobProfile.getCreatedAt() != null) {
            response.setCreatedAt(jobProfile.getCreatedAt().toString());
        }

        return response;
    }

    private String getEducationText(Integer level) {
        if (level == null) return "不限";
        return switch (level) {
            case 1 -> "高中及以下";
            case 2 -> "大专";
            case 3 -> "本科";
            case 4 -> "硕士";
            case 5 -> "博士";
            default -> "不限";
        };
    }

    private String getExperienceText(Integer level) {
        if (level == null) return "不限";
        return switch (level) {
            case 1 -> "不限";
            case 2 -> "1年以下";
            case 3 -> "1-3年";
            case 4 -> "3-5年";
            case 5 -> "5-10年";
            case 6 -> "10年以上";
            default -> "不限";
        };
    }
}