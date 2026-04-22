# AI智能招聘平台 - 接口文档

## 接口说明
本文档描述AI智能招聘平台的完整API接口，包含用户登录、身份切换、求职者信息管理、企业信息管理、文件上传、智能简历分析、岗位画像、人才画像、岗位推荐等功能。

## 业务规则
- **手机号唯一性**：同一个手机号只能有一个账号
- **身份切换**：如果已注册用户以不同角色登录，系统会提示身份冲突，需要用户确认切换身份
- **自动注册**：首次使用手机号登录时，系统会自动创建用户
- **AI画像**：系统提供岗位画像和人才画像功能，基于AI技术自动提取关键信息
- **智能推荐**：系统根据人才画像和岗位画像自动计算匹配度，推荐合适岗位

## 基础信息
- **Base URL**: `http://localhost:8080`
- **接口返回格式**: JSON
- **字符编码**: UTF-8
- **认证方式**: JWT Bearer Token

## 认证机制

### 全局拦截器
后端使用全局 JWT 拦截器进行认证校验，分为**公开接口**和**需认证接口**。

### 角色说明
| role | 角色名称 | 说明 |
|------|---------|------|
| 1 | 求职者 | 可投递简历、管理个人信息、查看投递记录、使用AI画像和推荐功能 |
| 2 | 企业HR | 可发布职位、查看投递、管理企业信息 |
| 3 | 管理员 | 预留 |

> **安全说明**：系统在后端对关键接口实施了角色校验和数据归属权校验，确保用户只能操作自己的数据。

---

## 接口认证要求汇总

#### 一、公开接口（无需登录）

| 接口地址 | 说明 |
|----------|------|
| `POST /api/user/send-code` | 发送验证码 |
| `POST /api/user/login` | 验证码登录 |
| `GET /api/position/list` | 职位列表（搜索筛选） |
| `GET /api/position/detail?id=1` | 职位详情 |
| `GET /api/position/latest` | 最新职位（首页推荐） |
| `GET /api/position/hot` | 热门职位（首页推荐） |
| `GET /api/position/company-info?positionId=1` | 职位对应的公司信息 |
| `GET /api/position/detail-with-company?id=1` | 职位详情（含公司信息） |
| `GET /api/company/{id}` | 根据公司ID查看公司详情 |

#### 二、需要登录认证的接口

| 角色 | 接口地址 | 说明 |
|------|----------|------|
| **求职者** | `GET /api/job-seeker/info` | 获取自己的完整简历信息 |
| **求职者** | `POST /api/job-seeker/info` | 创建或更新求职者信息 |
| **求职者** | `PUT /api/job-seeker/info` | 更新求职者基本信息 |
| **求职者** | `POST /api/job-seeker/resume` | 上传简历 |
| **求职者** | `GET /api/job-seeker/resume` | 获取简历 |
| **投递** | `POST /api/application/apply` | 投递简历（仅求职者，需校验角色） |
| **投递** | `GET /api/application/seeker/list` | 查看自己的投递记录列表 |
| **投递** | `DELETE /api/application/{id}` | 取消投递 |
| **投递** | `GET /api/application/{id}` | 查看投递详情 |
| **Boss** | `GET /api/application/company/list` | Boss查看收到的投递列表 |
| **Boss** | `PUT /api/application/{id}/status` | Boss更新投递状态 |
| **Boss** | `GET /api/application/job-seeker/resume` | Boss查看完整在线简历（仅企业HR，已校验归属权） |
| **Boss** | `PUT /api/application/resume-viewed` | Boss标记简历为已查看 |
| **智能分析** | `POST /api/resume/upload-and-analyze` | 上传并分析简历（需登录） |
| **智能分析** | `POST /api/resume/analyze` | 分析已有简历（需登录） |
| **智能填充** | `POST /api/resume/smart-fill` | 智能填充简历信息（需登录） |
| **岗位画像** | `POST /api/job-profile/generate/{positionId}` | 生成岗位画像（需登录） |
| **岗位画像** | `GET /api/job-profile/{positionId}` | 获取岗位画像（需登录） |
| **岗位画像** | `PUT /api/job-profile/update/{positionId}` | 更新岗位画像（需登录） |
| **岗位画像** | `DELETE /api/job-profile/{positionId}` | 删除岗位画像（需登录） |
| **人才画像** | `POST /api/talent-profile/generate` | 生成人才画像（需登录） |
| **人才画像** | `GET /api/talent-profile` | 获取人才画像（需登录） |
| **人才画像** | `PUT /api/talent-profile/update` | 更新人才画像（需登录） |
| **人才画像** | `DELETE /api/talent-profile` | 删除人才画像（需登录） |
| **岗位推荐** | `GET /api/job-recommend` | 获取岗位推荐列表（需登录） |
| **岗位推荐** | `GET /api/job-recommend/match/{jobId}` | 获取匹配度详情（需登录） |
| **岗位推荐** | `POST /api/job-recommend/batch-generate` | 批量生成匹配记录（需登录） |
| **岗位推荐** | `PUT /api/job-recommend/viewed/{recordId}` | 标记为已查看（需登录） |
| **数据统计** | `GET /api/statistics/seeker` | 获取求职者端统计数据（需登录） |
| **数据统计** | `GET /api/statistics/boss` | 获取HR端统计数据（需登录） |

---

## 一、用户登录

### 1.1 发送验证码

- **接口描述**: 向指定手机号发送验证码
- **请求方式**: POST
- **请求路径**: `/api/user/send-code`
- **认证要求**: 公开接口

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| phone | String | 是 | 手机号 |

**请求示例**:
```json
{
  "phone": "13800138000"
}
```

**成功响应**:
```json
{
  "code": 1000,
  "message": "发送成功",
  "data": null
}
```

### 1.2 验证码登录

- **接口描述**: 使用手机号和验证码登录
- **请求方式**: POST
- **请求路径**: `/api/user/login`
- **认证要求**: 公开接口

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| phone | String | 是 | 手机号 |
| code | String | 是 | 验证码 |

**请求示例**:
```json
{
  "phone": "13800138000",
  "code": "123456"
}
```

**成功响应**:
```json
{
  "code": 1000,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIyMjIzMjMxMjMxMjMxMjMxMjMifQ...",
    "userId": 2,
    "role": 1,
    "roleName": "求职者",
    "username": "张三",
    "phone": "13800138000"
  }
}
```

---

## 二、求职者信息管理

### 2.1 获取求职者信息

- **接口描述**: 获取当前求职者的完整信息
- **请求方式**: GET
- **请求路径**: `/api/job-seeker/info`
- **认证要求**: 需要登录认证

**成功响应**:
```json
{
  "code": 1000,
  "message": "获取成功",
  "data": {
    "id": 1,
    "userId": 2,
    "name": "张三",
    "gender": 1,
    "age": 28,
    "phone": "13800138000",
    "email": "zhangsan@example.com",
    "city": "北京市",
    "address": "朝阳区",
    "workYears": 5,
    "currentSalary": 20.00,
    "expectedSalary": 30.00,
    "currentStatus": 1,
    "introduction": "具有多年互联网开发经验...",
    "education": 3,
    "avatar": "https://example.com/avatar.jpg"
  }
}
```

### 2.2 创建求职者信息

- **接口描述**: 创建当前用户的求职者信息
- **请求方式**: POST
- **请求路径**: `/api/job-seeker/info`
- **认证要求**: 需要登录认证

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| name | String | 是 | 姓名 |
| gender | Integer | 否 | 性别：1-男，2-女 |
| age | Integer | 否 | 年龄 |
| phone | String | 否 | 手机号 |
| email | String | 否 | 邮箱 |
| city | String | 否 | 城市 |
| address | String | 否 | 详细地址 |
| education | Integer | 否 | 学历：1-高中及以下，2-大专，3-本科，4-硕士，5-博士 |
| workYears | Integer | 否 | 工作年限 |
| currentStatus | Integer | 否 | 当前状态：1-在职，2-离职，3-在读学生 |
| currentSalary | BigDecimal | 否 | 当前薪资（K/月） |
| expectedSalary | BigDecimal | 否 | 期望薪资（K/月） |
| introduction | String | 否 | 个人简介 |

**成功响应**:
```json
{
  "code": 1000,
  "message": "创建成功",
  "data": {
    "id": 1,
    "userId": 2,
    "name": "张三",
    ...
  }
}
```

### 2.3 更新求职者信息

- **接口描述**: 更新当前求职者的信息
- **请求方式**: PUT
- **请求路径**: `/api/job-seeker/info`
- **认证要求**: 需要登录认证

**请求参数**: 同2.2创建求职者信息

### 2.4 上传简历

- **接口描述**: 上传求职者的简历附件
- **请求方式**: POST
- **请求路径**: `/api/job-seeker/resume`
- **认证要求**: 需要登录认证
- **Content-Type**: multipart/form-data

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| file | File | 是 | 简历文件 |

**成功响应**:
```json
{
  "code": 1000,
  "message": "上传简历成功",
  "data": {
    "resumeUrl": "https://cdn.example.com/resumes/xxx.pdf"
  }
}
```

### 2.5 删除简历

- **接口描述**: 删除当前求职者的简历
- **请求方式**: DELETE
- **请求路径**: `/api/job-seeker/resume`
- **认证要求**: 需要登录认证

**成功响应**:
```json
{
  "code": 1000,
  "message": "删除成功",
  "data": null
}
```

### 2.6 查看简历

- **接口描述**: 获取当前求职者的简历URL
- **请求方式**: GET
- **请求路径**: `/api/job-seeker/resume`
- **认证要求**: 需要登录认证

**成功响应**:
```json
{
  "code": 1000,
  "message": "获取简历成功",
  "data": {
    "resumeUrl": "https://cdn.example.com/resumes/xxx.pdf",
    "resumeName": "张三_简历.pdf"
  }
}
```

---

## 三、投递管理

### 3.1 投递简历

- **接口描述**: 求职者投递简历到指定职位
- **请求方式**: POST
- **请求路径**: `/api/application/apply`
- **认证要求**: 需要登录认证（求职者）

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| positionId | Long | 是 | 职位ID |
| resumeId | Long | 否 | 简历ID |

**成功响应**:
```json
{
  "code": 1000,
  "message": "投递成功",
  "data": {
    "id": 1,
    "positionId": 1,
    "seekerId": 2,
    "status": "pending",
    "createdAt": "2026-04-18T10:00:00"
  }
}
```

### 3.2 查看投递记录

- **接口描述**: 查看当前用户的投递记录
- **请求方式**: GET
- **请求路径**: `/api/application/seeker/list`
- **认证要求**: 需要登录认证（求职者）

**成功响应**:
```json
{
  "code": 1000,
  "message": "获取成功",
  "data": [
    {
      "id": 1,
      "positionId": 1,
      "positionTitle": "Java开发工程师",
      "companyName": "某科技有限公司",
      "status": "pending",
      "createdAt": "2026-04-18T10:00:00"
    }
  ]
}
```

### 3.3 取消投递

- **接口描述**: 取消指定的投递记录
- **请求方式**: DELETE
- **请求路径**: `/api/application/{id}`
- **认证要求**: 需要登录认证

**成功响应**:
```json
{
  "code": 1000,
  "message": "取消成功",
  "data": null
}
```

---

## 四、职位管理

### 4.1 职位列表

- **接口描述**: 获取职位列表（支持搜索筛选）
- **请求方式**: GET
- **请求路径**: `/api/position/list`
- **认证要求**: 公开接口

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| title | String | 否 | 搜索关键词（职位名称、描述或要求） |
| city | String | 否 | 城市 |
| category | String | 否 | 职位类别 |
| workYearsMin | Integer | 否 | 最低工作年限 |
| educationMin | Integer | 否 | 最低学历要求 |
| salaryMin | Integer | 否 | 最低薪资（K） |
| salaryMax | Integer | 否 | 最高薪资（K） |
| searchType | String | 否 | 搜索范围（多个值用逗号分隔，可选：title,description,requirement） |
| pageNum | Integer | 否 | 页码（默认1） |
| pageSize | Integer | 否 | 每页数量（默认10） |

### 4.2 职位详情

- **接口描述**: 获取指定职位的详细信息
- **请求方式**: GET
- **请求路径**: `/api/position/detail`
- **认证要求**: 公开接口

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 职位ID |

---

## 五、智能简历分析

### 5.1 上传并分析简历

- **接口描述**: 上传简历文件并获取AI分析结果
- **请求方式**: POST
- **请求路径**: `/api/resume/upload-and-analyze`
- **认证要求**: 需要登录认证
- **Content-Type**: multipart/form-data

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| file | File | 是 | 简历文件（支持PDF、Word、图片） |

**成功响应**:
```json
{
  "code": 1000,
  "message": "分析成功",
  "data": {
    "overallScore": 85,
    "highlights": "1. 技术栈丰富... 2. 项目经验充足...",
    "improvements": "1. 建议补充大数据相关技能... 2. 可以增加架构设计经验...",
    "skillAssessment": {
      "strongSkills": ["Java", "Spring Boot", "MySQL"],
      "recommendedSkills": ["Redis", "Kafka", "Spring Cloud"]
    }
  }
}
```

### 5.2 分析已有简历

- **接口描述**: 分析用户已上传的简历
- **请求方式**: POST
- **请求路径**: `/api/resume/analyze`
- **认证要求**: 需要登录认证

**成功响应**: 同5.1上传并分析简历

---

## 六、智能简历填充

### 6.1 智能填充简历信息

- **接口描述**: 上传简历文件，通过AI分析并自动填充个人信息
- **请求方式**: POST
- **请求路径**: `/api/resume/smart-fill`
- **认证要求**: 需要登录认证
- **Content-Type**: multipart/form-data

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| file | File | 是 | 简历文件（支持PDF、Word格式） |

**成功响应**:
```json
{
  "code": 1000,
  "message": "操作成功",
  "data": {
    "success": true,
    "errorMessage": null,
    "name": "张三",
    "gender": 1,
    "age": 28,
    "phone": "13800138000",
    "email": "zhangsan@example.com",
    "city": "北京市",
    "address": "朝阳区",
    "workYears": 5,
    "currentSalary": 20.00,
    "expectedSalary": 30.00,
    "currentStatus": 1,
    "skills": ["Java", "Spring Boot", "MySQL", "Vue.js"],
    "introduction": "具有多年互联网开发经验...",
    "unfilledFields": ["年龄"],
    "confidence": 0.85
  }
}
```

**响应字段说明**:

| 字段 | 类型 | 说明 |
|------|------|------|
| success | Boolean | 是否成功 |
| errorMessage | String | 错误信息（失败时返回） |
| name | String | 姓名 |
| gender | Integer | 性别：1-男，2-女 |
| age | Integer | 年龄 |
| phone | String | 手机号 |
| email | String | 邮箱 |
| city | String | 所在城市 |
| address | String | 详细地址 |
| workYears | Integer | 工作年限 |
| currentSalary | BigDecimal | 当前薪资（K/月） |
| expectedSalary | BigDecimal | 期望薪资（K/月） |
| currentStatus | Integer | 当前状态：1-在职，2-离职，3-在读学生 |
| skills | List<String> | 技能标签列表 |
| introduction | String | 个人简介 |
| unfilledFields | List<String> | 未填充的字段列表 |
| confidence | Double | 解析置信度（0-1） |

---

## 七、岗位画像管理

### 7.1 生成岗位画像

- **接口描述**: 根据岗位信息生成AI画像
- **请求方式**: POST
- **请求路径**: `/api/job-profile/generate/{positionId}`
- **认证要求**: 需要登录认证

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| positionId | Long | 是 | 岗位ID |

**成功响应**:
```json
{
  "code": 1000,
  "message": "操作成功",
  "data": {
    "id": 1,
    "positionId": 1,
    "jobName": "Java开发工程师",
    "companyName": "某科技有限公司",
    "skills": ["Java", "Spring Boot", "MySQL"],
    "educationRequire": "本科",
    "educationLevel": 3,
    "experienceRequire": "3-5年",
    "experienceLevel": 4,
    "salaryRange": "15-30K/月",
    "salaryMin": 15.00,
    "salaryMax": 30.00,
    "jobTags": ["互联网", "技术开发"],
    "descriptionSummary": "负责公司核心业务系统开发...",
    "responsibilitiesSummary": "1. 参与系统架构设计...",
    "requirementsSummary": "1. 计算机相关专业本科以上学历...",
    "companyBenefits": "五险一金、带薪年假...",
    "matchKeywords": ["Java", "Spring", "后端开发"],
    "createdAt": "2026-04-18T10:00:00"
  }
}
```

### 7.2 获取岗位画像

- **接口描述**: 获取指定岗位的AI画像
- **请求方式**: GET
- **请求路径**: `/api/job-profile/{positionId}`
- **认证要求**: 需要登录认证

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| positionId | Long | 是 | 岗位ID |

**成功响应**: 同7.1生成岗位画像成功响应

### 7.3 更新岗位画像

- **接口描述**: 重新生成指定岗位的AI画像
- **请求方式**: PUT
- **请求路径**: `/api/job-profile/update/{positionId}`
- **认证要求**: 需要登录认证

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| positionId | Long | 是 | 岗位ID |

**成功响应**: 同7.1生成岗位画像成功响应

### 7.4 删除岗位画像

- **接口描述**: 删除指定岗位的AI画像
- **请求方式**: DELETE
- **请求路径**: `/api/job-profile/{positionId}`
- **认证要求**: 需要登录认证

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| positionId | Long | 是 | 岗位ID |

**成功响应**:
```json
{
  "code": 1000,
  "message": "操作成功",
  "data": true
}
```

---

## 八、人才画像管理

### 8.1 生成人才画像

- **接口描述**: 根据当前用户信息生成AI画像
- **请求方式**: POST
- **请求路径**: `/api/talent-profile/generate`
- **认证要求**: 需要登录认证（求职者）

**成功响应**:
```json
{
  "code": 1000,
  "message": "操作成功",
  "data": {
    "id": 1,
    "userId": 2,
    "userName": "张三",
    "skills": ["Java", "Spring Boot", "Vue.js"],
    "education": "本科",
    "educationLevel": 3,
    "workYears": 3,
    "salaryExpectation": 25.00,
    "currentSalary": 18.00,
    "talentTags": ["技术达人", "全栈开发"],
    "descriptionSummary": "具有多年互联网开发经验...",
    "strengthsSummary": "熟悉Java技术栈...",
    "careerGoals": "寻求高级Java开发工程师岗位...",
    "matchKeywords": ["Java", "后端开发", "全栈"],
    "aiEvaluation": "具备扎实的Java开发能力...",
    "createdAt": "2026-04-18T10:00:00"
  }
}
```

### 8.2 获取人才画像

- **接口描述**: 获取当前用户的人才画像
- **请求方式**: GET
- **请求路径**: `/api/talent-profile`
- **认证要求**: 需要登录认证（求职者）

**成功响应**: 同8.1生成人才画像成功响应

### 8.3 更新人才画像

- **接口描述**: 重新生成当前用户的人才画像
- **请求方式**: PUT
- **请求路径**: `/api/talent-profile/update`
- **认证要求**: 需要登录认证（求职者）

**成功响应**: 同8.1生成人才画像成功响应

### 8.4 删除人才画像

- **接口描述**: 删除当前用户的人才画像
- **请求方式**: DELETE
- **请求路径**: `/api/talent-profile`
- **认证要求**: 需要登录认证（求职者）

**成功响应**:
```json
{
  "code": 1000,
  "message": "操作成功",
  "data": true
}
```

---

## 九、岗位推荐

### 9.1 获取岗位推荐列表

- **接口描述**: 根据用户画像推荐匹配的岗位
- **请求方式**: GET
- **请求路径**: `/api/job-recommend`
- **认证要求**: 需要登录认证（求职者）

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| limit | Integer | 否 | 推荐数量限制，默认10 |

**成功响应**:
```json
{
  "code": 1000,
  "message": "操作成功",
  "data": [
    {
      "positionId": 1,
      "id": 1,
      "companyId": 1,
      "jobName": "Java开发工程师",
      "title": "Java开发工程师",
      "companyName": "某科技有限公司",
      "companyLogo": "https://example.com/logo.png",
      "city": "北京市",
      "address": "朝阳区",
      "salaryRange": "15-30K/月",
      "salaryMin": 15,
      "salaryMax": 30,
      "educationRequire": "本科",
      "educationMin": 3,
      "experienceRequire": "3-5年",
      "workYearsMin": 4,
      "category": "后端开发",
      "jobTags": ["互联网", "技术开发"],
      "tags": "["互联网", "技术开发"]",
      "description": "负责公司核心业务系统开发...",
      "requirement": "计算机相关专业本科以上学历...",
      "status": 1,
      "matchScore": 85.50,
      "skillMatchRate": 90.00,
      "experienceMatchRate": 80.00,
      "educationMatchRate": 100.00,
      "salaryMatchRate": 75.00,
      "matchDetails": {
        "matchedSkills": ["Java", "Spring Boot"],
        "missingSkills": ["Redis"],
        "matchDescription": "技能匹配度较高，经验要求满足"
      },
      "descriptionSummary": "负责公司核心业务系统开发...",
      "isFavorite": false,
      "createdAt": "2026-04-18T10:00:00"
    }
  ]
}
```

### 9.2 获取匹配度详情

- **接口描述**: 获取指定岗位与当前用户的匹配度详情
- **请求方式**: GET
- **请求路径**: `/api/job-recommend/match/{positionId}`
- **认证要求**: 需要登录认证（求职者）

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| positionId | Long | 是 | 岗位ID |

**成功响应**: 同9.1获取岗位推荐列表中的单个推荐项详情

### 9.3 批量生成匹配记录

- **接口描述**: 为当前用户批量生成所有岗位的匹配记录
- **请求方式**: POST
- **请求路径**: `/api/job-recommend/batch-generate`
- **认证要求**: 需要登录认证（求职者）

**成功响应**:
```json
{
  "code": 1000,
  "message": "操作成功",
  "data": 50
}
```

**响应说明**:
- data表示生成的匹配记录数量

### 9.4 标记匹配记录为已查看

- **接口描述**: 标记指定匹配记录为已查看状态
- **请求方式**: PUT
- **请求路径**: `/api/job-recommend/viewed/{recordId}`
- **认证要求**: 需要登录认证

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| recordId | Long | 是 | 匹配记录ID |

**成功响应**:
```json
{
  "code": 1000,
  "message": "操作成功",
  "data": true
}
```

---

## 十、数据统计

### 10.1 获取求职者端统计数据

- **接口描述**: 获取就业市场统计数据，帮助求职者了解行情
- **请求方式**: GET
- **请求路径**: `/api/statistics/seeker`
- **认证要求**: 需要登录认证（求职者）

**成功响应**:
```json
{
  "code": 1000,
  "message": "获取成功",
  "data": {
    "totalPositions": 156,
    "todayNewPositions": 12,
    "myApplications": 5,
    "myInterviews": 2,
    "hotCities": [
      { "city": "北京", "count": 45, "percentage": 28.85 },
      { "city": "上海", "count": 38, "percentage": 24.36 },
      { "city": "深圳", "count": 25, "percentage": 16.03 },
      { "city": "广州", "count": 20, "percentage": 12.82 },
      { "city": "杭州", "count": 18, "percentage": 11.54 }
    ],
    "hotCategories": [
      { "category": "技术", "count": 65, "percentage": 41.67 },
      { "category": "产品", "count": 28, "percentage": 17.95 },
      { "category": "运营", "count": 22, "percentage": 14.10 },
      { "category": "设计", "count": 18, "percentage": 11.54 },
      { "category": "市场", "count": 15, "percentage": 9.62 }
    ],
    "highSalaryPercentage": 35.50,
    "competitionIndex": 3.25
  }
}
```

**响应字段说明**:

| 字段 | 类型 | 说明 |
|------|------|------|
| totalPositions | Long | 总职位数（招聘中） |
| todayNewPositions | Long | 今日新增职位数 |
| myApplications | Long | 我的投递数 |
| myInterviews | Long | 我的面试邀请数 |
| hotCities | List | 热门城市TOP5分布 |
| hotCategories | List | 热门职位类别TOP5分布 |
| highSalaryPercentage | BigDecimal | 高薪职位占比（薪资>=20K） |
| competitionIndex | BigDecimal | 求职竞争指数（平均投递数/职位数） |

### 10.2 获取HR端统计数据

- **接口描述**: 获取招聘效果统计数据，帮助HR了解招聘情况
- **请求方式**: GET
- **请求路径**: `/api/statistics/boss`
- **认证要求**: 需要登录认证（企业HR）

**成功响应**:
```json
{
  "code": 1000,
  "message": "获取成功",
  "data": {
    "myPositions": 8,
    "myApplications": 45,
    "pendingApplications": 12,
    "interviewingCount": 8,
    "hiredCount": 3,
    "rejectedCount": 22,
    "conversionRate": 6.67,
    "positionStats": [
      {
        "positionId": 1,
        "positionTitle": "Java开发工程师",
        "applicationCount": 15,
        "conversionRate": 5.86
      },
      {
        "positionId": 2,
        "positionTitle": "前端开发工程师",
        "applicationCount": 12,
        "conversionRate": 6.06
      }
    ]
  }
}
```

**响应字段说明**:

| 字段 | 类型 | 说明 |
|------|------|------|
| myPositions | Long | 我发布的职位数 |
| myApplications | Long | 收到投递总数 |
| pendingApplications | Long | 待处理投递数 |
| interviewingCount | Long | 面试中数 |
| hiredCount | Long | 已录用数 |
| rejectedCount | Long | 不合适数 |
| conversionRate | BigDecimal | 整体转化率（录用/投递） |
| positionStats | List | 各职位投递情况（positionId-职位ID，positionTitle-职位名称，applicationCount-投递数，conversionRate-转化率） |

---

## 十一、错误码说明

### 系统级错误码

| 错误码 | 说明 |
|--------|------|
| 1000 | 操作成功 |
| 1001 | 系统异常 |
| 1002 | 操作失败 |
| 2000 | 业务异常 |
| 2001 | 参数错误 |
| 2002 | 权限不足 |
| 2003 | 资源不存在 |
| 2004 | 身份冲突 |
| 2005 | 验证码错误 |
| 2006 | 验证码已过期 |
| 2007 | 文件上传失败 |
| 2008 | 文件类型不支持 |
| 2009 | 文件大小超限 |

### 业务级错误码

| 错误码 | 说明 |
|--------|------|
| 3001 | 用户不存在 |
| 3002 | 求职者信息不存在 |
| 3003 | 企业信息不存在 |
| 3004 | 岗位不存在 |
| 3005 | 投递记录不存在 |
| 3006 | 收藏记录不存在 |
| 3007 | 简历不存在 |
| 3008 | 教育经历不存在 |
| 3009 | 工作经历不存在 |
| 3010 | 项目经历不存在 |
| 3011 | 面试记录不存在 |
| 3012 | 岗位画像不存在 |
| 3013 | 人才画像不存在 |

---

## 十二、数据字典

### 学历等级

| 等级 | 说明 |
|------|------|
| 1 | 高中及以下 |
| 2 | 大专 |
| 3 | 本科 |
| 4 | 硕士 |
| 5 | 博士 |

### 经验等级

| 等级 | 说明 |
|------|------|
| 1 | 不限 |
| 2 | 1年以下 |
| 3 | 1-3年 |
| 4 | 3-5年 |
| 5 | 5-10年 |
| 6 | 10年以上 |

### 当前状态

| 值 | 说明 |
|-----|------|
| 1 | 在职 |
| 2 | 离职 |
| 3 | 在读学生 |

### 性别

| 值 | 说明 |
|-----|------|
| 1 | 男 |
| 2 | 女 |

### 熟练度等级

| 等级 | 说明 |
|------|------|
| 1 | 了解 |
| 2 | 熟悉 |
| 3 | 掌握 |
| 4 | 精通 |
| 5 | 专家 |

### 技能等级

| 值 | 说明 |
|-----|------|
| required | 必须 |
| preferred | 优先 |

---

## 十三、注意事项

1. **验证码有效期**：验证码有效期为5分钟，超过时间需要重新获取
2. **JWT令牌**：需要认证的接口需要在请求头中携带 `Authorization: Bearer <token>`
3. **文件上传**：上传文件前需要先调用文件上传接口获取URL，然后将URL传给对应的业务接口
4. **自动创建**：首次登录时，系统会自动创建用户和相关业务信息（如job_seeker或company记录）
5. **查看自己简历**：登录后查看自己的简历，调用 `/api/job-seeker/info` 接口即可获取完整信息（包含基本信息、教育经历、工作经历、项目经历）
6. **Boss查看候选人**：Boss通过投递记录查看候选人的简历信息，使用 `/api/application/job-seeker/resume?id=1` 接口
7. **AI画像生成**：首次访问岗位画像或人才画像时，系统会自动生成画像
8. **智能推荐**：推荐系统基于人才画像和岗位画像的匹配度计算，推荐最合适的岗位

---

## 十四、匹配度计算说明

### 匹配分数计算

综合匹配分数 = 技能匹配率 × 0.5 + 经验匹配率 × 0.3 + 学历匹配率 × 0.1 + 薪资匹配率 × 0.1

### 各项匹配率计算

1. **技能匹配率**：求职者技能与岗位要求技能的匹配比例
2. **经验匹配率**：根据求职者工作年限与岗位要求经验对比计算
3. **学历匹配率**：根据求职者学历与岗位要求学历对比计算
4. **薪资匹配率**：根据求职者期望薪资与岗位薪资范围对比计算

### 匹配等级划分

| 匹配分数 | 等级 | 说明 |
|----------|------|------|
| 80-100 | 高 | 高度匹配，推荐优先 |
| 60-79 | 中 | 中度匹配，可以尝试 |
| 0-59 | 低 | 低度匹配，需要考虑 |
