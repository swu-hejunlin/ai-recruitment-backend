# 项目结构说明

## 当前项目状态

### 已完成模块
✅ 登录/注册模块（验证码登录）
✅ 身份切换功能
✅ 用户管理（求职者、企业HR）
✅ JWT认证
✅ Redis缓存（验证码存储）
✅ 统一返回格式
✅ 全局异常处理
✅ 跨域配置

### 待开发模块
⬜ 简历管理模块
⬜ 岗位管理模块
⬜ 简历投递模块
⬜ 简历智能解析（AI）
⬜ 岗位-简历智能匹配（AI）
⬜ AI文字面试

## 项目文件清单

### 配置文件
- `pom.xml` - Maven依赖配置
- `src/main/resources/application.yml` - 应用配置

### 文档文件
- `README.md` - 项目说明文档
- `API_DOCS.md` - 接口文档
- `SQL_INIT.sql` - 数据库初始化脚本

### 源代码结构

```
src/main/java/com/example/airecruitmentbackend/
│
├── common/
│   └── Result.java                    # 统一返回结果封装
│
├── config/
│   ├── CorsConfig.java                # 跨域配置
│   ├── JwtUtil.java                   # JWT工具类
│   ├── MyBatisPlusConfig.java         # MyBatis Plus配置
│   └── RedisConfig.java               # Redis配置
│
├── controller/
│   └── UserController.java            # 用户控制器（登录、切换身份）
│
├── dto/
│   ├── LoginRequest.java              # 登录请求DTO
│   ├── LoginResponse.java             # 登录响应DTO
│   ├── SendCodeRequest.java           # 发送验证码请求DTO
│   └── SwitchRoleRequest.java        # 身份切换请求DTO
│
├── entity/
│   └── User.java                    # 用户实体类
│
├── exception/
│   ├── BusinessException.java         # 自定义业务异常
│   └── GlobalExceptionHandler.java    # 全局异常处理器
│
├── mapper/
│   └── UserMapper.java              # 用户Mapper接口
│
└── service/
    ├── UserService.java              # 用户服务接口
    └── impl/
        └── UserServiceImpl.java      # 用户服务实现类
```

## 数据库表设计

### user表（用户表）
| 字段名 | 类型 | 说明 | 索引 |
|--------|------|------|------|
| id | BIGINT | 用户ID（主键，自增） | PRIMARY |
| phone | VARCHAR(11) | 手机号（唯一标识） | UNIQUE |
| role | TINYINT | 用户角色：1-求职者，2-企业HR | INDEX |
| create_time | DATETIME | 创建时间 | INDEX |
| update_time | DATETIME | 更新时间 | - |

## 核心接口

### 1. 发送验证码
- **接口**: `POST /api/user/send-code`
- **功能**: 生成6位验证码，存入Redis（5分钟过期）
- **参数**: phone, role
- **返回**: 发送成功提示

### 2. 登录
- **接口**: `POST /api/user/login`
- **功能**: 验证码登录，返回JWT令牌
- **参数**: phone, code, role
- **返回**: token, userId, role, needSwitchRole（是否需要切换身份）

### 3. 身份切换
- **接口**: `POST /api/user/switch-role`
- **功能**: 用户确认切换角色，更新用户角色
- **参数**: phone, code, role
- **返回**: 新的token, userId, role

## 技术栈详情

### 后端框架
- Spring Boot 3.1.5
- Spring MVC
- Spring Data Redis

### 数据访问
- MyBatis Plus 3.5.3.1
- MySQL Connector/J

### 认证授权
- JJWT 0.11.5（JWT生成和解析）
- 自定义JWT工具类

### 工具库
- Lombok（简化代码）
- Validation（参数校验）
- Jackson（JSON序列化）

### 开发工具
- JDK 17
- Maven 3.x

## 配置说明

### 数据库配置
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/ai_recruitment
    username: root
    password: root
```

### Redis配置
```yaml
spring:
  redis:
    host: localhost
    port: 6379
    password: 123456
    database: 2
```

### JWT配置
```yaml
jwt:
  secret: ai-recruitment-jwt-secret-key-2024-very-long-key-for-security
  expiration: 7200000  # 2小时（毫秒）
```

## 开发规范

### 分层架构
- **Controller层**: 接收HTTP请求，参数校验，调用Service
- **Service层**: 业务逻辑处理，事务控制
- **Mapper层**: 数据库操作

### 命名规范
- **Controller**: *Controller
- **Service接口**: *Service
- **Service实现**: *ServiceImpl
- **Mapper**: *Mapper
- **Entity/DTO**: 使用具体名称，不添加后缀

### 代码风格
- 使用Lombok简化getter/setter
- 使用@RequiredArgsConstructor构造器注入
- 统一使用@Slf4j日志注解
- 所有公开方法添加JavaDoc注释

## 安全特性

1. **验证码保护**
   - 6位随机数字验证码
   - 5分钟过期时间
   - 一次性使用（用完即删）

2. **JWT认证**
   - 2小时有效期
   - 包含用户ID和角色信息
   - 使用HMAC-SHA512签名

3. **参数校验**
   - 手机号格式校验
   - 验证码格式校验
   - 角色范围校验

4. **身份切换保护**
   - 需要重新验证码
   - 确保本人操作
   - 防止冒用

## 部署说明

### 环境要求
- JDK 17+
- MySQL 8.0+
- Redis 6.0+

### 启动步骤
1. 创建数据库：`mysql -u root -p < SQL_INIT.sql`
2. 启动Redis服务
3. 修改配置文件（数据库、Redis连接信息）
4. 运行：`mvn spring-boot:run`
5. 访问：`http://localhost:8080`

### 端口说明
- 应用端口：8080
- MySQL端口：3306
- Redis端口：6379

## 扩展计划

### 第二阶段开发（CRUD模块）
1. 简历管理
   - 简历创建、编辑、删除
   - 简历信息展示
   - 简历附件上传

2. 岗位管理
   - 岗位发布、编辑、删除
   - 岗位列表展示
   - 岗位详情查看

3. 简历投递
   - 投递简历
   - 查看投递记录
   - 投递状态管理

### 第三阶段开发（AI增强）
1. 简历智能解析
   - 简历文件解析（PDF、Word）
   - 提取关键信息（姓名、学历、工作经验等）
   - 结构化存储

2. 岗位-简历智能匹配
   - 基于技能匹配
   - 基于经验匹配
   - 匹配度评分

3. AI文字面试
   - 智能问答
   - 面试记录
   - 面试评估

## 注意事项

1. **验证码模拟**: 当前验证码打印在后端控制台，后续需要对接真实短信API（阿里云/腾讯云）

2. **JWT刷新**: 当前JWT过期后需要重新登录，后续可增加刷新令牌机制

3. **日志记录**: 建议增加操作日志记录，便于问题排查和审计

4. **文件上传**: 简历和头像上传功能需要配置文件存储路径

5. **并发控制**: 验证码发送频率限制，防止短信轰炸
