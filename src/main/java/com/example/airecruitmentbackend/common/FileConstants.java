package com.example.airecruitmentbackend.common;

/**
 * 文件相关常量
 */
public class FileConstants {

    /**
     * 文件大小限制
     */
    // 头像大小限制：2MB
    public static final long AVATAR_MAX_SIZE = 2 * 1024 * 1024L;

    // 简历文件大小限制：10MB
    public static final long RESUME_MAX_SIZE = 10 * 1024 * 1024L;

    // 企业Logo大小限制：5MB
    public static final long LOGO_MAX_SIZE = 5 * 1024 * 1024L;

    /**
     * 允许的文件类型
     */
    // 允许的图片格式
    public static final String[] ALLOWED_IMAGE_TYPES = {
            "image/jpeg",
            "image/jpg",
            "image/png",
            "image/gif",
            "image/webp"
    };

    // 允许的图片扩展名
    public static final String[] ALLOWED_IMAGE_EXTENSIONS = {
            ".jpg", ".jpeg", ".png", ".gif", ".webp"
    };

    // 允许的简历文件格式
    public static final String[] ALLOWED_RESUME_TYPES = {
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    };

    // 允许的简历文件扩展名
    public static final String[] ALLOWED_RESUME_EXTENSIONS = {
            ".pdf", ".doc", ".docx"
    };

    // 允许的Logo图片格式
    public static final String[] ALLOWED_LOGO_TYPES = {
            "image/jpeg",
            "image/jpg",
            "image/png",
            "image/gif",
            "image/webp"
    };

    /**
     * 文件路径常量
     */
    // 头像存储路径
    public static final String AVATAR_PATH = "avatar/";

    // 简历文件存储路径
    public static final String RESUME_PATH = "resume/";

    // 企业Logo存储路径
    public static final String LOGO_PATH = "logo/";

    // 其他文件存储路径
    public static final String COMMON_PATH = "common/";

    /**
     * 错误消息
     */
    public static final String FILE_NOT_NULL = "文件不能为空";
    public static final String FILE_SIZE_EXCEED = "文件大小超过限制";
    public static final String FILE_TYPE_NOT_ALLOWED = "文件类型不支持";
    public static final String FILE_UPLOAD_FAILED = "文件上传失败";
}
