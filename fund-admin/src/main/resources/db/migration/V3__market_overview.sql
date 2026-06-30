-- A 股市场概况与资金流向表
-- 说明：本项目当前未启用 Flyway 自动迁移，请手动在 MySQL 的 momentum_strategy 库中执行此脚本。

CREATE TABLE IF NOT EXISTS market_overview (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    trade_date DATE NOT NULL COMMENT '交易日期',

    -- 沪深两市成交量/成交额
    sh_volume BIGINT COMMENT '沪市成交量（股）',
    sh_amount DECIMAL(20,4) COMMENT '沪市成交额（元）',
    sz_volume BIGINT COMMENT '深市成交量（股）',
    sz_amount DECIMAL(20,4) COMMENT '深市成交额（元）',
    total_volume BIGINT COMMENT '两市总成交量（股）',
    total_amount DECIMAL(20,4) COMMENT '两市总成交额（元）',

    -- 资金流向（单位：元）
    main_inflow DECIMAL(20,4) COMMENT '主力净流入 = 超大单+大单',
    super_large_inflow DECIMAL(20,4) COMMENT '超大单净流入（映射：机构）',
    large_inflow DECIMAL(20,4) COMMENT '大单净流入（映射：大户）',
    medium_inflow DECIMAL(20,4) COMMENT '中单净流入',
    small_inflow DECIMAL(20,4) COMMENT '小单净流入（映射：散户）',
    north_bound_inflow DECIMAL(20,4) COMMENT '北向资金净流入（nullable，可能无数据）',

    -- 大盘指数收盘（用于展示）
    sh_close DECIMAL(10,4) COMMENT '上证指数收盘',
    sz_close DECIMAL(10,4) COMMENT '深证成指收盘',

    source VARCHAR(50) COMMENT '数据来源，如 eastmoney / sina',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_market_overview_date (trade_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='A股市场概况与资金流向';
