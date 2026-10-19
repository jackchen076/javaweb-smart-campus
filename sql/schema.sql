-- ============================================================
-- 智慧校园管理系统 - 数据库初始化脚本
-- 适用：MySQL 8.0
-- 执行：mysql -uroot -p < schema.sql
-- ============================================================

CREATE DATABASE IF NOT EXISTS smart_campus
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_general_ci;

USE smart_campus;

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
    `id`         INT          NOT NULL AUTO_INCREMENT COMMENT '主键，自增',
    `username`   VARCHAR(50)  NOT NULL                COMMENT '用户名，唯一',
    `password`   VARCHAR(100) NOT NULL                COMMENT '密码（演示明文；生产应存 BCrypt 哈希）',
    `email`      VARCHAR(100) DEFAULT NULL            COMMENT '邮箱',
    `phone`      VARCHAR(20)  DEFAULT NULL            COMMENT '手机号',
    `create_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COMMENT = '用户表';

-- 预置演示账号（admin / admin123）
INSERT INTO `user` (`username`, `password`, `email`, `phone`)
SELECT 'admin', 'admin123', 'admin@campus.edu', '13800000000'
WHERE NOT EXISTS (SELECT 1 FROM `user` WHERE `username` = 'admin');

SELECT '初始化完成，当前用户数：' AS msg, COUNT(*) AS cnt FROM `user`;
