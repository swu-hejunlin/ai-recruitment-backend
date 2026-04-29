# AI智能招聘平台 - 接口文档

## 接口说明
本文档描述AI智能招聘平台的完整API接口，包含用户登录、身份切换、求职者信息管理、企业信息管理、文件上传、智能简历分析、岗位画像、人才画像、岗位推荐、面试管理、通知、收藏等功能。

## 业务规则
- **手机号唯一**：同一个手机号只能有一个账号
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

> **安全说明**：系统在后端对关键接口实施了角色校验和数据归属权校验，确保用户只能操作自己的数据。

---

## 统一返回格式

### 成功响应
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {},
  "timestamp": 1714032000000
}
```

### 错误响应
```json
{
  "code": 400,
  "message": "错误描述",
  "data": null,
  "timestamp": 1714032000000
}
```

### 常用状态码
| code | 说明 |
|------|------|
| 200 | 成功 |
| 400 | 业务错误（参数错误、数据不存在等） |
| 401 | 未授权（Token无效或过期） |
| 403 | 权限不足 |
| 500 | 服务器内部错误 |

---

## 接口认证要求汇总

### 一、公开接口（无需登录）
| 接口地址 | 说明 |
|----------|------|
| `POST /api/user/send-code` | 发送验证码 |
| `POST /api/user/login` | 验证码登录 |
| `POST /api/user/switch-role` | 身份切换 |
| `GET /api/position/list` | 职位列表（搜索筛选） |
| `GET /api/position/detail` | 职位详情 |
| `GET /api/position/latest` | 最新职位（首页推荐） |
| `GET /api/position/hot` | 热门职位（首页推荐） |
| `GET /api/position/company-info` | 职位对应的公司信息 |
| `GET /api/position/detail-with-company` | 职位详情（含公司信息） |
| `GET /api/company/{id}` | 根据公司ID查看公司详情 |

### 二、需要登录认证的接口

| 模块 | 接口地址 | 说明 |
|------|----------|------|
| 求职者 | `GET /api/job-seeker/info` | 获取求职者完整信息 |
| 求职者 | `PUT /api/job-seeker/update` | 更新求职者信息 |
| 求职者 | `POST /api/job-seeker/avatar` | 上传头像 |
| 求职者 | `GET /api/job-seeker/avatar` | 获取头像 |
| 求职者 | `POST /api/job-seeker/resume` | 上传简历 |
| 求职者 | `GET /api/job-seeker/resume` | 获取简历 |
| 教育经历 | `GET /api/education/list` | 获取教育经历列表 |
| 教育经历 | `POST /api/education/add` | 新增教育经历 |
| 教育经历 | `PUT /api/education/update` | 更新教育经历 |
| 教育经历 | `DELETE /api/education/delete` | 删除教育经历 |
| 工作经历 | `GET /api/experience/list` | 获取工作/实习经历列表 |
| 工作经历 | `POST /api/experience/add` | 新增工作/实习经历 |
| 工作经历 | `PUT /api/experience/update` | 更新工作/实习经历 |
| 工作经历 | `DELETE /api/experience/delete` | 删除工作/实习经历 |
| 项目经历 | `GET /api/project/list` | 获取项目经历列表 |
| 项目经历 | `POST /api/project/add` | 新增项目经历 |
| 项目经历 | `PUT /api/project/update` | 更新项目经历 |
| 项目经历 | `DELETE /api/project/delete` | 删除项目经历 |
| 企业 | `GET /api/company/info` | 获取当前企业信息 |
| 企业 | `PUT /api/company/update` | 更新企业信息 |
| 企业 | `POST /api/company/logo` | 上传企业Logo |
| 企业 | `POST /api/company/license` | 上传营业执照 |
| 职位 | `POST /api/position/add` | 发布职位 |
| 职位 | `PUT /api/position/update` | 更新职位 |
| 职位 | `DELETE /api/position/delete` | 删除职位 |
| 职位 | `GET /api/position/boss/list` | Boss查看自己的职位 |
| 职位 | `PUT /api/position/close` | 关闭职位 |
| 职位 | `PUT /api/position/open` | 开启职位 |
| 职位 | `GET /api/position/company` | 查看公司下的职位 |
| 投递 | `POST /api/application/apply` | 投递简历 |
| 投递 | `GET /api/application/boss/list` | Boss查看收到的投递 |
| 投递 | `GET /api/application/seeker/list` | 求职者查看投递记录 |
| 投递 | `GET /api/application/detail` | 查看投递详情 |
| 投递 | `GET /api/application/position` | 查看投递对应的职位 |
| 投递 | `GET /api/application/company` | 查看投递对应的公司 |
| 投递 | `GET /api/application/job-seeker/simple` | Boss查看求职者简要信息 |
| 投递 | `GET /api/application/job-seeker/resume` | Boss查看求职者完整简历 |
| 投递 | `PUT /api/application/read` | 标记简历已查看 |
| 投递 | `PUT /api/application/status` | 更新投递状态 |
| 通知 | `GET /api/notification/list` | 通知列表 |
| 通知 | `GET /api/notification/unread-count` | 未读通知数量 |
| 通知 | `PUT /api/notification/read` | 标记通知已读 |
| 通知 | `PUT /api/notification/read-all` | 标记所有通知已读 |
| 收藏 | `POST /api/favorite/add` | 添加收藏 |
| 收藏 | `DELETE /api/favorite/remove` | 取消收藏 |
| 收藏 | `GET /api/favorite/list` | 收藏列表 |
| 收藏 | `GET /api/favorite/check` | 检查是否已收藏 |
| 面试 | `POST /api/interview/create` | 创建面试邀请 |
| 面试 | `PUT /api/interview/{id}/status` | 更新面试状态 |
| 面试 | `GET /api/interview/company/list` | 企业面试列表 |
| 面试 | `GET /api/interview/job-seeker/list` | 求职者面试列表 |
| 面试 | `GET /api/interview/{id}` | 面试详情 |
| 面试 | `DELETE /api/interview/{id}` | 删除面试 |
| 面试 | `POST /api/interview/mock` | 提交模拟面试 |
| 面试 | `GET /api/interview/mock/questions` | 生成面试题 |
| 面试 | `POST /api/interview/finish` | 结束AI面试 |
| 面试 | `GET /api/interview/evaluation/{id}` | 获取面试评估 |
| 面试 | `GET /api/interview/evaluation/{id}/exists` | 检查评估是否存在 |
| 简历AI | `POST /api/resume/analyze` | 分析简历 |
| 简历AI | `POST /api/resume/upload-and-analyze` | 上传并分析简历 |
| 简历AI | `POST /api/resume/smart-fill` | 智能填充简历 |
| 岗位画像 | `POST /api/job-profile/generate/{positionId}` | 生成岗位画像 |
| 岗位画像 | `GET /api/job-profile/{positionId}` | 获取岗位画像 |
| 岗位画像 | `PUT /api/job-profile/update/{positionId}` | 更新岗位画像 |
| 岗位画像 | `DELETE /api/job-profile/{positionId}` | 删除岗位画像 |
| 人才画像 | `POST /api/talent-profile/generate` | 生成人才画像 |
| 人才画像 | `GET /api/talent-profile` | 获取人才画像 |
| 人才画像 | `PUT /api/talent-profile/update` | 更新人才画像 |
| 人才画像 | `DELETE /api/talent-profile` | 删除人才画像 |
| 推荐 | `GET /api/job-recommend` | 岗位推荐列表 |
| 推荐 | `GET /api/job-recommend/match/{positionId}` | 匹配度详情 |
| 推荐 | `POST /api/job-recommend/batch-generate` | 批量生成匹配 |
| 推荐 | `PUT /api/job-recommend/viewed/{recordId}` | 标记已查看 |
| 人才推荐 | `GET /api/talent-recommend` | 人才推荐列表（HR） |
| 人才推荐 | `GET /api/talent-recommend/match` | 人才匹配详情（HR） |
| 人才推荐 | `POST /api/talent-recommend/batch-generate` | 批量生成人才匹配（HR） |
| 人才推荐 | `PUT /api/talent-recommend/viewed/{recordId}` | 标记人才已查看（HR） |
| 统计 | `GET /api/statistics/seeker` | 求职者统计 |
| 统计 | `GET /api/statistics/boss` | HR统计 |
| 统计 | `GET /api/statistics/wordcloud` | 词云数据 |
| 文件 | `POST /api/file/upload` | 上传文件 |
| 文件 | `POST /api/file/upload-batch` | 批量上传 |
| 文件 | `DELETE /api/file/delete` | 删除文件 |

---

## 1. 用户认证模块

### 1.1 发送验证码
- **URL**: `POST /api/user/send-code`
- **认证**: 无需登录

**请求参数**:
```json
{
  "phone": "13800138000"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "验证码发送成功",
  "data": null,
  "timestamp": 1714032000000
}
```

> 验证码有效期5分钟，存储在Redis中。开发环境验证码会打印在后端控制台。

### 1.2 验证码登录
- **URL**: `POST /api/user/login`
- **认证**: 无需登录

**请求参数**:
```json
{
  "phone": "13800138000",
  "code": "123456",
  "role": 1
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| phone | String | 是 | 手机号 |
| code | String | 是 | 验证码 |
| role | Integer | 是 | 角色：1-求职者，2-企业HR |

**响应示例**:
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "userInfo": {
      "userId": 1,
      "phone": "13800138000",
      "role": 1
    }
  },
  "timestamp": 1714032000000
}
```

> 如果手机号已注册但角色不同，返回身份冲突提示，需调用切换角色接口。

### 1.3 身份切换
- **URL**: `POST /api/user/switch-role`
- **认证**: 无需登录

**请求参数**:
```json
{
  "phone": "13800138000",
  "code": "123456",
  "role": 2
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "身份切换成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "userInfo": {
      "userId": 1,
      "phone": "13800138000",
      "role": 2
    }
  },
  "timestamp": 1714032000000
}
```

---

## 2. 求职者信息模块

### 2.1 获取求职者完整信息
- **URL**: `GET /api/job-seeker/info`
- **认证**: 需要登录（求职者）

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "userId": 1,
    "name": "张三",
    "gender": 1,
    "phone": "13800138000",
    "email": "zhangsan@example.com",
    "birthday": "1995-06-15",
    "city": "北京",
    "currentStatus": 1,
    "educationLevel": 3,
    "workYears": 3,
    "expectedSalaryMin": 15000,
    "expectedSalaryMax": 25000,
    "expectedCity": "北京",
    "expectedPosition": "Java工程师",
    "skills": ["Java", "Spring Boot", "MySQL"],
    "selfIntroduction": "3年Java开发经验",
    "avatarUrl": "https://oss.example.com/avatar/1.jpg",
    "resumeUrl": "https://oss.example.com/resume/1.pdf",
    "educationList": [...],
    "experienceList": [...],
    "projectList": [...]
  },
  "timestamp": 1714032000000
}
```

### 2.2 更新求职者信息
- **URL**: `PUT /api/job-seeker/update`
- **认证**: 需要登录（求职者）

**请求参数**:
```json
{
  "name": "张三",
  "gender": 1,
  "email": "zhangsan@example.com",
  "birthday": "1995-06-15",
  "city": "北京",
  "currentStatus": 1,
  "educationLevel": 3,
  "workYears": 3,
  "expectedSalaryMin": 15000,
  "expectedSalaryMax": 25000,
  "expectedCity": "北京",
  "expectedPosition": "Java工程师",
  "skills": ["Java", "Spring Boot", "MySQL"],
  "selfIntroduction": "3年Java开发经验"
}
```

### 2.3 上传头像
- **URL**: `POST /api/job-seeker/avatar`
- **认证**: 需要登录（求职者）
- **Content-Type**: `multipart/form-data`

**请求参数**:
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| file | File | 是 | 头像图片（支持jpg/png，最大2MB） |

### 2.4 获取头像
- **URL**: `GET /api/job-seeker/avatar`
- **认证**: 需要登录（求职者）

### 2.5 上传简历
- **URL**: `POST /api/job-seeker/resume`
- **认证**: 需要登录（求职者）
- **Content-Type**: `multipart/form-data`

**请求参数**:
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| file | File | 是 | 简历文件（支持pdf/doc/docx，最大10MB） |

### 2.6 获取简历
- **URL**: `GET /api/job-seeker/resume`
- **认证**: 需要登录（求职者）

---

## 3. 教育经历模块

### 3.1 获取教育经历列表
- **URL**: `GET /api/education/list`
- **认证**: 需要登录（求职者）

### 3.2 新增教育经历
- **URL**: `POST /api/education/add`
- **认证**: 需要登录（求职者）

**请求参数**:
```json
{
  "school": "北京大学",
  "major": "计算机科学与技术",
  "educationLevel": 3,
  "startTime": "2015-09",
  "endTime": "2019-06"
}
```

### 3.3 更新教育经历
- **URL**: `PUT /api/education/update`
- **认证**: 需要登录（求职者）

### 3.4 删除教育经历
- **URL**: `DELETE /api/education/delete`
- **认证**: 需要登录（求职者）

**请求参数**:
```json
{
  "id": 1
}
```

---

## 4. 工作/实习经历模块

### 4.1 获取工作经历列表
- **URL**: `GET /api/experience/list`
- **认证**: 需要登录（求职者）

### 4.2 新增工作经历
- **URL**: `POST /api/experience/add`
- **认证**: 需要登录（求职者）

**请求参数**:
```json
{
  "companyName": "字节跳动",
  "position": "Java开发工程师",
  "startTime": "2019-07",
  "endTime": "2022-03",
  "description": "负责后端服务开发",
  "isIntern": false
}
```

### 4.3 更新工作经历
- **URL**: `PUT /api/experience/update`
- **认证**: 需要登录（求职者）

### 4.4 删除工作经历
- **URL**: `DELETE /api/experience/delete`
- **认证**: 需要登录（求职者）

---

## 5. 项目经历模块

### 5.1 获取项目经历列表
- **URL**: `GET /api/project/list`
- **认证**: 需要登录（求职者）

### 5.2 新增项目经历
- **URL**: `POST /api/project/add`
- **认证**: 需要登录（求职者）

**请求参数**:
```json
{
  "projectName": "智能招聘平台",
  "role": "后端开发",
  "startTime": "2022-01",
  "endTime": "2022-06",
  "description": "负责AI推荐模块开发",
  "techStack": "Java, Spring Boot, MySQL"
}
```

### 5.3 更新项目经历
- **URL**: `PUT /api/project/update`
- **认证**: 需要登录（求职者）

### 5.4 删除项目经历
- **URL**: `DELETE /api/project/delete`
- **认证**: 需要登录（求职者）

---

## 6. 企业信息模块

### 6.1 获取当前企业信息
- **URL**: `GET /api/company/info`
- **认证**: 需要登录（HR）

### 6.2 根据ID查询企业信息
- **URL**: `GET /api/company/{id}`
- **认证**: 无需登录

### 6.3 更新企业信息
- **URL**: `PUT /api/company/update`
- **认证**: 需要登录（HR）

**请求参数**:
```json
{
  "name": "科技有限公司",
  "legalPerson": "李四",
  "industry": "互联网",
  "scale": 3,
  "financingStage": 4,
  "description": "一家专注于AI的科技公司",
  "welfareTags": ["五险一金", "弹性工作", "免费午餐"],
  "website": "https://example.com",
  "address": "北京市海淀区"
}
```

### 6.4 上传企业Logo
- **URL**: `POST /api/company/logo`
- **认证**: 需要登录（HR）
- **Content-Type**: `multipart/form-data`

### 6.5 上传营业执照
- **URL**: `POST /api/company/license`
- **认证**: 需要登录（HR）
- **Content-Type**: `multipart/form-data`

---

## 7. 职位管理模块

### 7.1 发布职位
- **URL**: `POST /api/position/add`
- **认证**: 需要登录（HR）

**请求参数**:
```json
{
  "title": "高级Java工程师",
  "category": "后端开发",
  "city": "北京",
  "salaryMin": 20000,
  "salaryMax": 40000,
  "educationRequired": 3,
  "experienceRequired": 3,
  "description": "负责后端系统架构设计和开发",
  "requirements": "3年以上Java开发经验，熟悉Spring Boot",
  "skills": ["Java", "Spring Boot", "MySQL", "Redis"],
  "benefits": "五险一金、弹性工作"
}
```

### 7.2 更新职位
- **URL**: `PUT /api/position/update`
- **认证**: 需要登录（HR）

### 7.3 删除职位
- **URL**: `DELETE /api/position/delete`
- **认证**: 需要登录（HR）

### 7.4 查询职位详情
- **URL**: `GET /api/position/detail`
- **认证**: 无需登录

**请求参数**:
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 职位ID |

### 7.5 Boss查看自己的职位列表
- **URL**: `GET /api/position/boss/list`
- **认证**: 需要登录（HR）

### 7.6 求职者搜索职位列表
- **URL**: `GET /api/position/list`
- **认证**: 无需登录

**请求参数**:
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| keyword | String | 否 | 搜索关键词 |
| city | String | 否 | 城市 |
| category | String | 否 | 职位类别 |
| educationRequired | Integer | 否 | 最低学历要求 |
| salaryMin | Integer | 否 | 最低薪资 |
| experienceRequired | Integer | 否 | 最低经验要求 |
| page | Integer | 否 | 页码（默认1） |
| size | Integer | 否 | 每页数量（默认10） |

### 7.7 关闭职位
- **URL**: `PUT /api/position/close`
- **认证**: 需要登录（HR）

### 7.8 开启职位
- **URL**: `PUT /api/position/open`
- **认证**: 需要登录（HR）

### 7.9 查看公司下的职位
- **URL**: `GET /api/position/company`
- **认证**: 无需登录

**请求参数**:
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| companyId | Long | 是 | 公司ID |

### 7.10 职位对应的公司信息
- **URL**: `GET /api/position/company-info`
- **认证**: 无需登录

**请求参数**:
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| positionId | Long | 是 | 职位ID |

### 7.11 最新职位列表
- **URL**: `GET /api/position/latest`
- **认证**: 无需登录

### 7.12 热门职位列表
- **URL**: `GET /api/position/hot`
- **认证**: 无需登录

### 7.13 职位详情（含公司信息）
- **URL**: `GET /api/position/detail-with-company`
- **认证**: 无需登录

**请求参数**:
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 职位ID |

---

## 8. 投递管理模块

### 8.1 投递简历
- **URL**: `POST /api/application/apply`
- **认证**: 需要登录（求职者）

**请求参数**:
```json
{
  "positionId": 1
}
```

> 同一职位不可重复投递。投递时系统会异步计算AI匹配评分。

### 8.2 Boss查看收到的投递列表
- **URL**: `GET /api/application/boss/list`
- **认证**: 需要登录（HR）

**请求参数**:
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| status | Integer | 否 | 投递状态筛选 |
| page | Integer | 否 | 页码 |
| size | Integer | 否 | 每页数量 |

### 8.3 求职者查看投递记录
- **URL**: `GET /api/application/seeker/list`
- **认证**: 需要登录（求职者）

### 8.4 查看投递详情
- **URL**: `GET /api/application/detail`
- **认证**: 需要登录

**请求参数**:
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 投递ID |

### 8.5 查看投递对应的职位信息
- **URL**: `GET /api/application/position`
- **认证**: 需要登录

### 8.6 查看投递对应的公司信息
- **URL**: `GET /api/application/company`
- **认证**: 需要登录

### 8.7 Boss查看求职者简要信息
- **URL**: `GET /api/application/job-seeker/simple`
- **认证**: 需要登录（HR）

**请求参数**:
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| jobSeekerId | Long | 是 | 求职者ID |

### 8.8 Boss查看求职者完整在线简历
- **URL**: `GET /api/application/job-seeker/resume`
- **认证**: 需要登录（HR）

**请求参数**:
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| jobSeekerId | Long | 是 | 求职者ID |

### 8.9 标记简历已查看
- **URL**: `PUT /api/application/read`
- **认证**: 需要登录（HR）

**请求参数**:
```json
{
  "id": 1
}
```

### 8.10 更新投递状态
- **URL**: `PUT /api/application/status`
- **认证**: 需要登录（HR）

**请求参数**:
```json
{
  "id": 1,
  "status": 3
}
```

**投递状态流转**:
| status | 说明 |
|--------|------|
| 1 | 待查看 |
| 2 | 已查看 |
| 3 | 面试中 |
| 4 | 不合适 |
| 5 | 录用 |

---

## 9. 通知模块

### 9.1 获取通知列表
- **URL**: `GET /api/notification/list`
- **认证**: 需要登录

**请求参数**:
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | Integer | 否 | 页码 |
| size | Integer | 否 | 每页数量 |

### 9.2 获取未读通知数量
- **URL**: `GET /api/notification/unread-count`
- **认证**: 需要登录

### 9.3 标记通知已读
- **URL**: `PUT /api/notification/read`
- **认证**: 需要登录

**请求参数**:
```json
{
  "id": 1
}
```

### 9.4 标记所有通知已读
- **URL**: `PUT /api/notification/read-all`
- **认证**: 需要登录

---

## 10. 收藏模块

### 10.1 添加收藏
- **URL**: `POST /api/favorite/add`
- **认证**: 需要登录

**请求参数**:
```json
{
  "targetId": 1,
  "targetType": 1
}
```

| targetType | 说明 |
|------------|------|
| 1 | 职位 |
| 2 | 公司 |
| 3 | 求职者 |

### 10.2 取消收藏
- **URL**: `DELETE /api/favorite/remove`
- **认证**: 需要登录

**请求参数**:
```json
{
  "targetId": 1,
  "targetType": 1
}
```

### 10.3 获取收藏列表
- **URL**: `GET /api/favorite/list`
- **认证**: 需要登录

**请求参数**:
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| targetType | Integer | 否 | 收藏类型筛选 |
| page | Integer | 否 | 页码 |
| size | Integer | 否 | 每页数量 |

### 10.4 检查是否已收藏
- **URL**: `GET /api/favorite/check`
- **认证**: 需要登录

**请求参数**:
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| targetId | Long | 是 | 目标ID |
| targetType | Integer | 是 | 目标类型 |

---

## 11. 面试模块

### 11.1 创建面试邀请
- **URL**: `POST /api/interview/create`
- **认证**: 需要登录（HR）

**请求参数**:
```json
{
  "applicationId": 1,
  "interviewType": 1,
  "interviewTime": "2026-05-01 14:00",
  "address": "北京市海淀区xx大厦",
  "onlineLink": "",
  "notes": "请携带身份证"
}
```

| interviewType | 说明 |
|---------------|------|
| 1 | 线下面试 |
| 2 | 线上面试 |
| 3 | AI面试 |

### 11.2 更新面试状态
- **URL**: `PUT /api/interview/{id}/status`
- **认证**: 需要登录

**请求参数**:
```json
{
  "status": 2
}
```

### 11.3 企业面试列表
- **URL**: `GET /api/interview/company/list`
- **认证**: 需要登录（HR）

### 11.4 求职者面试列表
- **URL**: `GET /api/interview/job-seeker/list`
- **认证**: 需要登录（求职者）

### 11.5 面试详情
- **URL**: `GET /api/interview/{id}`
- **认证**: 需要登录

### 11.6 删除面试
- **URL**: `DELETE /api/interview/{id}`
- **认证**: 需要登录（HR）

### 11.7 提交模拟面试
- **URL**: `POST /api/interview/mock`
- **认证**: 需要登录（求职者）
- **Content-Type**: `multipart/form-data`

**请求参数**:
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| positionTitle | String | 是 | 面试岗位名称 |
| video | File | 是 | 面试视频文件 |

### 11.8 生成模拟面试题
- **URL**: `GET /api/interview/mock/questions`
- **认证**: 需要登录（求职者）

**请求参数**:
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| positionTitle | String | 是 | 岗位名称 |

### 11.9 结束AI面试
- **URL**: `POST /api/interview/finish`
- **认证**: 需要登录（求职者）
- **Content-Type**: `multipart/form-data`

**请求参数**:
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| interviewId | Long | 是 | 面试ID |
| video | File | 是 | 面试视频文件 |

> AI面试评估为异步处理，提交后需轮询检查评估结果。

### 11.10 获取面试评估结果
- **URL**: `GET /api/interview/evaluation/{interviewId}`
- **认证**: 需要登录

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "interviewId": 1,
    "overallScore": 85,
    "languageScore": 80,
    "logicScore": 90,
    "professionalScore": 85,
    "evaluation": "面试表现良好，逻辑清晰...",
    "suggestions": "建议加强算法能力..."
  },
  "timestamp": 1714032000000
}
```

### 11.11 检查评估结果是否存在
- **URL**: `GET /api/interview/evaluation/{interviewId}/exists`
- **认证**: 需要登录

---

## 12. AI智能分析模块

### 12.1 分析简历（文本输入）
- **URL**: `POST /api/resume/analyze`
- **认证**: 需要登录（求职者）

**请求参数**:
```json
{
  "resumeText": "张三，3年Java开发经验..."
}
```

### 12.2 上传并分析简历
- **URL**: `POST /api/resume/upload-and-analyze`
- **认证**: 需要登录（求职者）
- **Content-Type**: `multipart/form-data`

**请求参数**:
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| file | File | 是 | 简历文件（支持pdf/doc/docx） |

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "name": "张三",
    "phone": "13800138000",
    "email": "zhangsan@example.com",
    "education": "本科",
    "skills": ["Java", "Spring Boot", "MySQL"],
    "workExperience": [...],
    "educationExperience": [...],
    "projectExperience": [...]
  },
  "timestamp": 1714032000000
}
```

### 12.3 智能填充简历
- **URL**: `POST /api/resume/smart-fill`
- **认证**: 需要登录（求职者）
- **Content-Type**: `multipart/form-data`

**请求参数**:
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| file | File | 是 | 简历文件 |

> AI解析简历后自动填充求职者表单，返回结构化的简历数据。

---

## 13. 岗位画像模块

### 13.1 AI生成岗位画像
- **URL**: `POST /api/job-profile/generate/{positionId}`
- **认证**: 需要登录（HR）

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "positionId": 1,
    "coreSkills": ["Java", "Spring Boot", "MySQL"],
    "educationRequirement": "本科及以上",
    "experienceRequirement": "3年以上",
    "skillTags": [
      {"name": "Java", "level": "核心", "weight": 5},
      {"name": "Spring Boot", "level": "核心", "weight": 4}
    ],
    "matchKeywords": ["后端开发", "微服务", "分布式"],
    "salaryRange": "20K-40K",
    "aiSummary": "该岗位需要扎实的Java基础..."
  },
  "timestamp": 1714032000000
}
```

### 13.2 获取岗位画像
- **URL**: `GET /api/job-profile/{positionId}`
- **认证**: 需要登录

### 13.3 更新岗位画像
- **URL**: `PUT /api/job-profile/update/{positionId}`
- **认证**: 需要登录（HR）

### 13.4 删除岗位画像
- **URL**: `DELETE /api/job-profile/{positionId}`
- **认证**: 需要登录（HR）

---

## 14. 人才画像模块

### 14.1 AI生成人才画像
- **URL**: `POST /api/talent-profile/generate`
- **认证**: 需要登录（求职者）

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "userId": 1,
    "skillEvaluation": "Java开发能力突出，熟悉主流框架",
    "advantages": ["扎实的Java基础", "丰富的项目经验"],
    "careerGoal": "高级Java工程师/架构师",
    "skillTags": [
      {"name": "Java", "level": "精通", "weight": 5},
      {"name": "Spring Boot", "level": "熟练", "weight": 4}
    ],
    "aiSummary": "该求职者具有3年Java开发经验..."
  },
  "timestamp": 1714032000000
}
```

### 14.2 获取人才画像
- **URL**: `GET /api/talent-profile`
- **认证**: 需要登录（求职者）

### 14.3 更新人才画像
- **URL**: `PUT /api/talent-profile/update`
- **认证**: 需要登录（求职者）

### 14.4 删除人才画像
- **URL**: `DELETE /api/talent-profile`
- **认证**: 需要登录（求职者）

---

## 15. 岗位推荐模块

### 15.1 获取岗位推荐列表
- **URL**: `GET /api/job-recommend`
- **认证**: 需要登录（求职者）

**请求参数**:
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | Integer | 否 | 页码 |
| size | Integer | 否 | 每页数量 |

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "records": [
      {
        "recordId": 1,
        "positionId": 1,
        "positionTitle": "高级Java工程师",
        "companyName": "字节跳动",
        "matchScore": 92.5,
        "skillMatchRate": 90.0,
        "experienceMatchRate": 80.0,
        "educationMatchRate": 100.0,
        "salaryMatchRate": 75.0,
        "matchDetails": {
          "matchedSkills": ["Java", "Spring Boot"],
          "missingSkills": ["Redis"],
          "matchDescription": "技能匹配度较高，经验要求满足"
        }
      }
    ],
    "total": 50,
    "size": 10,
    "current": 1
  },
  "timestamp": 1714032000000
}
```

### 15.2 获取匹配度详情
- **URL**: `GET /api/job-recommend/match/{positionId}`
- **认证**: 需要登录（求职者）

### 15.3 批量生成匹配记录
- **URL**: `POST /api/job-recommend/batch-generate`
- **认证**: 需要登录（求职者）

> 触发后台为当前求职者与所有招聘中岗位计算匹配度，生成推荐记录。

### 15.4 标记推荐已查看
- **URL**: `PUT /api/job-recommend/viewed/{recordId}`
- **认证**: 需要登录（求职者）

---

## 16. 人才推荐模块（牛人发现）

### 16.1 获取人才推荐列表
- **URL**: `GET /api/talent-recommend`
- **认证**: 需要登录（HR）

**请求参数**:
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| positionId | Long | 否 | 岗位ID（不传则使用HR最新的招聘中岗位） |
| limit | Integer | 否 | 推荐数量（默认10） |

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "jobSeekerId": 1,
      "userId": 1,
      "name": "张三",
      "avatarUrl": "https://oss.example.com/avatar/1.jpg",
      "workYears": 3,
      "education": "本科",
      "skills": "[\"Java\",\"Spring Boot\"]",
      "salaryExpectation": 20,
      "currentStatus": "离职-随时到岗",
      "expectedPosition": "Java高级工程师",
      "expectedCity": "北京",
      "selfIntroduction": "3年Java开发经验",
      "strengthsSummary": "扎实的Java基础，熟悉微服务架构",
      "matchScore": 88.5,
      "skillMatchRate": 90.0,
      "experienceMatchRate": 80.0,
      "educationMatchRate": 100.0,
      "salaryMatchRate": 75.0,
      "positionId": 1,
      "positionTitle": "高级Java工程师",
      "matchDetails": {
        "matchedSkills": ["Java", "Spring Boot"],
        "missingSkills": ["Redis"],
        "matchDescription": "技能匹配度较高，经验要求满足"
      }
    }
  ],
  "timestamp": 1714032000000
}
```

### 16.2 获取人才匹配详情
- **URL**: `GET /api/talent-recommend/match`
- **认证**: 需要登录（HR）

**请求参数**:
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| jobSeekerId | Long | 是 | 求职者用户ID |
| positionId | Long | 是 | 岗位ID |

### 16.3 批量生成人才匹配记录
- **URL**: `POST /api/talent-recommend/batch-generate`
- **认证**: 需要登录（HR）

**请求参数**:
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| positionId | Long | 是 | 岗位ID |

> 为当前HR的指定岗位与所有有人才画像的求职者计算匹配度，生成推荐记录。

### 16.4 标记人才推荐已查看
- **URL**: `PUT /api/talent-recommend/viewed/{recordId}`
- **认证**: 需要登录（HR）

---

## 17. 数据统计模块

### 17.1 求职者端统计数据
- **URL**: `GET /api/statistics/seeker`
- **认证**: 需要登录（求职者）

### 17.2 HR端统计数据
- **URL**: `GET /api/statistics/boss`
- **认证**: 需要登录（HR）

### 17.3 词云数据
- **URL**: `GET /api/statistics/wordcloud`
- **认证**: 需要登录

---

## 17. 文件上传模块

### 17.1 上传文件
- **URL**: `POST /api/file/upload`
- **认证**: 需要登录
- **Content-Type**: `multipart/form-data`

**请求参数**:
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| file | File | 是 | 文件 |
| fileType | String | 否 | 文件类型（avatar/resume/logo/license） |

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "url": "https://ai-recruitment.oss-cn-chengdu.aliyuncs.com/avatar/1.jpg",
    "fileName": "1.jpg"
  },
  "timestamp": 1714032000000
}
```

### 17.2 批量上传文件
- **URL**: `POST /api/file/upload-batch`
- **认证**: 需要登录
- **Content-Type**: `multipart/form-data`

### 17.3 删除文件
- **URL**: `DELETE /api/file/delete`
- **认证**: 需要登录

**请求参数**:
```json
{
  "url": "https://ai-recruitment.oss-cn-chengdu.aliyuncs.com/avatar/1.jpg"
}
```

---

## 匹配度计算说明

### 综合匹配分数公式
```
MatchScore = 技能匹配率 × 0.5 + 经验匹配率 × 0.3 + 学历匹配率 × 0.1 + 薪资匹配率 × 0.1
```

### 各维度计算方法
| 维度 | 权重 | 计算方法 |
|------|------|----------|
| 技能匹配 | 50% | 匹配技能数 / 岗位要求技能数 × 100% |
| 经验匹配 | 30% | 根据工作年限对比计算 |
| 学历匹配 | 10% | 根据学历等级对比计算 |
| 薪资匹配 | 10% | 期望薪资是否在岗位薪资范围内 |

---

## 数据字典

### 性别 (Gender)
| 值 | 说明 |
|----|------|
| 0 | 未知 |
| 1 | 男 |
| 2 | 女 |

### 学历 (EducationLevel)
| 值 | 说明 |
|----|------|
| 1 | 高中及以下 |
| 2 | 大专 |
| 3 | 本科 |
| 4 | 硕士 |
| 5 | 博士 |

### 当前状态 (CurrentStatus)
| 值 | 说明 |
|----|------|
| 1 | 在职-暂不考虑 |
| 2 | 离职-随时到岗 |
| 3 | 在职-考虑机会 |

### 企业规模 (CompanyScale)
| 值 | 说明 |
|----|------|
| 1 | 0-20人 |
| 2 | 20-99人 |
| 3 | 100-499人 |
| 4 | 500-999人 |
| 5 | 1000-9999人 |
| 6 | 10000人以上 |

### 融资阶段 (FinancingStage)
| 值 | 说明 |
|----|------|
| 1 | 未融资 |
| 2 | 天使轮 |
| 3 | A轮 |
| 4 | B轮 |
| 5 | C轮 |
| 6 | 已上市 |
| 7 | 不需要融资 |

### 投递状态 (ApplicationStatus)
| 值 | 说明 |
|----|------|
| 1 | 待查看 |
| 2 | 已查看 |
| 3 | 面试中 |
| 4 | 不合适 |
| 5 | 录用 |

### 通知类型 (NotificationType)
| 值 | 说明 |
|----|------|
| 1 | 新投递提醒 |
| 2 | 面试状态变更 |
| 3 | 系统公告 |

### 面试类型 (InterviewType)
| 值 | 说明 |
|----|------|
| 1 | 线下面试 |
| 2 | 线上面试 |
| 3 | AI面试 |

### 收藏类型 (FavoriteType)
| 值 | 说明 |
|----|------|
| 1 | 职位 |
| 2 | 公司 |
| 3 | 求职者 |
