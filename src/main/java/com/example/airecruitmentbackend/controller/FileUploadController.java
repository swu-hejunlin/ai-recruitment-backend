package com.example.airecruitmentbackend.controller;

import com.example.airecruitmentbackend.common.Result;
import com.example.airecruitmentbackend.dto.UploadResponse;
import com.example.airecruitmentbackend.exception.FileUploadException;
import com.example.airecruitmentbackend.service.UserService;
import com.example.airecruitmentbackend.util.FileConstants;
import com.example.airecruitmentbackend.util.FileValidator;
import com.example.airecruitmentbackend.util.OssUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 文件上传控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/file")
@RequiredArgsConstructor
public class FileUploadController extends BaseController {

    private final OssUtil ossUtil;
    private final UserService userService;

    /**
     * 上传文件
     */
    @PostMapping("/upload")
    public Result<UploadResponse> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("fileType") String fileType) {
        Long userId = getCurrentUserId();
        log.info("收到文件上传请求，userId：{}，fileType：{}，fileName：{}",
                userId, fileType, file.getOriginalFilename());

        try {
            Integer role = getCurrentUserRole();
            String filePath = FileConstants.getFilePathByFileType(fileType, role);

            if (filePath == null) {
                return Result.error("不支持的文件类型");
            }

            FileValidator.validateFile(file, fileType);

            String fileUrl = ossUtil.uploadFile(file, filePath);

            UploadResponse response = new UploadResponse();
            response.setFileUrl(fileUrl);
            response.setFileName(file.getOriginalFilename());
            response.setFileSize(file.getSize());

            log.info("文件上传成功，userId：{}，fileType：{}，fileUrl：{}", userId, fileType, fileUrl);
            return Result.success("文件上传成功", response);

        } catch (FileUploadException e) {
            log.error("文件上传失败，userId：{}，fileType：{}，错误：{}", userId, fileType, e.getMessage());
            return Result.error(e.getMessage());
        } catch (IOException e) {
            log.error("文件上传IO异常，userId：{}，fileType：{}，错误：{}", userId, fileType, e.getMessage());
            return Result.error("文件上传失败：" + e.getMessage());
        } catch (Exception e) {
            log.error("文件上传未知异常，userId：{}，fileType：{}，错误：{}", userId, fileType, e.getMessage());
            return Result.error("文件上传失败，请稍后重试");
        }
    }

    /**
     * 批量上传文件
     */
    @PostMapping("/upload-batch")
    public Result<UploadResponse[]> uploadFiles(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam("fileType") String fileType) {
        Long userId = getCurrentUserId();
        log.info("收到批量文件上传请求，userId：{}，fileType：{}，文件数量：{}",
                userId, fileType, files.length);

        try {
            Integer role = getCurrentUserRole();
            String filePath = FileConstants.getFilePathByFileType(fileType, role);

            if (filePath == null) {
                return Result.error("不支持的文件类型");
            }

            String[] fileUrls = ossUtil.uploadFiles(files, filePath);

            UploadResponse[] responses = new UploadResponse[files.length];
            for (int i = 0; i < files.length; i++) {
                responses[i] = new UploadResponse();
                responses[i].setFileUrl(fileUrls[i]);
                responses[i].setFileName(files[i].getOriginalFilename());
                responses[i].setFileSize(files[i].getSize());
            }

            log.info("批量文件上传成功，userId：{}，fileType：{}，成功数量：{}",
                    userId, fileType, fileUrls.length);
            return Result.success("批量文件上传成功", responses);

        } catch (FileUploadException e) {
            log.error("批量文件上传失败，userId：{}，fileType：{}，错误：{}", userId, fileType, e.getMessage());
            return Result.error(e.getMessage());
        } catch (IOException e) {
            log.error("批量文件上传IO异常，userId：{}，fileType：{}，错误：{}", userId, fileType, e.getMessage());
            return Result.error("批量文件上传失败：" + e.getMessage());
        } catch (Exception e) {
            log.error("批量文件上传未知异常，userId：{}，fileType：{}，错误：{}", userId, fileType, e.getMessage());
            return Result.error("批量文件上传失败，请稍后重试");
        }
    }

    /**
     * 删除文件
     */
    @DeleteMapping("/delete")
    public Result<Void> deleteFile(@RequestParam("fileUrl") String fileUrl) {
        log.info("收到删除文件请求，fileUrl：{}", fileUrl);

        try {
            ossUtil.deleteFile(fileUrl);
            log.info("文件删除成功，fileUrl：{}", fileUrl);
            return Result.success("文件删除成功", null);
        } catch (Exception e) {
            log.error("文件删除失败，fileUrl：{}，错误：{}", fileUrl, e.getMessage());
            return Result.error("文件删除失败：" + e.getMessage());
        }
    }
}
