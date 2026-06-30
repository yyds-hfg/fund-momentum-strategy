package com.hacker.code.domain.strategy.repository;

import com.hacker.code.domain.strategy.entity.FundMomentumTrend;
import com.hacker.code.domain.strategy.valueobject.StrategyType;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface FundMomentumTrendRepository {

    void saveAll(List<FundMomentumTrend> trends);

    Optional<FundMomentumTrend> findByStrategyTypeAndFundCodeAndTradeDate(StrategyType strategyType,
                                                                          String fundCode,
                                                                          LocalDate tradeDate);

    List<FundMomentumTrend> findByStrategyTypeAndTradeDate(StrategyType strategyType, LocalDate tradeDate);

    void deleteByStrategyTypeAndTradeDate(StrategyType strategyType, LocalDate tradeDate);
}
