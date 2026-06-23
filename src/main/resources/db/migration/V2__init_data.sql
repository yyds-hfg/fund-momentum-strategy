USE momentum_strategy;

-- 初始候选 ETF 池
INSERT INTO fund (fund_code, fund_name, fund_type, status) VALUES
('510300', '沪深300ETF', 'WIDE_BASE', 1),
('510500', '中证500ETF', 'WIDE_BASE', 1),
('159949', '创业板50ETF', 'WIDE_BASE', 1),
('588000', '科创50ETF', 'WIDE_BASE', 1),
('512480', '半导体ETF', 'SECTOR', 1),
('516100', '新能源ETF', 'SECTOR', 1),
('512010', '医药ETF', 'SECTOR', 1),
('512880', '证券ETF', 'SECTOR', 1),
('512660', '军工ETF', 'SECTOR', 1),
('511010', '国债ETF', 'BOND', 1),
('518880', '黄金ETF', 'GOLD', 1),
('000852', '中证A500', 'OTHER', 1)
ON DUPLICATE KEY UPDATE fund_name = VALUES(fund_name), fund_type = VALUES(fund_type), status = VALUES(status);

-- 初始标签
INSERT INTO fund_tag (tag_code, tag_name, color) VALUES
('CORE', '核心资产', '#1890ff'),
('TECH', '科技', '#722ed1'),
('GROWTH', '成长', '#52c41a'),
('DEFENSE', '防御', '#faad14'),
('HIGH_BETA', '高弹性', '#f5222d'),
('BOND_LIKE', '债性', '#13c2c2')
ON DUPLICATE KEY UPDATE tag_name = VALUES(tag_name), color = VALUES(color);

-- 为 ETF 打标签
INSERT INTO fund_tag_relation (fund_code, tag_id)
SELECT f.fund_code, t.id
FROM fund f, fund_tag t
WHERE (f.fund_code = '510300' AND t.tag_code = 'CORE')
   OR (f.fund_code = '510500' AND t.tag_code IN ('GROWTH', 'HIGH_BETA'))
   OR (f.fund_code = '159949' AND t.tag_code IN ('GROWTH', 'HIGH_BETA'))
   OR (f.fund_code = '588000' AND t.tag_code IN ('TECH', 'GROWTH', 'HIGH_BETA'))
   OR (f.fund_code = '512480' AND t.tag_code IN ('TECH', 'HIGH_BETA'))
   OR (f.fund_code = '516100' AND t.tag_code IN ('GROWTH', 'HIGH_BETA'))
   OR (f.fund_code = '512010' AND t.tag_code IN ('DEFENSE', 'GROWTH'))
   OR (f.fund_code = '512880' AND t.tag_code IN ('HIGH_BETA', 'CORE'))
   OR (f.fund_code = '512660' AND t.tag_code IN ('HIGH_BETA', 'TECH'))
   OR (f.fund_code = '511010' AND t.tag_code IN ('DEFENSE', 'BOND_LIKE'))
   OR (f.fund_code = '518880' AND t.tag_code IN ('DEFENSE'))
   OR (f.fund_code = '000852' AND t.tag_code IN ('CORE'))
ON DUPLICATE KEY UPDATE fund_code = fund_code;

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
