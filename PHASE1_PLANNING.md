# 第一阶段（传统招聘平台）开发计划

## 目标概述
完成传统CRUD招聘平台，为第二阶段的AI增强奠定基础。第一阶段重点实现用户、简历、岗位、投递等核心业务模块。

## 已完成模块 ✅

### 1. 用户认证模块
- [x] 用户登录/注册（手机号+验证码）
- [x] 身份切换（求职者 ↔ 企业HR）
- [x] JWT认证机制
- [x] 验证码管理（Redis）

---

## 待开发模块 ⬜

### 2. 求职者模块（7个子模块）

#### 2.1 个人信息管理
**功能需求**：
- 查看个人信息
- 编辑基本信息（姓名、性别、年龄、学历等）
- 上传头像

**接口设计**：
```
GET    /api/jobseeker/profile      # 获取个人信息
PUT    /api/jobseeker/profile      # 更新个人信息
POST   /api/jobseeker/avatar      # 上传头像
GET    /api/jobseeker/avatar      # 获取头像
```

#### 2.2 简历管理
**功能需求**：
- 创建简历（手动填写）
- 编辑简历
- 删除简历
- 简历列表展示
- 简历详情查看
- 简历设置为默认

**接口设计**：
```
GET    /api/jobseeker/resumes         # 获取简历列表
POST   /api/jobseeker/resumes         # 创建简历
GET    /api/jobseeker/resumes/{id}    # 获取简历详情
PUT    /api/jobseeker/resumes/{id}    # 更新简历
DELETE  /api/jobseeker/resumes/{id}    # 删除简历
PUT    /api/jobseeker/resumes/{id}/set-default  # 设置默认简历
```

**简历信息结构**：
- 基本信息（姓名、联系方式）
- 教育经历
- 工作经验
- 技能标签
- 自我介绍

#### 2.3 职位浏览
**功能需求**：
- 职位列表展示（分页）
- 职位搜索（关键词、地点、薪资范围）
- 职位筛选（行业、全职/兼职）
- 职位详情查看

**接口设计**：
```
GET    /api/jobseeker/jobs?page=1&size=10                    # 职位列表（分页）
GET    /api/jobseeker/jobs/search?keyword=Java&city=北京     # 搜索职位
GET    /api/jobseeker/jobs/{id}                             # 职位详情
GET    /api/jobseeker/jobs/hot                               # 热门职位
```

#### 2.4 简历投递
**功能需求**：
- 投递简历给企业
- 查看投递记录
- 撤销投递
- 投递状态查看

**接口设计**：
```
POST   /api/jobseeker/applications          # 投递简历
GET    /api/jobseeker/applications          # 获取投递记录
GET    /api/jobseeker/applications/{id}     # 获取投递详情
DELETE  /api/jobseeker/applications/{id}     # 撤销投递
```

**投递状态流转**：
- 待处理 → 已查看 → 面试邀请 → 录用/拒绝

#### 2.5 面试管理
**功能需求**：
- 查看面试邀请
- 接受/拒绝面试邀请
- 查看面试记录

**接口设计**：
```
GET    /api/jobseeker/interviews           # 查看面试邀请
POST   /api/jobseeker/interviews/{id}/accept  # 接受面试
POST   /api/jobseeker/interviews/{id}/reject  # 拒绝面试
GET    /api/jobseeker/interviews/history      # 面试记录
```

#### 2.6 收藏管理
**功能需求**：
- 收藏职位
- 取消收藏
- 查看收藏列表

**接口设计**：
```
POST   /api/jobseeker/favorites/jobs/{jobId}      # 收藏职位
DELETE  /api/jobseeker/favorites/jobs/{jobId}      # 取消收藏
GET    /api/jobseeker/favorites                 # 收藏列表
```

#### 2.7 消息通知
**功能需求**：
- 查看系统通知
- 消息已读/未读状态
- 删除消息

**接口设计**：
```
GET    /api/jobseeker/notifications             # 获取通知列表
PUT    /api/jobseeker/notifications/{id}/read  # 标记已读
DELETE  /api/jobseeker/notifications/{id}      # 删除通知
```

---

### 3. 企业HR模块（6个子模块）

#### 3.1 企业信息管理
**功能需求**：
- 查看企业信息
- 编辑企业信息（公司名称、简介、规模、地址等）
- 上传企业Logo

**接口设计**：
```
GET    /api/enterprise/info         # 获取企业信息
PUT    /api/enterprise/info         # 更新企业信息
POST   /api/enterprise/logo        # 上传Logo
GET    /api/enterprise/logo        # 获取Logo
```

#### 3.2 岗位管理
**功能需求**：
- 发布岗位
- 编辑岗位
- 删除岗位
- 岗位列表展示
- 岗位详情查看
- 岗位上下架

**接口设计**：
```
GET    /api/enterprise/jobs                   # 获取岗位列表
POST   /api/enterprise/jobs                   # 发布岗位
GET    /api/enterprise/jobs/{id}              # 获取岗位详情
PUT    /api/enterprise/jobs/{id}              # 更新岗位
DELETE  /api/enterprise/jobs/{id}              # 删除岗位
PUT    /api/enterprise/jobs/{id}/status      # 岗位上下架
```

**岗位信息结构**：
- 基本信息（岗位名称、薪资范围、地点、学历要求）
- 岗位描述（岗位职责、任职要求）
- 福利待遇
- 招聘人数
- 岗位标签（技能要求）

#### 3.3 简历管理（接收投递）
**功能需求**：
- 查看投递列表
- 查看简历详情
- 标记投递状态（已查看、面试、录用、拒绝）

**接口设计**：
```
GET    /api/enterprise/applications          # 获取投递列表
GET    /api/enterprise/applications/{id}     # 获取投递详情（含简历）
PUT    /api/enterprise/applications/{id}/status  # 更新投递状态
```

**投递状态**：
- `VIEWED` - 已查看
- `INTERVIEW` - 面试中
- `HIRED` - 已录用
- `REJECTED` - 已拒绝

#### 3.4 面试管理
**功能需求**：
- 发送面试邀请
- 查看面试邀请记录
- 取消面试邀请

**接口设计**：
```
POST   /api/enterprise/interviews                 # 发送面试邀请
GET    /api/enterprise/interviews                 # 查看面试邀请
DELETE  /api/enterprise/interviews/{id}           # 取消面试邀请
PUT    /api/enterprise/interviews/{id}/complete    # 标记面试完成
```

#### 3.5 人才库（可选，为第二阶段AI匹配打基础）
**功能需求**：
- 查看求职者库
- 按条件筛选求职者
- 保存优秀求职者

**接口设计**：
```
GET    /api/enterprise/candidates            # 人才库列表
POST   /api/enterprise/candidates/{userId}/save  # 保存到人才库
DELETE  /api/enterprise/candidates/{userId}/remove  # 移出人才库
```

#### 3.6 数据统计
**功能需求**：
- 岗位发布统计
- 简历投递统计
- 面试统计
- 录用统计

**接口设计**：
```
GET    /api/enterprise/statistics/dashboard    # 仪表盘数据
GET    /api/enterprise/statistics/jobs         # 岗位统计
GET    /api/enterprise/statistics/applications # 投递统计
```

---

### 4. 公共模块（3个子模块）

#### 4.1 文件上传
**功能需求**：
- 头像上传
- 简历文件上传（PDF/Word）
- 企业Logo上传
- 文件大小限制
- 文件类型校验

**接口设计**：
```
POST   /api/common/upload/avatar          # 上传头像
POST   /api/common/upload/resume         # 上传简历
POST   /api/common/upload/logo           # 上传Logo
```

#### 4.2 数据字典
**功能需求**：
- 行业列表
- 岗位类型列表
- 薪资区间列表
- 学历要求列表

**接口设计**：
```
GET    /api/dict/industries      # 行业列表
GET    /api/dict/job-types        # 岗位类型
GET    /api/dict/salary-ranges    # 薪资区间
GET    /api/dict/educations       # 学历要求
```

#### 4.3 搜索与推荐（基础版，为第二阶段AI打基础）
**功能需求**：
- 关键词搜索
- 搜索历史记录
- 热门搜索

**接口设计**：
```
GET    /api/search/jobs?keyword=Java              # 搜索职位
GET    /api/search/history                        # 搜索历史
GET    /api/search/hot-keywords                  # 热门关键词
```

---

### 5. 权限与安全（2个子模块）

#### 5.1 JWT拦截器
**功能需求**：
- 校验JWT令牌
- 提取用户ID和角色
- 鉴权（区分求职者/企业HR接口）

**实现方案**：
- 自定义`JwtInterceptor`
- 拦截需要认证的接口
- 将用户信息存入ThreadLocal

#### 5.2 接口鉴权
**功能需求**：
- 求职者只能访问求职者接口
- 企业HR只能访问企业接口
- 防止越权访问

**实现方案**：
- 自定义注解`@RequireRole`
- 注解处理器判断角色
- 未授权返回403错误

---

## 数据库设计

### 表结构规划

#### 已有表
```
user (用户表)
```

#### 待建表

**1. jobseeker_profile（求职者信息表）**
```
id (主键)
user_id (外键，关联user表)
real_name (真实姓名)
gender (性别)
age (年龄)
education (学历)
work_years (工作年限)
avatar_url (头像URL)
city (所在城市)
introduce (自我介绍)
create_time
update_time
```

**2. enterprise_info（企业信息表）**
```
id (主键)
user_id (外键，关联user表)
company_name (公司名称)
industry (行业)
scale (公司规模)
address (公司地址)
logo_url (Logo URL)
description (公司简介)
website (官网)
create_time
update_time
```

**3. resume（简历表）**
```
id (主键)
user_id (外键，关联user表)
resume_name (简历名称)
real_name (姓名)
phone (手机号)
email (邮箱)
education (学历)
work_years (工作年限)
skills (技能标签，JSON格式)
work_experience (工作经历，JSON格式)
education_experience (教育经历，JSON格式)
introduce (自我介绍)
is_default (是否默认简历)
create_time
update_time
```

**4. job（岗位表）**
```
id (主键)
user_id (外键，关联enterprise_info表)
job_title (岗位名称)
job_type (岗位类型：全职/兼职/实习)
salary_min (最低薪资)
salary_max (最高薪资)
city (工作地点)
education_require (学历要求)
experience_require (经验要求)
job_description (岗位描述)
job_requirements (任职要求)
benefits (福利待遇)
recruit_count (招聘人数)
tags (岗位标签，JSON格式)
status (状态：上架/下架)
view_count (浏览次数)
apply_count (投递次数)
create_time
update_time
```

**5. job_application（投递记录表）**
```
id (主键)
job_id (外键，关联job表)
user_id (外键，关联user表)
resume_id (外键，关联resume表)
status (状态：待处理/已查看/面试/录用/拒绝)
status_time (状态更新时间)
remark (备注)
create_time
update_time
```

**6. interview（面试表）**
```
id (主键)
application_id (外键，关联job_application表)
interview_time (面试时间)
interview_address (面试地址)
interview_type (面试类型：线上/线下)
interviewer_name (面试官姓名)
interviewer_phone (面试官电话)
status (状态：待确认/已确认/已拒绝/已完成)
remark (备注)
create_time
update_time
```

**7. job_favorite（职位收藏表）**
```
id (主键)
job_id (外键，关联job表)
user_id (外键，关联user表)
create_time
```

**8. notification（通知表）**
```
id (主键)
user_id (外键，关联user表)
type (通知类型：投递/面试/系统)
title (标题)
content (内容)
is_read (是否已读)
create_time
```

**9. talent_pool（人才库表）**
```
id (主键)
enterprise_id (外键，关联enterprise_info表)
user_id (外键，关联user表)
resume_id (外键，关联resume表)
remark (备注)
create_time
```

**10. search_history（搜索历史表）**
```
id (主键)
user_id (外键，关联user表)
keyword (关键词)
create_time
```

---

## 开发优先级排序

### 高优先级（核心功能）
1. **个人/企业信息管理** - 基础用户信息
2. **简历管理** - 求职者核心功能
3. **岗位管理** - 企业HR核心功能
4. **简历投递** - 连接求职者和企业
5. **JWT拦截器** - 接口鉴权

### 中优先级（重要功能）
6. **职位浏览** - 求职者查看岗位
7. **投递管理** - 企业HR查看投递
8. **面试管理** - 面试流程
9. **文件上传** - 头像/简历上传

### 低优先级（增强功能）
10. **收藏管理** - 职位收藏
11. **消息通知** - 系统通知
12. **数据统计** - 数据分析
13. **人才库** - 企业人才储备
14. **数据字典** - 基础数据

---

## 第一阶段完成标准

### 功能完整性
- [x] 用户登录/注册
- [ ] 求职者功能完整（信息、简历、投递、面试）
- [ ] 企业HR功能完整（信息、岗位、投递、面试）
- [ ] JWT鉴权完善
- [ ] 文件上传功能

### 数据完整性
- [x] 用户数据
- [ ] 简历数据
- [ ] 岗位数据
- [ ] 投递数据
- [ ] 面试数据

### 接口文档
- [x] 登录模块接口文档
- [ ] 其他模块接口文档

### 测试验证
- [ ] 单元测试（可选）
- [ ] 接口联调测试
- [ ] 功能完整性测试

---

## 为第二阶段AI功能预留接口

### 简历智能解析预留
```
POST   /api/ai/resume/parse         # 解析简历文件
```

### 智能推荐预留
```
GET    /api/recommend/jobs               # 为求职者推荐岗位
GET    /api/recommend/candidates          # 为企业推荐候选人
```

### AI面试预留
```
POST   /api/ai/interview/start         # 开始AI面试
GET    /api/ai/interview/record        # 获取面试记录
GET    /api/ai/interview/eval          # 获取面试评估
```

### 匹配度评分预留
```
POST   /api/matching/score              # 计算匹配度
GET    /api/matching/ranking             # 候选人排名
```

---

## 开发建议

### 1. 模块化开发
每个模块独立开发、独立测试，最后联调

### 2. 接口先行
先定义接口和数据库表结构，再实现业务逻辑

### 3. 分阶段交付
- 第一批：信息管理 + 简历 + 岗位 + 投递
- 第二批：面试 + 收藏 + 通知 + 统计
- 第三批：文件上传 + 搜索推荐 + 数据字典

### 4. 代码复用
- 公共方法提取（如文件上传、JWT解析）
- 通用DTO复用

### 5. 日志完善
- 关键操作记录日志
- 异常情况记录详细日志

---

## 总结

**第一阶段总模块数**：17个
**预计开发周期**：4-6周
**接口数量**：约60-80个接口
**数据库表数**：10张表

**完成第一阶段后**：
- 实现完整的传统招聘平台功能
- 为第二阶段AI增强打下坚实基础
- 积累足够的数据用于AI模型训练
