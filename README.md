# 智能招聘平台 - 后端

## 项目简介
基于Spring Boot 3.x的智能招聘与人才匹配平台后端系统，支持求职者和企业HR两种角色，提供验证码登录、身份切换、简历/企业信息管理、文件上传等功能。

## 技术栈
- **Spring Boot 3.1.5**
- **JDK 17**
- **MyBatis Plus 3.5.3.1**
- **MySQL 8.0+**
- **Redis**（验证码存储）
- **JWT**（用户认证）
- **阿里云OSS**（文件存储）

## 项目结构
```
src/main/java/com/example/airecruitmentbackend/
├── common/          # 通用类（统一返回结果、常量）
├── config/          # 配置类（Redis、JWT、跨域、MyBatis Plus、OSS）
├── controller/      # 控制器层（接口定义）
├── dto/            # 数据传输对象
├── entity/         # 实体类
├── exception/     # 异常处理
├── mapper/         # 数据访问层
├── service/        # 业务逻辑层
│   └── impl/       # 服务实现类
└── util/           # 工具类
```

## 快速开始

### 1. 数据库配置
创建数据库并执行SQL脚本：
```bash
mysql -u root -p < SQL_INIT.sql
```

### 2. Redis配置
确保Redis服务运行在 `localhost:6379`，密码为 `123456`，数据库为 `2`

### 3. 配置文件
修改 `src/main/resources/application.yml` 中的数据库、Redis和OSS连接信息

### 4. 启动项目
```bash
mvn spring-boot:run
```

## 核心功能

### 1. 验证码登录
- 支持手机号+验证码登录
- 验证码有效期5分钟
- 自动注册新用户
- JWT令牌有效期2小时

### 2. 身份切换
- 同一手机号只能有一个账号
- 切换角色需要重新验证码
- 确保本人操作，防止冒用

### 3. 用户角色
- **求职者（role=1）**：完善个人信息、上传头像和简历
- **企业HR（role=2）**：完善企业信息、上传logo和营业执照

### 4. 文件上传
- 支持头像、简历、企业logo、营业执照上传
- 阿里云OSS存储
- 支持批量上传

## 接口文档
详见 [API_DOCS.md](./API_DOCS.md)

## 开发计划

### 第一阶段（已完成）
- [x] 用户登录/注册模块
- [x] 验证码登录
- [x] 身份切换功能
- [x] JWT认证
- [x] 求职者信息管理
- [x] 企业信息管理
- [x] 文件上传（阿里云OSS）

### 第二阶段（待开发）
- [ ] 岗位管理模块
- [ ] 简历投递模块
- [ ] 简历智能解析（AI）
- [ ] 岗位-简历智能匹配（AI）
- [ ] AI文字面试

## 配置说明

### application.yml
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/ai_recruitment
    username: root
    password: root
  redis:
    host: localhost
    port: 6379
    password: 123456
    database: 2

jwt:
  secret: ai-recruitment-jwt-secret-key-2024-very-long-key-for-security
  expiration: 7200000  # 2小时（毫秒）
```

## 注意事项
1. 验证码会打印在后端控制台，便于测试（后续可对接短信API）
2. JWT令牌需要在请求头中携带：`Authorization: Bearer {token}`
3. 身份切换需要重新发送验证码，确保本人操作
4. 文件上传需要配置阿里云OSS
