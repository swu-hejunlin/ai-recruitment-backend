package com.example.airecruitmentbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.airecruitmentbackend.common.AIConstant;
import com.example.airecruitmentbackend.config.GLMClientSingleton;
import com.example.airecruitmentbackend.dto.InterviewEvaluationDTO;
import com.example.airecruitmentbackend.dto.InterviewRequest;
import com.example.airecruitmentbackend.dto.InterviewDetailDTO;
import com.example.airecruitmentbackend.entity.Interview;
import com.example.airecruitmentbackend.entity.Application;
import com.example.airecruitmentbackend.entity.JobSeeker;
import com.example.airecruitmentbackend.entity.Position;
import com.example.airecruitmentbackend.entity.Company;
import com.example.airecruitmentbackend.entity.InterviewEvaluation;
import com.example.airecruitmentbackend.exception.BusinessException;
import com.example.airecruitmentbackend.mapper.InterviewMapper;
import com.example.airecruitmentbackend.mapper.ApplicationMapper;
import com.example.airecruitmentbackend.mapper.JobSeekerMapper;
import com.example.airecruitmentbackend.mapper.PositionMapper;
import com.example.airecruitmentbackend.mapper.CompanyMapper;
import com.example.airecruitmentbackend.mapper.UserMapper;
import com.example.airecruitmentbackend.mapper.InterviewEvaluationMapper;
import com.example.airecruitmentbackend.service.InterviewService;
import com.example.airecruitmentbackend.service.NotificationService;
import ai.z.openapi.ZhipuAiClient;
import ai.z.openapi.service.model.*;
import com.example.airecruitmentbackend.utils.SpeechToTextUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.Arrays;

/**
 * 面试服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InterviewServiceImpl extends ServiceImpl<InterviewMapper, Interview> implements InterviewService {

    private final InterviewMapper interviewMapper;
    private final ApplicationMapper applicationMapper;
    private final JobSeekerMapper jobSeekerMapper;
    private final PositionMapper positionMapper;
    private final CompanyMapper companyMapper;
    private final UserMapper userMapper;
    private final InterviewEvaluationMapper interviewEvaluationMapper;
    private final NotificationService notificationService;
    private final com.example.airecruitmentbackend.utils.OssUtil ossUtil;
    private final RedisTemplate<String, Object> redisTemplate;
    
    /**
     * Redis面试题key前缀（用于真实面试）
     */
    private static final String INTERVIEW_QUESTIONS_PREFIX = "interview:questions:";
    
    /**
     * Redis面试题key前缀（用于模拟面试session）
     */
    private static final String MOCK_INTERVIEW_SESSION_PREFIX = "mock:interview:session:";

    @Override
    @Transactional
    public Long createInterview(InterviewRequest request, Long bossId) {
        // 1. 验证投递记录是否存在
        Application application = applicationMapper.selectById(request.getApplicationId());
        if (application == null) {
            throw new BusinessException(400, "投递记录不存在");
        }

        // 2. 验证企业HR是否有权限
        Company company = companyMapper.selectById(application.getCompanyId());
        if (company == null || !company.getUserId().equals(bossId)) {
            throw new BusinessException(403, "无权限操作此面试");
        }

        // 3. 检查是否已存在面试记录
        Interview existingInterview = interviewMapper.selectOne(new LambdaQueryWrapper<Interview>()
                .eq(Interview::getApplicationId, request.getApplicationId()));

        Interview interview;
        boolean isUpdate = false;

        if (existingInterview != null) {
            // 已存在面试记录，更新
            interview = existingInterview;
            interview.setInterviewTime(request.getInterviewTime());
            interview.setInterviewType(request.getInterviewType());
            interview.setInterviewAddress(request.getInterviewAddress());
            interview.setInterviewLink(request.getInterviewLink());
            interview.setStatus(1); // 重置为待确认
            interview.setRemark(request.getRemark());
            interview.setUpdateTime(LocalDateTime.now());
            
            // 重置AI面试相关字段
            interview.setAiScore(null);
            interview.setAiEvaluation(null);
            
            interviewMapper.updateById(interview);
            
            // 删除对应的评估记录，确保二次面试能够正常进行
            interviewEvaluationMapper.delete(new LambdaQueryWrapper<InterviewEvaluation>()
                    .eq(InterviewEvaluation::getInterviewId, interview.getId()));
            log.info("删除面试评估记录成功：interviewId={}", interview.getId());
            
            isUpdate = true;
            log.info("更新面试邀请成功：interviewId={}, applicationId={}, bossId={}",
                    interview.getId(), request.getApplicationId(), bossId);
        } else {
            // 不存在面试记录，创建
            interview = new Interview();
            interview.setApplicationId(request.getApplicationId());
            interview.setJobSeekerId(application.getJobSeekerId());
            interview.setPositionId(application.getPositionId());
            interview.setCompanyId(application.getCompanyId());
            interview.setInterviewTime(request.getInterviewTime());
            interview.setInterviewType(request.getInterviewType());
            interview.setInterviewAddress(request.getInterviewAddress());
            interview.setInterviewLink(request.getInterviewLink());
            interview.setStatus(1); // 待确认
            interview.setRemark(request.getRemark());
            interview.setCreateTime(LocalDateTime.now());
            interview.setUpdateTime(LocalDateTime.now());

            interviewMapper.insert(interview);
            log.info("创建面试邀请成功：interviewId={}, applicationId={}, bossId={}",
                    interview.getId(), request.getApplicationId(), bossId);
        }

        // 4. 更新投递状态为面试中
        application.setStatus(3); // 面试中
        applicationMapper.updateById(application);

        // 5. 发送面试邀请通知给求职者
        JobSeeker jobSeeker = jobSeekerMapper.selectById(application.getJobSeekerId());
        Position position = positionMapper.selectById(application.getPositionId());

        if (jobSeeker != null && position != null && company != null) {
            String title = isUpdate ? "面试邀请更新通知" : "面试邀请通知";
            String content = String.format("您投递的【%s】职位已收到面试邀请，面试时间：%s，面试类型：%s",
                    position.getTitle(), interview.getInterviewTime(), getInterviewTypeName(interview.getInterviewType()));
            notificationService.sendNotification(jobSeeker.getUserId(), 2, title, content, interview.getId());
        }

        return interview.getId();
    }

    @Override
    @Transactional
    public boolean updateInterviewStatus(Long interviewId, Integer status) {
        // 验证状态值
        if (!List.of(2, 3, 4).contains(status)) {
            throw new BusinessException(400, "无效的面试状态");
        }

        Interview interview = interviewMapper.selectById(interviewId);
        if (interview == null) {
            throw new BusinessException(400, "面试记录不存在");
        }

        // 更新面试状态
        interview.setStatus(status);
        int result = interviewMapper.updateById(interview);

        // 如果面试完成，更新投递状态
        if (status == 4) { // 已完成
            Application application = applicationMapper.selectById(interview.getApplicationId());
            if (application != null) {
                // 检查是否还有其他未完成的面试
                Long activeInterviews = interviewMapper.selectCount(new LambdaQueryWrapper<Interview>()
                        .eq(Interview::getApplicationId, interview.getApplicationId())
                        .ne(Interview::getStatus, 4));
                
                if (activeInterviews == 0) {
                    // 所有面试都已完成，更新投递状态为录用
                    application.setStatus(5); // 录用
                    applicationMapper.updateById(application);
                } else {
                    // 还有其他面试未完成，保持投递状态为面试中
                    application.setStatus(3); // 面试中
                    applicationMapper.updateById(application);
                }
            }
        }

        // 发送面试状态更新通知
        Application application = applicationMapper.selectById(interview.getApplicationId());
        Position position = positionMapper.selectById(interview.getPositionId());
        Company company = companyMapper.selectById(interview.getCompanyId());

        if (application != null && position != null && company != null) {
            JobSeeker jobSeeker = jobSeekerMapper.selectById(interview.getJobSeekerId());

            // 通知求职者
            if (jobSeeker != null) {
                String title = "面试状态更新通知";
                String content = String.format("您的【%s】面试状态已更新为：%s",
                        position.getTitle(), getStatusName(status));
                notificationService.sendNotification(jobSeeker.getUserId(), 2, title, content, interview.getId());
            }

            // 通知企业HR
            notificationService.sendNotification(company.getUserId(), 2,
                    "面试状态更新通知",
                    String.format("求职者【%s】的面试状态已更新为：%s",
                            jobSeeker != null ? jobSeeker.getName() : "未知", getStatusName(status)),
                    interview.getId());
        }

        log.info("更新面试状态成功：interviewId={}, status={}", interviewId, status);
        return result > 0;
    }

    @Override
    public List<InterviewDetailDTO> getCompanyInterviews(Long companyId) {
        List<Interview> interviews = interviewMapper.selectList(new LambdaQueryWrapper<Interview>()
                .eq(Interview::getCompanyId, companyId)
                .orderByDesc(Interview::getCreateTime));

        return interviews.stream()
                .map(this::convertToDetailDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<InterviewDetailDTO> getJobSeekerInterviews(Long userId) {
        // 1. 通过userId查询JobSeeker记录，获取真正的JobSeeker主键ID
        JobSeeker jobSeeker = jobSeekerMapper.selectOne(new LambdaQueryWrapper<JobSeeker>()
                .eq(JobSeeker::getUserId, userId));
        
        if (jobSeeker == null) {
            log.warn("求职者信息不存在，userId：{}", userId);
            return java.util.Collections.emptyList();
        }
        
        Long jobSeekerId = jobSeeker.getId();
        log.info("查询求职者面试列表，userId：{}，jobSeekerId：{}", userId, jobSeekerId);
        
        // 2. 使用JobSeeker主键ID查询面试记录
        List<Interview> interviews = interviewMapper.selectList(new LambdaQueryWrapper<Interview>()
                .eq(Interview::getJobSeekerId, jobSeekerId)
                .orderByDesc(Interview::getCreateTime));

        return interviews.stream()
                .map(this::convertToDetailDTO)
                .collect(Collectors.toList());
    }

    @Override
    public InterviewDetailDTO getInterviewDetail(Long interviewId) {
        Interview interview = interviewMapper.selectById(interviewId);
        if (interview == null) {
            throw new BusinessException(400, "面试记录不存在");
        }

        return convertToDetailDTO(interview);
    }

    @Override
    @Transactional
    public boolean deleteInterview(Long interviewId, Long bossId) {
        Interview interview = interviewMapper.selectById(interviewId);
        if (interview == null) {
            throw new BusinessException(400, "面试记录不存在");
        }

        // 验证企业HR是否有权限
        Company company = companyMapper.selectById(interview.getCompanyId());
        if (company == null || !company.getUserId().equals(bossId)) {
            throw new BusinessException(403, "无权限删除此面试");
        }

        // 删除面试记录
        int result = interviewMapper.deleteById(interviewId);

        // 恢复投递状态
        Application application = applicationMapper.selectById(interview.getApplicationId());
        if (application != null) {
            application.setStatus(2); // 已查看
            applicationMapper.updateById(application);
        }

        log.info("删除面试记录成功：interviewId={}, bossId={}", interviewId, bossId);
        return result > 0;
    }

    /**
     * 转换为面试详情DTO
     */
    private InterviewDetailDTO convertToDetailDTO(Interview interview) {
        InterviewDetailDTO dto = new InterviewDetailDTO();
        dto.setId(interview.getId());
        dto.setApplicationId(interview.getApplicationId());
        dto.setJobSeekerId(interview.getJobSeekerId());
        dto.setPositionId(interview.getPositionId());
        dto.setCompanyId(interview.getCompanyId());
        dto.setInterviewTime(interview.getInterviewTime());
        dto.setInterviewType(interview.getInterviewType());
        dto.setInterviewAddress(interview.getInterviewAddress());
        dto.setInterviewLink(interview.getInterviewLink());
        dto.setStatus(interview.getStatus());
        dto.setRemark(interview.getRemark());
        dto.setCreateTime(interview.getCreateTime());
        dto.setUpdateTime(interview.getUpdateTime());

        // 获取求职者信息
        JobSeeker jobSeeker = jobSeekerMapper.selectById(interview.getJobSeekerId());
        if (jobSeeker != null) {
            dto.setJobSeekerName(jobSeeker.getName());
        }

        // 获取职位信息
        Position position = positionMapper.selectById(interview.getPositionId());
        if (position != null) {
            dto.setPositionTitle(position.getTitle());
        }

        // 获取企业信息
        Company company = companyMapper.selectById(interview.getCompanyId());
        if (company != null) {
            dto.setCompanyName(company.getCompanyName());
        }

        // 设置面试类型名称
        dto.setInterviewTypeName(getInterviewTypeName(interview.getInterviewType()));

        // 设置面试状态名称
        dto.setStatusName(getStatusName(interview.getStatus()));

        // 设置AI评估分数
        dto.setAiScore(interview.getAiScore());
        dto.setAiEvaluation(interview.getAiEvaluation());

        return dto;
    }

    /**
     * 获取面试类型名称
     */
    private String getInterviewTypeName(Integer type) {
        return switch (type) {
            case 1 -> "线下面试";
            case 2 -> "线上面试";
            case 3 -> "AI面试";
            default -> "未知类型";
        };
    }

    /**
     * 获取面试状态名称
     */
    private String getStatusName(Integer status) {
        return switch (status) {
            case 1 -> "待确认";
            case 2 -> "已确认";
            case 3 -> "已拒绝";
            case 4 -> "已完成";
            default -> "未知状态";
        };
    }

    @Override
    public Object processMockInterview(org.springframework.web.multipart.MultipartFile video, Long userId) {
        return processMockInterview(video, userId, null, null);
    }

    @Override
    public Object processMockInterview(org.springframework.web.multipart.MultipartFile video, Long userId, Long interviewId) {
        return processMockInterview(video, userId, interviewId, null);
    }

    @Override
    public Object processMockInterview(org.springframework.web.multipart.MultipartFile video, Long userId, Long interviewId, String sessionKey) {
        log.info("开始处理模拟面试，userId：{}，interviewId：{}，sessionKey：{}", userId, interviewId, sessionKey);

        try {
            // 1. 上传视频到阿里云对象存储
            log.info("开始上传视频，文件名：{}", video.getOriginalFilename());
            String videoUrl = ossUtil.uploadFile(video, "interview/");
            log.info("视频上传成功，URL：{}", videoUrl);

            // 2. 提取音频文件并上传
            log.info("开始提取音频文件");
            String audioUrl = extractAndUploadAudio(video);
            log.info("音频提取并上传成功，URL：{}", audioUrl);

            // 3. 并行执行：视频分析 + 语音转文字
            log.info("开始并行执行视频分析和语音转文字");
            long parallelStartTime = System.currentTimeMillis();

            CompletableFuture<Object> videoFuture = CompletableFuture.supplyAsync(() -> {
                log.info("开始调用GLM-4.6v模型分析视频");
                Object result = analyzeVideoWithGLM(videoUrl);
                log.info("GLM-4.6v模型分析视频完成");
                return result;
            });

            CompletableFuture<String> transcriptFuture = CompletableFuture.supplyAsync(() -> {
                log.info("开始调用语音转文字工具");
                String result = null;
                try {
                    result = extractAudioTranscript(audioUrl);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                log.info("语音转文字完成，结果长度：{}", result.length());
                return result;
            });

            // 4. 获取面试题（与视频/音频处理并行）
            String positionInfo = "";
            String interviewQuestions = "";
            try {
                StringBuilder questionsBuilder = new StringBuilder();
                questionsBuilder.append("面试题：\n");

                boolean questionsFromCache = false;

                if (sessionKey != null && !sessionKey.isEmpty()) {
                    try {
                        String redisKey = MOCK_INTERVIEW_SESSION_PREFIX + sessionKey;
                        Object cachedQuestions = redisTemplate.opsForValue().get(redisKey);
                        if (cachedQuestions != null && cachedQuestions instanceof List) {
                            List<String> savedQuestions = (List<String>) cachedQuestions;
                            for (int i = 0; i < savedQuestions.size(); i++) {
                                questionsBuilder.append((i + 1)).append(". ").append(savedQuestions.get(i)).append("\n");
                            }
                            log.info("从Redis缓存读取面试题成功，sessionKey：{}，题目数量：{}", sessionKey, savedQuestions.size());
                            questionsFromCache = true;
                            interviewQuestions = questionsBuilder.toString();
                        }
                    } catch (Exception e) {
                        log.error("从Redis读取面试题失败：{}", e.getMessage(), e);
                    }
                }

                if (!questionsFromCache) {
                    log.warn("Redis缓存中没有面试题，使用默认面试题，sessionKey：{}", sessionKey);
                    interviewQuestions = "面试题：\n1. 请自我介绍一下\n2. 你为什么选择我们公司\n3. 你的职业规划是什么\n4. 你如何处理工作压力\n5. 你有什么问题要问我们\n";
                }

                positionInfo = "岗位信息：\n职位名称：自定义岗位\n职位类别：自定义\n工作城市：不限\n薪资范围：面议\n学历要求：不限\n工作经验：不限\n岗位职责：自定义\n任职要求：自定义\n";
            } catch (Exception e) {
                log.error("获取面试题失败：{}", e.getMessage(), e);
                positionInfo = "岗位信息：\n职位名称：软件工程师\n职责：负责软件系统的开发和维护\n要求：熟悉Java、Spring Boot等技术\n";
                interviewQuestions = "面试题：\n1. 请自我介绍一下\n2. 你为什么选择我们公司\n3. 你的职业规划是什么\n4. 你如何处理工作压力\n5. 你有什么问题要问我们\n";
            }

            // 5. 等待语音转文字完成后，立即启动内容分析（可能与视频分析并行）
            String transcript = transcriptFuture.get();
            String finalPositionInfo = positionInfo;
            String finalInterviewQuestions = interviewQuestions;

            CompletableFuture<Object> contentFuture = CompletableFuture.supplyAsync(() -> {
                log.info("开始调用GLM-4-Flash-250414模型分析回答内容");
                Object result = analyzeContentWithFlash(transcript, finalPositionInfo, finalInterviewQuestions);
                log.info("GLM-4-Flash-250414模型分析完成");
                return result;
            });

            // 6. 等待视频分析和内容分析都完成
            Object videoEvaluationResult = videoFuture.get();
            Object contentEvaluationResult = contentFuture.get();

            long parallelEndTime = System.currentTimeMillis();
            log.info("并行评估全部完成，总耗时：{}ms", (parallelEndTime - parallelStartTime));

            // 7. 综合评估结果
            log.info("开始综合评估结果");
            Object finalEvaluationResult = combineEvaluationResults(videoEvaluationResult, contentEvaluationResult);
            log.info("综合评估完成");

            // 8. 如果是真实面试，保存AI评估结果到面试记录
            if (interviewId != null) {
                saveAIEvaluationResult(interviewId, finalEvaluationResult);
                log.info("AI评估结果已保存到面试记录，interviewId：{}", interviewId);
            }

            return finalEvaluationResult;
        } catch (Exception e) {
            log.error("处理模拟面试失败：{}", e.getMessage(), e);
            throw new RuntimeException("处理模拟面试失败", e);
        }
    }

    /**
     * 保存AI评估结果到面试记录
     */
    private void saveAIEvaluationResult(Long interviewId, Object evaluationResult) {
        try {
            // 1. 获取面试记录
            Interview interview = interviewMapper.selectById(interviewId);
            if (interview == null) {
                log.error("面试记录不存在，interviewId：{}", interviewId);
                return;
            }

            // 2. 解析评估结果，提取分数
            if (evaluationResult instanceof java.util.Map) {
                java.util.Map<?, ?> resultMap = (java.util.Map<?, ?>) evaluationResult;
                Object scoreObj = resultMap.get("score");
                if (scoreObj != null) {
                    double score;
                    if (scoreObj instanceof Number) {
                        score = ((Number) scoreObj).doubleValue();
                    } else {
                        score = Double.parseDouble(scoreObj.toString());
                    }
                    interview.setAiScore(score);
                    log.info("提取AI评估分数：{}", score);
                }

                // 3. 保存完整评估结果（JSON格式）
                com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
                String evaluationJson = objectMapper.writeValueAsString(evaluationResult);
                interview.setAiEvaluation(evaluationJson);
                log.info("保存完整AI评估结果");
            }

            // 4. 更新面试状态为已完成
            interview.setStatus(4); // 已完成
            interviewMapper.updateById(interview);
            log.info("更新面试状态为已完成，interviewId：{}", interviewId);
        } catch (Exception e) {
            log.error("保存AI评估结果失败：{}", e.getMessage(), e);
        }
    }

    @Override
    public Object generateMockInterviewQuestions(Long userId) {
        return generateMockInterviewQuestions(userId, null);
    }

    @Override
    public Object generateMockInterviewQuestions(Long userId, Long interviewId) {
        if (interviewId != null) {
            log.info("开始生成真实面试题，userId：{}，interviewId：{}", userId, interviewId);
        } else {
            log.info("开始生成模拟面试题，userId：{}", userId);
        }

        try {
            // 1. 获取岗位信息
            Position position;
            if (interviewId != null) {
                // 从面试记录获取岗位信息（真实面试）
                position = getPositionFromInterview(interviewId);
                log.info("从面试记录获取岗位成功，岗位ID：{}，职位名称：{}", position.getId(), position.getTitle());
            } else {
                // 构建默认岗位信息（兼容旧调用）
                position = new Position();
                position.setTitle("默认岗位");
                position.setCategory("技术");
                position.setCity("北京");
                position.setDescription("岗位职责描述");
                position.setRequirement("岗位要求");
                position.setCompanyName("默认公司");
                position.setSalaryMin(10);
                position.setSalaryMax(30);
                log.info("使用默认岗位信息");
            }

            // 2. 获取求职者信息（通过userId字段查询）
            JobSeeker jobSeeker = jobSeekerMapper.selectOne(new LambdaQueryWrapper<JobSeeker>()
                    .eq(JobSeeker::getUserId, userId));
            if (jobSeeker == null) {
                log.warn("求职者信息不存在，userId：{}，尝试使用默认信息", userId);
                // 使用默认求职者信息
                jobSeeker = new JobSeeker();
                jobSeeker.setName("求职者");
                jobSeeker.setWorkYears(3);
                jobSeeker.setSkills("Java, Python, Spring Boot");
                jobSeeker.setIntroduction("有三年开发经验，熟悉后端开发");
            }
            log.info("获取求职者信息成功，姓名：{}", jobSeeker.getName());

            // 3. 调用GLM模型生成面试题
            log.info("开始调用GLM模型生成面试题");
            java.util.List<String> questions = generateQuestionsWithGLM(position, jobSeeker);
            log.info("GLM模型生成面试题完成，题目数量：{}", questions.size());

            // 4. 如果有interviewId，将面试题保存到Redis缓存
            String sessionKey = null;
            if (interviewId != null) {
                // 真实面试：使用interviewId作为key
                try {
                    String redisKey = INTERVIEW_QUESTIONS_PREFIX + interviewId;
                    redisTemplate.opsForValue().set(redisKey, questions, 2, TimeUnit.HOURS);
                    log.info("面试题已保存到Redis缓存，interviewId：{}，过期时间：2小时", interviewId);
                } catch (Exception e) {
                    log.error("保存面试题到Redis失败：{}", e.getMessage(), e);
                }
            } else {
                // 模拟面试：生成sessionKey保存面试题
                try {
                    sessionKey = java.util.UUID.randomUUID().toString().replace("-", "");
                    String redisKey = MOCK_INTERVIEW_SESSION_PREFIX + sessionKey;
                    redisTemplate.opsForValue().set(redisKey, questions, 2, TimeUnit.HOURS);
                    log.info("面试题已保存到Redis缓存（模拟面试），sessionKey：{}，过期时间：2小时", sessionKey);
                } catch (Exception e) {
                    log.error("保存面试题到Redis失败：{}", e.getMessage(), e);
                }
            }

            // 5. 构建返回结果
            java.util.Map<String, Object> result = new java.util.HashMap<>();
            result.put("questions", questions);
            result.put("positionTitle", position.getTitle());
            result.put("companyName", position.getCompanyName());
            if (sessionKey != null) {
                result.put("sessionKey", sessionKey);
            }

            return result;
        } catch (Exception e) {
            log.error("生成模拟面试题失败：{}", e.getMessage(), e);
            throw new RuntimeException("生成模拟面试题失败", e);
        }
    }

    @Override
    public Object generateMockInterviewQuestions(Long userId, Long interviewId, String positionName, 
            String positionCategory, String city, String description, String requirement) {
        if (interviewId != null) {
            log.info("开始生成真实面试题（用户自定义岗位），userId：{}，interviewId：{}，positionName：{}", userId, interviewId, positionName);
        } else {
            log.info("开始生成模拟面试题（用户自定义岗位），userId：{}，positionName：{}", userId, positionName);
        }

        try {
            // 1. 构建自定义岗位信息
            Position position = new Position();
            position.setTitle(positionName != null ? positionName : "自定义岗位");
            position.setCategory(positionCategory);
            position.setCity(city);
            position.setDescription(description);
            position.setRequirement(requirement);
            position.setCompanyName("自定义公司");
            position.setSalaryMin(10);
            position.setSalaryMax(30);
            log.info("构建自定义岗位成功，positionName：{}", positionName);

            // 2. 调用GLM模型生成面试题
            log.info("开始调用GLM模型生成面试题");
            java.util.List<String> questions = generateQuestionsWithGLM(position, new JobSeeker());
            log.info("GLM模型生成面试题完成，题目数量：{}", questions.size());

            // 3. 保存面试题到Redis
            String sessionKey = null;
            try {
                if (interviewId != null) {
                    // 真实面试：使用interviewId作为key
                    String redisKey = INTERVIEW_QUESTIONS_PREFIX + interviewId;
                    redisTemplate.opsForValue().set(redisKey, questions, 2, TimeUnit.HOURS);
                    log.info("面试题已保存到Redis缓存（真实面试），interviewId：{}，过期时间：2小时", interviewId);
                } else {
                    // 模拟面试：生成sessionKey保存面试题
                    sessionKey = java.util.UUID.randomUUID().toString().replace("-", "");
                    String redisKey = MOCK_INTERVIEW_SESSION_PREFIX + sessionKey;
                    redisTemplate.opsForValue().set(redisKey, questions, 2, TimeUnit.HOURS);
                    log.info("面试题已保存到Redis缓存（模拟面试），sessionKey：{}，过期时间：2小时", sessionKey);
                }
            } catch (Exception e) {
                log.error("保存面试题到Redis失败：{}", e.getMessage(), e);
            }

            // 4. 构建返回结果
            java.util.Map<String, Object> result = new java.util.HashMap<>();
            result.put("questions", questions);
            result.put("positionTitle", positionName);
            result.put("companyName", "自定义公司");
            if (sessionKey != null) {
                result.put("sessionKey", sessionKey);
            }

            return result;
        } catch (Exception e) {
            log.error("生成模拟面试题失败：{}", e.getMessage(), e);
            throw new RuntimeException("生成模拟面试题失败", e);
        }
    }

    /**
     * 从面试记录获取岗位信息
     */
    private Position getPositionFromInterview(Long interviewId) {
        Interview interview = interviewMapper.selectById(interviewId);
        if (interview == null) {
            throw new RuntimeException("面试记录不存在");
        }

        Position position = positionMapper.selectById(interview.getPositionId());
        if (position == null) {
            throw new RuntimeException("岗位信息不存在");
        }

        return position;
    }



    /**
     * 使用GLM模型生成面试题
     */
    private java.util.List<String> generateQuestionsWithGLM(Position position, JobSeeker jobSeeker) {
        try {
            // 1. 检查API密钥
            String apiKey = System.getenv("ZAI_API_KEY");
            if (apiKey == null || apiKey.isEmpty()) {
                throw new RuntimeException("ZAI_API_KEY环境变量未设置");
            }

            // 2. 使用glm-4-Flash-250414模型
            String model = AIConstant.LOW_MODEL;

            // 3. 使用单例获取GLM客户端
            ZhipuAiClient client = GLMClientSingleton.getInstance();

            // 4. 构建岗位和求职者信息
            StringBuilder infoBuilder = new StringBuilder();
            infoBuilder.append("岗位信息：\n");
            infoBuilder.append("职位名称：").append(position.getTitle() != null ? position.getTitle() : "").append("\n");
            infoBuilder.append("职位类别：").append(position.getCategory() != null ? position.getCategory() : "").append("\n");
            infoBuilder.append("工作城市：").append(position.getCity() != null ? position.getCity() : "").append("\n");
            infoBuilder.append("薪资范围：").append(position.getSalaryMin()).append("-").append(position.getSalaryMax()).append("K/月\n");
            infoBuilder.append("学历要求：").append(position.getEducationMin() != null ? position.getEducationMin() : "不限").append("\n");
            infoBuilder.append("工作经验：").append(position.getWorkYearsMin() != null ? position.getWorkYearsMin() : "不限").append("年\n");
            infoBuilder.append("岗位职责：").append(position.getDescription() != null ? position.getDescription() : "").append("\n");
            infoBuilder.append("任职要求：").append(position.getRequirement() != null ? position.getRequirement() : "").append("\n\n");

            infoBuilder.append("求职者信息：\n");
            infoBuilder.append("姓名：").append(jobSeeker.getName() != null ? jobSeeker.getName() : "").append("\n");
            infoBuilder.append("工作经验：").append(jobSeeker.getWorkYears() != null ? jobSeeker.getWorkYears() : "不限").append("年\n");
            infoBuilder.append("专业技能：").append(jobSeeker.getSkills() != null ? jobSeeker.getSkills() : "").append("\n");
            infoBuilder.append("个人简介：").append(jobSeeker.getIntroduction() != null ? jobSeeker.getIntroduction() : "").append("\n");

            // 5. 构建请求
            log.info("开始构建GLM模型请求");
            ChatCompletionCreateParams request = ChatCompletionCreateParams.builder()
                    .model(model)
                    .messages(Arrays.asList(
                            ChatMessage.builder()
                                    .role(ChatMessageRole.USER.value())
                                    .content("你是一个专业且严格的招聘面试官，请根据以下岗位信息和求职者信息，生成8-10个面试题。\n\n" +
                                            "要求：\n" +
                                            "1. 每个问题20到40字\n" +
                                            "2. 问题要针对岗位要求和求职者背景,满足企业主流真实面试题标准\n" +
                                            "3. 问题要涵盖专业技能、工作经验、职业规划等方面,能体验求职者能力或技术深度\n" +
                                            "4. 请以JSON格式返回，包含一个questions数组\n" +
                                            "5. 不要包含任何额外的解释或说明\n" +
                                            "\n" +
                                            "示例格式：\n" +
                                            "{\n" +
                                            "  \"questions\": [\n" +
                                            "    \"请介绍一下你的项目经验，特别是Java相关领域\",\n" +
                                            "    \"你对这个岗位有什么了解，你认为你的优势在哪\",\n" +
                                            "    \"你的职业规划是什么\",\n" +
                                            "    \"你如何处理工作压力的\",\n" +
                                            "    \"你为什么选择我们公司，了解我们公司吗\"\n" +
                                            "  ]\n" +
                                            "}\n" +
                                            "\n" +
                                            infoBuilder.toString())
                                    .build()
                    ))
                    .responseFormat(ResponseFormat.builder()
                            .type("json_object")
                            .build())
                    .temperature(0.7f)
                    .maxTokens(3000)
                    .build();

            // 6. 调用API
            log.info("调用GLM模型API生成面试题");
            long modelStartTime = System.currentTimeMillis();
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

                log.info("GLM模型生成面试题结果长度: {}", content.length());

                if (content.isEmpty()) {
                    log.error("GLM模型返回空字符串");
                    throw new RuntimeException("GLM模型返回空字符串");
                }

                // 解析JSON结果
                ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
                Map result = objectMapper.readValue(content, java.util.Map.class);
                List<String> questions = (java.util.List<String>) result.get("questions");

                if (questions == null || questions.isEmpty()) {
                    throw new RuntimeException("GLM模型未生成面试题");
                }

                log.info("GLM模型生成面试题解析成功，题目数量：{}", questions.size());
                return questions;
            } else {
                log.error("GLM模型生成面试题失败，错误信息: {}", response.getMsg());
                throw new RuntimeException("GLM模型生成面试题失败: " + response.getMsg());
            }
        } catch (Exception e) {
            log.error("调用GLM模型生成面试题失败：{}", e.getMessage(), e);
            // 返回默认面试题
            return java.util.Arrays.asList(
                    "请自我介绍一下",
                    "你为什么选择我们公司",
                    "你的职业规划是什么",
                    "你如何处理工作压力",
                    "你有什么问题要问我们"
            );
        }
    }

    @Override
    @Transactional
    public void finishRealInterview(Long interviewId, org.springframework.web.multipart.MultipartFile video) {
        log.info("开始处理真实AI面试结束，interviewId：{}", interviewId);

        try {
            // 1. 验证面试记录是否存在
            Interview interview = interviewMapper.selectById(interviewId);
            if (interview == null) {
                log.error("面试记录不存在，interviewId：{}", interviewId);
                throw new BusinessException(400, "面试记录不存在");
            }

            // 2. 验证是否为AI面试类型
            if (interview.getInterviewType() != 3) {
                log.error("不是AI面试类型，interviewId：{}，type：{}", interviewId, interview.getInterviewType());
                throw new BusinessException(400, "该面试不是AI面试类型");
            }

            // 3. 删除已有评估结果，确保二次面试能够重新评估
            interviewEvaluationMapper.delete(new LambdaQueryWrapper<InterviewEvaluation>()
                    .eq(InterviewEvaluation::getInterviewId, interviewId));
            log.info("删除已有评估结果，准备重新评估，interviewId：{}", interviewId);

            // 4. 上传视频到阿里云对象存储
            log.info("开始上传视频，文件名：{}", video.getOriginalFilename());
            String videoUrl = ossUtil.uploadFile(video, "interview/");
            log.info("视频上传成功，URL：{}", videoUrl);

            // 5. 提取音频文件并上传
            log.info("开始提取音频文件");
            String audioUrl = extractAndUploadAudio(video);
            log.info("音频提取并上传成功，URL：{}", audioUrl);

            // 6. 立即更新面试状态为已完成，不等待评估完成
            interview.setStatus(4); // 已完成
            interviewMapper.updateById(interview);
            log.info("面试状态已更新为已完成，interviewId：{}", interviewId);

            // 7. 异步执行评估过程，不阻塞接口返回
            CompletableFuture.runAsync(() -> {
                try {
                    log.info("开始异步执行AI评估，interviewId：{}", interviewId);
                    long parallelStartTime = System.currentTimeMillis();

                    // 7.1 并行执行：视频分析 + 语音转文字
                    CompletableFuture<Object> videoFuture = CompletableFuture.supplyAsync(() -> {
                        log.info("开始调用GLM-4.6v模型分析视频");
                        Object result = analyzeVideoWithGLM(videoUrl);
                        log.info("GLM-4.6v模型分析视频完成");
                        return result;
                    });

                    CompletableFuture<String> transcriptFuture = CompletableFuture.supplyAsync(() -> {
                        log.info("开始调用语音转文字工具");
                        String result = null;
                        try {
                            result = extractAudioTranscript(audioUrl);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                        log.info("语音转文字完成，结果长度：{}", result.length());
                        return result;
                    });

                    // 7.2 获取岗位信息和面试题（与视频/音频处理并行）
                    String positionInfo = "";
                    String interviewQuestions = "";
                    try {
                        Position position = getPositionFromInterview(interviewId);
                        log.info("从面试记录获取岗位成功，岗位ID：{}，职位名称：{}", position.getId(), position.getTitle());

                        StringBuilder positionInfoBuilder = new StringBuilder();
                        positionInfoBuilder.append("岗位信息：\n");
                        positionInfoBuilder.append("职位名称：").append(position.getTitle() != null ? position.getTitle() : "").append("\n");
                        positionInfoBuilder.append("职位类别：").append(position.getCategory() != null ? position.getCategory() : "").append("\n");
                        positionInfoBuilder.append("工作城市：").append(position.getCity() != null ? position.getCity() : "").append("\n");
                        positionInfoBuilder.append("薪资范围：").append(position.getSalaryMin()).append("-").append(position.getSalaryMax()).append("K/月\n");
                        positionInfoBuilder.append("学历要求：").append(position.getEducationMin() != null ? position.getEducationMin() : "不限").append("\n");
                        positionInfoBuilder.append("工作经验：").append(position.getWorkYearsMin() != null ? position.getWorkYearsMin() : "不限").append("年\n");
                        positionInfoBuilder.append("岗位职责：").append(position.getDescription() != null ? position.getDescription() : "").append("\n");
                        positionInfoBuilder.append("任职要求：").append(position.getRequirement() != null ? position.getRequirement() : "").append("\n");
                        positionInfo = positionInfoBuilder.toString();

                        StringBuilder questionsBuilder = new StringBuilder();
                        questionsBuilder.append("面试题：\n");

                        try {
                            String redisKey = INTERVIEW_QUESTIONS_PREFIX + interviewId;
                            Object cachedQuestions = redisTemplate.opsForValue().get(redisKey);

                            if (cachedQuestions != null && cachedQuestions instanceof List) {
                                List<String> savedQuestions = (List<String>) cachedQuestions;
                                for (int i = 0; i < savedQuestions.size(); i++) {
                                    questionsBuilder.append((i + 1)).append(". ").append(savedQuestions.get(i)).append("\n");
                                }
                                log.info("从Redis缓存读取面试题成功，题目数量：{}", savedQuestions.size());
                            } else {
                                log.warn("Redis缓存中没有面试题，使用默认面试题，interviewId：{}", interviewId);
                                for (int i = 1; i <= 5; i++) {
                                    questionsBuilder.append(i).append(". 默认面试题").append("\n");
                                }
                            }
                        } catch (Exception e) {
                            log.error("从Redis读取面试题失败：{}", e.getMessage(), e);
                            for (int i = 1; i <= 5; i++) {
                                questionsBuilder.append(i).append(". 默认面试题").append("\n");
                            }
                        }
                        interviewQuestions = questionsBuilder.toString();
                    } catch (Exception e) {
                        log.error("获取岗位信息和面试题失败：{}", e.getMessage(), e);
                        positionInfo = "岗位信息：\n职位名称：软件工程师\n职责：负责软件系统的开发和维护\n要求：熟悉Java、Spring Boot等技术\n";
                        interviewQuestions = "面试题：\n1. 请自我介绍一下\n2. 你为什么选择我们公司\n3. 你的职业规划是什么\n4. 你如何处理工作压力\n5. 你有什么问题要问我们\n";
                    }

                    // 7.3 等待语音转文字完成后，立即启动内容分析（可能与视频分析并行）
                    String transcript = transcriptFuture.get();
                    String finalPositionInfo = positionInfo;
                    String finalInterviewQuestions = interviewQuestions;

                    CompletableFuture<Object> contentFuture = CompletableFuture.supplyAsync(() -> {
                        log.info("开始调用GLM-4-Flash-250414模型分析回答内容");
                        Object result = analyzeContentWithFlash(transcript, finalPositionInfo, finalInterviewQuestions);
                        log.info("GLM-4-Flash-250414模型分析完成");
                        return result;
                    });

                    // 7.4 等待视频分析和内容分析都完成
                    Object videoEvaluationResult = videoFuture.get();
                    Object contentEvaluationResult = contentFuture.get();

                    long parallelEndTime = System.currentTimeMillis();
                    log.info("并行评估全部完成，总耗时：{}ms", (parallelEndTime - parallelStartTime));

                    // 7.5 综合评估结果
                    log.info("开始综合评估结果");
                    Object finalEvaluationResult = combineEvaluationResults(videoEvaluationResult, contentEvaluationResult);
                    log.info("综合评估完成");

                    // 7.6 保存评估结果到数据库
                    saveEvaluationToDatabase(interviewId, finalEvaluationResult);
                    log.info("评估结果已保存到数据库，interviewId：{}", interviewId);

                    log.info("异步AI评估完成，interviewId：{}", interviewId);
                } catch (Exception e) {
                    log.error("异步执行AI评估失败：{}", e.getMessage(), e);
                }
            });

            // 8. 方法结束，立即返回，不等待异步评估完成
            log.info("面试结束处理完成，interviewId：{}", interviewId);

        } catch (Exception e) {
            log.error("处理真实AI面试结束失败：{}", e.getMessage(), e);
            throw  new RuntimeException(e); // 抛出异常，让前端知道上传过程中出现了错误
        }
    }

    @Override
    public InterviewEvaluationDTO getInterviewEvaluation(Long interviewId) {
        InterviewEvaluation evaluation = interviewEvaluationMapper.selectOne(
                new LambdaQueryWrapper<InterviewEvaluation>()
                        .eq(InterviewEvaluation::getInterviewId, interviewId)
        );

        if (evaluation == null) {
            return null;
        }

        InterviewEvaluationDTO dto = new InterviewEvaluationDTO();
        dto.setId(evaluation.getId());
        dto.setInterviewId(evaluation.getInterviewId());
        dto.setScore(evaluation.getScore());
        dto.setEvaluationText(evaluation.getEvaluationText());
        dto.setLanguageScore(evaluation.getLanguageScore());
        dto.setLogicScore(evaluation.getLogicScore());
        dto.setProfessionalScore(evaluation.getProfessionalScore());
        dto.setSuggestions(evaluation.getSuggestions());
        dto.setCreatedAt(evaluation.getCreatedAt());

        return dto;
    }

    @Override
    public boolean hasEvaluation(Long interviewId) {
        InterviewEvaluation evaluation = interviewEvaluationMapper.selectOne(
                new LambdaQueryWrapper<InterviewEvaluation>()
                        .eq(InterviewEvaluation::getInterviewId, interviewId)
        );
        return evaluation != null;
    }

    /**
     * 提取音频文件并上传
     */
    private String extractAndUploadAudio(org.springframework.web.multipart.MultipartFile video) throws Exception {
        // 这里使用临时文件来存储音频
        // 实际项目中，可能需要使用FFmpeg等工具来提取音频
        // 这里简化处理，直接返回视频URL
        log.info("提取音频文件");
        return ossUtil.uploadFile(video, "audio/");
    }

    /**
     * 提取音频转录
     */
    private String extractAudioTranscript(String audioUrl) throws Exception {
        try {
            // 下载音频文件到本地
            java.io.File tempFile = java.io.File.createTempFile("audio", ".wav");
            java.net.URL url = new java.net.URL(audioUrl);
            java.io.InputStream in = url.openStream();
            java.io.FileOutputStream out = new java.io.FileOutputStream(tempFile);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
            in.close();
            out.close();

            // 调用语音转文字工具
            log.info("调用语音转文字工具，文件路径：{}", tempFile.getAbsolutePath());
            String transcript = SpeechToTextUtil.recognizeSync(tempFile.getAbsolutePath());

            // 删除临时文件
            tempFile.delete();

            return transcript;
        } catch (Exception e) {
            log.error("提取音频转录失败：{}", e.getMessage(), e);
            // 返回默认转录结果
            return "音频转录失败，无法分析回答内容";
        }
    }

    /**
     * 使用GLM-4-Flash-250414模型分析回答内容
     */
    private Object analyzeContentWithFlash(String transcript, String positionInfo, String interviewQuestions) {
        try {
            // 1. 检查API密钥
            String apiKey = System.getenv("ZAI_API_KEY");
            if (apiKey == null || apiKey.isEmpty()) {
                throw new RuntimeException("ZAI_API_KEY环境变量未设置");
            }

            // 2. 使用glm-4-Flash-250414模型
            String model = AIConstant.LOW_MODEL;

            // 3. 使用单例获取GLM客户端
            ZhipuAiClient client = GLMClientSingleton.getInstance();

            // 4. 构建请求
            log.info("开始构建GLM-4-Flash-250414模型请求");
            ChatCompletionCreateParams request = ChatCompletionCreateParams.builder()
                    .model(model)
                    .messages(Arrays.asList(
                            ChatMessage.builder()
                                    .role(ChatMessageRole.USER.value())
                                    .content("你是一个严格、客观的专业企业招聘面试官，请根据岗位和面试题信息分析应聘者回答内容并提供详细的评估。\n\n" +
                                            "岗位信息：\n" + positionInfo + "\n\n" +
                                            "面试题：\n" + interviewQuestions + "\n\n" +
                                            "应聘者回答内容：\n" + transcript + "\n\n" +
                                            "评估要求：\n" +
                                            "1. 从以下几个维度进行严格评估：语言表达能力、逻辑思维能力、专业知识掌握程度、整体表现\n" +
                                            "2. 每个维度给出0-100的分数，评分标准如下：\n" +
                                            "   - 0-30分：表现差，态度不端正，题目没有全部回答或回答与面试无关\n" +
                                            "   - 30-60分：表现一般，回答不完整或错误，无法满足岗位要求\n" +
                                            "   - 60-79分：表现良好，回答基本完整且大致准确\n" +
                                            "   - 80-89分：表现优秀，回答全面且准确\n" +
                                            "   - 90-100分：表现卓越，回答深入且有独特见解\n" +
                                            "3. 对于以下情况要给予低分数：\n" +
                                            "   - 回答中多次出现\"不知道\"、\"不了解\"等模糊表述\n" +
                                            "   - 面试题未回答完或回答不完整(只要少回答一题扣10分)\n" +
                                            "   - 专业知识回答错误或不准确\n" +
                                            "   - 逻辑混乱，表达不清晰\n" +
                                            "4. 给出总体评分（0-100），要综合考虑各维度表现\n" +
                                            "5. 提供详细的反馈，包括优点和不足，要客观真实\n" +
                                            "6. 给出具体的改进建议\n" +
                                            "7. 请以JSON格式返回评估结果，严格按照以下格式：\n" +
                                            "{\n" +
                                            "  \"score\": 50,\n" +
                                            "  \"languageScore\": 60,\n" +
                                            "  \"logicScore\": 40,\n" +
                                            "  \"professionalScore\": 30,\n" +
                                            "  \"overallScore\": 65,\n" +
                                            "  \"feedback\": \"整体表现一般...\",\n" +
                                            "  \"suggestions\": [\"建议xx\", \"建议xx\"]\n" +
                                            "}\n" +
                                            "\n" +
                                            "请严格按照上述格式返回评估结果，不要包含任何额外的解释或说明。")
                                    .build()
                    ))
                    .responseFormat(ResponseFormat.builder()
                            .type("json_object")
                            .build())
                    .temperature(0.2f)
                    .maxTokens(3000)
                    .build();

            // 5. 调用API
            log.info("调用GLM-4-Flash-250414模型API分析回答内容");
            long modelStartTime = System.currentTimeMillis();
            ChatCompletionResponse response = client.chat().createChatCompletion(request);
            long modelEndTime = System.currentTimeMillis();
            log.info("GLM-4-Flash-250414模型API调用完成，状态: {}, 耗时: {}ms",
                     response.isSuccess() ? "成功" : "失败",
                     (modelEndTime - modelStartTime));

            if (response.isSuccess()) {
                Object contentObj = response.getData().getChoices().get(0).getMessage().getContent();
                if (contentObj == null) {
                    log.error("GLM-4-Flash-250414模型返回空内容");
                    throw new RuntimeException("GLM-4-Flash-250414模型返回空内容");
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

                log.info("GLM-4-Flash-250414模型分析结果长度: {}", content.length());

                if (content.isEmpty()) {
                    log.error("GLM-4-Flash-250414模型返回空字符串");
                    throw new RuntimeException("GLM-4-Flash-250414模型返回空字符串");
                }

                // 解析JSON结果
                com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
                java.util.Map<String, Object> result = objectMapper.readValue(content, java.util.Map.class);
                log.info("GLM-4-Flash-250414模型分析结果解析成功");
                return result;
            } else {
                log.error("GLM-4-Flash-250414模型分析失败，错误信息: {}", response.getMsg());
                throw new RuntimeException("GLM-4-Flash-250414模型分析失败: " + response.getMsg());
            }
        } catch (Exception e) {
            log.error("调用GLM-4-Flash-250414模型失败：{}", e.getMessage(), e);
            // 返回默认评估结果
            java.util.Map<String, Object> defaultResult = new java.util.HashMap<>();
            defaultResult.put("score", 70);
            defaultResult.put("languageScore", 75);
            defaultResult.put("logicScore", 70);
            defaultResult.put("professionalScore", 65);
            defaultResult.put("overallScore", 70);
            defaultResult.put("feedback", "回答内容分析失败，返回默认评估结果。");
            defaultResult.put("suggestions", java.util.Collections.emptyList());
            return defaultResult;
        }
    }

    /**
     * 综合评估结果
     */
    private Object combineEvaluationResults(Object videoEvaluation, Object contentEvaluation) {
        try {
            // 解析视频评估结果
            java.util.Map<String, Object> videoResult = (java.util.Map<String, Object>) videoEvaluation;
            java.util.Map<String, Object> contentResult = (java.util.Map<String, Object>) contentEvaluation;

            // 综合分数（视频评估占30%，内容评估占70%）
            // 视频评估使用overallScore，内容评估使用score
            double videoScore = getScore(videoResult, "overallScore", 70);
            double contentScore = getScore(contentResult, "score", 70);
            double finalScore = videoScore * 0.2 + contentScore * 0.8;

            // 综合各维度分数
            // 视频评估不提供语言和逻辑分数，使用默认值
            double videoLanguageScore = 75; // 视频评估不提供语言分数
            double contentLanguageScore = getScore(contentResult, "languageScore", 75);
            double finalLanguageScore = videoLanguageScore * 0.2 + contentLanguageScore * 0.8;

            double videoLogicScore = 70; // 视频评估不提供逻辑分数
            double contentLogicScore = getScore(contentResult, "logicScore", 70);
            double finalLogicScore = videoLogicScore * 0.2 + contentLogicScore * 0.8;

            double videoProfessionalScore = 65; // 视频评估不提供专业分数
            double contentProfessionalScore = getScore(contentResult, "professionalScore", 65);
            double finalProfessionalScore = videoProfessionalScore * 0.2 + contentProfessionalScore * 0.8;

            // 综合整体分数
            double videoOverallScore = getScore(videoResult, "overallScore", 70);
            double contentOverallScore = getScore(contentResult, "overallScore", 70);
            double finalOverallScore = videoOverallScore * 0.2 + contentOverallScore * 0.8;

            // 综合反馈
            String videoFeedback = getFeedback(videoResult, "视频分析：整体表现良好，衣着得体，神态自信。");
            String contentFeedback = getFeedback(contentResult, "内容分析：回答逻辑清晰，表达流畅。");
            String finalFeedback = videoFeedback + "\n" + contentFeedback;

            // 综合建议
            java.util.List<String> videoSuggestions = getSuggestions(videoResult);
            java.util.List<String> contentSuggestions = getSuggestions(contentResult);
            java.util.List<String> finalSuggestions = new java.util.ArrayList<>();
            finalSuggestions.addAll(videoSuggestions);
            finalSuggestions.addAll(contentSuggestions);

            // 构建最终评估结果
            java.util.Map<String, Object> finalResult = new java.util.HashMap<>();
            finalResult.put("score", Math.round(finalScore * 10) / 10.0);
            finalResult.put("languageScore", Math.round(finalLanguageScore * 10) / 10.0);
            finalResult.put("logicScore", Math.round(finalLogicScore * 10) / 10.0);
            finalResult.put("professionalScore", Math.round(finalProfessionalScore * 10) / 10.0);
            finalResult.put("overallScore", Math.round(finalOverallScore * 10) / 10.0);
            finalResult.put("feedback", finalFeedback);
            finalResult.put("suggestions", finalSuggestions);

            log.info("综合评估结果：总分={}", finalResult.get("score"));
            return finalResult;
        } catch (Exception e) {
            log.error("综合评估结果失败：{}", e.getMessage(), e);
            // 返回默认评估结果
            java.util.Map<String, Object> defaultResult = new java.util.HashMap<>();
            defaultResult.put("score", 70);
            defaultResult.put("languageScore", 75);
            defaultResult.put("logicScore", 70);
            defaultResult.put("professionalScore", 65);
            defaultResult.put("overallScore", 70);
            defaultResult.put("feedback", "综合评估失败，返回默认评估结果。");
            defaultResult.put("suggestions", java.util.Collections.emptyList());
            return defaultResult;
        }
    }

    /**
     * 获取分数
     */
    private double getScore(java.util.Map<String, Object> result, String key, double defaultValue) {
        Object value = result.get(key);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        } else if (value != null) {
            try {
                return Double.parseDouble(value.toString());
            } catch (Exception e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    /**
     * 获取反馈
     */
    private String getFeedback(Map<String, Object> result, String defaultValue) {
        Object value = result.get("feedback");
        return value != null ? value.toString() : defaultValue;
    }

    /**
     * 获取建议
     */
    private List<String> getSuggestions(Map<String, Object> result) {
        Object value = result.get("suggestions");
        if (value instanceof List) {
            return (List<String>) value;
        }
        return Collections.emptyList();
    }

    /**
     * 保存评估结果到数据库
     */
    private void saveEvaluationToDatabase(Long interviewId, Object evaluationResult) {
        try {
            if (!(evaluationResult instanceof java.util.Map)) {
                log.error("评估结果格式不正确，不是Map类型");
                return;
            }

            java.util.Map<String, Object> resultMap = (java.util.Map<String, Object>) evaluationResult;

            InterviewEvaluation evaluation = new InterviewEvaluation();
            evaluation.setInterviewId(interviewId);

            // 提取分数
            Object scoreObj = resultMap.get("score");
            if (scoreObj != null) {
                if (scoreObj instanceof Number) {
                    evaluation.setScore(((Number) scoreObj).doubleValue());
                } else {
                    evaluation.setScore(Double.parseDouble(scoreObj.toString()));
                }
            }

            // 提取各维度分数
            Object languageScoreObj = resultMap.get("languageScore");
            if (languageScoreObj instanceof Number) {
                evaluation.setLanguageScore(((Number) languageScoreObj).doubleValue());
            }

            Object logicScoreObj = resultMap.get("logicScore");
            if (logicScoreObj instanceof Number) {
                evaluation.setLogicScore(((Number) logicScoreObj).doubleValue());
            }

            Object professionalScoreObj = resultMap.get("professionalScore");
            if (professionalScoreObj instanceof Number) {
                evaluation.setProfessionalScore(((Number) professionalScoreObj).doubleValue());
            }

            // 提取评估文本
            Object feedbackObj = resultMap.get("feedback");
            if (feedbackObj != null) {
                evaluation.setEvaluationText(feedbackObj.toString());
            }

            // 提取改进建议
            Object suggestionsObj = resultMap.get("suggestions");
            if (suggestionsObj instanceof java.util.List) {
                ObjectMapper objectMapper = new ObjectMapper();
                evaluation.setSuggestions(objectMapper.writeValueAsString(suggestionsObj));
            }

            evaluation.setCreatedAt(LocalDateTime.now());

            interviewEvaluationMapper.insert(evaluation);
            log.info("评估结果保存成功，interviewId：{}", interviewId);

        } catch (Exception e) {
            log.error("保存评估结果到数据库失败：{}", e.getMessage(), e);
        }
    }

    /**
     * 分析视频（专注于衣着和动作神态）
     */
    private Object analyzeVideoWithGLM(String videoUrl) {
        try {
            // 1. 检查API密钥
            String apiKey = System.getenv("ZAI_API_KEY");
            if (apiKey == null || apiKey.isEmpty()) {
                throw new RuntimeException("ZAI_API_KEY环境变量未设置");
            }

            // 2. 使用glm-4.6v模型
            String model = AIConstant.DEFAULT_VISION_MODEL;

            // 3. 使用单例获取GLM客户端
            ZhipuAiClient client = GLMClientSingleton.getInstance();

            // 4. 构建请求
            log.info("开始构建GLM-4.6v模型请求，视频URL：{}", videoUrl);
            ChatCompletionCreateParams request = ChatCompletionCreateParams.builder()
                    .model(model)
                    .messages(Arrays.asList(
                            ChatMessage.builder()
                                    .role(ChatMessageRole.USER.value())
                                    .content(Arrays.asList(
                                            MessageContent.builder()
                                                    .type("video_url")
                                                    .videoUrl(VideoUrl.builder()
                                                            .url(videoUrl)
                                                            .build())
                                                    .build(),
                                            MessageContent.builder()
                                                    .type("text")
                                                    .text("你是一位专业且严格的招聘面试官。请根据视频画面，分析求职者的外在表现，并按以下维度打分（0-100分）：\n" +
                                                            "1. appearanceScore：衣着得体度\n" +
                                                            "2. demeanorScore：神态自信度\n" +
                                                            "3. movementScore：动作自然度\n" +
                                                            "4. overallScore：整体形象总分\n\n" +
                                                            "请以纯JSON格式返回，不要输出其他无关字符。格式示例如下：\n" +
                                                            "{\n" +
                                                            "  \"appearanceScore\": 85,\n" +
                                                            "  \"demeanorScore\": 80,\n" +
                                                            "  \"movementScore\": 75,\n" +
                                                            "  \"overallScore\": 80,\n" +
                                                            "  \"feedback\": \"整体外在表现良好，衣着整洁，但动作略显紧张\",\n" +
                                                            "  \"suggestions\": [\"建议保持更自然的眼神交流\", \"建议手部动作适当放松\"]\n" +
                                                            "}")
                                                    .build()
                                    ))
                                    .build()
                    ))
                    .responseFormat(ResponseFormat.builder()
                            .type("json_object")
                            .build())
                    .temperature(0.1f)
                    .maxTokens(3000)
                    .build();

            // 5. 调用API
            log.info("调用GLM-4.6v模型API分析视频");
            long modelStartTime = System.currentTimeMillis();
            ChatCompletionResponse response = client.chat().createChatCompletion(request);
            long modelEndTime = System.currentTimeMillis();
            log.info("GLM-4.6v模型API调用完成，状态: {}, 耗时: {}ms",
                     response.isSuccess() ? "成功" : "失败",
                     (modelEndTime - modelStartTime));

            if (response.isSuccess()) {
                Object contentObj = response.getData().getChoices().get(0).getMessage().getContent();
                if (contentObj == null) {
                    log.error("GLM-4.6v模型返回空内容");
                    throw new RuntimeException("GLM-4.6v模型返回空内容");
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

                log.info("GLM-4.6v模型分析结果长度: {}", content.length());

                if (content.isEmpty()) {
                    log.error("GLM-4.6v模型返回空字符串");
                    throw new RuntimeException("GLM-4.6v模型返回空字符串");
                }

                // 解析JSON结果
                com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
                java.util.Map<String, Object> result = objectMapper.readValue(content, java.util.Map.class);
                log.info("GLM-4.6v模型分析结果解析成功");
                return result;
            } else {
                log.error("GLM-4.6v模型分析失败，错误信息: {}", response.getMsg());
                throw new RuntimeException("GLM-4.6v模型分析失败: " + response.getMsg());
            }
        } catch (Exception e) {
            log.error("调用GLM-4.6v模型失败：{}", e.getMessage(), e);
            // 返回默认评估结果
            java.util.Map<String, Object> defaultResult = new java.util.HashMap<>();
            defaultResult.put("appearanceScore", 75);
            defaultResult.put("demeanorScore", 70);
            defaultResult.put("movementScore", 70);
            defaultResult.put("overallScore", 70);
            defaultResult.put("feedback", "视频分析失败，返回默认评估结果。");
            defaultResult.put("suggestions", java.util.Collections.emptyList());
            return defaultResult;
        }
    }
}
