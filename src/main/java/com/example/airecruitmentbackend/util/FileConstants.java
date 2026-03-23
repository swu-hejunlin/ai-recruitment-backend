package com.example.airecruitmentbackend.util;

/**
 * 文件相关常量类
 */
public class FileConstants {

    /**
     * 求职者头像路径
     */
    public static final String JOB_SEEKER_AVATAR_PATH = "avatar/job-seeker/";

    /**
     * 企业logo路径
     */
    public static final String COMPANY_LOGO_PATH = "logo/company/";

    /**
     * 求职者简历路径
     */
    public static final String RESUME_PATH = "resume/job-seeker/";

    /**
     * 企业营业执照路径
     */
    public static final String BUSINESS_LICENSE_PATH = "license/company/";

    /**
     * 头像文件最大大小（5MB）
     */
    public static final long MAX_AVATAR_SIZE = 5 * 1024 * 1024;

    /**
     * 简历文件最大大小（20MB）
     */
    public static final long MAX_RESUME_SIZE = 20 * 1024 * 1024;

    /**
     * 营业执照文件最大大小（10MB）
     */
    public static final long MAX_LICENSE_SIZE = 10 * 1024 * 1024;

    /**
     * Logo文件最大大小（5MB）
     */
    public static final long MAX_LOGO_SIZE = 5 * 1024 * 1024;

    /**
     * 支持的头像文件类型
     */
    public static final String[] ALLOWED_AVATAR_TYPES = {
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    };

    /**
     * 支持的简历文件类型
     */
    public static final String[] ALLOWED_RESUME_TYPES = {
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    };

    /**
     * 支持的营业执照文件类型
     */
    public static final String[] ALLOWED_LICENSE_TYPES = {
            "image/jpeg", "image/jpg", "image/png", "image/gif",
            "application/pdf"
    };

    /**
     * 支持的Logo文件类型
     */
    public static final String[] ALLOWED_LOGO_TYPES = {
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    };

    /**
     * 根据文件类型和用户角色获取文件路径
     *
     * @param fileType 文件类型（avatar/resume/logo/license）
     * @param role 用户角色（1-求职者，2-企业HR）
     * @return 文件路径
     */
    public static String getFilePathByFileType(String fileType, Integer role) {
        switch (fileType) {
            case "avatar":
                if (role == 1) {
                    return JOB_SEEKER_AVATAR_PATH;
                }
                break;
            case "resume":
                return RESUME_PATH;
            case "logo":
                if (role == 2) {
                    return COMPANY_LOGO_PATH;
                }
                break;
            case "license":
                return BUSINESS_LICENSE_PATH;
            default:
                return null;
        }
        return null;
    }

    /**
     * 根据文件类型获取允许的文件类型数组
     *
     * @param fileType 文件类型
     * @return 允许的文件类型数组
     */
    public static String[] getAllowedTypesByFileType(String fileType) {
        switch (fileType) {
            case "avatar":
                return ALLOWED_AVATAR_TYPES;
            case "resume":
                return ALLOWED_RESUME_TYPES;
            case "logo":
                return ALLOWED_LOGO_TYPES;
            case "license":
                return ALLOWED_LICENSE_TYPES;
            default:
                return new String[0];
        }
    }

    /**
     * 根据文件类型获取文件最大大小
     *
     * @param fileType 文件类型
     * @return 文件最大大小（字节）
     */
    public static long getMaxSizeByFileType(String fileType) {
        switch (fileType) {
            case "avatar":
                return MAX_AVATAR_SIZE;
            case "resume":
                return MAX_RESUME_SIZE;
            case "logo":
                return MAX_LOGO_SIZE;
            case "license":
                return MAX_LICENSE_SIZE;
            default:
                return 0;
        }
    }
}
