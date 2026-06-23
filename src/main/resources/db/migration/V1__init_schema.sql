CREATE DATABASE IF NOT EXISTS momentum_strategy
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE momentum_strategy;

-- 基金基础信息
CREATE TABLE IF NOT EXISTS fund (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    fund_code VARCHAR(20) NOT NULL COMMENT '基金代码',
    fund_name VARCHAR(100) NOT NULL COMMENT '基金名称',
    fund_type VARCHAR(20) NOT NULL COMMENT '基金类型：WIDE_BASE/SECTOR/BOND/GOLD/OTHER',
    listed_date DATE COMMENT '上市日期',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '0-禁用 1-启用',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_fund_code (fund_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='基金基础信息';

-- 基金净值数据
CREATE TABLE IF NOT EXISTS fund_nav (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    fund_code VARCHAR(20) NOT NULL COMMENT '基金代码',
    nav_date DATE NOT NULL COMMENT '净值日期',
    open_nav DECIMAL(10,4) COMMENT '开盘净值',
    high_nav DECIMAL(10,4) COMMENT '最高净值',
    low_nav DECIMAL(10,4) COMMENT '最低净值',
    close_nav DECIMAL(10,4) NOT NULL COMMENT '收盘净值',
    volume BIGINT COMMENT '成交量',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_fund_nav (fund_code, nav_date),
    KEY idx_fund_code_date (fund_code, nav_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='基金净值数据';

-- ETF 标签定义
CREATE TABLE IF NOT EXISTS fund_tag (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tag_code VARCHAR(50) NOT NULL COMMENT '标签编码',
    tag_name VARCHAR(100) NOT NULL COMMENT '标签名称',
    color VARCHAR(20) COMMENT '前端展示颜色',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_tag_code (tag_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ETF 标签定义';

-- ETF 与标签多对多关系
CREATE TABLE IF NOT EXISTS fund_tag_relation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    fund_code VARCHAR(20) NOT NULL COMMENT '基金代码',
    tag_id BIGINT NOT NULL COMMENT '标签 ID',
    UNIQUE KEY uk_fund_tag (fund_code, tag_id),
    KEY idx_tag_id (tag_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ETF 标签关联';

-- 策略配置
CREATE TABLE IF NOT EXISTS strategy_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    strategy_type VARCHAR(20) NOT NULL COMMENT '策略类型：ACTIVE/BALANCED',
    short_momentum_window INT NOT NULL COMMENT '短期动量窗口',
    long_momentum_window INT NOT NULL COMMENT '长期动量窗口',
    up_days_threshold DECIMAL(5,4) NOT NULL COMMENT '上涨天数占比阈值',
    ma_window INT NOT NULL COMMENT '均线周期',
    volatility_window INT NOT NULL COMMENT '波动率窗口',
    max_holding_count INT NOT NULL COMMENT '最大持仓数',
    single_weight_cap DECIMAL(5,4) NOT NULL COMMENT '单只权重上限',
    rebalancing_frequency VARCHAR(20) NOT NULL COMMENT '调仓频率：WEEKLY/MONTHLY',
    cooling_period_days INT NOT NULL COMMENT '冷静期天数',
    allocation_ratio DECIMAL(5,4) NOT NULL COMMENT '资金占比',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '0-禁用 1-启用',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_strategy_type (strategy_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='策略配置';

-- 策略计算结果
CREATE TABLE IF NOT EXISTS strategy_result (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    trade_date DATE NOT NULL COMMENT '交易日期',
    strategy_type VARCHAR(20) NOT NULL COMMENT '策略类型',
    market_status VARCHAR(20) NOT NULL COMMENT '大盘状态：STRONG/WEAK',
    total_weight DECIMAL(5,4) NOT NULL COMMENT '目标总仓位',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_trade_date (trade_date),
    KEY idx_strategy_type (strategy_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='策略计算结果';

-- 策略持仓建议明细
CREATE TABLE IF NOT EXISTS strategy_position (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    result_id BIGINT NOT NULL COMMENT '策略结果 ID',
    fund_code VARCHAR(20) NOT NULL COMMENT '基金代码',
    fund_name VARCHAR(100) COMMENT '基金名称',
    weight DECIMAL(5,4) NOT NULL COMMENT '权重',
    source_strategy VARCHAR(20) NOT NULL COMMENT '来源策略',
    reason VARCHAR(500) COMMENT '入选原因',
    KEY idx_result_id (result_id),
    KEY idx_fund_code (fund_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='策略持仓建议明细';

-- 回测记录
CREATE TABLE IF NOT EXISTS backtest_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    start_date DATE NOT NULL COMMENT '回测开始日期',
    end_date DATE NOT NULL COMMENT '回测结束日期',
    strategy_types VARCHAR(100) NOT NULL COMMENT '使用的策略类型',
    annual_return DECIMAL(10,4) COMMENT '年化收益',
    max_drawdown DECIMAL(10,4) COMMENT '最大回撤',
    sharpe_ratio DECIMAL(10,4) COMMENT '夏普比率',
    detail_json TEXT COMMENT '详细结果 JSON',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='回测记录';
