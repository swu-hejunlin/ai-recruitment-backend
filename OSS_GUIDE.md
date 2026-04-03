# 阿里云OSS文件上传使用指南

## 配置说明

### application.yml配置
```yaml
aliyun:
  oss:
    endpoint: oss-cn-chengdu.aliyuncs.com        # OSS服务Endpoint
    access-key-id: YOUR_ACCESS_KEY_ID            # 阿里云AccessKey ID
    access-key-secret: YOUR_ACCESS_KEY_SECRET    # 阿里云AccessKey Secret
    bucket-name: your-bucket-name                # OSS存储空间名称
```

### 获取阿里云OSS配置信息

1. **登录阿里云控制台**：https://oss.console.aliyun.com
2. **创建Bucket**：
   - 点击"创建Bucket"
   - 选择地域（如：成都）
   - 设置Bucket名称（如：ai-recruitment）
   - 设置读写权限为"公共读"
3. **获取AccessKey**：
   - 访问：https://ram.console.aliyun.com/manage/ak
   - 创建AccessKey
   - 记录AccessKey ID和AccessKey Secret

---

## 核心类说明

### 1. OssConfig（配置类）
- 作用：配置OSS客户端和属性
- 位置：`com.example.airecruitmentbackend.config.OssConfig`
- 自动注入：无需手动调用

### 2. OssUtil（工具类）
- 作用：提供文件上传、下载、删除等基础功能
- 位置：`com.example.airecruitmentbackend.util.OssUtil`
- 使用方式：在Service中自动注入

### 3. FileValidator（文件校验类）
- 作用：校验文件大小、类型等
- 位置：`com.example.airecruitmentbackend.util.FileValidator`
- 使用方式：在Service中自动注入

### 4. FileConstants（常量类）
- 作用：定义文件大小限制、允许的类型等常量
- 位置：`com.example.airecruitmentbackend.common.FileConstants`

---

## 使用示例

### 示例1：上传头像

```java
@Service
@RequiredArgsConstructor
public class JobSeekerService {

    private final OssUtil ossUtil;
    private final FileValidator fileValidator;

    /**
     * 上传头像
     */
    public UploadResponse uploadAvatar(MultipartFile file) throws IOException {
        // 1. 校验文件
        fileValidator.validateImage(file);

        // 2. 上传到OSS
        String fileUrl = ossUtil.uploadFile(file, FileConstants.AVATAR_PATH);

        // 3. 返回结果
        UploadResponse response = new UploadResponse();
        response.setFileUrl(fileUrl);
        response.setFileName(file.getOriginalFilename());
        response.setFileSize(file.getSize());

        return response;
    }
}
```

### 示例2：上传简历

```java
@Service
@RequiredArgsConstructor
public class ResumeService {

    private final OssUtil ossUtil;
    private final FileValidator fileValidator;

    /**
     * 上传简历文件
     */
    public UploadResponse uploadResume(MultipartFile file) throws IOException {
        // 1. 校验文件
        fileValidator.validateResume(file);

        // 2. 上传到OSS
        String fileUrl = ossUtil.uploadFile(file, FileConstants.RESUME_PATH);

        // 3. 返回结果
        UploadResponse response = new UploadResponse();
        response.setFileUrl(fileUrl);
        response.setFileName(file.getOriginalFilename());
        response.setFileSize(file.getSize());

        return response;
    }
}
```

### 示例3：上传企业Logo

```java
@Service
@RequiredArgsConstructor
public class EnterpriseService {

    private final OssUtil ossUtil;
    private final FileValidator fileValidator;

    /**
     * 上传企业Logo
     */
    public UploadResponse uploadLogo(MultipartFile file) throws IOException {
        // 1. 校验文件
        fileValidator.validateLogo(file);

        // 2. 上传到OSS
        String fileUrl = ossUtil.uploadFile(file, FileConstants.LOGO_PATH);

        // 3. 返回结果
        UploadResponse response = new UploadResponse();
        response.setFileUrl(fileUrl);
        response.setFileName(file.getOriginalFilename());
        response.setFileSize(file.getSize());

        return response;
    }
}
```

### 示例4：删除文件

```java
@Service
@RequiredArgsConstructor
public class FileService {

    private final OssUtil ossUtil;

    /**
     * 删除文件
     */
    public void deleteFile(String fileUrl) {
        ossUtil.deleteFile(fileUrl);
    }
}
```

### 示例5：批量删除文件

```java
@Service
@RequiredArgsConstructor
public class FileService {

    private final OssUtil ossUtil;

    /**
     * 批量删除文件
     */
    public void deleteFiles(String[] fileUrls) {
        ossUtil.deleteFiles(fileUrls);
    }
}
```

### 示例6：检查文件是否存在

```java
@Service
@RequiredArgsConstructor
public class FileService {

    private final OssUtil ossUtil;

    /**
     * 检查文件是否存在
     */
    public boolean checkFileExists(String fileUrl) {
        return ossUtil.doesFileExist(fileUrl);
    }
}
```

---

## 文件路径规范

### 存储路径
- 头像：`avatar/`
- 简历：`resume/`
- 企业Logo：`logo/`
- 其他：`common/`

### 文件命名规则
- 使用UUID作为文件名，避免冲突
- 格式：`{UUID}.{扩展名}`
- 示例：`550e8400-e29b-41d4-a716-446655440000.jpg`

---

## 文件大小限制

| 文件类型 | 最大大小 | 说明 |
|---------|---------|------|
| 头像 | 2MB | 图片格式：jpg、png、gif、webp |
| 简历 | 10MB | 文档格式：pdf、doc、docx |
| 企业Logo | 5MB | 图片格式：jpg、png、gif、webp |

---

## 允许的文件类型

### 图片格式
- JPEG: `image/jpeg`, `image/jpg`
- PNG: `image/png`
- GIF: `image/gif`
- WebP: `image/webp`

### 简历格式
- PDF: `application/pdf`
- Word: `application/msword`, `application/vnd.openxmlformats-officedocument.wordprocessingml.document`

---

## Controller层实现示例

### 通用文件上传Controller

```java
@RestController
@RequestMapping("/api/common")
@RequiredArgsConstructor
public class FileController {

    private final OssUtil ossUtil;
    private final FileValidator fileValidator;

    /**
     * 上传头像
     */
    @PostMapping("/upload/avatar")
    public Result<UploadResponse> uploadAvatar(@RequestParam("file") MultipartFile file) {
        try {
            fileValidator.validateImage(file);
            String fileUrl = ossUtil.uploadFile(file, FileConstants.AVATAR_PATH);

            UploadResponse response = new UploadResponse();
            response.setFileUrl(fileUrl);
            response.setFileName(file.getOriginalFilename());
            response.setFileSize(file.getSize());

            return Result.success("头像上传成功", response);
        } catch (Exception e) {
            return Result.error("头像上传失败：" + e.getMessage());
        }
    }

    /**
     * 上传简历
     */
    @PostMapping("/upload/resume")
    public Result<UploadResponse> uploadResume(@RequestParam("file") MultipartFile file) {
        try {
            fileValidator.validateResume(file);
            String fileUrl = ossUtil.uploadFile(file, FileConstants.RESUME_PATH);

            UploadResponse response = new UploadResponse();
            response.setFileUrl(fileUrl);
            response.setFileName(file.getOriginalFilename());
            response.setFileSize(file.getSize());

            return Result.success("简历上传成功", response);
        } catch (Exception e) {
            return Result.error("简历上传失败：" + e.getMessage());
        }
    }

    /**
     * 上传Logo
     */
    @PostMapping("/upload/logo")
    public Result<UploadResponse> uploadLogo(@RequestParam("file") MultipartFile file) {
        try {
            fileValidator.validateLogo(file);
            String fileUrl = ossUtil.uploadFile(file, FileConstants.LOGO_PATH);

            UploadResponse response = new UploadResponse();
            response.setFileUrl(fileUrl);
            response.setFileName(file.getOriginalFilename());
            response.setFileSize(file.getSize());

            return Result.success("Logo上传成功", response);
        } catch (Exception e) {
            return Result.error("Logo上传失败：" + e.getMessage());
        }
    }
}
```

---

## 前端调用示例

### 使用Axios上传文件

```javascript
// 上传头像
async function uploadAvatar(file) {
  const formData = new FormData();
  formData.append('file', file);

  const response = await axios.post('/api/common/upload/avatar', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  });

  return response.data.data;  // 返回 { fileUrl, fileName, fileSize }
}

// 上传简历
async function uploadResume(file) {
  const formData = new FormData();
  formData.append('file', file);

  const response = await axios.post('/api/common/upload/resume', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  });

  return response.data.data;
}

// 上传Logo
async function uploadLogo(file) {
  const formData = new FormData();
  formData.append('file', file);

  const response = await axios.post('/api/common/upload/logo', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  });

  return response.data.data;
}
```

---

## 注意事项

1. **安全性**
   - AccessKey Secret不要提交到代码仓库
   - 建议使用环境变量或配置中心管理
   - 生产环境建议使用RAM子账号的AccessKey

2. **性能优化**
   - 大文件上传建议使用分片上传
   - 可以考虑使用CDN加速访问

3. **文件管理**
   - 定期清理无用文件
   - 可以设置生命周期规则自动删除过期文件

4. **成本控制**
   - 开启访问日志，监控流量和请求次数
   - 设置合理的存储类型（标准、低频、归档）

5. **权限控制**
   - Bucket权限设置为"公共读"
   - 敏感文件可以设置为"私有"，使用签名URL临时访问

---

## 故障排查

### 问题1：上传失败，提示权限不足
**原因**：AccessKey没有OSS操作权限
**解决**：在RAM控制台为AccessKey授权AliyunOSSFullAccess权限

### 问题2：文件上传成功但无法访问
**原因**：Bucket权限设置为私有
**解决**：在OSS控制台将Bucket权限设置为"公共读"

### 问题3：文件大小超过限制
**原因**：单个文件大小超过5GB限制
**解决**：使用分片上传功能

### 问题4：URL无法访问
**原因**：Endpoint配置错误或Bucket不存在
**解决**：检查application.yml中的配置是否正确
