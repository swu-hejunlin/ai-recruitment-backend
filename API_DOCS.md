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

### 2.1 获取求职者完整信息

- **接口地址**: `GET /api/job-seeker/info`
- **接口描述**: 获取当前求职者的完整信息（包含基本信息、教育经历、工作/实习经历、项目经历）
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
    "jobSeeker": {
      "id": 1,
      "userId": 1,
      "phone": "13800138000",
      "name": "张三",
      "gender": 1,
      "avatar": "https://xxx.oss.com/avatar/xxx.jpg",
      "email": "zhangsan@example.com",
      "age": 28,
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
    "educations": [
      {
        "id": 1,
        "jobSeekerId": 1,
        "schoolName": "北京大学",
        "major": "计算机科学与技术",
        "educationLevel": 4,
        "startDate": "2015-09-01",
        "endDate": "2019-06-30",
        "description": "主修课程：数据结构、算法、操作系统",
        "createTime": "2024-01-01T10:00:00",
        "updateTime": "2024-01-01T10:00:00"
      }
    ],
    "experiences": [
      {
        "id": 1,
        "jobSeekerId": 1,
        "companyName": "某某科技有限公司",
        "companyIndustry": "互联网",
        "position": "Java开发工程师",
        "startDate": "2022-01-01",
        "endDate": "2023-12-31",
        "description": "负责后端开发工作",
        "isInternship": 0,
        "createTime": "2024-01-01T10:00:00",
        "updateTime": "2024-01-01T10:00:00"
      }
    ],
    "projects": [
      {
        "id": 1,
        "jobSeekerId": 1,
        "projectName": "AI招聘系统",
        "projectRole": "技术负责人",
        "startDate": "2023-01-01",
        "endDate": "2023-06-30",
        "description": "设计与实现智能招聘系统",
        "createTime": "2024-01-01T10:00:00",
        "updateTime": "2024-01-01T10:00:00"
      }
    ]
  },
  "timestamp": 1710931200000
}
```

**字段说明**：

| 字段名 | 类型 | 说明 |
|--------|------|------|
| jobSeeker | Object | 求职者基本信息 |
| educations | Array | 教育经历列表 |
| experiences | Array | 工作/实习经历列表 |
| projects | Array | 项目经历列表 |

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
  "phone": "13800138000",
  "name": "张三",
  "gender": 1,
  "email": "zhangsan@example.com",
  "age": 28,
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
| phone | String | 否 | 手机号（与账号关联，默认使用注册手机号） |
| name | String | 是 | 姓名 |
| gender | Integer | 否 | 性别：0-未知，1-男，2-女 |
| email | String | 否 | 邮箱地址 |
| age | Integer | 否 | 年龄 |
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

### 2.5 查看头像

- **接口地址**: `GET /api/job-seeker/avatar`
- **接口描述**: 获取当前求职者的头像URL
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
  "message": "获取头像成功",
  "data": "https://xxx.oss.com/avatar/xxx.jpg",
  "timestamp": 1710931200000
}
```

**未上传头像时**：
```json
{
  "code": 200,
  "message": "获取头像成功",
  "data": null,
  "timestamp": 1710931200000
}
```

---

### 2.6 查看简历

- **接口地址**: `GET /api/job-seeker/resume`
- **接口描述**: 获取当前求职者的简历URL
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
  "message": "获取简历成功",
  "data": "https://xxx.oss.com/resume/xxx.pdf",
  "timestamp": 1710931200000
}
```

**未上传简历时**：
```json
{
  "code": 200,
  "message": "获取简历成功",
  "data": null,
  "timestamp": 1710931200000
}
```

---

## 三、教育经历模块

### 3.1 获取教育经历列表

- **接口地址**: `GET /api/education/list`
- **接口描述**: 获取当前用户的所有教育经历
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
  "message": "获取教育经历成功",
  "data": [
    {
      "id": 1,
      "jobSeekerId": 1,
      "schoolName": "北京大学",
      "major": "计算机科学与技术",
      "educationLevel": 4,
      "startDate": "2015-09-01",
      "endDate": "2019-06-30",
      "description": "主修课程：数据结构、算法、操作系统",
      "createTime": "2024-01-01T10:00:00",
      "updateTime": "2024-01-01T10:00:00"
    }
  ],
  "timestamp": 1710931200000
}
```

**字段说明**：

| 字段名 | 类型 | 说明 |
|--------|------|------|
| schoolName | String | 学校名称 |
| major | String | 专业 |
| educationLevel | Integer | 学历：1-高中及以下，2-大专，3-本科，4-硕士，5-博士 |
| startDate | Date | 入学时间 |
| endDate | Date | 毕业时间 |
| description | String | 在校表现/主要课程描述 |

---

### 3.2 新增教育经历

- **接口地址**: `POST /api/education/add`
- **接口描述**: 新增一条教育经历
- **认证**: 需要

#### 请求参数

**Headers**：
```
Authorization: Bearer {token}
Content-Type: application/json
```

**Body（JSON）**：
```json
{
  "schoolName": "北京大学",
  "major": "计算机科学与技术",
  "educationLevel": 4,
  "startDate": "2015-09",
  "endDate": "2019-06",
  "description": "主修课程：数据结构、算法、操作系统"
}
```

**参数说明**：

| 参数名 | 类型 | 必填 | 说明 | 校验规则 |
|--------|------|------|------|----------|
| schoolName | String | 是 | 学校名称 | 最大100字符 |
| major | String | 否 | 专业 | 最大100字符 |
| educationLevel | Integer | 是 | 学历 | 1-5 |
| startDate | String | 否 | 入学时间 | 格式：yyyy-MM，后端自动补全为每月1号 |
| endDate | String | 否 | 毕业时间 | 格式：yyyy-MM，后端自动补全为每月1号 |
| description | String | 否 | 在校表现描述 | 最大2000字符 |

#### 返回参数

**成功响应**：
```json
{
  "code": 200,
  "message": "新增教育经历成功",
  "data": null,
  "timestamp": 1710931200000
}
```

---

### 3.3 更新教育经历

- **接口地址**: `PUT /api/education/update`
- **接口描述**: 更新指定的教育经历
- **认证**: 需要

#### 请求参数

**Headers**：
```
Authorization: Bearer {token}
Content-Type: application/json
```

**Body（JSON）**：
```json
{
  "id": 1,
  "schoolName": "清华大学",
  "major": "软件工程",
  "educationLevel": 4,
  "startDate": "2015-09",
  "endDate": "2019-06",
  "description": "主修课程：软件工程、编译原理"
}
```

#### 返回参数

**成功响应**：
```json
{
  "code": 200,
  "message": "更新教育经历成功",
  "data": null,
  "timestamp": 1710931200000
}
```

---

### 3.4 删除教育经历

- **接口地址**: `DELETE /api/education/delete?id=1`
- **接口描述**: 删除指定的教育经历
- **认证**: 需要

#### 请求参数

**Headers**：
```
Authorization: Bearer {token}
```

**Query参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 教育经历ID |

#### 返回参数

**成功响应**：
```json
{
  "code": 200,
  "message": "删除教育经历成功",
  "data": null,
  "timestamp": 1710931200000
}
```

---

## 四、职位模块

### 4.1 发布职位

- **接口地址**: `POST /api/position/add`
- **接口描述**: 发布一个新职位
- **认证**: 需要（仅Boss角色）

#### 请求参数

**Headers**：
```
Authorization: Bearer {token}
Content-Type: application/json
```

**Body（JSON）**：
```json
{
  "companyId": 1,
  "title": "Java高级工程师",
  "category": "后端开发",
  "city": "北京",
  "address": "朝阳区建国路88号",
  "salaryMin": 20,
  "salaryMax": 40,
  "educationMin": 3,
  "workYearsMin": 3,
  "description": "1. 负责公司核心业务系统开发\n2. 参与技术方案设计与评审\n3. 指导初级工程师",
  "requirement": "1. 本科及以上学历，计算机相关专业\n2. 5年以上Java开发经验\n3. 熟悉Spring Boot、MySQL",
  "tags": "[\"五险一金\",\"弹性工作\",\"年终奖\"]"
}
```

**参数说明**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| companyId | Long | 是 | 企业ID |
| title | String | 是 | 职位名称 |
| category | String | 是 | 职位类别 |
| city | String | 是 | 工作城市 |
| address | String | 否 | 详细工作地址 |
| salaryMin | Integer | 否 | 最低薪资（K） |
| salaryMax | Integer | 否 | 最高薪资（K），必须大于salaryMin |
| educationMin | Integer | 否 | 最低学历：1-5 |
| workYearsMin | Integer | 否 | 最低工作年限 |
| description | String | 是 | 岗位职责 |
| requirement | String | 是 | 任职要求 |
| tags | String | 否 | 福利标签（JSON数组） |

#### 返回参数

**成功响应**：
```json
{
  "code": 200,
  "message": "发布职位成功",
  "data": null,
  "timestamp": 1710931200000
}
```

---

### 4.2 更新职位

- **接口地址**: `PUT /api/position/update`
- **接口描述**: 更新指定职位信息
- **认证**: 需要（仅Boss角色，只能修改本人发布的职位）

#### 请求参数

**Headers**：
```
Authorization: Bearer {token}
Content-Type: application/json
```

**Body（JSON）**：
```json
{
  "id": 1,
  "companyId": 1,
  "title": "Java技术专家",
  "category": "后端开发",
  "city": "北京",
  "salaryMin": 30,
  "salaryMax": 50,
  "educationMin": 4,
  "workYearsMin": 5,
  "description": "岗位职责更新内容",
  "requirement": "任职要求更新内容",
  "tags": "[\"六险一金\",\"股票期权\"]"
}
```

#### 返回参数

**成功响应**：
```json
{
  "code": 200,
  "message": "更新职位成功",
  "data": null,
  "timestamp": 1710931200000
}
```

---

### 4.3 删除职位

- **接口地址**: `DELETE /api/position/delete?id=1`
- **接口描述**: 删除指定职位
- **认证**: 需要（仅Boss角色，只能删除本人发布的职位）

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 职位ID |

#### 返回参数

**成功响应**：
```json
{
  "code": 200,
  "message": "删除职位成功",
  "data": null,
  "timestamp": 1710931200000
}
```

---

### 4.4 查询职位详情

- **接口地址**: `GET /api/position/detail?id=1`
- **接口描述**: 查询指定职位的详细信息（含企业信息）
- **认证**: 需要

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 职位ID |

#### 返回参数

**成功响应**：
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "id": 1,
    "companyId": 1,
    "bossId": 2,
    "title": "Java高级工程师",
    "category": "后端开发",
    "city": "北京",
    "address": "朝阳区建国路88号",
    "salaryMin": 20,
    "salaryMax": 40,
    "educationMin": 3,
    "workYearsMin": 3,
    "description": "岗位职责...",
    "requirement": "任职要求...",
    "status": 1,
    "tags": "[\"五险一金\",\"弹性工作\"]",
    "createTime": "2024-01-01T10:00:00",
    "updateTime": "2024-01-01T10:00:00",
    "companyLogo": "https://xxx.oss.com/logo/xxx.png",
    "companyName": "某某科技有限公司"
  },
  "timestamp": 1710931200000
}
```

---

### 4.5 Boss查询自己发布的职位

- **接口地址**: `GET /api/position/boss/list`
- **接口描述**: Boss查询自己发布的所有职位（含已关闭）
- **认证**: 需要（仅Boss角色）

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| pageNum | Integer | 否 | 页码，默认1 |
| pageSize | Integer | 否 | 每页数量，默认10 |

#### 返回参数

**成功响应**：
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "records": [
      {
        "id": 1,
        "title": "Java高级工程师",
        "category": "后端开发",
        "city": "北京",
        "salaryMin": 20,
        "salaryMax": 40,
        "status": 1,
        "createTime": "2024-01-01T10:00:00"
      }
    ],
    "total": 20,
    "size": 10,
    "current": 1,
    "pages": 2
  },
  "timestamp": 1710931200000
}
```

---

### 4.6 求职者查询职位列表

- **接口地址**: `GET /api/position/list`
- **接口描述**: 求职者查询招聘中的职位列表，支持筛选
- **认证**: 需要

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| title | String | 否 | 职位名称（模糊搜索） |
| city | String | 否 | 工作城市（精确匹配） |
| category | String | 否 | 职位类别（精确匹配） |
| pageNum | Integer | 否 | 页码，默认1 |
| pageSize | Integer | 否 | 每页数量，默认10 |

#### 返回参数

**成功响应**：
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "records": [
      {
        "id": 1,
        "companyId": 1,
        "title": "Java高级工程师",
        "category": "后端开发",
        "city": "北京",
        "salaryMin": 20,
        "salaryMax": 40,
        "educationMin": 3,
        "workYearsMin": 3,
        "status": 1,
        "tags": "[\"五险一金\",\"弹性工作\"]",
        "companyLogo": "https://xxx.oss.com/logo/xxx.png",
        "companyName": "某某科技有限公司"
      }
    ],
    "total": 100,
    "size": 10,
    "current": 1
  },
  "timestamp": 1710931200000
}
```

---

### 4.7 关闭职位

- **接口地址**: `PUT /api/position/close?id=1`
- **接口描述**: 关闭指定职位（停止招聘）
- **认证**: 需要（仅Boss角色）

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 职位ID |

#### 返回参数

**成功响应**：
```json
{
  "code": 200,
  "message": "关闭职位成功",
  "data": null,
  "timestamp": 1710931200000
}
```

---

### 4.8 开启职位

- **接口地址**: `PUT /api/position/open?id=1`
- **接口描述**: 重新开启已关闭的职位
- **认证**: 需要（仅Boss角色）

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 职位ID |

#### 返回参数

**成功响应**：
```json
{
  "code": 200,
  "message": "开启职位成功",
  "data": null,
  "timestamp": 1710931200000
}
```

---

### 4.9 根据公司查询职位

- **接口地址**: `GET /api/position/company?companyId=1`
- **接口描述**: 查询指定公司的所有招聘中职位
- **认证**: 需要

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| companyId | Long | 是 | 企业ID |

#### 返回参数

**成功响应**：
```json
{
  "code": 200,
  "message": "查询成功",
  "data": [
    {
      "id": 1,
      "title": "Java高级工程师",
      "category": "后端开发",
      "city": "北京",
      "salaryMin": 20,
      "salaryMax": 40,
      "status": 1,
      "tags": "[\"五险一金\"]"
    }
  ],
  "timestamp": 1710931200000
}
```

---

### 4.10 查看职位对应的公司信息

- **接口地址**: `GET /api/position/company-info?positionId=1`
- **接口描述**: 求职者查看指定职位的公司详细信息
- **认证**: 需要

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| positionId | Long | 是 | 职位ID |

#### 返回参数

**成功响应**：
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "id": 1,
    "companyName": "某某科技有限公司",
    "logo": "https://xxx.oss.com/logo/xxx.png",
    "industry": "互联网",
    "scale": 3,
    "financingStage": 4,
    "city": "北京",
    "address": "朝阳区建国路88号",
    "description": "企业简介..."
  },
  "timestamp": 1710931200000
}
```

---

## 六、经历模块

### 6.1 获取工作/实习经历列表

- **接口地址**: `GET /api/experience/list`
- **接口描述**: 获取当前用户的所有工作/实习经历
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
  "message": "获取工作/实习经历成功",
  "data": [
    {
      "id": 1,
      "jobSeekerId": 1,
      "companyName": "某某科技有限公司",
      "companyIndustry": "互联网",
      "position": "Java开发工程师",
      "startDate": "2022-01-01",
      "endDate": "2023-12-31",
      "description": "负责后端开发工作",
      "isInternship": 0,
      "createTime": "2024-01-01T10:00:00",
      "updateTime": "2024-01-01T10:00:00"
    }
  ],
  "timestamp": 1710931200000
}
```

---

### 3.2 新增工作/实习经历

- **接口地址**: `POST /api/experience/add`
- **接口描述**: 新增一条工作或实习经历
- **认证**: 需要

#### 请求参数

**Headers**：
```
Authorization: Bearer {token}
Content-Type: application/json
```

**Body（JSON）**：
```json
{
  "companyName": "某某科技有限公司",
  "companyIndustry": "互联网",
  "position": "Java开发工程师",
  "startDate": "2022-01-01",
  "endDate": "2023-12-31",
  "description": "负责后端开发工作",
  "isInternship": 0
}
```

**参数说明**：

| 参数名 | 类型 | 必填 | 说明 | 校验规则 |
|--------|------|------|------|----------|
| companyName | String | 是 | 公司名称 | 最大100字符 |
| companyIndustry | String | 否 | 公司所属行业 | 最大50字符 |
| position | String | 是 | 职位 | 最大100字符 |
| startDate | Date | 是 | 开始时间 | 格式：yyyy-MM-dd |
| endDate | Date | 否 | 结束时间 | 格式：yyyy-MM-dd，空表示至今 |
| description | String | 否 | 工作内容描述 | 最大2000字符 |
| isInternship | Integer | 是 | 是否为实习 | 0-否，1-是 |

#### 返回参数

**成功响应**：
```json
{
  "code": 200,
  "message": "新增工作/实习经历成功",
  "data": null,
  "timestamp": 1710931200000
}
```

---

### 3.3 更新工作/实习经历

- **接口地址**: `PUT /api/experience/update`
- **接口描述**: 更新指定的工作或实习经历
- **认证**: 需要

#### 请求参数

**Headers**：
```
Authorization: Bearer {token}
Content-Type: application/json
```

**Body（JSON）**：
```json
{
  "id": 1,
  "companyName": "某某科技有限公司",
  "companyIndustry": "互联网",
  "position": "Java高级工程师",
  "startDate": "2022-01-01",
  "endDate": "2023-12-31",
  "description": "负责后端架构设计",
  "isInternship": 0
}
```

**参数说明**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 经历ID |
| companyName | String | 否 | 公司名称 |
| companyIndustry | String | 否 | 公司所属行业 |
| position | String | 否 | 职位 |
| startDate | Date | 否 | 开始时间 |
| endDate | Date | 否 | 结束时间 |
| description | String | 否 | 工作内容描述 |
| isInternship | Integer | 否 | 是否为实习 |

#### 返回参数

**成功响应**：
```json
{
  "code": 200,
  "message": "更新工作/实习经历成功",
  "data": null,
  "timestamp": 1710931200000
}
```

---

### 3.4 删除工作/实习经历

- **接口地址**: `DELETE /api/experience/delete?id=1`
- **接口描述**: 删除指定的工作或实习经历
- **认证**: 需要

#### 请求参数

**Headers**：
```
Authorization: Bearer {token}
```

**Query参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 经历ID |

#### 返回参数

**成功响应**：
```json
{
  "code": 200,
  "message": "删除工作/实习经历成功",
  "data": null,
  "timestamp": 1710931200000
}
```

---

## 七、项目模块

### 7.1 获取项目经历列表

- **接口地址**: `GET /api/project/list`
- **接口描述**: 获取当前用户的所有项目经历
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
  "message": "获取项目经历成功",
  "data": [
    {
      "id": 1,
      "jobSeekerId": 1,
      "projectName": "AI招聘系统",
      "projectRole": "技术负责人",
      "startDate": "2023-01-01",
      "endDate": "2023-06-30",
      "description": "设计与实现智能招聘系统",
      "createTime": "2024-01-01T10:00:00",
      "updateTime": "2024-01-01T10:00:00"
    }
  ],
  "timestamp": 1710931200000
}
```

---

### 4.2 新增项目经历

- **接口地址**: `POST /api/project/add`
- **接口描述**: 新增一个项目经历
- **认证**: 需要

#### 请求参数

**Headers**：
```
Authorization: Bearer {token}
Content-Type: application/json
```

**Body（JSON）**：
```json
{
  "projectName": "AI招聘系统",
  "projectRole": "技术负责人",
  "startDate": "2023-01-01",
  "endDate": "2023-06-30",
  "description": "设计与实现智能招聘系统"
}
```

**参数说明**：

| 参数名 | 类型 | 必填 | 说明 | 校验规则 |
|--------|------|------|------|----------|
| projectName | String | 是 | 项目名称 | 最大100字符 |
| projectRole | String | 否 | 项目角色 | 最大100字符 |
| startDate | Date | 是 | 项目开始时间 | 格式：yyyy-MM-dd |
| endDate | Date | 否 | 项目结束时间 | 格式：yyyy-MM-dd |
| description | String | 否 | 项目描述 | 最大2000字符 |

#### 返回参数

**成功响应**：
```json
{
  "code": 200,
  "message": "新增项目经历成功",
  "data": null,
  "timestamp": 1710931200000
}
```

---

### 4.3 更新项目经历

- **接口地址**: `PUT /api/project/update`
- **接口描述**: 更新指定的项目经历
- **认证**: 需要

#### 请求参数

**Headers**：
```
Authorization: Bearer {token}
Content-Type: application/json
```

**Body（JSON）**：
```json
{
  "id": 1,
  "projectName": "AI招聘系统v2.0",
  "projectRole": "技术负责人",
  "startDate": "2023-01-01",
  "endDate": "2023-08-31",
  "description": "升级智能招聘系统功能"
}
```

#### 返回参数

**成功响应**：
```json
{
  "code": 200,
  "message": "更新项目经历成功",
  "data": null,
  "timestamp": 1710931200000
}
```

---

### 4.4 删除项目经历

- **接口地址**: `DELETE /api/project/delete?id=1`
- **接口描述**: 删除指定的项目经历
- **认证**: 需要

#### 请求参数

**Headers**：
```
Authorization: Bearer {token}
```

**Query参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 项目ID |

#### 返回参数

**成功响应**：
```json
{
  "code": 200,
  "message": "删除项目经历成功",
  "data": null,
  "timestamp": 1710931200000
}
```

---

## 八、文件上传模块

### 8.1 获取企业信息

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

## 六、文件上传模块

### 6.1 上传文件

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

## 九、状态码说明

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

## 十、接口调用示例

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

## 十一、投递模块

### 11.1 投递简历

- **接口地址**: `POST /api/application/apply`
- **接口描述**: 求职者投递简历到指定职位
- **认证**: 需要（仅求职者角色）

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| positionId | Long | 是 | 职位ID |

#### 返回参数

**成功响应**：
```json
{
  "code": 200,
  "message": "投递成功",
  "data": null,
  "timestamp": 1710931200000
}
```

**错误响应**：
- 该职位已停止招聘
- 您已投递过该职位

---

### 11.2 Boss查询收到的投递列表

- **接口地址**: `GET /api/application/boss/list`
- **接口描述**: Boss查询自己收到的所有投递（含各种状态）
- **认证**: 需要（仅Boss角色）

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| status | Integer | 否 | 投递状态筛选：1-待查看，2-已查看，3-面试中，4-不合适，5-录用 |
| pageNum | Integer | 否 | 页码，默认1 |
| pageSize | Integer | 否 | 每页数量，默认10 |

#### 返回参数

**成功响应**：
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "records": [
      {
        "id": 1,
        "jobSeekerId": 1,
        "positionId": 1,
        "companyId": 1,
        "bossId": 2,
        "status": 1,
        "aiScore": null,
        "createTime": "2024-01-01T10:00:00",
        "updateTime": "2024-01-01T10:00:00"
      }
    ],
    "total": 10,
    "size": 10,
    "current": 1
  },
  "timestamp": 1710931200000
}
```

---

### 11.3 求职者查询投递列表

- **接口地址**: `GET /api/application/seeker/list`
- **接口描述**: 求职者查询自己的投递记录
- **认证**: 需要（仅求职者角色）

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| pageNum | Integer | 否 | 页码，默认1 |
| pageSize | Integer | 否 | 每页数量，默认10 |

#### 返回参数

**成功响应**：
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "records": [
      {
        "id": 1,
        "jobSeekerId": 1,
        "positionId": 1,
        "companyId": 1,
        "bossId": 2,
        "status": 1,
        "createTime": "2024-01-01T10:00:00"
      }
    ],
    "total": 5,
    "size": 10,
    "current": 1
  },
  "timestamp": 1710931200000
}
```

---

### 11.4 查看投递详情（含职位、公司信息）

- **接口地址**: `GET /api/application/detail?id=1`
- **接口描述**: 查看投递详情（包含投递记录、职位信息、公司信息），Boss查看时自动标记为已查看
- **认证**: 需要

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 投递记录ID |

#### 返回参数

**成功响应**：
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "application": {
      "id": 1,
      "jobSeekerId": 1,
      "positionId": 1,
      "companyId": 1,
      "bossId": 2,
      "status": 2,
      "createTime": "2024-01-01T10:00:00",
      "updateTime": "2024-01-01T10:30:00"
    },
    "position": {
      "id": 1,
      "title": "Java高级工程师",
      "category": "后端开发",
      "city": "北京",
      "salaryMin": 20,
      "salaryMax": 40
    },
    "company": {
      "id": 1,
      "companyName": "某某科技有限公司",
      "logo": "https://xxx.oss.com/logo/xxx.png",
      "industry": "互联网"
    }
  },
  "timestamp": 1710931200000
}
```

---

### 11.5 根据投递记录查看职位信息

- **接口地址**: `GET /api/application/position?id=1`
- **接口描述**: 通过投递记录查看对应的职位详细信息
- **认证**: 需要

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 投递记录ID |

#### 返回参数

**成功响应**：
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "id": 1,
    "title": "Java高级工程师",
    "category": "后端开发",
    "city": "北京",
    "salaryMin": 20,
    "salaryMax": 40,
    "description": "岗位职责...",
    "requirement": "任职要求..."
  },
  "timestamp": 1710931200000
}
```

---

### 11.6 根据投递记录查看公司信息

- **接口地址**: `GET /api/application/company?id=1`
- **接口描述**: 通过投递记录查看对应的公司详细信息
- **认证**: 需要

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 投递记录ID |

#### 返回参数

**成功响应**：
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "id": 1,
    "companyName": "某某科技有限公司",
    "logo": "https://xxx.oss.com/logo/xxx.png",
    "industry": "互联网",
    "scale": 3,
    "city": "北京",
    "description": "公司简介..."
  },
  "timestamp": 1710931200000
}
```

---

### 11.7 Boss查看求职者信息（简历）

- **接口地址**: `GET /api/application/job-seeker?id=1`
- **接口描述**: Boss查看投递者对应的求职者信息（含简历URL）
- **认证**: 需要（仅Boss角色）

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 投递记录ID |

#### 返回参数

**成功响应**：
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "id": 1,
    "name": "张三",
    "gender": 1,
    "avatar": "https://xxx.oss.com/avatar/xxx.jpg",
    "phone": "13800138000",
    "email": "zhangsan@example.com",
    "age": 28,
    "workYears": 5,
    "city": "北京",
    "introduction": "热爱技术，善于学习",
    "skills": "[\"Java\",\"Spring\",\"MySQL\"]",
    "resumeUrl": "https://xxx.oss.com/resume/xxx.pdf",
    "createTime": "2024-01-01T10:00:00"
  },
  "timestamp": 1710931200000
}
```

---

### 11.8 标记简历为已查看

- **接口地址**: `PUT /api/application/read?id=1`
- **接口描述**: Boss标记简历为已查看
- **认证**: 需要（仅Boss角色）

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 投递记录ID |

#### 返回参数

**成功响应**：
```json
{
  "code": 200,
  "message": "已查看",
  "data": null,
  "timestamp": 1710931200000
}
```

---

### 11.9 更新投递状态

- **接口地址**: `PUT /api/application/status?id=1&status=3`
- **接口描述**: Boss更新投递状态，并通知求职者
- **认证**: 需要（仅Boss角色）

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 投递记录ID |
| status | Integer | 是 | 新状态：2-已查看，3-面试中，4-不合适，5-录用 |

#### 返回参数

**成功响应**：
```json
{
  "code": 200,
  "message": "更新成功",
  "data": null,
  "timestamp": 1710931200000
}
```

---

## 十二、通知模块

### 12.1 获取通知列表

- **接口地址**: `GET /api/notification/list`
- **接口描述**: 获取当前用户的通知列表
- **认证**: 需要

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| pageNum | Integer | 否 | 页码，默认1 |
| pageSize | Integer | 否 | 每页数量，默认20 |

#### 返回参数

**成功响应**：
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "records": [
      {
        "id": 1,
        "receiverId": 2,
        "type": 1,
        "title": "新投递提醒",
        "content": "您收到了来自 张三 对 Java工程师 的新投递",
        "businessId": 1,
        "isRead": 0,
        "createTime": "2024-01-01T10:00:00"
      }
    ],
    "total": 5,
    "size": 20,
    "current": 1
  },
  "timestamp": 1710931200000
}
```

**通知类型说明**：
- `1`: 新投递提醒
- `2`: 面试状态变更
- `3`: 系统公告

---

### 12.2 获取未读通知数量

- **接口地址**: `GET /api/notification/unread-count`
- **接口描述**: 获取当前用户未读通知数量（用于小红点）
- **认证**: 需要

#### 返回参数

**成功响应**：
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "count": 3
  },
  "timestamp": 1710931200000
}
```

---

### 12.3 标记通知为已读

- **接口地址**: `PUT /api/notification/read?id=1`
- **接口描述**: 将单条通知标记为已读
- **认证**: 需要

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 通知ID |

#### 返回参数

**成功响应**：
```json
{
  "code": 200,
  "message": "已标记为已读",
  "data": null,
  "timestamp": 1710931200000
}
```

---

### 12.4 标记所有通知为已读

- **接口地址**: `PUT /api/notification/read-all`
- **接口描述**: 将所有通知标记为已读
- **认证**: 需要

#### 返回参数

**成功响应**：
```json
{
  "code": 200,
  "message": "已全部标记为已读",
  "data": null,
  "timestamp": 1710931200000
}
```

---

## 十三、注意事项

1. **验证码有效期**：验证码有效期为5分钟，超过时间需要重新获取
2. **JWT令牌**：除登录相关接口外，其他接口都需要在请求头中携带JWT令牌
3. **文件上传**：上传文件前需要先调用文件上传接口获取URL，然后将URL传给对应的业务接口
4. **角色权限**：求职者只能访问 `/api/job-seeker/*` 和 `/api/file/*` 接口，企业HR只能访问 `/api/company/*` 和 `/api/file/*` 接口
5. **自动创建**：首次登录时，系统会自动创建用户和相关业务信息（如job_seeker或company记录）
