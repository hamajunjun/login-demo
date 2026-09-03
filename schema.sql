-- ============================================================
-- 社区论坛 login-demo 建表脚本
-- 适用：MySQL 8.x（字符集 utf8mb4）
-- 说明：本脚本用于【全新环境】初始化数据库。
--       如果表已存在会跳过（IF NOT EXISTS），不会覆盖现有数据。
-- ============================================================

CREATE DATABASE IF NOT EXISTS login_demo DEFAULT CHARACTER SET utf8mb4;
USE login_demo;

-- ------------------------------------------------------------
-- 1. 用户表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `user` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `username`    VARCHAR(50)  NOT NULL COMMENT '用户名',
    `password`    VARCHAR(100) NOT NULL COMMENT '密码（BCrypt 加密）',
    `email`       VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `role`        VARCHAR(20)  NOT NULL DEFAULT 'USER' COMMENT '角色：USER / ADMIN',
    `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：1 启用，0 禁用',
    `avatar`      VARCHAR(255) DEFAULT NULL COMMENT '头像路径',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    FULLTEXT KEY `ft_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ------------------------------------------------------------
-- 2. 帖子表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `post` (
    `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `title`        VARCHAR(100) NOT NULL COMMENT '标题',
    `content`      TEXT         NOT NULL COMMENT '正文',
    `user_id`      BIGINT       NOT NULL COMMENT '作者 ID',
    `username`     VARCHAR(50)  NOT NULL COMMENT '作者名（冗余，改名时同步）',
    `community_id` BIGINT       DEFAULT NULL COMMENT '所属小区 ID',
    `rating`       INT          NOT NULL DEFAULT 0 COMMENT '评分',
    `like_count`   INT          NOT NULL DEFAULT 0 COMMENT '点赞数（冗余字段）',
    `view_count`   INT          NOT NULL DEFAULT 0 COMMENT '浏览数',
    `create_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
    `update_time`  DATETIME     DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_community_time` (`community_id`, `create_time`),
    KEY `idx_user_time` (`user_id`, `create_time`),
    FULLTEXT KEY `ft_title_content` (`title`, `content`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='帖子表';

-- ------------------------------------------------------------
-- 3. 评论表（parent_id 为 NULL 是一级评论，否则是回复）
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `comment` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `post_id`     BIGINT       NOT NULL COMMENT '所属帖子 ID',
    `user_id`     BIGINT       NOT NULL COMMENT '评论人 ID',
    `username`    VARCHAR(50)  NOT NULL COMMENT '评论人名（冗余）',
    `content`     VARCHAR(500) NOT NULL COMMENT '评论内容',
    `parent_id`   BIGINT       DEFAULT NULL COMMENT '父评论 ID，NULL 表示一级评论',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评论时间',
    `update_time` DATETIME     DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_post_time` (`post_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论表';

-- ------------------------------------------------------------
-- 4. 通知表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `notification` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`     BIGINT       NOT NULL COMMENT '接收通知的用户 ID',
    `type`        VARCHAR(20)  NOT NULL COMMENT '类型：LIKE / COMMENT / REPLY',
    `content`     VARCHAR(255) NOT NULL COMMENT '通知内容（业务层已截断到 200 字）',
    `is_read`     TINYINT      NOT NULL DEFAULT 0 COMMENT '是否已读：0 未读，1 已读',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '通知时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_time` (`user_id`, `create_time`),
    KEY `idx_user_read` (`user_id`, `is_read`),
    KEY `idx_user_type_time` (`user_id`, `type`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知表';

-- ------------------------------------------------------------
-- 5. 私信表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `private_message` (
    `id`          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    `sender_id`   BIGINT        NOT NULL COMMENT '发送者 ID',
    `receiver_id` BIGINT        NOT NULL COMMENT '接收者 ID',
    `content`     VARCHAR(1000) NOT NULL COMMENT '内容',
    `is_read`     TINYINT       NOT NULL DEFAULT 0 COMMENT '是否已读：0 未读，1 已读',
    `create_time` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
    PRIMARY KEY (`id`),
    KEY `idx_receiver_time` (`receiver_id`, `create_time`),
    KEY `idx_sender_time` (`sender_id`, `create_time`),
    KEY `idx_receiver_read` (`receiver_id`, `is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='私信表';

-- ------------------------------------------------------------
-- 6. 帖子点赞表（联合唯一索引防重复点赞）
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `post_like` (
    `id`          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
    `post_id`     BIGINT   NOT NULL COMMENT '帖子 ID',
    `user_id`     BIGINT   NOT NULL COMMENT '点赞人 ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_post_user` (`post_id`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='帖子点赞表';

-- ------------------------------------------------------------
-- 7. 帖子收藏表（联合唯一索引防重复收藏）
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `post_favorite` (
    `id`          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`     BIGINT   NOT NULL COMMENT '收藏人 ID',
    `post_id`     BIGINT   NOT NULL COMMENT '帖子 ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_post` (`user_id`, `post_id`),
    KEY `idx_user_time` (`user_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='帖子收藏表';

-- ------------------------------------------------------------
-- 8. 用户关注表（联合唯一索引防重复关注）
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `user_follow` (
    `id`           BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
    `follower_id`  BIGINT   NOT NULL COMMENT '关注者 ID',
    `following_id` BIGINT   NOT NULL COMMENT '被关注者 ID',
    `create_time`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '关注时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_follower_following` (`follower_id`, `following_id`),
    KEY `idx_follower_time` (`follower_id`, `create_time`),
    KEY `idx_following_time` (`following_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户关注表';

-- ------------------------------------------------------------
-- 9. 小区表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `community` (
    `id`          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    `name`        VARCHAR(100)  NOT NULL COMMENT '小区名称',
    `city`        VARCHAR(100)  DEFAULT NULL COMMENT '城市',
    `district`    VARCHAR(100)  DEFAULT NULL COMMENT '区县',
    `address`     VARCHAR(255)  DEFAULT NULL COMMENT '详细地址',
    `price`       DECIMAL(12,2) DEFAULT NULL COMMENT '均价（元/平）',
    `rating`      DECIMAL(3,1)  DEFAULT NULL COMMENT '综合评分',
    `create_time` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME      DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='小区表';
