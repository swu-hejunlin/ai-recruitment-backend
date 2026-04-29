# AI智能招聘平台 - 后端

## 项目简介
基于 Spring Boot 3 + JDK 17 的智能招聘与人才匹配平台后端系统，集成智谱GLM大模型，支持求职者和企业HR两种角色，提供简历智能分析、岗位画像、人才画像、AI面试评估、智能推荐等AI驱动功能。

## 技术栈
- **Spring Boot 3.1.5** + **JDK 17**
- **MyBatis Plus 3.5.3.1**（ORM框架）
- **MySQL 8.0+**（关系数据库）
- **Redis**（验证码存储、缓存）
- **JWT**（用户认证，2小时过期）
- **阿里云OSS**（文件存储）
- **智谱GLM大模型**（AI能力：简历分析、画像生成、面试评估、智能推荐）
- **Apache PDFBox 3.0.5**（PDF解析）
- **讯飞语音识别**（视频面试语音转文字）

## 项目结构
```
src/main/java/com/example/airecruitmentbackend/
├── common/          # 通用类（Result统一返回、AIConstant常量、FileConstants）
├── config/          # 配置类（CORS、Redis、OSS、MyBatisPlus、JWT、线程池、GLM客户端）
├── controller/      # 控制器层（18个Controller）
│   ├── UserController              # 用户认证（登录/验证码/角色切换）
│   ├── JobSeekerController         # 求职者信息管理
│   ├── EducationController         # 教育经历CRUD
│   ├── ExperienceController        # 工作/实习经历CRUD
│   ├── ProjectController           # 项目经历CRUD
│   ├── CompanyController           # 企业信息管理
│   ├── PositionController          # 职位管理（发布/搜索/详情）
│   ├── ApplicationController       # 投递管理
│   ├── NotificationController      # 通知模块
│   ├── FavoriteController          # 收藏模块
│   ├── InterviewController         # 面试管理（含AI面试）
│   ├── ResumeController            # 简历AI分析/智能填充
│   ├── JobProfileController        # 岗位画像
│   ├── TalentProfileController     # 人才画像
│   ├── JobRecommendController      # 岗位推荐
│   ├── StatisticsController        # 数据统计
│   ├── FileUploadController        # 文件上传
│   └── BaseController              # 基础控制器（获取当前用户信息）
├── dto/             # 数据传输对象（28个DTO）
├── entity/          # 实体类（17个Entity）
├── exception/       # 异常处理（GlobalExceptionHandler + 5种业务异常）
├── filter/          # 过滤器（JWT认证、请求追踪）
├── interceptor/     # 拦截器（JWT备用）
├── mapper/          # 数据访问层（17个Mapper）
├── service/         # 业务逻辑层
│   └── impl/        # 服务实现（17个Service + AIScoreService）
└── util/            # 工具类（JWT、OSS、PDF解析、语音转文字、文件校验等）
```

## 快速开始

### 1. 环境准备
- JDK 17+
- MySQL 8.0+
- Redis
- Maven

### 2. 数据库配置
创建数据库并执行SQL脚本：
```bash
mysql -u root -p < SQL_INIT.sql
mysql -u root -p < sql/init_profile_tables.sql
```

### 3. Redis配置
确保Redis服务运行在 `localhost:6379`，密码为 `123456`，数据库为 `2`

### 4. 配置文件
修改 `src/main/resources/application.yml` 中的数据库、Redis和OSS连接信息

### 5. 环境变量
设置智谱AI API Key：
```bash
export ZAI_API_KEY=your_api_key
```

设置阿里云OSS密钥：
```bash
export ALIBABA_CLOUD_ACCESS_KEY_ID=your_key_id
export ALIBABA_CLOUD_ACCESS_KEY_SECRET=your_key_secret
```

### 6. 启动项目
```bash
mvn spring-boot:run
```

## 功能模块

### 1. 用户认证模块
- 手机号+验证码登录，验证码有效期5分钟
- 自动注册新用户
- JWT令牌有效期2小时
- 支持求职者/HR角色切换

### 2. 求职者信息管理
- 个人简历CRUD（基本信息、头像、简历附件）
- 教育/工作/项目经历独立CRUD
- 技能标签动态管理

### 3. 企业信息管理
- 企业资料管理（名称、行业、规模、融资等）
- Logo和营业执照上传
- 福利待遇标签管理

### 4. 职位管理
- HR发布/编辑/删除职位
- 职位搜索筛选（关键词+城市+类别+学历+薪资）
- 职位状态管理（开启/关闭招聘）
- 最新/热门职位推荐

### 5. 投递与通知
- 求职者一键投递，防重复投递
- HR查看投递列表，按状态筛选
- 投递状态流转（待查看→已查看→面试中→不合适/录用）
- 系统通知（新投递提醒、状态变更）
- 收藏功能（职位/公司/求职者）

### 6. AI智能模块（核心亮点）
- **简历智能分析**：上传PDF简历，AI自动解析结构化信息
- **智能填充**：AI解析简历后自动填充求职者表单
- **岗位画像**：AI根据职位描述生成结构化岗位画像（技能标签、学历/经验要求、匹配关键词）
- **人才画像**：AI根据求职者信息生成人才画像（技能评估、优势亮点、职业目标）
- **智能推荐**：基于画像的多维度匹配（技能50%+经验30%+学历10%+薪资10%），计算综合匹配分
- **牛人发现**：HR端人才推荐，基于岗位画像与人才画像的智能匹配，向HR推荐最合适的候选人
- **AI评分**：投递时异步计算求职者与岗位的匹配度

### 7. 面试模块
- HR创建面试邀请（线下面试/线上面试/AI面试）
- AI模拟面试：自动生成面试题，视频录制，AI评估
- AI真实面试：基于实际面试邀请的AI视频面试
- 面试评估报告（语言表达/逻辑思维/专业能力评分）

### 8. 数据统计
- 求职者端：总职位数、投递数、面试数、热门城市/类别分布
- HR端：职位数、投递数、转化率、各职位投递情况
- 词云可视化数据

## 接口文档
详见 [API_DOCS.md](./API_DOCS.md)

## OSS使用指南
详见 [OSS_GUIDE.md](./OSS_GUIDE.md)

## 系统设计文档
详见 [docs/系统总体设计.md](./docs/系统总体设计.md)

## 统一返回格式
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {},
  "timestamp": 1714032000000
}
```
- `code: 200` 表示成功
- `code: 400` 业务错误
- `code: 401` 未授权
- `code: 403` 权限不足
- `code: 500` 服务器错误

## 注意事项
1. 验证码会打印在后端控制台，便于测试（后续可对接短信API）
2. JWT令牌需要在请求头中携带：`Authorization: Bearer {token}`
3. 文件上传需要配置阿里云OSS（AccessKey通过环境变量注入）
4. AI功能需要配置智谱GLM API Key（通过环境变量注入）
5. 身份切换需要重新发送验证码，确保本人操作
