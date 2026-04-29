-- ========================================
-- 智能招聘平台 - 数据库初始化脚本
-- 创建时间：2026-03-22
-- 说明：包含用户表、求职者信息表、企业信息表及相关索引设计
-- ========================================

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS ai_recruitment DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE ai_recruitment;

-- ========================================
-- 用户表
-- 存储求职者和企业HR的基本登录信息
-- ========================================
DROP TABLE IF EXISTS `user`;

CREATE TABLE `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID（主键）',
    `phone` VARCHAR(11) NOT NULL COMMENT '手机号（唯一标识）',
    `role` TINYINT NOT NULL COMMENT '用户角色：1-求职者，2-企业HR',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_phone` (`phone`),
    KEY `idx_role` (`role`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ========================================
-- 求职者信息表
-- 存储求职者的详细个人信息，与user表通过user_id关联
-- ========================================
DROP TABLE IF EXISTS `job_seeker`;

CREATE TABLE `job_seeker` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '求职者ID（主键）',
    `user_id` BIGINT NOT NULL COMMENT '关联用户ID',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号（与账号关联，默认使用注册手机号）',
    `name` VARCHAR(50) NOT NULL COMMENT '姓名',
    `gender` TINYINT DEFAULT NULL COMMENT '性别：0-未知，1-男，2-女',
    `avatar` VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱地址',
    `age` INT DEFAULT NULL COMMENT '年龄',
    `work_years` INT DEFAULT NULL COMMENT '工作年限（年）',
    `current_salary` DECIMAL(10,2) DEFAULT NULL COMMENT '当前薪资（万元/年）',
    `expected_salary` DECIMAL(10,2) DEFAULT NULL COMMENT '期望薪资（万元/年）',
    `current_status` TINYINT DEFAULT NULL COMMENT '当前状态：1-在职，2-离职，3-在读学生',
    `city` VARCHAR(50) DEFAULT NULL COMMENT '所在城市',
    `address` VARCHAR(200) DEFAULT NULL COMMENT '详细地址',
    `introduction` VARCHAR(500) DEFAULT NULL COMMENT '个人简介',
    `skills` TEXT DEFAULT NULL COMMENT '技能标签（JSON数组格式，如：["Java","Spring","MySQL"]）',
    `resume_url` VARCHAR(500) DEFAULT NULL COMMENT '简历附件URL',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`),
    KEY `idx_name` (`name`),
    KEY `idx_city` (`city`),
    KEY `idx_work_years` (`work_years`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='求职者信息表';

-- ========================================
-- 教育经历表
-- 存储求职者的教育背景，与job_seeker表通过job_seeker_id关联
-- ========================================
CREATE TABLE `education` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `job_seeker_id` BIGINT NOT NULL COMMENT '求职者ID（外键）',
    `school_name` VARCHAR(100) NOT NULL COMMENT '学校名称',
    `major` VARCHAR(100) DEFAULT NULL COMMENT '专业',
    `education_level` TINYINT NOT NULL COMMENT '学历：1-高中及以下，2-大专，3-本科，4-硕士，5-博士',
    `start_date` DATE DEFAULT NULL COMMENT '入学时间',
    `end_date` DATE DEFAULT NULL COMMENT '毕业时间',
    `description` TEXT DEFAULT NULL COMMENT '在校表现/主要课程描述',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_job_seeker_id` (`job_seeker_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='教育经历表';

-- ========================================
-- 工作/实习经历表
-- 存储求职者的实际工作经验和实习经历，与job_seeker表通过job_seeker_id关联
-- ========================================
CREATE TABLE `experience` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '经历ID（主键）',
    `job_seeker_id` BIGINT NOT NULL COMMENT '求职者ID（外键）',
    `company_name` VARCHAR(100) NOT NULL COMMENT '公司名称',
    `company_industry` VARCHAR(50) DEFAULT NULL COMMENT '公司所属行业',
    `position` VARCHAR(100) NOT NULL COMMENT '职位',
    `start_date` DATE NOT NULL COMMENT '开始时间',
    `end_date` DATE DEFAULT NULL COMMENT '结束时间',
    `description` TEXT DEFAULT NULL COMMENT '工作/实习内容描述',
    `is_internship` TINYINT NOT NULL DEFAULT 0 COMMENT '是否为实习：0-否，1-是',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_job_seeker_id` (`job_seeker_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='工作/实习经历表';

-- ========================================
-- 项目经历表
-- 存储求职者的项目经验，与job_seeker表通过job_seeker_id关联
-- ========================================
CREATE TABLE `project` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '项目ID（主键）',
    `job_seeker_id` BIGINT NOT NULL COMMENT '求职者ID（外键）',
    `project_name` VARCHAR(100) NOT NULL COMMENT '项目名称',
    `project_role` VARCHAR(100) DEFAULT NULL COMMENT '项目角色',
    `start_date` DATE NOT NULL COMMENT '项目开始时间',
    `end_date` DATE DEFAULT NULL COMMENT '项目结束时间',
    `description` TEXT DEFAULT NULL COMMENT '项目描述',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_job_seeker_id` (`job_seeker_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='项目经历表';

-- ========================================
-- 职位发布表
-- 存储企业发布的职位信息，与company表通过company_id关联
-- ========================================
DROP TABLE IF EXISTS `position`;
CREATE TABLE `position` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '职位ID（主键）',
    `company_id` BIGINT NOT NULL COMMENT '所属企业ID',
    `boss_id` BIGINT NOT NULL COMMENT '发布者(Boss/HR)ID',
    `title` VARCHAR(100) NOT NULL COMMENT '职位名称',
    `category` VARCHAR(50) NOT NULL COMMENT '职位类别(如: 后端开发)',
    `city` VARCHAR(50) NOT NULL COMMENT '工作城市',
    `address` VARCHAR(200) COMMENT '详细工作地址',
    `salary_min` INT COMMENT '最低薪资(K)',
    `salary_max` INT COMMENT '最高薪资(K)',
    `education_min` TINYINT COMMENT '最低学历要求: 1-5',
    `work_years_min` INT COMMENT '最低工作年限要求',
    `description` TEXT NOT NULL COMMENT '岗位职责',
    `requirement` TEXT NOT NULL COMMENT '任职要求',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 1-招聘中, 0-已关闭',
    `tags` VARCHAR(500) COMMENT '职位福利标签(JSON数组, 如["五险一金","双休"])',
    `company_logo` VARCHAR(500) DEFAULT NULL COMMENT '企业Logo（冗余字段）',
    `company_name` VARCHAR(100) DEFAULT NULL COMMENT '企业名称（冗余字段，方便前端展示）',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_boss` (`boss_id`),
    KEY `idx_company` (`company_id`),
    KEY `idx_category` (`category`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='职位发布表';

-- ========================================
-- 投递记录表
-- 建立求职者与职位的关联
-- ========================================
DROP TABLE IF EXISTS `application`;
CREATE TABLE `application` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '投递ID',
    `job_seeker_id` BIGINT NOT NULL COMMENT '求职者ID',
    `position_id` BIGINT NOT NULL COMMENT '职位ID',
    `company_id` BIGINT NOT NULL COMMENT '所属企业ID（冗余，方便Boss快速查询）',
    `boss_id` BIGINT NOT NULL COMMENT '接收投递的Boss/HR ID（冗余，方便通知查询）',
    `job_seeker_name` VARCHAR(50) DEFAULT NULL COMMENT '求职者姓名（冗余字段，方便前端展示）',
    `company_name` VARCHAR(100) DEFAULT NULL COMMENT '企业名称（冗余字段，方便前端展示）',
    `position_title` VARCHAR(100) DEFAULT NULL COMMENT '职位名称（冗余字段，方便前端展示）',
    `status` TINYINT DEFAULT 1 COMMENT '投递状态：1-待查看，2-已查看，3-面试中，4-不合适，5-录用',
    `ai_score` DECIMAL(5,2) DEFAULT NULL COMMENT 'AI匹配分（第二阶段预留：0-100分）',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '投递时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_seeker_position` (`job_seeker_id`, `position_id`),
    KEY `idx_boss_status` (`boss_id`, `status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='投递记录表';

-- ========================================
-- 系统通知表
-- 负责站内提醒（红点反馈）
-- ========================================
DROP TABLE IF EXISTS `notification`;
CREATE TABLE `notification` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '通知ID',
    `receiver_id` BIGINT NOT NULL COMMENT '接收者用户ID',
    `type` TINYINT NOT NULL COMMENT '通知类型：1-新投递提醒，2-面试状态变更，3-系统公告',
    `title` VARCHAR(100) NOT NULL COMMENT '通知标题',
    `content` TEXT COMMENT '通知内容',
    `business_id` BIGINT COMMENT '业务关联ID（如指向具体的application_id）',
    `is_read` TINYINT DEFAULT 0 COMMENT '已读状态：0-未读，1-已读',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '触发时间',
    PRIMARY KEY (`id`),
    KEY `idx_receiver_read` (`receiver_id`, `is_read`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统通知表';

-- ========================================
-- 企业信息表
-- 存储企业的详细信息，与user表通过user_id关联
-- ========================================
DROP TABLE IF EXISTS `company`;

CREATE TABLE `company` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '企业ID（主键）',
    `user_id` BIGINT NOT NULL COMMENT '关联用户ID（企业HR）',
    `company_name` VARCHAR(100) NOT NULL COMMENT '企业名称',
    `logo` VARCHAR(500) DEFAULT NULL COMMENT '企业logo URL',
    `legal_person` VARCHAR(50) DEFAULT NULL COMMENT '法人代表',
    `industry` VARCHAR(50) DEFAULT NULL COMMENT '所属行业',
    `scale` TINYINT DEFAULT NULL COMMENT '企业规模：1-0-20人，2-20-99人，3-100-499人，4-500-999人，5-1000-9999人，6-10000人以上',
    `financing_stage` TINYINT DEFAULT NULL COMMENT '融资阶段：1-未融资，2-天使轮，3-A轮，4-B轮，5-C轮，6-D轮及以上，7-已上市，8-不需要融资',
    `city` VARCHAR(50) DEFAULT NULL COMMENT '所在城市',
    `address` VARCHAR(200) DEFAULT NULL COMMENT '详细地址',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '企业邮箱',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '企业联系电话',
    `website` VARCHAR(200) DEFAULT NULL COMMENT '企业官网',
    `description` TEXT DEFAULT NULL COMMENT '企业简介',
    `welfare` TEXT DEFAULT NULL COMMENT '福利待遇（JSON数组格式）',
    `business_license` VARCHAR(500) DEFAULT NULL COMMENT '营业执照URL',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`),
    KEY `idx_company_name` (`company_name`),
    KEY `idx_city` (`city`),
    KEY `idx_industry` (`industry`),
    KEY `idx_scale` (`scale`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='企业信息表';

-- ========================================
-- 数据字典说明
-- ========================================

-- 【用户角色 role】
-- 1: 求职者
-- 2: 企业HR

-- 【性别 gender】
-- 0: 未知
-- 1: 男
-- 2: 女

-- 【学历层次 education_level】
-- 1: 高中及以下
-- 2: 大专
-- 3: 本科
-- 4: 硕士
-- 5: 博士

-- 【当前状态 current_status】
-- 1: 在职
-- 2: 离职
-- 3: 在读学生

-- 【企业规模 scale】
-- 1: 0-20人
-- 2: 20-99人
-- 3: 100-499人
-- 4: 500-999人
-- 5: 1000-9999人
-- 6: 10000人以上

-- 【融资阶段 financing_stage】
-- 1: 未融资
-- 2: 天使轮
-- 3: A轮
-- 4: B轮
-- 5: C轮
-- 6: D轮及以上
-- 7: 已上市
-- 8: 不需要融资

-- ========================================
-- 表关系说明
-- ========================================

-- 1. user表是主表，存储用户的手机号和角色信息
-- 2. job_seeker表和company表通过user_id与user表一一对应
-- 3. education表、experience表和project表通过job_seeker_id与job_seeker表关联（一对多）
-- 4. 一个手机号只能有一个账号，但可以在求职者和企业HR之间切换
-- 5. 验证码存储在Redis中，无需建表
--    Redis key格式：login:code:{phone}
--    Redis value：6位数字验证码
--    Redis过期时间：300秒（5分钟）

-- 表关系：
-- user (1) ────── (1) job_seeker
-- user (1) ────── (1) company
-- job_seeker (1) ──── (N) education
-- job_seeker (1) ──── (N) experience
-- job_seeker (1) ──── (N) project
-- application 表新增冗余字段 job_seeker_name、company_name、position_title，用于前端快速展示
-- position 表新增冗余字段 company_logo、company_name，用于前端快速展示

-- ========================================
-- 增量迁移SQL（已有数据库执行）
-- ========================================
-- 【application 表迁移】
-- ALTER TABLE `application` ADD COLUMN `job_seeker_name` VARCHAR(50) DEFAULT NULL COMMENT '求职者姓名（冗余字段）' AFTER `boss_id`;
-- ALTER TABLE `application` ADD COLUMN `company_name` VARCHAR(100) DEFAULT NULL COMMENT '企业名称（冗余字段）' AFTER `job_seeker_name`;
-- ALTER TABLE `application` ADD COLUMN `position_title` VARCHAR(100) DEFAULT NULL COMMENT '职位名称（冗余字段）' AFTER `company_name`;
-- UPDATE application a
-- INNER JOIN job_seeker js ON a.job_seeker_id = js.id
-- INNER JOIN company c ON a.company_id = c.id
-- INNER JOIN position p ON a.position_id = p.id
-- SET a.job_seeker_name = js.name, a.company_name = c.company_name, a.position_title = p.title
-- WHERE a.job_seeker_name IS NULL OR a.company_name IS NULL OR a.position_title IS NULL;

-- 【position 表迁移】
-- ALTER TABLE `position` ADD COLUMN `company_logo` VARCHAR(500) DEFAULT NULL COMMENT '企业Logo（冗余字段）' AFTER `tags`;
-- ALTER TABLE `position` ADD COLUMN `company_name` VARCHAR(100) DEFAULT NULL COMMENT '企业名称（冗余字段）' AFTER `company_logo`;
-- UPDATE position p
-- INNER JOIN company c ON p.company_id = c.id
-- SET p.company_logo = c.logo, p.company_name = c.company_name
-- WHERE p.company_logo IS NULL OR p.company_name IS NULL;

-- ========================================
-- 面试记录表
-- ========================================
CREATE TABLE `interview` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '面试ID',
    `application_id` BIGINT NOT NULL COMMENT '关联投递ID',
    `job_seeker_id` BIGINT NOT NULL COMMENT '求职者ID',
    `position_id` BIGINT NOT NULL COMMENT '职位ID',
    `company_id` BIGINT NOT NULL COMMENT '企业ID',
    `interview_time` DATETIME NOT NULL COMMENT '面试时间',
    `interview_type` TINYINT NOT NULL COMMENT '面试类型：1-线下，2-线上，3-AI面试',
    `interview_address` VARCHAR(200) DEFAULT NULL COMMENT '面试地址（线下）',
    `interview_link` VARCHAR(500) DEFAULT NULL COMMENT '面试链接（线上）',
    `status` TINYINT DEFAULT 1 COMMENT '面试状态：1-待确认，2-已确认，3-已拒绝，4-已完成',
    `remark` TEXT DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_application` (`application_id`),
    KEY `idx_job_seeker` (`job_seeker_id`),
    KEY `idx_position` (`position_id`),
    KEY `idx_company` (`company_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='面试记录表';

-- ========================================
-- 收藏表
-- ========================================
CREATE TABLE `favorite` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '收藏ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `target_type` TINYINT NOT NULL COMMENT '收藏类型：1-职位，2-企业，3-求职者',
    `target_id` BIGINT NOT NULL COMMENT '目标ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_target` (`user_id`, `target_type`, `target_id`),
    KEY `idx_user` (`user_id`),
    KEY `idx_target` (`target_type`, `target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收藏表';

-- ========================================
-- 人才画像表
-- 存储人才的结构化画像信息
-- ========================================
DROP TABLE IF EXISTS `talent_profile`;
CREATE TABLE `talent_profile` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `skills` TEXT DEFAULT NULL COMMENT '技能标签列表（JSON格式）',
    `education` VARCHAR(20) DEFAULT NULL COMMENT '学历：1-高中及以下，2-大专，3-本科，4-硕士，5-博士',
    `work_years` INT DEFAULT NULL COMMENT '工作年限',
    `salary_expectation` DECIMAL(10,2) DEFAULT NULL COMMENT '期望薪资（K/月）',
    `current_salary` DECIMAL(10,2) DEFAULT NULL COMMENT '当前薪资（K/月）',
    `talent_tags` TEXT DEFAULT NULL COMMENT '人才标签列表（JSON格式）',
    `description_summary` VARCHAR(500) DEFAULT NULL COMMENT '个人简介摘要',
    `strengths_summary` TEXT DEFAULT NULL COMMENT '优势亮点摘要',
    `career_goals` VARCHAR(500) DEFAULT NULL COMMENT '职业目标',
    `match_keywords` TEXT DEFAULT NULL COMMENT '匹配关键词（JSON格式）',
    `ai_evaluation` TEXT DEFAULT NULL COMMENT 'AI综合评估',
    `embedding_vector` TEXT DEFAULT NULL COMMENT '文本嵌入向量（JSON格式，用于语义相似度计算）',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`),
    KEY `idx_work_years` (`work_years`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='人才画像表';

-- ========================================
-- 岗位画像表
-- 存储岗位的结构化画像信息
-- ========================================
DROP TABLE IF EXISTS `job_profile`;
CREATE TABLE `job_profile` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `position_id` BIGINT NOT NULL COMMENT '岗位ID',
    `skills` TEXT DEFAULT NULL COMMENT '技能标签列表（JSON格式）',
    `education_require` VARCHAR(20) DEFAULT NULL COMMENT '学历要求：1-高中及以下，2-大专，3-本科，4-硕士，5-博士',
    `experience_require` VARCHAR(20) DEFAULT NULL COMMENT '经验要求：1-不限，2-1年以下，3-1-3年，4-3-5年，5-5-10年，6-10年以上',
    `salary_min` DECIMAL(10,2) DEFAULT NULL COMMENT '最低薪资（K/月）',
    `salary_max` DECIMAL(10,2) DEFAULT NULL COMMENT '最高薪资（K/月）',
    `job_tags` TEXT DEFAULT NULL COMMENT '岗位标签列表（JSON格式）',
    `description_summary` VARCHAR(500) DEFAULT NULL COMMENT '岗位描述摘要',
    `responsibilities_summary` VARCHAR(500) DEFAULT NULL COMMENT '工作职责摘要',
    `requirements_summary` VARCHAR(500) DEFAULT NULL COMMENT '任职要求摘要',
    `company_benefits` TEXT DEFAULT NULL COMMENT '公司福利',
    `match_keywords` TEXT DEFAULT NULL COMMENT '匹配关键词（JSON格式）',
    `embedding_vector` TEXT DEFAULT NULL COMMENT '文本嵌入向量（JSON格式，用于语义相似度计算）',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_position_id` (`position_id`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='岗位画像表';

-- ========================================
-- 岗位匹配记录表
-- 存储岗位与人才的匹配记录
-- ========================================
DROP TABLE IF EXISTS `job_match_record`;
CREATE TABLE `job_match_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '求职者ID',
    `position_id` BIGINT NOT NULL COMMENT '岗位ID',
    `match_score` DECIMAL(5,2) DEFAULT NULL COMMENT '匹配分数（0-100）',
    `skill_match_rate` DECIMAL(5,2) DEFAULT NULL COMMENT '技能匹配率（0-100）',
    `experience_match_rate` DECIMAL(5,2) DEFAULT NULL COMMENT '经验匹配率（0-100）',
    `education_match_rate` DECIMAL(5,2) DEFAULT NULL COMMENT '学历匹配率（0-100）',
    `salary_match_rate` DECIMAL(5,2) DEFAULT NULL COMMENT '薪资匹配率（0-100）',
    `match_details` TEXT DEFAULT NULL COMMENT '匹配详情（JSON格式）',
    `is_viewed` TINYINT DEFAULT 0 COMMENT '是否查看：0-否，1-是',
    `is_applied` TINYINT DEFAULT 0 COMMENT '是否申请：0-否，1-是',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_position_id` (`position_id`),
    KEY `idx_match_score` (`match_score`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='岗位匹配记录表';

-- ========================================
-- 人才匹配记录表
-- 存储人才与岗位的匹配记录（HR视角）
-- ========================================
DROP TABLE IF EXISTS `talent_match_record`;
CREATE TABLE `talent_match_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `boss_id` BIGINT NOT NULL COMMENT 'BOSS/HR ID',
    `job_seeker_id` BIGINT NOT NULL COMMENT '求职者ID',
    `position_id` BIGINT NOT NULL COMMENT '岗位ID',
    `match_score` DECIMAL(5,2) DEFAULT NULL COMMENT '匹配分数（0-100）',
    `skill_match_rate` DECIMAL(5,2) DEFAULT NULL COMMENT '技能匹配率（0-100）',
    `experience_match_rate` DECIMAL(5,2) DEFAULT NULL COMMENT '经验匹配率（0-100）',
    `education_match_rate` DECIMAL(5,2) DEFAULT NULL COMMENT '学历匹配率（0-100）',
    `salary_match_rate` DECIMAL(5,2) DEFAULT NULL COMMENT '薪资匹配率（0-100）',
    `match_details` TEXT DEFAULT NULL COMMENT '匹配详情（JSON格式）',
    `is_viewed` TINYINT DEFAULT 0 COMMENT '是否查看：0-否，1-是',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_boss_id` (`boss_id`),
    KEY `idx_job_seeker_id` (`job_seeker_id`),
    KEY `idx_position_id` (`position_id`),
    KEY `idx_match_score` (`match_score`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='人才匹配记录表';

-- ========================================
-- 岗位技能标签表
-- 存储岗位的技能标签信息
-- ========================================
DROP TABLE IF EXISTS `job_skill_tag`;
CREATE TABLE `job_skill_tag` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `position_id` BIGINT NOT NULL COMMENT '岗位ID',
    `skill_tag` VARCHAR(100) NOT NULL COMMENT '技能标签',
    `skill_level` VARCHAR(20) DEFAULT NULL COMMENT '技能等级：required-必须，preferred-优先',
    `proficiency_weight` INT DEFAULT 50 COMMENT '熟练度权重（1-100）',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_position_id` (`position_id`),
    KEY `idx_skill_tag` (`skill_tag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='岗位技能标签表';

-- ========================================
-- 人才技能标签表
-- 存储人才的技能标签信息
-- ========================================
DROP TABLE IF EXISTS `talent_skill_tag`;
CREATE TABLE `talent_skill_tag` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `skill_tag` VARCHAR(100) NOT NULL COMMENT '技能标签',
    `proficiency_level` TINYINT DEFAULT NULL COMMENT '熟练度等级（1-5）：1-了解，2-熟悉，3-掌握，4-精通，5-专家',
    `years_used` INT DEFAULT NULL COMMENT '使用年限',
    `last_used_time` DATE DEFAULT NULL COMMENT '最后使用时间',
    `is_highlight` TINYINT DEFAULT 0 COMMENT '是否亮点技能：0-否，1-是',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_skill_tag` (`skill_tag`),
    KEY `idx_is_highlight` (`is_highlight`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='人才技能标签表';

-- ========================================
-- 面试评估结果表
-- 存储AI面试的评估结果
-- ========================================
DROP TABLE IF EXISTS `interview_evaluation`;
CREATE TABLE `interview_evaluation` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '评估ID',
    `interview_id` BIGINT NOT NULL COMMENT '面试ID',
    `score` DECIMAL(5,2) DEFAULT NULL COMMENT '评估分数(0-100)',
    `evaluation_text` TEXT DEFAULT NULL COMMENT '评估文本(优势+不足)',
    `language_score` DECIMAL(5,2) DEFAULT NULL COMMENT '语言表达分数',
    `logic_score` DECIMAL(5,2) DEFAULT NULL COMMENT '逻辑思维分数',
    `professional_score` DECIMAL(5,2) DEFAULT NULL COMMENT '专业能力分数',
    `suggestions` TEXT DEFAULT NULL COMMENT '改进建议(JSON数组)',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_interview_id` (`interview_id`),
    KEY `idx_score` (`score`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='面试评估结果表';

-- ========================================
-- 预留扩展
-- ========================================

-- 后续可创建以下表：
-- - chat: 聊天记录表（AI面试）
