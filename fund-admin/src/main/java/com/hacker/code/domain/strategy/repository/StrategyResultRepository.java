package com.hacker.code.domain.strategy.repository;

import com.hacker.code.domain.strategy.entity.StrategyResult;
import com.hacker.code.domain.strategy.valueobject.StrategyType;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface StrategyResultRepository {

    void save(StrategyResult result);

    Optional<StrategyResult> findLatest(StrategyType strategyType);

    List<StrategyResult> findByDate(LocalDate tradeDate);

    List<StrategyResult> findByDateRange(LocalDate startDate, LocalDate endDate);
}
