package com.example.airecruitmentbackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.airecruitmentbackend.common.AIConstant;
import com.example.airecruitmentbackend.dto.TalentProfileExtractDTO;
import com.example.airecruitmentbackend.dto.TalentProfileResponse;
import com.example.airecruitmentbackend.entity.*;
import com.example.airecruitmentbackend.mapper.*;
import com.example.airecruitmentbackend.service.TalentProfileService;
import com.fasterxml.jackson.databind.ObjectMapper;
import ai.z.openapi.ZhipuAiClient;
import ai.z.openapi.service.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

/**
 * 人才画像Service实现类
 */
@Slf4j
@Service
public class TalentProfileServiceImpl extends ServiceImpl<TalentProfileMapper, TalentProfile> implements TalentProfileService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JobSeekerMapper jobSeekerMapper;

    @Autowired
    private TalentSkillTagMapper talentSkillTagMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    @Qualifier("asyncExecutor")
    private ExecutorService asyncExecutor;

    @Override
    @Transactional
    public TalentProfileResponse generateTalentProfile(Long userId) {
        log.info("==========================================");
        log.info("开始生成人才画像流程，用户ID：{}", userId);
        log.info("==========================================");

        User user = userMapper.selectById(userId);
        if (user == null) {
            log.error("用户不存在，ID：{}", userId);
            throw new RuntimeException("用户不存在，ID：" + userId);
        }
        log.info("获取用户信息成功，用户手机号：{}", user.getPhone());

        TalentProfileExtractDTO extractDTO = extractTalentProfileInfo(userId, user);
        log.info("提取人才画像信息完成，技能数量：{}，人才标签数量：{}",
                 extractDTO.getSkills() != null ? extractDTO.getSkills().size() : 0,
                 extractDTO.getTalentTags() != null ? extractDTO.getTalentTags().size() : 0);

        TalentProfile talentProfile = new TalentProfile();
        talentProfile.setUserId(userId);
        try {
            talentProfile.setSkills(objectMapper.writeValueAsString(extractDTO.getSkills()));
            talentProfile.setTalentTags(objectMapper.writeValueAsString(extractDTO.getTalentTags()));
            talentProfile.setMatchKeywords(objectMapper.writeValueAsString(extractDTO.getMatchKeywords()));
            log.info("序列化JSON数据成功");
        } catch (Exception e) {
            log.error("序列化JSON失败", e);
        }
        talentProfile.setEducation(String.valueOf(extractDTO.getEducationLevel()));
        talentProfile.setWorkYears(extractDTO.getWorkYears());
        talentProfile.setSalaryExpectation(extractDTO.getSalaryExpectation());
        talentProfile.setCurrentSalary(extractDTO.getCurrentSalary());
        talentProfile.setDescriptionSummary(extractDTO.getDescriptionSummary());
        talentProfile.setStrengthsSummary(extractDTO.getStrengthsSummary());
        talentProfile.setCareerGoals(extractDTO.getCareerGoals());
        talentProfile.setAiEvaluation(extractDTO.getStrengthsSummary());

        TalentProfile existing = baseMapper.selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<TalentProfile>()
                .eq(TalentProfile::getUserId, userId)
        );

        if (existing != null) {
            talentProfile.setId(existing.getId());
            baseMapper.updateById(talentProfile);
            log.info("更新人才画像成功，用户ID：{}", userId);
        } else {
            baseMapper.insert(talentProfile);
            log.info("创建人才画像成功，用户ID：{}", userId);
        }

        user.setProfileGenerated(1);
        userMapper.updateById(user);
        log.info("更新用户画像生成标记成功");

        saveTalentSkillTags(userId, extractDTO.getSkills());

        TalentProfileResponse response = buildTalentProfileResponse(talentProfile, user);
        log.info("人才画像生成流程完成，返回响应数据");
        log.info("==========================================");
        return response;
    }

    @Override
    public CompletableFuture<TalentProfileResponse> generateTalentProfileAsync(Long userId) {
        return CompletableFuture.supplyAsync(() -> {
            log.info("==========================================");
            log.info("开始异步生成人才画像，用户ID：{}", userId);
            log.info("==========================================");
            try {
                TalentProfileResponse response = generateTalentProfile(userId);
                log.info("异步生成人才画像完成，用户ID：{}", userId);
                log.info("==========================================");
                return response;
            } catch (Exception e) {
                log.error("异步生成人才画像失败，用户ID：{}", userId, e);
                throw new RuntimeException("异步生成人才画像失败", e);
            }
        }, asyncExecutor);
    }

    @Override
    public TalentProfileResponse getTalentProfile(Long userId) {
        log.info("==========================================");
        log.info("开始获取人才画像，用户ID：{}", userId);
        log.info("==========================================");

        TalentProfile talentProfile = baseMapper.selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<TalentProfile>()
                .eq(TalentProfile::getUserId, userId)
        );

        if (talentProfile == null) {
            log.info("人才画像不存在，需要生成，用户ID：{}", userId);
            TalentProfileResponse response = generateTalentProfile(userId);
            log.info("人才画像生成并返回成功");
            log.info("==========================================");
            return response;
        }

        log.info("人才画像已存在，直接返回，用户ID：{}", userId);
        User user = userMapper.selectById(userId);
        TalentProfileResponse response = buildTalentProfileResponse(talentProfile, user);
        log.info("获取人才画像完成");
        log.info("==========================================");
        return response;
    }

    @Override
    @Transactional
    public TalentProfileResponse updateTalentProfile(Long userId) {
        log.info("==========================================");
        log.info("开始更新人才画像，用户ID：{}", userId);
        log.info("==========================================");

        TalentProfileResponse response = generateTalentProfile(userId);
        log.info("人才画像更新完成");
        log.info("==========================================");
        return response;
    }

    @Override
    public CompletableFuture<TalentProfileResponse> updateTalentProfileAsync(Long userId) {
        return CompletableFuture.supplyAsync(() -> {
            log.info("==========================================");
            log.info("开始异步更新人才画像，用户ID：{}", userId);
            log.info("==========================================");
            try {
                TalentProfileResponse response = updateTalentProfile(userId);
                log.info("异步更新人才画像完成，用户ID：{}", userId);
                log.info("==========================================");
                return response;
            } catch (Exception e) {
                log.error("异步更新人才画像失败，用户ID：{}", userId, e);
                throw new RuntimeException("异步更新人才画像失败", e);
            }
        }, asyncExecutor);
    }

    @Override
    @Transactional
    public boolean deleteTalentProfile(Long userId) {
        log.info("==========================================");
        log.info("开始删除人才画像，用户ID：{}", userId);
        log.info("==========================================");

        int deleted = baseMapper.delete(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<TalentProfile>()
                .eq(TalentProfile::getUserId, userId)
        );
        log.info("删除人才画像记录，影响行数：{}", deleted);

        int skillTagsDeleted = talentSkillTagMapper.delete(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<TalentSkillTag>()
                .eq(TalentSkillTag::getUserId, userId)
        );
        log.info("删除人才技能标签，影响行数：{}", skillTagsDeleted);

        User user = userMapper.selectById(userId);
        if (user != null) {
            user.setProfileGenerated(0);
            userMapper.updateById(user);
            log.info("更新用户画像生成标记为0");
        } else {
            log.warn("用户不存在，无法更新画像生成标记");
        }

        boolean success = deleted > 0;
        log.info("删除人才画像完成，结果：{}", success ? "成功" : "失败");
        log.info("==========================================");
        return success;
    }

    private TalentProfileExtractDTO extractTalentProfileInfo(Long userId, User user) {
        log.info("==========================================");
        log.info("开始提取人才画像信息，用户ID：{}", userId);
        log.info("==========================================");

        JobSeeker jobSeeker = null;
        try {
            // 获取求职者详细信息
            jobSeeker = jobSeekerMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<JobSeeker>()
                    .eq(JobSeeker::getUserId, userId)
            );
            log.info("获取求职者信息成功，姓名：{}", jobSeeker != null ? jobSeeker.getName() : "未知");

            // 构建人才描述文本
            StringBuilder talentText = new StringBuilder();
            talentText.append("姓名：").append(jobSeeker != null ? jobSeeker.getName() : "未知").append("\n");
            talentText.append("手机号：").append(user.getPhone()).append("\n");
            talentText.append("邮箱：").append(jobSeeker != null ? jobSeeker.getEmail() : "未知").append("\n");
            talentText.append("城市：").append(jobSeeker != null ? jobSeeker.getCity() : "未知").append("\n");
            talentText.append("年龄：").append(jobSeeker != null ? jobSeeker.getAge() : "未知").append("\n");
            talentText.append("性别：").append(jobSeeker != null ? getGenderText(jobSeeker.getGender()) : "未知").append("\n");
            talentText.append("工作年限：").append(jobSeeker != null ? jobSeeker.getWorkYears() : "未知").append("年\n");
            talentText.append("当前薪资：").append(jobSeeker != null ? jobSeeker.getCurrentSalary() : "未知").append("K/月\n");
            talentText.append("期望薪资：").append(jobSeeker != null ? jobSeeker.getExpectedSalary() : "未知").append("K/月\n");
            talentText.append("当前状态：").append(jobSeeker != null ? getCurrentStatusText(jobSeeker.getCurrentStatus()) : "未知").append("\n");
            talentText.append("个人简介：").append(jobSeeker != null ? jobSeeker.getIntroduction() : "暂无").append("\n");
            talentText.append("技能标签：").append(jobSeeker != null ? jobSeeker.getSkills() : "暂无").append("\n");

            log.info("构建人才描述文本完成，文本长度：{}", talentText.length());

            // 调用GLM模型分析
            log.info("开始调用GLM模型进行智能分析");
            String analysisResult = analyzeWithGLM(talentText.toString());
            log.info("GLM模型分析完成，分析结果长度：{}", analysisResult.length());
            log.info("开始解析GLM模型分析结果");

            // 解析分析结果
            TalentProfileExtractDTO dto = parseTalentProfileResult(analysisResult, jobSeeker);
            log.info("人才画像分析结果解析完成");
            log.info("技能标签数量：{}", dto.getSkills() != null ? dto.getSkills().size() : 0);
            log.info("人才标签数量：{}", dto.getTalentTags() != null ? dto.getTalentTags().size() : 0);
            log.info("匹配关键词数量：{}", dto.getMatchKeywords() != null ? dto.getMatchKeywords().size() : 0);
            log.info("学历等级：{}", dto.getEducationLevel());
            log.info("工作年限：{}", dto.getWorkYears());
            log.info("==========================================");
            return dto;
        } catch (Exception e) {
            log.error("提取人才画像信息失败", e);
            // 失败时返回默认值
            log.warn("使用默认人才画像信息");
            TalentProfileExtractDTO defaultDto = getDefaultTalentProfileInfo(jobSeeker);
            log.info("默认人才画像信息准备完成");
            log.info("==========================================");
            return defaultDto;
        }
    }

    /**
     * 使用GLM模型分析人才信息
     */
    private String analyzeWithGLM(String talentText) {
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
                                .content("你是一个专业的职业顾问和人才评估专家，请从以下人才信息中提取结构化的人才画像信息。\n\n" +
                                        "要求：\n" +
                                        "1. 只返回JSON格式，不要包含任何解释或说明\n" +
                                        "2. 严格按照以下JSON Schema返回\n" +
                                        "3. 如果某字段在信息中无法确定，设置为null或空列表\n" +
                                        "4. 技能标签请提取简历中提到的核心技术技能\n" +
                                        "5. 人才标签请提取能够描述人才特点的标签（如：全栈开发、技术达人、团队领袖等）\n" +
                                        "6. 匹配关键词请提取用于匹配岗位的重要关键词\n" +
                                        "7. 优势亮点请总结人才的核心竞争优势\n" +
                                        "8. 职业目标请根据人才背景推断合理的职业发展方向\n" +
                                        "\n" +
                                        "JSON Schema:\n" +
                                        "{\n" +
                                        "  \"skills\": [\"技能1\", \"技能2\", \"技能3\"],\n" +
                                        "  \"educationLevel\": 3, // 1-高中及以下，2-大专，3-本科，4-硕士，5-博士\n" +
                                        "  \"workYears\": 3, // 工作年限（数字）\n" +
                                        "  \"salaryExpectation\": 25, // 期望薪资（K/月）\n" +
                                        "  \"currentSalary\": 20, // 当前薪资（K/月）\n" +
                                        "  \"talentTags\": [\"标签1\", \"标签2\"],\n" +
                                        "  \"descriptionSummary\": \"个人简介摘要\",\n" +
                                        "  \"strengthsSummary\": \"核心优势亮点总结\",\n" +
                                        "  \"careerGoals\": \"职业发展目标\"\n" +
                                        "  \"matchKeywords\": [\"关键词1\", \"关键词2\"]\n" +
                                        "}\n" +
                                        "\n" +
                                        "人才信息：\n" +
                                        talentText + "\n\n" +
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
        log.info("调用GLM模型API进行人才画像分析");
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
     * 解析人才画像分析结果
     */
    private TalentProfileExtractDTO parseTalentProfileResult(String jsonResult, JobSeeker jobSeeker) {
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
            TalentProfileExtractDTO dto = objectMapper.readValue(cleanedResult, TalentProfileExtractDTO.class);

            // 确保技能列表不为空
            if (dto.getSkills() == null || dto.getSkills().isEmpty()) {
                List<String> defaultSkills = new ArrayList<>();
                defaultSkills.add("Java");
                defaultSkills.add("Spring Boot");
                defaultSkills.add("Vue.js");
                dto.setSkills(defaultSkills);
            }

            // 确保人才标签不为空
            if (dto.getTalentTags() == null || dto.getTalentTags().isEmpty()) {
                List<String> defaultTags = new ArrayList<>();
                defaultTags.add("技术达人");
                defaultTags.add("全栈开发");
                dto.setTalentTags(defaultTags);
            }

            // 确保匹配关键词不为空
            if (dto.getMatchKeywords() == null || dto.getMatchKeywords().isEmpty()) {
                List<String> defaultKeywords = new ArrayList<>();
                defaultKeywords.add("Java");
                defaultKeywords.add("后端开发");
                defaultKeywords.add("全栈");
                dto.setMatchKeywords(defaultKeywords);
            }

            // 确保优势亮点不为空
            if (dto.getStrengthsSummary() == null || dto.getStrengthsSummary().isEmpty()) {
                dto.setStrengthsSummary("具有扎实的技术基础和良好的学习能力");
            }

            // 确保职业目标不为空
            if (dto.getCareerGoals() == null || dto.getCareerGoals().isEmpty()) {
                dto.setCareerGoals("寻求技术成长机会，致力于成为技术专家");
            }

            log.info("人才画像分析结果解析完成");
            return dto;
        } catch (Exception e) {
            log.error("解析人才画像分析结果失败", e);
            // 解析失败时返回默认值
            return getDefaultTalentProfileInfo(jobSeeker);
        }
    }

    /**
     * 获取默认人才画像信息（当AI分析失败时使用）
     */
    private TalentProfileExtractDTO getDefaultTalentProfileInfo(JobSeeker jobSeeker) {
        TalentProfileExtractDTO dto = new TalentProfileExtractDTO();

        List<String> skills = new ArrayList<>();
        skills.add("Java");
        skills.add("Spring Boot");
        skills.add("Vue.js");
        dto.setSkills(skills);

        dto.setEducationLevel(3);
        dto.setWorkYears(3);

        List<String> talentTags = new ArrayList<>();
        talentTags.add("技术达人");
        talentTags.add("全栈开发");
        dto.setTalentTags(talentTags);

        dto.setDescriptionSummary("具有多年互联网开发经验，熟悉前后端开发技术");
        dto.setStrengthsSummary("熟悉Java技术栈，具有良好的代码编写习惯和团队协作能力");
        dto.setCareerGoals("寻求高级Java开发工程师岗位，致力于技术成长");

        List<String> matchKeywords = new ArrayList<>();
        matchKeywords.add("Java");
        matchKeywords.add("后端开发");
        matchKeywords.add("全栈");
        dto.setMatchKeywords(matchKeywords);

        return dto;
    }

    private void saveTalentSkillTags(Long userId, List<String> skills) {
        log.info("开始保存人才技能标签，用户ID：{}，技能数量：{}", userId, skills != null ? skills.size() : 0);

        if (skills == null || skills.isEmpty()) {
            log.info("技能列表为空，跳过保存，用户ID：{}", userId);
            return;
        }

        // 删除原有技能标签
        int deleted = talentSkillTagMapper.delete(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<TalentSkillTag>()
                .eq(TalentSkillTag::getUserId, userId)
        );
        log.info("删除原有技能标签数量：{}", deleted);

        // 保存新技能标签
        int saved = 0;
        for (String skill : skills) {
            TalentSkillTag skillTag = new TalentSkillTag();
            skillTag.setUserId(userId);
            skillTag.setSkillTag(skill);
            skillTag.setProficiencyLevel(3);
            skillTag.setIsHighlight(0);
            talentSkillTagMapper.insert(skillTag);
            saved++;
        }
        log.info("保存新技能标签数量：{}", saved);
    }

    private TalentProfileResponse buildTalentProfileResponse(TalentProfile talentProfile, User user) {
        TalentProfileResponse response = new TalentProfileResponse();
        response.setId(talentProfile.getId());
        response.setUserId(talentProfile.getUserId());
        response.setUserName(user != null ? user.getUsername() : "");

        try {
            if (talentProfile.getSkills() != null) {
                response.setSkills(objectMapper.readValue(talentProfile.getSkills(), List.class));
            }
            if (talentProfile.getTalentTags() != null) {
                response.setTalentTags(objectMapper.readValue(talentProfile.getTalentTags(), List.class));
            }
            if (talentProfile.getMatchKeywords() != null) {
                response.setMatchKeywords(objectMapper.readValue(talentProfile.getMatchKeywords(), List.class));
            }
        } catch (Exception e) {
            log.error("解析JSON失败", e);
        }

        if (talentProfile.getEducation() != null) {
            response.setEducationLevel(Integer.parseInt(talentProfile.getEducation()));
            response.setEducation(getEducationText(response.getEducationLevel()));
        }

        response.setWorkYears(talentProfile.getWorkYears());
        response.setSalaryExpectation(talentProfile.getSalaryExpectation());
        response.setCurrentSalary(talentProfile.getCurrentSalary());
        response.setDescriptionSummary(talentProfile.getDescriptionSummary());
        response.setStrengthsSummary(talentProfile.getStrengthsSummary());
        response.setCareerGoals(talentProfile.getCareerGoals());
        response.setAiEvaluation(talentProfile.getAiEvaluation());

        if (talentProfile.getCreatedAt() != null) {
            response.setCreatedAt(talentProfile.getCreatedAt().toString());
        }

        return response;
    }

    private String getGenderText(Integer gender) {
        if (gender == null) return "未知";
        return switch (gender) {
            case 1 -> "男";
            case 2 -> "女";
            default -> "未知";
        };
    }

    private String getCurrentStatusText(Integer status) {
        if (status == null) return "未知";
        return switch (status) {
            case 1 -> "在职";
            case 2 -> "离职";
            case 3 -> "在读学生";
            default -> "未知";
        };
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
}