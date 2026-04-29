package com.example.airecruitmentbackend.common;

/**
 * AI模型常量类
 * 用于管理可用的AI模型列表
 */
public class AIConstant {

    /**
     * 私有构造函数，防止实例化
     */
    private AIConstant() {
        throw new IllegalStateException("常量类不允许实例化");
    }

    /**
     * 免费模型列表（按优先级排序）
     */
    public static final String[] FREE_MODELS = {
        "glm-4.7-flash",   // 最新免费模型，优先使用
        "glm-4.6v-flash"   // 视觉免费模型，支持图片分析
    };

    /**
     * 付费模型列表（按优先级排序）
     */
    public static final String[] PAID_MODELS = {
        "glm-4-plus",
        "glm-4",
        "glm-3 Turbo"
    };

    /**
     * 视觉模型列表（支持图片分析）
     */
    public static final String[] VISION_MODELS = {
        "glm-4v-flash",
        "glm-4.6v-flash"
    };

    /**
     * 默认使用的免费模型
     */
    public static final String DEFAULT_FREE_MODEL = "glm-4.7-flash";

    /**
     * 默认使用的视觉模型
     */
    public static final String Free_VISION_MODEL = "glm-4.6v-flash";

    public static final String LOW_MODEL = "glm-4-flash";

    public static final String DEFAULT_VISION_MODEL = "glm-4.6v-flashx";
    public static final String FLASH_FREE_MODEL = "glm-4-Flash-250414";

    /**
     * 默认使用的通用模型
     */
    public static final String DEFAULT_MODEL = DEFAULT_FREE_MODEL;

    /**
     * 获取默认模型
     * @return 默认模型名称
     */
    public static String getDefaultModel() {
        return DEFAULT_MODEL;
    }

    /**
     * 获取默认视觉模型（用于图片分析）
     * @return 默认视觉模型名称
     */
    public static String getDefaultVisionModel() {
        return DEFAULT_VISION_MODEL;
    }

    /**
     * 检查模型是否为视觉模型
     * @param modelName 模型名称
     * @return 是否为视觉模型
     */
    public static boolean isVisionModel(String modelName) {
        for (String model : VISION_MODELS) {
            if (model.equals(modelName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检查模型是否免费
     * @param modelName 模型名称
     * @return 是否免费
     */
    public static boolean isFreeModel(String modelName) {
        for (String model : FREE_MODELS) {
            if (model.equals(modelName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取所有可用模型
     * @return 所有可用模型列表
     */
    public static String[] getAllModels() {
        return FREE_MODELS;
    }
}
