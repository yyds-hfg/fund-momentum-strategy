USE momentum_strategy;

-- 初始候选 ETF 池
INSERT INTO fund (fund_code, fund_name, fund_type, description, status) VALUES
('510300', '沪深300ETF', 'WIDE_BASE', 'A股核心宽基，覆盖大盘蓝筹', 1),
('510500', '中证500ETF', 'WIDE_BASE', '中盘成长宽基', 1),
('159949', '创业板50ETF', 'WIDE_BASE', '创业板头部成长企业', 1),
('588000', '科创50ETF', 'WIDE_BASE', '科创板硬科技代表', 1),
('512480', '半导体ETF', 'SECTOR', '半导体芯片行业', 1),
('516100', '新能源ETF', 'SECTOR', '新能源产业链', 1),
('512010', '医药ETF', 'SECTOR', '医药生物行业', 1),
('512880', '证券ETF', 'SECTOR', '证券公司板块', 1),
('512660', '军工ETF', 'SECTOR', '国防军工行业', 1),
('511010', '国债ETF', 'BOND', '长久期国债，避险工具', 1),
('518880', '黄金ETF', 'GOLD', '黄金现货，避险/抗通胀', 1),
('000852', '中证A500', 'OTHER', '大盘风向标，用于风控判断', 1)
ON DUPLICATE KEY UPDATE fund_name = VALUES(fund_name), fund_type = VALUES(fund_type), description = VALUES(description), status = VALUES(status);

-- 平衡型策略配置
INSERT INTO strategy_config (
    strategy_type, short_momentum_window, long_momentum_window, up_days_threshold,
    ma_window, volatility_window, max_holding_count, single_weight_cap,
    rebalancing_frequency, cooling_period_days, allocation_ratio, status
) VALUES (
    'BALANCED', 10, 20, 0.50,
    60, 20, 3, 0.40,
    'WEEKLY', 3, 0.70, 1
) ON DUPLICATE KEY UPDATE
    short_momentum_window = VALUES(short_momentum_window),
    long_momentum_window = VALUES(long_momentum_window),
    up_days_threshold = VALUES(up_days_threshold),
    ma_window = VALUES(ma_window),
    volatility_window = VALUES(volatility_window),
    max_holding_count = VALUES(max_holding_count),
    single_weight_cap = VALUES(single_weight_cap),
    rebalancing_frequency = VALUES(rebalancing_frequency),
    cooling_period_days = VALUES(cooling_period_days),
    allocation_ratio = VALUES(allocation_ratio),
    status = VALUES(status);

-- 积极型策略配置
INSERT INTO strategy_config (
    strategy_type, short_momentum_window, long_momentum_window, up_days_threshold,
    ma_window, volatility_window, max_holding_count, single_weight_cap,
    rebalancing_frequency, cooling_period_days, allocation_ratio, status
) VALUES (
    'ACTIVE', 5, 10, 0.50,
    20, 10, 2, 0.50,
    'WEEKLY', 3, 0.30, 1
) ON DUPLICATE KEY UPDATE
    short_momentum_window = VALUES(short_momentum_window),
    long_momentum_window = VALUES(long_momentum_window),
    up_days_threshold = VALUES(up_days_threshold),
    ma_window = VALUES(ma_window),
    volatility_window = VALUES(volatility_window),
    max_holding_count = VALUES(max_holding_count),
    single_weight_cap = VALUES(single_weight_cap),
    rebalancing_frequency = VALUES(rebalancing_frequency),
    cooling_period_days = VALUES(cooling_period_days),
    allocation_ratio = VALUES(allocation_ratio),
    status = VALUES(status);
