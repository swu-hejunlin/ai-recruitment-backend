package com.example.airecruitmentbackend.util;

import com.example.airecruitmentbackend.exception.FileUploadException;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件验证工具类
 */
public class FileValidator {

    /**
     * 验证文件
     *
     * @param file 上传的文件
     * @param fileType 文件类型
     * @throws FileUploadException 文件上传异常
     */
    public static void validateFile(MultipartFile file, String fileType) throws FileUploadException {
        if (file == null || file.isEmpty()) {
            throw new FileUploadException("文件不能为空");
        }

        String contentType = file.getContentType();
        if (contentType == null) {
            throw new FileUploadException("无法识别文件类型");
        }

        long fileSize = file.getSize();

        switch (fileType) {
            case "avatar":
                validateAvatar(contentType, fileSize);
                break;
            case "resume":
                validateResume(contentType, fileSize);
                break;
            case "logo":
                validateLogo(contentType, fileSize);
                break;
            case "license":
                validateLicense(contentType, fileSize);
                break;
            default:
                throw new FileUploadException("不支持的文件类型：" + fileType);
        }
    }

    /**
     * 验证头像文件
     */
    private static void validateAvatar(String contentType, long fileSize) throws FileUploadException {
        boolean allowed = false;
        for (String type : FileConstants.ALLOWED_AVATAR_TYPES) {
            if (type.equals(contentType)) {
                allowed = true;
                break;
            }
        }

        if (!allowed) {
            throw new FileUploadException("仅支持 JPG、PNG、GIF、WebP 格式的图片");
        }

        if (fileSize > FileConstants.MAX_AVATAR_SIZE) {
            throw new FileUploadException("头像文件大小不能超过5MB");
        }
    }

    /**
     * 验证简历文件
     */
    private static void validateResume(String contentType, long fileSize) throws FileUploadException {
        boolean allowed = false;
        for (String type : FileConstants.ALLOWED_RESUME_TYPES) {
            if (type.equals(contentType)) {
                allowed = true;
                break;
            }
        }

        if (!allowed) {
            throw new FileUploadException("仅支持 PDF、Word、Word2007+ 格式的文档");
        }

        if (fileSize > FileConstants.MAX_RESUME_SIZE) {
            throw new FileUploadException("简历文件大小不能超过20MB");
        }
    }

    /**
     * 验证营业执照文件
     */
    private static void validateLicense(String contentType, long fileSize) throws FileUploadException {
        boolean allowed = false;
        for (String type : FileConstants.ALLOWED_LICENSE_TYPES) {
            if (type.equals(contentType)) {
                allowed = true;
                break;
            }
        }

        if (!allowed) {
            throw new FileUploadException("仅支持 JPG、PNG、GIF、PDF 格式的文件");
        }

        if (fileSize > FileConstants.MAX_LICENSE_SIZE) {
            throw new FileUploadException("营业执照文件大小不能超过10MB");
        }
    }

    /**
     * 验证企业Logo文件
     */
    private static void validateLogo(String contentType, long fileSize) throws FileUploadException {
        boolean allowed = false;
        for (String type : FileConstants.ALLOWED_LOGO_TYPES) {
            if (type.equals(contentType)) {
                allowed = true;
                break;
            }
        }

        if (!allowed) {
            throw new FileUploadException("仅支持 JPG、PNG、GIF、WebP 格式的图片");
        }

        if (fileSize > FileConstants.MAX_LOGO_SIZE) {
            throw new FileUploadException("Logo文件大小不能超过5MB");
        }
    }
}
