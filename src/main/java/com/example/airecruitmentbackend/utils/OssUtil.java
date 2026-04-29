package com.example.airecruitmentbackend.utils;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.*;
import com.example.airecruitmentbackend.config.OssConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * 阿里云OSS工具类
 * 提供文件上传、下载、删除等基础功能
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OssUtil {

    private final OSS ossClient;
    private final OssConfig ossConfig;

    /**
     * 上传文件到OSS
     *
     * @param file     要上传的文件
     * @param filePath 文件路径（如：avatar/、resume/、logo/）
     * @return 文件访问URL
     * @throws IOException IO异常
     */
    public String uploadFile(MultipartFile file, String filePath) throws IOException {
        // 1. 入参校验（精简且明确）
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("上传文件不能为空");
        }
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new IllegalArgumentException("文件名不能为空");
        }

        // 2. 修复核心漏洞：生成唯一文件名（原代码缺失 fileName 定义）
        String fileExtension = getFileExtension(originalFilename);
        String newFileName = UUID.randomUUID().toString() + fileExtension;
        String objectKey = filePath + newFileName;

        // 3. 设置元数据 → 解决PDF预览问题
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentDisposition("inline"); // 强制预览，不下载

        // 自动识别PDF，设置正确ContentType
        if (originalFilename.endsWith(".pdf") || originalFilename.endsWith(".PDF")) {
            metadata.setContentType("application/pdf");
        }

        // 4. 上传文件（try-with-resources 自动关闭流，无需手动处理）
        try (InputStream inputStream = file.getInputStream()) {
            PutObjectRequest putObjectRequest = new PutObjectRequest(
                    ossConfig.getBucketName(),
                    objectKey,
                    inputStream,
                    metadata
            );
            ossClient.putObject(putObjectRequest);
            ossClient.setObjectAcl(ossConfig.getBucketName(), objectKey, CannedAccessControlList.PublicRead);

            String fileUrl = getFileUrl(objectKey);
            log.info("文件上传成功：objectKey={}, fileUrl={}, originalName={}",
                    objectKey, fileUrl, originalFilename);
            return fileUrl;
        } catch (Exception e) {
            log.error("文件上传失败：originalName={}", originalFilename, e);
            throw new IOException("文件上传失败：" + e.getMessage(), e); // 保留根异常，便于排查
        }
    }

    /**
     * 批量上传文件到OSS
     *
     * @param files    要上传的文件列表
     * @param filePath 文件路径
     * @return 文件访问URL列表
     * @throws IOException IO异常
     */
    public String[] uploadFiles(MultipartFile[] files, String filePath) throws IOException {
        // 精简校验逻辑
        if (files == null || files.length == 0) {
            throw new IllegalArgumentException("上传文件列表不能为空");
        }

        String[] fileUrls = new String[files.length];
        for (int i = 0; i < files.length; i++) {
            fileUrls[i] = uploadFile(files[i], filePath); // 复用单文件上传逻辑，消除冗余
        }
        return fileUrls;
    }

    /**
     * 通过URL上传文件到OSS
     *
     * @param fileUrl  远程文件URL
     * @param filePath 文件路径
     * @return 文件访问URL
     * @throws IOException IO异常
     */
    public String uploadFileByUrl(String fileUrl, String filePath) throws IOException {
        if (fileUrl == null || fileUrl.isEmpty()) {
            throw new IllegalArgumentException("文件URL不能为空");
        }

        String fileName = getFileNameFromUrl(fileUrl);
        String fileExtension = getFileExtension(fileName);
        String newFileName = UUID.randomUUID().toString() + fileExtension;
        String objectKey = filePath + newFileName;

        try (InputStream inputStream = new URL(fileUrl).openStream()) {
            PutObjectRequest putObjectRequest = new PutObjectRequest(
                    ossConfig.getBucketName(),
                    objectKey,
                    inputStream
            );
            ossClient.putObject(putObjectRequest);
            ossClient.setObjectAcl(ossConfig.getBucketName(), objectKey, CannedAccessControlList.PublicRead);

            String uploadUrl = getFileUrl(objectKey);
            log.info("文件上传成功（URL方式）：objectKey={}, uploadUrl={}, sourceUrl={}",
                    objectKey, uploadUrl, fileUrl);
            return uploadUrl;
        } catch (Exception e) {
            log.error("文件上传失败（URL方式）：sourceUrl={}", fileUrl, e);
            throw new IOException("文件上传失败：" + e.getMessage(), e);
        }
    }

    /**
     * 删除OSS中的文件
     *
     * @param fileUrl 文件访问URL
     */
    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            throw new IllegalArgumentException("文件URL不能为空");
        }

        String objectKey = getObjectKeyFromUrl(fileUrl);
        if (objectKey == null) {
            log.warn("无法从URL中提取对象键：fileUrl={}", fileUrl);
            return;
        }

        try {
            ossClient.deleteObject(ossConfig.getBucketName(), objectKey);
            log.info("文件删除成功：objectKey={}", objectKey);
        } catch (Exception e) {
            log.error("文件删除失败：objectKey={}", objectKey, e);
            throw new RuntimeException("文件删除失败：" + e.getMessage(), e);
        }
    }

    /**
     * 批量删除OSS中的文件
     *
     * @param fileUrls 文件访问URL列表
     */
    public void deleteFiles(String[] fileUrls) {
        if (fileUrls == null || fileUrls.length == 0) {
            log.warn("批量删除文件列表为空，无需处理");
            return;
        }

        List<String> objectKeys = new ArrayList<>();
        for (String fileUrl : fileUrls) {
            String objectKey = getObjectKeyFromUrl(fileUrl);
            if (objectKey != null) {
                objectKeys.add(objectKey);
            }
        }

        // 避免空列表删除（OSS批量删除空列表会报错）
        if (objectKeys.isEmpty()) {
            log.warn("无有效文件可删除");
            return;
        }

        try {
            DeleteObjectsRequest deleteObjectsRequest = new DeleteObjectsRequest(ossConfig.getBucketName());
            deleteObjectsRequest.setKeys(objectKeys);
            ossClient.deleteObjects(deleteObjectsRequest);
            log.info("批量删除文件成功：count={}", objectKeys.size());
        } catch (Exception e) {
            log.error("批量删除文件失败", e);
            throw new RuntimeException("批量删除文件失败：" + e.getMessage(), e);
        }
    }

    /**
     * 判断文件是否存在
     *
     * @param fileUrl 文件访问URL
     * @return true-存在，false-不存在
     */
    public boolean doesFileExist(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return false;
        }

        String objectKey = getObjectKeyFromUrl(fileUrl);
        if (objectKey == null) {
            return false;
        }

        try {
            return ossClient.doesObjectExist(ossConfig.getBucketName(), objectKey);
        } catch (Exception e) {
            log.error("判断文件是否存在失败：objectKey={}", objectKey, e);
            return false;
        }
    }

    /**
     * 获取文件信息
     *
     * @param fileUrl 文件访问URL
     * @return 文件信息
     */
    public OSSObject getFileInfo(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            throw new IllegalArgumentException("文件URL不能为空");
        }

        String objectKey = getObjectKeyFromUrl(fileUrl);
        if (objectKey == null) {
            throw new IllegalArgumentException("无法从URL中提取对象键");
        }

        try {
            return ossClient.getObject(ossConfig.getBucketName(), objectKey);
        } catch (Exception e) {
            log.error("获取文件信息失败：objectKey={}", objectKey, e);
            throw new RuntimeException("获取文件信息失败：" + e.getMessage(), e);
        }
    }

    /**
     * 生成带签名的临时URL（用于私有文件的临时访问）
     *
     * @param fileUrl     文件访问URL
     * @param expiration 过期时间（秒）
     * @return 带签名的URL
     */
    public String generatePresignedUrl(String fileUrl, int expiration) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            throw new IllegalArgumentException("文件URL不能为空");
        }
        if (expiration <= 0) {
            throw new IllegalArgumentException("过期时间必须大于0");
        }

        String objectKey = getObjectKeyFromUrl(fileUrl);
        if (objectKey == null) {
            throw new IllegalArgumentException("无法从URL中提取对象键");
        }

        try {
            Date expirationDate = new Date(System.currentTimeMillis() + expiration * 1000L);

            // 强制覆盖响应头，实现预览
            GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(
                    ossConfig.getBucketName(),
                    objectKey
            );
            request.setExpiration(expirationDate);

            // 关键：强制 inline
            ResponseHeaderOverrides headers = new ResponseHeaderOverrides();
            headers.setContentDisposition("inline");
            request.setResponseHeaders(headers);

            URL url = ossClient.generatePresignedUrl(request);
            log.info("生成预览签名URL：objectKey={}", objectKey);
            return url.toString().replace("http://", "https://");
        } catch (Exception e) {
            log.error("生成签名URL失败：objectKey={}", objectKey, e);
            throw new RuntimeException("生成签名URL失败：" + e.getMessage(), e);
        }
    }

    // ===================== 私有工具方法（精简+优化） =====================
    /**
     * 获取文件访问URL
     */
    private String getFileUrl(String objectKey) {
        // 兼容endpoint带/不带http的情况（避免URL拼接错误）
        String endpoint = ossConfig.getEndpoint().replace("http://", "").replace("https://", "");
        return "https://" + ossConfig.getBucketName() + "." + endpoint + "/" + objectKey;
    }

    /**
     * 从URL中提取对象键
     */
    private String getObjectKeyFromUrl(String fileUrl) {
        try {
            if (fileUrl.contains("?")) {
                fileUrl = fileUrl.substring(0, fileUrl.indexOf("?"));
            }
            String endpoint = ossConfig.getEndpoint().replace("http://", "").replace("https://", "");
            String prefix = "https://" + ossConfig.getBucketName() + "." + endpoint + "/";
            if (fileUrl.startsWith(prefix)) {
                return fileUrl.substring(prefix.length());
            }
            return null;
        } catch (Exception e) {
            log.error("从URL中提取对象键失败：fileUrl={}", fileUrl, e);
            return null;
        }
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf(".");
        // 修复：避免扩展名为空或只有点的情况
        if (lastDotIndex > 0 && lastDotIndex < filename.length() - 1) {
            return filename.substring(lastDotIndex);
        }
        return "";
    }

    /**
     * 从URL中提取文件名
     */
    private String getFileNameFromUrl(String fileUrl) {
        int lastSlashIndex = fileUrl.lastIndexOf("/");
        if (lastSlashIndex > -1 && lastSlashIndex < fileUrl.length() - 1) {
            return fileUrl.substring(lastSlashIndex + 1);
        }
        return UUID.randomUUID().toString(); // 兜底：避免文件名异常
    }
}