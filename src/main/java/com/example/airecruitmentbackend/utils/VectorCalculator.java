package com.example.airecruitmentbackend.utils;

import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 向量计算工具类
 * 提供向量相似度计算等功能
 */
@Slf4j
public class VectorCalculator {

    /**
     * 计算两个向量的余弦相似度
     *
     * @param vectorA 向量A
     * @param vectorB 向量B
     * @return 余弦相似度值（范围0-1，1表示完全相似）
     */
    public static double cosineSimilarity(List<Float> vectorA, List<Float> vectorB) {
        if (vectorA == null || vectorB == null || vectorA.isEmpty() || vectorB.isEmpty()) {
            log.warn("向量为空，无法计算相似度");
            return 0.0;
        }

        if (vectorA.size() != vectorB.size()) {
            log.warn("向量维度不一致，无法计算相似度: {} vs {}", vectorA.size(), vectorB.size());
            return 0.0;
        }

        double dotProduct = 0.0;
        double magnitudeA = 0.0;
        double magnitudeB = 0.0;

        for (int i = 0; i < vectorA.size(); i++) {
            double a = vectorA.get(i) != null ? vectorA.get(i) : 0.0;
            double b = vectorB.get(i) != null ? vectorB.get(i) : 0.0;

            dotProduct += a * b;
            magnitudeA += a * a;
            magnitudeB += b * b;
        }

        magnitudeA = Math.sqrt(magnitudeA);
        magnitudeB = Math.sqrt(magnitudeB);

        if (magnitudeA == 0 || magnitudeB == 0) {
            log.warn("向量模为0，无法计算相似度");
            return 0.0;
        }

        return dotProduct / (magnitudeA * magnitudeB);
    }

    /**
     * 计算两个向量的余弦距离
     * 余弦距离 = 1 - 余弦相似度
     *
     * @param vectorA 向量A
     * @param vectorB 向量B
     * @return 余弦距离值（范围0-1，0表示完全相似）
     */
    public static double cosineDistance(List<Float> vectorA, List<Float> vectorB) {
        return 1.0 - cosineSimilarity(vectorA, vectorB);
    }

    /**
     * 将相似度转换为百分制分数
     *
     * @param similarity 余弦相似度（0-1）
     * @return 百分制分数（0-100）
     */
    public static double similarityToPercent(double similarity) {
        return similarity * 100.0;
    }

    /**
     * 归一化向量到指定范围
     *
     * @param vector 原始向量
     * @param min    最小值
     * @param max    最大值
     * @return 归一化后的向量
     */
    public static List<Double> normalize(List<Float> vector, double min, double max) {
        if (vector == null || vector.isEmpty()) {
            return List.of();
        }

        double magnitude = 0.0;
        for (Float v : vector) {
            double value = v != null ? v : 0.0;
            magnitude += value * value;
        }
        magnitude = Math.sqrt(magnitude);

        if (magnitude == 0) {
            return vector.stream().map(v -> 0.0).toList();
        }

        final double finalMagnitude = magnitude;
        return vector.stream().map(v -> {
            double value = v != null ? v : 0.0;
            double normalized = value / finalMagnitude;
            return min + normalized * (max - min);
        }).toList();
    }

    /**
     * 检查向量是否有效
     *
     * @param vector 向量
     * @return 是否有效
     */
    public static boolean isValidVector(List<Float> vector) {
        return vector != null && !vector.isEmpty() && vector.stream().allMatch(v -> v != null && !v.isNaN() && !v.isInfinite());
    }

    /**
     * 将JSON格式的向量字符串解析为List<Float>
     *
     * @param jsonVector JSON格式的向量字符串
     * @return 解析后的向量
     */
    public static List<Float> parseFromJson(String jsonVector) {
        if (jsonVector == null || jsonVector.isEmpty()) {
            return List.of();
        }

        try {
            com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return objectMapper.readValue(jsonVector, objectMapper.getTypeFactory().constructCollectionType(List.class, Float.class));
        } catch (Exception e) {
            log.error("解析向量JSON失败: {}", e.getMessage());
            return List.of();
        }
    }
}