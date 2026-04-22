package com.example.airecruitmentbackend.service.impl;

import ai.z.openapi.ZhipuAiClient;
import ai.z.openapi.service.model.*;
import com.example.airecruitmentbackend.common.AIConstant;
import com.example.airecruitmentbackend.dto.ResumeAnalysisRequest;
import com.example.airecruitmentbackend.dto.ResumeAnalysisResponse;
import com.example.airecruitmentbackend.dto.ResumeSmartFillResponse;
import com.example.airecruitmentbackend.service.ResumeService;
import com.example.airecruitmentbackend.util.OssUtil;
import com.example.airecruitmentbackend.util.PdfUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.*;

/**
 * 简历分析服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ResumeServiceImpl implements ResumeService {

    private final OssUtil ossUtil;

    @Override
    public ResumeAnalysisResponse analyzeResume(ResumeAnalysisRequest request) {
        try {
            log.info("开始分析简历");
            
            // 1. 获取简历文件内容
            byte[] resumeBytes;
            if (request.getResumeUrl() != null) {
                // 从OSS下载文件
                resumeBytes = downloadFileFromOss(request.getResumeUrl());
            } else if (request.getResumeBase64() != null) {
                // 解析Base64编码的文件
                resumeBytes = Base64.getDecoder().decode(request.getResumeBase64());
            } else {
                return createErrorResponse("简历文件不能为空");
            }
            
            // 2. 处理文件，转换为图片（如果是PDF）
            String base64Image;
            if (request.getFileType() != null && request.getFileType().toLowerCase().contains("pdf")) {
                base64Image = convertPdfFirstPageToBase64(resumeBytes);
            } else {
                // 对于图片文件，直接转换为Base64
                base64Image = Base64.getEncoder().encodeToString(resumeBytes);
            }
            
            // 3. 调用GLM模型分析简历
            String analysisResult = analyzeWithGLM(base64Image);
            
            // 4. 直接返回原始分析结果，不做解析
            ResumeAnalysisResponse response = new ResumeAnalysisResponse();
            response.setSuccess(true);
            response.setOverallRating(4); // 默认评级
            response.setRatingDescription("分析完成");
            
            // 将原始分析结果作为亮点返回，前端直接展示
            List<String> highlights = new ArrayList<>();
            highlights.add(analysisResult);
            response.setHighlights(highlights);
            
            // 其他字段设置为空列表，避免前端报错
            response.setWeaknesses(new ArrayList<>());
            response.setSuggestions(new ArrayList<>());
            response.setSkillEvaluations(new ArrayList<>());
            
            return response;
            
        } catch (Exception e) {
            log.error("简历分析失败", e);
            return createErrorResponse("简历分析失败: " + e.getMessage());
        }
    }

    @Override
    public ResumeAnalysisResponse uploadAndAnalyzeResume(MultipartFile file) {
        try {
            log.info("上传并分析简历，文件名：{}", file.getOriginalFilename());
            
            // 1. 上传文件到OSS
            String fileUrl = ossUtil.uploadFile(file, "resume/temp");
            
            // 2. 分析简历
            ResumeAnalysisRequest request = new ResumeAnalysisRequest();
            request.setResumeUrl(fileUrl);
            request.setFileType(file.getOriginalFilename() != null ? file.getOriginalFilename().toLowerCase() : "");
            
            return analyzeResume(request);
            
        } catch (Exception e) {
            log.error("上传并分析简历失败", e);
            return createErrorResponse("上传并分析简历失败: " + e.getMessage());
        }
    }

    @Override
    public ResumeSmartFillResponse smartFillResume(MultipartFile file) {
        log.info("开始智能填充简历信息，文件名：{}", file.getOriginalFilename());
        long startTime = System.currentTimeMillis();
        try {
            // 1. 获取文件内容
            byte[] fileBytes = file.getBytes();
            
            // 2. 从文件中提取文本
            String resumeText;
            String fileName = file.getOriginalFilename();
            
            if (fileName != null && fileName.toLowerCase().contains("pdf")) {
                log.debug("检测到PDF文件，开始提取文本");
                resumeText = PdfUtil.extractAndCleanText(fileBytes);
                log.debug("PDF文本提取完成，文本长度：{}", resumeText.length());
            } else {
                // 对于Word文档，暂时直接读取为字符串
                log.debug("检测到非PDF文件，直接读取为文本");
                resumeText = new String(fileBytes, java.nio.charset.StandardCharsets.UTF_8);
                log.debug("非PDF文件读取完成，文本长度：{}", resumeText.length());
            }
            
            // 3. 调用GLM模型进行结构化提取
            log.debug("开始调用GLM模型进行结构化提取");
            String jsonResult = analyzeWithGLMStructured(resumeText);
            log.debug("GLM模型调用完成，返回结果长度：{}", jsonResult.length());
            
            // 4. 解析JSON结果
            log.debug("开始解析JSON结果");
            ResumeSmartFillResponse response = parseSmartFillResult(jsonResult);
            response.setSuccess(true);
            
            // 5. 标记未填充的字段
            List<String> unfilledFields = findUnfilledFields(response);
            response.setUnfilledFields(unfilledFields);
            response.setConfidence(0.8); // 默认置信度
            
            long endTime = System.currentTimeMillis();
            log.info("智能填充简历信息完成，耗时：{}ms，提取字段数：{}", 
                     (endTime - startTime), 
                     countFilledFields(response));
            return response;
            
        } catch (Exception e) {
            long endTime = System.currentTimeMillis();
            log.error("智能填充简历失败，耗时：{}ms", (endTime - startTime), e);
            ResumeSmartFillResponse response = new ResumeSmartFillResponse();
            response.setSuccess(false);
            response.setErrorMessage("智能填充失败: " + e.getMessage());
            return response;
        }
    }
    
    /**
     * 统计已填充的字段数
     */
    private int countFilledFields(ResumeSmartFillResponse response) {
        int count = 0;
        if (response.getName() != null) count++;
        if (response.getGender() != null) count++;
        if (response.getAge() != null) count++;
        if (response.getPhone() != null) count++;
        if (response.getEmail() != null) count++;
        if (response.getCity() != null) count++;
        if (response.getWorkYears() != null) count++;
        if (response.getSkills() != null && !response.getSkills().isEmpty()) count++;
        return count;
    }

    /**
     * 从OSS下载文件
     */
    private byte[] downloadFileFromOss(String fileUrl) throws IOException {
        // 这里需要实现从OSS下载文件的逻辑
        // 暂时使用URL.openStream()作为示例
        URL url = new URL(fileUrl);
        return url.openStream().readAllBytes();
    }

    /**
     * 将PDF的第一页转换为Base64编码的PNG图片
     */
    public String convertPdfFirstPageToBase64(byte[] pdfBytes) {
        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            PDFRenderer renderer = new PDFRenderer(document);
            // 0 表示第一页，150 是 DPI（分辨率平衡清晰度和体积）
            BufferedImage image = renderer.renderImageWithDPI(0, 150);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            byte[] imageBytes = baos.toByteArray();

            return Base64.getEncoder().encodeToString(imageBytes);
        } catch (Exception e) {
            throw new RuntimeException("PDF解析失败", e);
        }
    }

    /**
     * 使用GLM模型分析简历
     */
    private String analyzeWithGLM(String base64Image) {
        // 1. 检查API密钥
        String apiKey = System.getenv("ZAI_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            throw new RuntimeException("ZAI_API_KEY环境变量未设置");
        }
        
        // 2. 只使用免费的glm-4.6v-flash模型
        String model = AIConstant.DEFAULT_VISION_MODEL;
        
        // 3. 重试机制
        int maxRetries = 3;
        int retryDelay = 1000; // 1秒
        
        // 4. 创建GLM客户端
        ZhipuAiClient client = ZhipuAiClient.builder().ofZHIPU()
                .apiKey(apiKey)
                .build();
        
        // 5. 构建请求
        log.info("开始构建GLM模型请求");
        ChatCompletionCreateParams request = ChatCompletionCreateParams.builder()
                .model(model)
                .messages(Arrays.asList(
                        ChatMessage.builder()
                                .role(ChatMessageRole.USER.value())
                                .content(Arrays.asList(
                                        MessageContent.builder()
                                                    .type("text")
                                                    .text("你是一个资深HR和职业顾问，需要对这份简历进行全面分析。请提供详细、专业的分析报告，包括以下几个部分：\n\n" +
                                                            "1. 整体评价：对简历进行1-5分的评分，并给出简要的评级描述\n" +
                                                            "2. 个人亮点：分析简历中的优势和亮点\n" +
                                                            "3. 存在的不足：指出简历中存在的问题和不足\n" +
                                                            "4. 改进建议：针对不足提出具体的改进建议\n" +
                                                            "5. 技能评估：对简历中提到的技能进行评估\n\n" +
                                                            "请使用Markdown格式输出，确保内容清晰易读。不要输出任何JSON格式，直接输出分析报告。")
                                            .build(),
                                        MessageContent.builder()
                                                .type("image_url")
                                                .imageUrl(ImageUrl.builder()
                                                        .url("data:image/png;base64," + base64Image)
                                                        .build())
                                        .build()))
                                .build()
                ))
                .build();

        // 6. 调用API，带重试
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            log.info("第 {} 次尝试调用GLM模型API", attempt);
            try {
                ChatCompletionResponse response = client.chat().createChatCompletion(request);
                log.info("GLM模型API调用完成，状态: {}", response.isSuccess() ? "成功" : "失败");
                if (response.isSuccess()) {
                    String content = (String) response.getData().getChoices().get(0).getMessage().getContent();
                    log.info("GLM模型分析结果长度: {}", content.length());
                    return content;
                } else {
                    log.error("GLM模型分析失败，错误信息: {}", response.getMsg());
                    // 如果不是服务过载错误，直接抛出异常
                    if (!response.getMsg().contains("overloaded")) {
                        throw new RuntimeException("GLM模型分析失败: " + response.getMsg());
                    }
                }
            } catch (Exception e) {
                log.error("GLM模型API调用异常", e);
                // 如果不是服务过载错误，直接抛出异常
                if (!e.getMessage().contains("overloaded")) {
                    throw new RuntimeException("GLM模型分析失败: " + e.getMessage(), e);
                }
            }
            
            // 重试前等待
            if (attempt < maxRetries) {
                log.info("等待 {}ms 后重试", retryDelay);
                try {
                    Thread.sleep(retryDelay);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
                // 指数退避
                retryDelay *= 2;
            }
        }
        
        // 所有尝试都失败
        throw new RuntimeException("GLM模型分析失败: 服务暂时过载，请稍后再试");
    }

    /**
     * 使用GLM模型进行结构化提取（智能填充）
     */
    private String analyzeWithGLMStructured(String resumeText) {
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
        log.debug("开始构建GLM模型结构化提取请求");
        ChatCompletionCreateParams request = ChatCompletionCreateParams.builder()
                .model(model)
                .messages(Arrays.asList(
                        ChatMessage.builder()
                                .role(ChatMessageRole.USER.value())
                                .content("你是一个专业的简历信息提取助手。请从以下简历文本中提取个人基本信息。\n\n" +
                                        "要求：\n" +
                                        "1. 只返回JSON格式，不要包含任何解释或说明\n" +
                                        "2. 严格按照以下JSON Schema返回\n" +
                                        "3. 如果某字段在简历中无法确定，设置为null\n" +
                                        "4. 性别：男返回1，女返回2\n" +
                                        "5. 当前状态：1-在职，2-离职，3-在读学生\n" +
                                        "\n" +
                                        "JSON Schema:\n" +
                                        "{\n" +
                                        "  \"name\": \"姓名（字符串）\",\n" +
                                        "  \"gender\": \"性别（1-男，2-女）\",\n" +
                                        "  \"age\": \"年龄（数字）\",\n" +
                                        "  \"phone\": \"手机号（字符串）\",\n" +
                                        "  \"email\": \"邮箱（字符串）\",\n" +
                                        "  \"city\": \"所在城市（字符串）\",\n" +
                                        "  \"workYears\": \"工作年限（数字）\",\n" +
                                        "  \"currentStatus\": \"当前状态（1-在职，2-离职，3-在读学生）\",\n" +
                                        "  \"skills\": [\"技能1\", \"技能2\", \"技能3\"],\n" +
                                        "  \"introduction\": \"个人简介或自我评价（字符串）\"\n" +
                                        "}\n" +
                                        "\n" +
                                        "简历文本：\n" +
                                        resumeText + "\n\n" +
                                        "请提取信息并以 JSON 格式输出。如果无法提取，对应字段设为 null。注意：你可以输出 JSON 代码块，无需额外解释。")
                                .build()
                ))
                .responseFormat(ResponseFormat.builder()
                        .type("json_object")
                        .build())
                .temperature(0.1f)
                .maxTokens(2000)
                .build();

        // 5. 调用API，不使用重试机制
        log.debug("调用GLM模型API进行结构化提取");
        long modelStartTime = System.currentTimeMillis();
        try {
            ChatCompletionResponse response = client.chat().createChatCompletion(request);
            long modelEndTime = System.currentTimeMillis();
            log.debug("GLM模型API调用完成，状态: {}, 耗时: {}ms", 
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
                
                log.debug("GLM模型结构化提取结果长度: {}", content.length());
                
                if (content.isEmpty()) {
                    log.error("GLM模型返回空字符串");
                    throw new RuntimeException("GLM模型返回空字符串");
                }
                
                // 检查返回的内容是否是有效的JSON
                try {
                    com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    objectMapper.readTree(content);
                } catch (Exception e) {
                    log.error("GLM模型返回的内容不是有效的JSON", e);
                    throw new RuntimeException("GLM模型返回的内容不是有效的JSON: " + e.getMessage(), e);
                }
                
                return content;
            } else {
                log.error("GLM模型结构化提取失败，错误信息: {}", response.getMsg());
                throw new RuntimeException("GLM模型结构化提取失败: " + response.getMsg());
            }
        } catch (Exception e) {
            long modelEndTime = System.currentTimeMillis();
            log.error("GLM模型API调用异常，耗时: {}ms", (modelEndTime - modelStartTime), e);
            throw new RuntimeException("GLM模型结构化提取失败: " + e.getMessage(), e);
        }
    }

    /**
     * 解析智能填充结果
     */
    private ResumeSmartFillResponse parseSmartFillResult(String jsonResult) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return objectMapper.readValue(jsonResult, ResumeSmartFillResponse.class);
        } catch (Exception e) {
            log.error("解析智能填充结果失败: {}", e.getMessage());
            throw new RuntimeException("解析智能填充结果失败: " + e.getMessage(), e);
        }
    }

    /**
     * 查找未填充的字段
     */
    private List<String> findUnfilledFields(ResumeSmartFillResponse response) {
        List<String> unfilledFields = new ArrayList<>();
        
        if (response.getName() == null) unfilledFields.add("姓名");
        if (response.getGender() == null) unfilledFields.add("性别");
        if (response.getAge() == null) unfilledFields.add("年龄");
        if (response.getPhone() == null) unfilledFields.add("手机号");
        if (response.getEmail() == null) unfilledFields.add("邮箱");
        if (response.getCity() == null) unfilledFields.add("所在城市");
        if (response.getWorkYears() == null) unfilledFields.add("工作年限");
        if (response.getSkills() == null || response.getSkills().isEmpty()) unfilledFields.add("技能标签");
        
        return unfilledFields;
    }

    /**
     * 解析分析结果
     */
    private ResumeAnalysisResponse parseAnalysisResult(String analysisResult) {
        ResumeAnalysisResponse response = new ResumeAnalysisResponse();
        response.setSuccess(true);
        
        try {
            // 清理分析结果，处理Markdown代码块
            String cleanedResult = analysisResult.trim();
            
            // 检查是否被Markdown代码块包裹
            if (cleanedResult.startsWith("```json") || cleanedResult.startsWith("```")) {
                // 提取代码块内容
                int startIndex = cleanedResult.indexOf("\n");
                int endIndex = cleanedResult.lastIndexOf("```");
                if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
                    cleanedResult = cleanedResult.substring(startIndex + 1, endIndex).trim();
                    log.info("已从Markdown代码块中提取JSON内容");
                }
            }
            
            // 解析JSON格式的输出
            com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
            
            // 尝试直接解析为ResumeAnalysisResponse
            try {
                return objectMapper.readValue(cleanedResult, ResumeAnalysisResponse.class);
            } catch (Exception e) {
                // 如果直接解析失败，尝试解析为中间对象
                log.warn("直接解析为ResumeAnalysisResponse失败，尝试解析为中间对象: {}", e.getMessage());
                
                // 定义中间对象结构
                class SkillEvaluationDTO {
                    private String skillName;
                    private int proficiencyLevel;
                    private String evaluation;
                    
                    //  getter and setter methods
                    public String getSkillName() { return skillName; }
                    public void setSkillName(String skillName) { this.skillName = skillName; }
                    public int getProficiencyLevel() { return proficiencyLevel; }
                    public void setProficiencyLevel(int proficiencyLevel) { this.proficiencyLevel = proficiencyLevel; }
                    public String getEvaluation() { return evaluation; }
                    public void setEvaluation(String evaluation) { this.evaluation = evaluation; }
                }
                
                class AnalysisResultDTO {
                    private int overallRating;
                    private String ratingDescription;
                    private List<String> highlights;
                    private List<String> weaknesses;
                    private List<String> suggestions;
                    private List<SkillEvaluationDTO> skillEvaluations;
                    
                    //  getter and setter methods
                    public int getOverallRating() { return overallRating; }
                    public void setOverallRating(int overallRating) { this.overallRating = overallRating; }
                    public String getRatingDescription() { return ratingDescription; }
                    public void setRatingDescription(String ratingDescription) { this.ratingDescription = ratingDescription; }
                    public List<String> getHighlights() { return highlights; }
                    public void setHighlights(List<String> highlights) { this.highlights = highlights; }
                    public List<String> getWeaknesses() { return weaknesses; }
                    public void setWeaknesses(List<String> weaknesses) { this.weaknesses = weaknesses; }
                    public List<String> getSuggestions() { return suggestions; }
                    public void setSuggestions(List<String> suggestions) { this.suggestions = suggestions; }
                    public List<SkillEvaluationDTO> getSkillEvaluations() { return skillEvaluations; }
                    public void setSkillEvaluations(List<SkillEvaluationDTO> skillEvaluations) { this.skillEvaluations = skillEvaluations; }
                }
                
                AnalysisResultDTO dto = objectMapper.readValue(cleanedResult, AnalysisResultDTO.class);
                
                // 转换为ResumeAnalysisResponse
                response.setOverallRating(dto.getOverallRating());
                response.setRatingDescription(dto.getRatingDescription());
                response.setHighlights(dto.getHighlights());
                response.setWeaknesses(dto.getWeaknesses());
                response.setSuggestions(dto.getSuggestions());
                
                // 转换技能评估
                List<ResumeAnalysisResponse.SkillEvaluation> skillEvaluations = new ArrayList<>();
                if (dto.getSkillEvaluations() != null) {
                    for (SkillEvaluationDTO skillDTO : dto.getSkillEvaluations()) {
                        ResumeAnalysisResponse.SkillEvaluation skill = new ResumeAnalysisResponse.SkillEvaluation();
                        skill.setSkillName(skillDTO.getSkillName());
                        skill.setProficiencyLevel(skillDTO.getProficiencyLevel());
                        skill.setEvaluation(skillDTO.getEvaluation());
                        skillEvaluations.add(skill);
                    }
                }
                response.setSkillEvaluations(skillEvaluations);
                
                return response;
            }
        } catch (Exception e) {
            log.error("解析分析结果失败: {}", e.getMessage());
            // 如果解析失败，返回错误响应
            return createErrorResponse("解析分析结果失败: " + e.getMessage());
        }
    }
    
    /**
     * 创建错误响应
     */
    private ResumeAnalysisResponse createErrorResponse(String errorMessage) {
        ResumeAnalysisResponse response = new ResumeAnalysisResponse();
        response.setSuccess(false);
        response.setErrorMessage(errorMessage);
        
        // 设置默认值，确保前端不会因为缺少字段而报错
        response.setOverallRating(0);
        response.setRatingDescription("分析失败");
        response.setHighlights(new ArrayList<>());
        response.setWeaknesses(new ArrayList<>());
        response.setSuggestions(new ArrayList<>());
        response.setSkillEvaluations(new ArrayList<>());
        
        return response;
    }
}