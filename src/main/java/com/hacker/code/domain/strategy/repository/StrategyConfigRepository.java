package com.hacker.code.domain.strategy.repository;

import com.hacker.code.domain.strategy.entity.StrategyConfig;
import com.hacker.code.domain.strategy.valueobject.StrategyType;

import java.util.List;
import java.util.Optional;

public interface StrategyConfigRepository {

    Optional<StrategyConfig> findByType(StrategyType strategyType);

    List<StrategyConfig> findAllEnabled();

    void save(StrategyConfig config);

    void update(StrategyConfig config);
}
