USE momentum_strategy;

-- 将历史 GOLD 类型重命名为 COMMODITY，与新的基金类型枚举保持一致
UPDATE fund SET fund_type = 'COMMODITY' WHERE fund_type = 'GOLD';
