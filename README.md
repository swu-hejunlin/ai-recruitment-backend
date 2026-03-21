# ai-recruitment-backend
智能招聘和人才匹配平台（毕业设计）-后端

## 项目状态
✅ 项目基础结构已搭建完成  
✅ Spring Boot框架已配置  
✅ MySQL数据库连接已配置  
✅ MyBatis Plus已配置完成  
✅ 简单的API示例已实现  

## 技术栈
- **Spring Boot 2.6.13**
- **MyBatis Plus 3.5.3.1**（替代JPA）
- **MySQL 8.0+**
- **Java 8**

## 数据库配置

### 连接信息
- **数据库URL**: `jdbc:mysql://localhost:3306/ai_recruitment`
- **用户名**: `root`
- **密码**: `root`

### 配置文件
- **主配置**: `src/main/resources/application.yml`
- **简化配置**: `src/main/resources/application-simple.yml`

### 测试API
启动应用后，可以通过以下端点测试数据库连接：
1. `GET /api/test/status` - 测试数据库连接状态
2. `POST /api/test/users/create` - 创建测试用户
3. `GET /api/test/users/all` - 获取所有用户
4. `GET /api/test/users/count` - 获取用户数量
5. `GET /api/test/users/{id}` - 根据ID获取用户
6. `GET /api/test/users/username/{username}` - 根据用户名查找用户
7. `POST /api/test/users/init` - 初始化测试数据
8. `DELETE /api/test/users/{id}` - 删除用户

## 运行要求
1. MySQL数据库服务（端口3306）
2. 创建数据库：`ai_recruitment`
3. Java 8或更高版本

## 详细配置说明
- [DATABASE_SETUP.md](./DATABASE_SETUP.md) - 数据库配置说明
- [SQL_SETUP.md](./SQL_SETUP.md) - SQL脚本执行指南
- [MYBATIS_PLUS_GUIDE.md](./MYBATIS_PLUS_GUIDE.md) - MyBatis Plus使用指南
