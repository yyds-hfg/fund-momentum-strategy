-- 说明：本项目当前未启用 Flyway 自动迁移，请手动在 MySQL 的 momentum_strategy 库中执行此脚本。
-- 本脚本用于从已有数据库中清理标签相关表，并为 fund 表新增 description 字段。

DROP TABLE IF EXISTS fund_tag_relation;
DROP TABLE IF EXISTS fund_tag;

ALTER TABLE fund ADD COLUMN IF NOT EXISTS description VARCHAR(500) COMMENT '基金描述';

