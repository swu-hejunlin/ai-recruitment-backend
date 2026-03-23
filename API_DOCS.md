# 智能招聘平台 - 接口文档

## 接口说明
本文档描述智能招聘平台的完整API接口，包含用户登录、身份切换、求职者信息管理、企业信息管理、文件上传等功能。

## 业务规则
- **手机号唯一性**：同一个手机号只能有一个账号
- **身份切换**：如果已注册用户以不同角色登录，系统会提示身份冲突，需要用户确认切换身份
- **自动注册**：首次使用手机号登录时，系统会自动创建用户

## 基础信息
- **Base URL**: `http://localhost:8080`
- **接口返回格式**: JSON
- **字符编码**: UTF-8
- **认证方式**: JWT Bearer Token

## 统一返回格式

### 成功响应
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {},
  "timestamp": 1710931200000
}
```

### 失败响应
```json
{
  "code": 400,
  "message": "错误信息描述",
  "data": null,
  "timestamp": 1710931200000
}
```

**字段说明**：
- `code`: 状态码，200-成功，其他-失败
- `message`: 返回消息描述
- `data`: 返回数据
- `timestamp`: 时间戳（毫秒）

---

## 一、用户模块

### 1.1 发送验证码接口

- **接口地址**: `POST /api/user/send-code`
- **接口描述**: 发送手机验证码（用于登录或身份切换）

#### 请求参数

**Headers**：
```
Content-Type: application/json
```

**Body（JSON）**：
```json
{
  "phone": "13800138000",
  "role": 1
}
```

**参数说明**：

| 参数名 | 类型 | 必填 | 说明 | 校验规则 |
|--------|------|------|------|----------|
| phone | String | 是 | 手机号 | 1开头，11位数字 |
| role | Integer | 否 | 用户角色 | 1-求职者，2-企业HR |

#### 返回参数

**成功响应**：
```json
{
  "code": 200,
  "message": "验证码发送成功",
  "data": null,
  "timestamp": 1710931200000
}
```

**业务说明**：
- 验证码为6位随机数字
- 验证码有效期：5分钟（300秒）
- 验证码存储在Redis中，key格式为`login:code:{phone}`
- 当前为模拟发送，验证码会打印在后端控制台

---

### 1.2 验证码登录接口

- **接口地址**: `POST /api/user/login`
- **接口描述**: 使用手机号+验证码进行登录，返回JWT令牌

#### 请求参数

**Headers**：
```
Content-Type: application/json
```

**Body（JSON）**：
```json
{
  "phone": "13800138000",
  "code": "123456",
  "role": 1
}
```

**参数说明**：

| 参数名 | 类型 | 必填 | 说明 | 校验规则 |
|--------|------|------|------|----------|
| phone | String | 是 | 手机号 | 1开头，11位数字 |
| code | String | 是 | 验证码 | 6位数字 |
| role | Integer | 是 | 用户角色 | 1-求职者，2-企业HR |

#### 返回参数

**成功响应（正常登录）**：
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzUxMiJ9...",
    "userId": 1,
    "role": 1,
    "needSwitchRole": false,
    "currentRole": null
  },
  "timestamp": 1710931200000
}
```

**需要身份切换响应**：
```json
{
  "code": 200,
  "message": "检测到身份冲突，请确认是否切换身份",
  "data": {
    "token": null,
    "userId": 1,
    "role": 1,
    "needSwitchRole": true,
    "currentRole": 1
  },
  "timestamp": 1710931200000
}
```

**返回数据字段说明**：

| 字段名 | 类型 | 说明 |
|--------|------|------|
| token | String | JWT令牌（有效期2小时） |
| userId | Long | 用户ID |
| role | Integer | 用户角色：1-求职者，2-企业HR |
| needSwitchRole | Boolean | 是否需要身份切换 |
| currentRole | Integer | 当前用户的现有角色 |

---

### 1.3 身份切换接口

- **接口地址**: `POST /api/user/switch-role`
- **接口描述**: 用户确认切换身份后，修改用户角色并返回新的JWT令牌

#### 请求参数

**Headers**：
```
Content-Type: application/json
```

**Body（JSON）**：
```json
{
  "phone": "13800138000",
  "code": "123456",
  "role": 2
}
```

**参数说明**：

| 参数名 | 类型 | 必填 | 说明 | 校验规则 |
|--------|------|------|------|----------|
| phone | String | 是 | 手机号 | 1开头，11位数字 |
| code | String | 是 | 验证码 | 6位数字（需要重新获取） |
| role | Integer | 是 | 目标角色 | 1-求职者，2-企业HR |

#### 返回参数

**成功响应**：
```json
{
  "code": 200,
  "message": "身份切换成功",
  "data": {
    "token": "eyJhbGciOiJIUzUxMiJ9...",
    "userId": 1,
    "role": 2,
    "needSwitchRole": false,
    "currentRole": null
  },
  "timestamp": 1710931200000
}
```

---

## 二、求职者模块

### 2.1 获取求职者信息

- **接口地址**: `GET /api/job-seeker/info`
- **接口描述**: 获取当前登录用户的求职者信息
- **认证**: 需要

#### 请求参数

**Headers**：
```
Authorization: Bearer {token}
```

#### 返回参数

**成功响应**：
```json
{
  "code": 200,
  "message": "获取求职者信息成功",
  "data": {
    "id": 1,
    "userId": 1,
    "name": "张三",
    "gender": 1,
    "avatar": "https://xxx.oss.com/avatar/xxx.jpg",
    "email": "zhangsan@example.com",
    "age": 28,
    "educationLevel": 3,
    "graduateSchool": "北京大学",
    "major": "计算机科学与技术",
    "workYears": 5,
    "currentSalary": 30.00,
    "expectedSalary": 40.00,
    "currentStatus": 1,
    "city": "北京",
    "address": "海淀区中关村",
    "introduction": "热爱技术，善于学习",
    "skills": "[\"Java\",\"Spring\",\"MySQL\"]",
    "resumeUrl": "https://xxx.oss.com/resume/xxx.pdf",
    "createTime": "2024-01-01T10:00:00",
    "updateTime": "2024-01-01T10:00:00"
  },
  "timestamp": 1710931200000
}
```

**字段说明**：

| 字段名 | 类型 | 说明 |
|--------|------|------|
| gender | Integer | 性别：0-未知，1-男，2-女 |
| educationLevel | Integer | 学历：1-高中及以下，2-大专，3-本科，4-硕士，5-博士 |
| currentStatus | Integer | 当前状态：1-在职，2-离职，3-在读学生 |
| skills | String | JSON数组格式，如：["Java","Spring"] |
| resumeUrl | String | 简历附件URL |

---

### 2.2 更新求职者信息

- **接口地址**: `PUT /api/job-seeker/update`
- **接口描述**: 更新求职者的个人信息
- **认证**: 需要

#### 请求参数

**Headers**：
```
Content-Type: application/json
Authorization: Bearer {token}
```

**Body（JSON）**：
```json
{
  "id": 1,
  "name": "张三",
  "gender": 1,
  "email": "zhangsan@example.com",
  "age": 28,
  "educationLevel": 3,
  "graduateSchool": "北京大学",
  "major": "计算机科学与技术",
  "workYears": 5,
  "currentSalary": 30.00,
  "expectedSalary": 40.00,
  "currentStatus": 1,
  "city": "北京",
  "address": "海淀区中关村",
  "introduction": "热爱技术，善于学习",
  "skills": "[\"Java\",\"Spring\",\"MySQL\"]"
}
```

**参数说明**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 求职者ID |
| name | String | 是 | 姓名 |
| gender | Integer | 否 | 性别：0-未知，1-男，2-女 |
| email | String | 否 | 邮箱地址 |
| age | Integer | 否 | 年龄 |
| educationLevel | Integer | 否 | 学历层次 |
| graduateSchool | String | 否 | 毕业院校 |
| major | String | 否 | 专业 |
| workYears | Integer | 否 | 工作年限 |
| currentSalary | BigDecimal | 否 | 当前薪资（万元/年） |
| expectedSalary | BigDecimal | 否 | 期望薪资（万元/年） |
| currentStatus | Integer | 否 | 当前状态 |
| city | String | 否 | 所在城市 |
| address | String | 否 | 详细地址 |
| introduction | String | 否 | 个人简介 |
| skills | String | 否 | 技能标签（JSON数组格式） |

#### 返回参数

**成功响应**：
```json
{
  "code": 200,
  "message": "更新求职者信息成功",
  "data": null,
  "timestamp": 1710931200000
}
```

---

### 2.3 上传头像

- **接口地址**: `POST /api/job-seeker/avatar`
- **接口描述**: 上传求职者的头像
- **认证**: 需要

#### 请求参数

**Headers**：
```
Authorization: Bearer {token}
```

**Body（form-data）**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| avatarUrl | String | 是 | 头像URL（先通过文件上传接口获取URL） |

#### 返回参数

**成功响应**：
```json
{
  "code": 200,
  "message": "上传头像成功",
  "data": null,
  "timestamp": 1710931200000
}
```

---

### 2.4 上传简历

- **接口地址**: `POST /api/job-seeker/resume`
- **接口描述**: 上传求职者的简历附件
- **认证**: 需要

#### 请求参数

**Headers**：
```
Authorization: Bearer {token}
```

**Body（form-data）**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| resumeUrl | String | 是 | 简历URL（先通过文件上传接口获取URL） |

#### 返回参数

**成功响应**：
```json
{
  "code": 200,
  "message": "上传简历成功",
  "data": null,
  "timestamp": 1710931200000
}
```

---

## 三、企业模块

### 3.1 获取企业信息

- **接口地址**: `GET /api/company/info`
- **接口描述**: 获取当前登录用户的企业信息
- **认证**: 需要

#### 请求参数

**Headers**：
```
Authorization: Bearer {token}
```

#### 返回参数

**成功响应**：
```json
{
  "code": 200,
  "message": "获取企业信息成功",
  "data": {
    "id": 1,
    "userId": 1,
    "companyName": "某某科技有限公司",
    "logo": "https://xxx.oss.com/logo/xxx.png",
    "legalPerson": "李四",
    "industry": "互联网",
    "scale": 3,
    "financingStage": 4,
    "city": "北京",
    "address": "朝阳区建国路",
    "email": "hr@example.com",
    "phone": "010-12345678",
    "website": "https://www.example.com",
    "description": "专注于人工智能领域的创新公司",
    "welfare": "[\"六险一金\",\"带薪年假\",\"弹性工作\",\"年度旅游\"]",
    "businessLicense": "https://xxx.oss.com/license/xxx.jpg",
    "createTime": "2024-01-01T10:00:00",
    "updateTime": "2024-01-01T10:00:00"
  },
  "timestamp": 1710931200000
}
```

**字段说明**：

| 字段名 | 类型 | 说明 |
|--------|------|------|
| scale | Integer | 企业规模：1-0-20人，2-20-99人，3-100-499人，4-500-999人，5-1000-9999人，6-10000人以上 |
| financingStage | Integer | 融资阶段：1-未融资，2-天使轮，3-A轮，4-B轮，5-C轮，6-D轮及以上，7-已上市，8-不需要融资 |
| welfare | String | 福利待遇（JSON数组格式） |
| businessLicense | String | 营业执照URL |

---

### 3.2 更新企业信息

- **接口地址**: `PUT /api/company/update`
- **接口描述**: 更新企业信息
- **认证**: 需要

#### 请求参数

**Headers**：
```
Content-Type: application/json
Authorization: Bearer {token}
```

**Body（JSON）**：
```json
{
  "id": 1,
  "companyName": "某某科技有限公司",
  "legalPerson": "李四",
  "industry": "互联网",
  "scale": 3,
  "financingStage": 4,
  "city": "北京",
  "address": "朝阳区建国路",
  "email": "hr@example.com",
  "phone": "010-12345678",
  "website": "https://www.example.com",
  "description": "专注于人工智能领域的创新公司",
  "welfare": "[\"六险一金\",\"带薪年假\",\"弹性工作\",\"年度旅游\"]"
}
```

**参数说明**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 企业ID |
| companyName | String | 是 | 企业名称 |
| legalPerson | String | 否 | 法人代表 |
| industry | String | 否 | 所属行业 |
| scale | Integer | 否 | 企业规模 |
| financingStage | Integer | 否 | 融资阶段 |
| city | String | 否 | 所在城市 |
| address | String | 否 | 详细地址 |
| email | String | 否 | 企业邮箱 |
| phone | String | 否 | 企业联系电话 |
| website | String | 否 | 企业官网 |
| description | String | 否 | 企业简介 |
| welfare | String | 否 | 福利待遇（JSON数组格式） |

#### 返回参数

**成功响应**：
```json
{
  "code": 200,
  "message": "更新企业信息成功",
  "data": null,
  "timestamp": 1710931200000
}
```

---

### 3.3 上传企业logo

- **接口地址**: `POST /api/company/logo`
- **接口描述**: 上传企业logo
- **认证**: 需要

#### 请求参数

**Headers**：
```
Authorization: Bearer {token}
```

**Body（form-data）**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| logoUrl | String | 是 | logo URL（先通过文件上传接口获取URL） |

#### 返回参数

**成功响应**：
```json
{
  "code": 200,
  "message": "上传logo成功",
  "data": null,
  "timestamp": 1710931200000
}
```

---

### 3.4 上传营业执照

- **接口地址**: `POST /api/company/license`
- **接口描述**: 上传企业营业执照
- **认证**: 需要

#### 请求参数

**Headers**：
```
Authorization: Bearer {token}
```

**Body（form-data）**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| licenseUrl | String | 是 | 营业执照URL（先通过文件上传接口获取URL） |

#### 返回参数

**成功响应**：
```json
{
  "code": 200,
  "message": "上传营业执照成功",
  "data": null,
  "timestamp": 1710931200000
}
```

---

## 四、文件上传模块

### 4.1 上传文件

- **接口地址**: `POST /api/file/upload`
- **接口描述**: 上传文件到阿里云OSS
- **认证**: 需要

#### 请求参数

**Headers**：
```
Authorization: Bearer {token}
```

**Body（form-data）**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| file | File | 是 | 上传的文件 |
| fileType | String | 是 | 文件类型：avatar/resume/logo/license |

**文件类型说明**：
- `avatar`: 求职者头像
- `resume`: 求职者简历
- `logo`: 企业logo
- `license`: 企业营业执照

#### 返回参数

**成功响应**：
```json
{
  "code": 200,
  "message": "文件上传成功",
  "data": {
    "fileUrl": "https://xxx.oss.com/avatar/1/xxx.jpg",
    "fileName": "avatar.jpg",
    "fileSize": 102400
  },
  "timestamp": 1710931200000
}
```

**返回数据字段说明**：

| 字段名 | 类型 | 说明 |
|--------|------|------|
| fileUrl | String | 文件的OSS访问URL |
| fileName | String | 原始文件名 |
| fileSize | Long | 文件大小（字节） |

---

### 4.2 批量上传文件

- **接口地址**: `POST /api/file/upload-batch`
- **接口描述**: 批量上传文件到阿里云OSS
- **认证**: 需要

#### 请求参数

**Headers**：
```
Authorization: Bearer {token}
```

**Body（form-data）**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| files | File[] | 是 | 上传的文件列表 |
| fileType | String | 是 | 文件类型：avatar/resume/logo/license |

#### 返回参数

**成功响应**：
```json
{
  "code": 200,
  "message": "批量文件上传成功",
  "data": [
    {
      "fileUrl": "https://xxx.oss.com/xxx1.jpg",
      "fileName": "file1.jpg",
      "fileSize": 102400
    },
    {
      "fileUrl": "https://xxx.oss.com/xxx2.jpg",
      "fileName": "file2.jpg",
      "fileSize": 204800
    }
  ],
  "timestamp": 1710931200000
}
```

---

### 4.3 删除文件

- **接口地址**: `DELETE /api/file/delete`
- **接口描述**: 删除OSS上的文件
- **认证**: 不需要

#### 请求参数

**Query参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| fileUrl | String | 是 | 要删除的文件URL |

#### 返回参数

**成功响应**：
```json
{
  "code": 200,
  "message": "文件删除成功",
  "data": null,
  "timestamp": 1710931200000
}
```

---

## 五、状态码说明

### HTTP状态码

| 状态码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 请求参数错误 / 业务逻辑错误 |
| 401 | 未授权（JWT令牌无效或过期） |
| 403 | 无权访问 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

### 业务错误码（code=400时message字段）

| 错误消息 | 说明 |
|----------|------|
| 手机号格式不正确 | 手机号不是以1开头的11位数字 |
| 角色参数错误 | role值不是1或2 |
| 验证码已过期或不存在 | 验证码超过5分钟有效期或未获取验证码 |
| 验证码错误 | 验证码输入错误 |
| 用户不存在 | 该手机号未注册 |
| 当前角色与目标角色相同，无需切换 | 尝试切换到相同的角色 |
| 企业信息不存在 | 调用企业接口前需先完善企业信息 |
| 求职者信息不存在 | 调用求职者接口前需先完善个人信息 |
| 无权修改其他用户的信息 | 尝试修改非自己的信息 |
| 不支持的文件类型 | fileType参数不在允许范围内（avatar/resume/logo/license） |
| 文件大小超过限制 | 超过允许的最大文件大小 |
| 文件格式不支持 | 文件MIME类型不在允许范围内 |
| 文件上传失败，请稍后重试 | 服务器处理文件时发生错误 |

---

## 六、接口调用示例

### 完整业务流程示例

```bash
# 1. 发送验证码
curl -X POST http://localhost:8080/api/user/send-code \
  -H "Content-Type: application/json" \
  -d '{"phone": "13800138000", "role": 1}'

# 2. 登录（获取token）
curl -X POST http://localhost:8080/api/user/login \
  -H "Content-Type: application/json" \
  -d '{"phone": "13800138000", "code": "123456", "role": 1}'

# 3. 上传头像（获取fileUrl）
curl -X POST http://localhost:8080/api/file/upload \
  -H "Authorization: Bearer {token}" \
  -F "file=@/path/to/avatar.jpg" \
  -F "fileType=avatar"

# 4. 更新求职者信息（包含头像URL）
curl -X PUT http://localhost:8080/api/job-seeker/update \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {token}" \
  -d '{
    "id": 1,
    "name": "张三",
    "avatar": "https://xxx.oss.com/avatar/xxx.jpg"
  }'
```

---

## 七、注意事项

1. **验证码有效期**：验证码有效期为5分钟，超过时间需要重新获取
2. **JWT令牌**：除登录相关接口外，其他接口都需要在请求头中携带JWT令牌
3. **文件上传**：上传文件前需要先调用文件上传接口获取URL，然后将URL传给对应的业务接口
4. **角色权限**：求职者只能访问 `/api/job-seeker/*` 和 `/api/file/*` 接口，企业HR只能访问 `/api/company/*` 和 `/api/file/*` 接口
5. **自动创建**：首次登录时，系统会自动创建用户和相关业务信息（如job_seeker或company记录）
