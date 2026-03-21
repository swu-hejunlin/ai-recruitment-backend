-- ========================================
-- 智能招聘平台 - 数据库初始化脚本
-- 创建时间：2026-03-20
-- 说明：包含用户表及相关索引设计
-- ========================================

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS ai_recruitment DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE ai_recruitment;

-- ========================================
-- 用户表
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
-- 说明：
-- 1. 验证码通过Redis存储，无需建表
--    Redis key格式：login:code:{phone}
--    Redis value：6位数字验证码
--    Redis过期时间：300秒（5分钟）
--
-- 2. 登录流程：
--    - 用户调用发送验证码接口，后端生成验证码存入Redis
--    - 用户提交手机号+验证码+角色进行登录
--    - 后端校验验证码，成功则生成JWT令牌返回
--    - 如果用户不存在，自动注册新用户
--
-- 3. 预留扩展：
--    - 后续可增加用户昵称、头像、邮箱等字段
--    - 预留企业关联字段，用于企业HR管理岗位
--    - 预留简历关联字段，用于求职者管理简历
-- ========================================
