-- ============================================
-- AI智能招聘系统 - 画像与推荐功能数据库表
-- 创建时间: 2026-04-18
-- 功能模块: 岗位画像、人才画像、岗位推荐
-- 说明: 使用逻辑外键约束，物理外键由应用层控制
-- ============================================

-- 1. 岗位画像表
DROP TABLE IF EXISTS job_profile;
CREATE TABLE job_profile (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    position_id BIGINT NOT NULL COMMENT '岗位ID（逻辑外键关联position表）',
    skills JSON COMMENT '技能标签列表',
    education_require VARCHAR(50) COMMENT '学历要求：1-高中及以下，2-大专，3-本科，4-硕士，5-博士',
    experience_require VARCHAR(50) COMMENT '经验要求：1-不限，2-1年以下，3-1-3年，4-3-5年，5-5-10年，6-10年以上',
    salary_min DECIMAL(10,2) COMMENT '最低薪资（K/月）',
    salary_max DECIMAL(10,2) COMMENT '最高薪资（K/月）',
    job_tags JSON COMMENT '岗位标签列表',
    description_summary TEXT COMMENT '岗位描述摘要',
    responsibilities_summary TEXT COMMENT '工作职责摘要',
    requirements_summary TEXT COMMENT '任职要求摘要',
    company_benefits TEXT COMMENT '公司福利',
    match_keywords JSON COMMENT '匹配关键词',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_position_id (position_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='岗位画像表';

-- 2. 人才画像表
DROP TABLE IF EXISTS talent_profile;
CREATE TABLE talent_profile (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID（逻辑外键关联user表）',
    skills JSON COMMENT '技能标签列表',
    education VARCHAR(50) COMMENT '学历：1-高中及以下，2-大专，3-本科，4-硕士，5-博士',
    work_years INT COMMENT '工作年限',
    salary_expectation DECIMAL(10,2) COMMENT '期望薪资（K/月）',
    current_salary DECIMAL(10,2) COMMENT '当前薪资（K/月）',
    talent_tags JSON COMMENT '人才标签列表',
    description_summary TEXT COMMENT '个人简介摘要',
    strengths_summary TEXT COMMENT '优势亮点摘要',
    career_goals TEXT COMMENT '职业目标',
    match_keywords JSON COMMENT '匹配关键词',
    ai_evaluation TEXT COMMENT 'AI综合评估',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_id (user_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='人才画像表';

-- 3. 岗位技能标签表
DROP TABLE IF EXISTS job_skill_tag;
CREATE TABLE job_skill_tag (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    position_id BIGINT NOT NULL COMMENT '岗位ID（逻辑外键关联position表）',
    skill_tag VARCHAR(100) NOT NULL COMMENT '技能标签',
    skill_level VARCHAR(50) DEFAULT 'required' COMMENT '技能等级：required-必须，preferred-优先',
    proficiency_weight INT DEFAULT 100 COMMENT '熟练度权重（1-100）',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_position_id (position_id),
    INDEX idx_skill_tag (skill_tag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='岗位技能标签表';

-- 4. 人才技能标签表
DROP TABLE IF EXISTS talent_skill_tag;
CREATE TABLE talent_skill_tag (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID（逻辑外键关联user表）',
    skill_tag VARCHAR(100) NOT NULL COMMENT '技能标签',
    proficiency_level INT DEFAULT 3 COMMENT '熟练度等级（1-5）：1-了解，2-熟悉，3-掌握，4-精通，5-专家',
    years_used INT COMMENT '使用年限',
    last_used_time DATE COMMENT '最后使用时间',
    is_highlight TINYINT DEFAULT 0 COMMENT '是否亮点技能：0-否，1-是',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id),
    INDEX idx_skill_tag (skill_tag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='人才技能标签表';

-- 5. 岗位匹配记录表（用于记录推荐历史）
DROP TABLE IF EXISTS job_match_record;
CREATE TABLE job_match_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '求职者ID（逻辑外键关联user表）',
    position_id BIGINT NOT NULL COMMENT '岗位ID（逻辑外键关联position表）',
    match_score DECIMAL(5,2) COMMENT '匹配分数（0-100）',
    skill_match_rate DECIMAL(5,2) COMMENT '技能匹配率',
    experience_match_rate DECIMAL(5,2) COMMENT '经验匹配率',
    education_match_rate DECIMAL(5,2) COMMENT '学历匹配率',
    salary_match_rate DECIMAL(5,2) COMMENT '薪资匹配率',
    match_details JSON COMMENT '匹配详情',
    is_viewed TINYINT DEFAULT 0 COMMENT '是否查看：0-否，1-是',
    is_applied TINYINT DEFAULT 0 COMMENT '是否申请：0-否，1-是',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_id (user_id),
    INDEX idx_position_id (position_id),
    INDEX idx_match_score (match_score),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='岗位匹配记录表';

-- 6. 修改岗位表，添加画像生成标记
ALTER TABLE `position` ADD COLUMN profile_generated TINYINT DEFAULT 0 COMMENT '是否已生成画像：0-否，1-是';

-- 7. 修改用户表，添加画像生成标记和求职者标记
ALTER TABLE `user` ADD COLUMN profile_generated TINYINT DEFAULT 0 COMMENT '是否已生成画像：0-否，1-是';
ALTER TABLE `user` ADD COLUMN is_job_seeker TINYINT DEFAULT 0 COMMENT '是否为求职者：0-否，1-是';
