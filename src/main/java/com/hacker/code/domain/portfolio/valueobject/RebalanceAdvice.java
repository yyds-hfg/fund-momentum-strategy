package com.hacker.code.domain.portfolio.valueobject;

import com.hacker.code.domain.strategy.entity.StrategyResult;
import com.hacker.code.domain.strategy.valueobject.MarketStatus;
import lombok.Value;

import java.time.LocalDate;
import java.util.List;

@Value
public class RebalanceAdvice {

    LocalDate tradeDate;
    MarketStatus marketStatus;
    List<StrategyResult> subResults;
}
