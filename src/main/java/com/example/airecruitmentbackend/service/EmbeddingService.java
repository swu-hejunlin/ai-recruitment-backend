package com.example.airecruitmentbackend.service;

import com.alibaba.dashscope.embeddings.TextEmbedding;
import com.alibaba.dashscope.embeddings.TextEmbeddingParam;
import com.alibaba.dashscope.embeddings.TextEmbeddingResult;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 文本嵌入服务
 * 使用阿里云DashScope的Text Embedding API将文本转换为向量
 */
@Slf4j
@Service
public class EmbeddingService {

    private final TextEmbedding textEmbedding;
    private final ObjectMapper objectMapper;
    private static final String DEFAULT_MODEL = "text-embedding-v4";

    public EmbeddingService() {
        this.textEmbedding = new TextEmbedding();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 将单个文本转换为向量
     */
    public List<Float> getEmbedding(String text) {
        if (text == null || text.trim().isEmpty()) {
            log.warn("输入文本为空");
            return Collections.emptyList();
        }

        try {
            TextEmbeddingParam param = TextEmbeddingParam.builder()
                    .model(DEFAULT_MODEL)
                    .texts(Collections.singletonList(text))
                    .build();

            TextEmbeddingResult result = textEmbedding.call(param);
            return parseEmbeddingResult(result);
        } catch (NoApiKeyException e) {
            log.error("调用Embedding API失败，请检查API Key配置: {}", e.getMessage());
            throw new RuntimeException("Embedding服务调用失败: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("获取文本Embedding失败: {}", e.getMessage(), e);
            throw new RuntimeException("获取文本Embedding失败: " + e.getMessage(), e);
        }
    }

    /**
     * 将多个文本批量转换为向量
     */
    public List<List<Float>> getEmbeddings(List<String> texts) {
        if (texts == null || texts.isEmpty()) {
            return Collections.emptyList();
        }

        try {
            TextEmbeddingParam param = TextEmbeddingParam.builder()
                    .model(DEFAULT_MODEL)
                    .texts(texts)
                    .build();

            TextEmbeddingResult result = textEmbedding.call(param);
            return parseEmbeddingsResult(result, texts.size());
        } catch (NoApiKeyException e) {
            log.error("调用Embedding API失败，请检查API Key配置: {}", e.getMessage());
            throw new RuntimeException("Embedding服务调用失败: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("批量获取文本Embedding失败: {}", e.getMessage(), e);
            throw new RuntimeException("批量获取文本Embedding失败: " + e.getMessage(), e);
        }
    }

    /**
     * 解析单个嵌入结果
     */
    private List<Float> parseEmbeddingResult(TextEmbeddingResult result) {
        try {
            if (result == null || result.getOutput() == null) {
                log.warn("Embedding结果为空");
                return Collections.emptyList();
            }

            String outputJson = objectMapper.writeValueAsString(result.getOutput());
            return parseVectorFromJson(outputJson);
        } catch (Exception e) {
            log.error("解析Embedding结果失败: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * 解析批量嵌入结果
     */
    private List<List<Float>> parseEmbeddingsResult(TextEmbeddingResult result, int expectedSize) {
        try {
            if (result == null || result.getOutput() == null) {
                log.warn("Embedding结果为空");
                return Collections.emptyList();
            }

            String outputJson = objectMapper.writeValueAsString(result.getOutput());
            return parseVectorsFromJson(outputJson);
        } catch (Exception e) {
            log.error("解析批量Embedding结果失败: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * 从JSON字符串中解析单个向量
     */
    private List<Float> parseVectorFromJson(String json) {
        try {
            com.fasterxml.jackson.databind.JsonNode root = objectMapper.readTree(json);

            if (root.has("embeddings") && root.get("embeddings").isArray() && root.get("embeddings").size() > 0) {
                com.fasterxml.jackson.databind.JsonNode embeddingItem = root.get("embeddings").get(0);

                if (embeddingItem.has("embedding") && embeddingItem.get("embedding").isArray()) {
                    List<Float> vector = new ArrayList<>();
                    com.fasterxml.jackson.databind.JsonNode embeddingArray = embeddingItem.get("embedding");
                    for (com.fasterxml.jackson.databind.JsonNode node : embeddingArray) {
                        vector.add((float) node.asDouble());
                    }
                    return vector;
                }
            }

            log.warn("无法从JSON中解析向量");
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("解析向量失败: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * 从JSON字符串中解析多个向量
     */
    private List<List<Float>> parseVectorsFromJson(String json) {
        try {
            com.fasterxml.jackson.databind.JsonNode root = objectMapper.readTree(json);
            List<List<Float>> vectors = new ArrayList<>();

            if (root.has("embeddings") && root.get("embeddings").isArray()) {
                for (com.fasterxml.jackson.databind.JsonNode embeddingItem : root.get("embeddings")) {
                    List<Float> vector = new ArrayList<>();
                    if (embeddingItem.has("embedding") && embeddingItem.get("embedding").isArray()) {
                        for (com.fasterxml.jackson.databind.JsonNode node : embeddingItem.get("embedding")) {
                            vector.add((float) node.asDouble());
                        }
                    }
                    vectors.add(vector);
                }
            }

            return vectors;
        } catch (Exception e) {
            log.error("解析向量列表失败: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * 检查Embedding服务是否可用
     */
    public boolean isAvailable() {
        try {
            List<Float> result = getEmbedding("测试");
            return !result.isEmpty();
        } catch (Exception e) {
            log.warn("Embedding服务不可用: {}", e.getMessage());
            return false;
        }
    }
}