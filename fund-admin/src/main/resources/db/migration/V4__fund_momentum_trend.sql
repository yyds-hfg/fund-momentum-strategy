-- 说明：本项目当前未启用 Flyway 自动迁移，请手动在 MySQL 的 momentum_strategy 库中执行此脚本。

CREATE TABLE IF NOT EXISTS fund_momentum_trend (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    strategy_type VARCHAR(20) NOT NULL COMMENT '策略类型：BALANCED / ACTIVE',
    fund_code VARCHAR(20) NOT NULL COMMENT '基金代码',
    trade_date DATE NOT NULL COMMENT '动量趋势所属交易日',
    slope_7 DECIMAL(10,4) COMMENT '7 日动量分斜率（score/天）',
    slope_14 DECIMAL(10,4) COMMENT '14 日动量分斜率（score/天）',
    slope_20 DECIMAL(10,4) COMMENT '20 日动量分斜率（score/天）',
    sigma DECIMAL(10,4) COMMENT '该基金近期动量分变化标准差',
    trend VARCHAR(20) NOT NULL COMMENT '趋势标签：SHARP_UP / UP / FLAT_UP / FLAT / FLAT_DOWN / DOWN / SHARP_DOWN',
    description VARCHAR(255) COMMENT '趋势文字描述',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_strategy_fund_date (strategy_type, fund_code, trade_date),
    KEY idx_trade_date (trade_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='基金动量趋势';
