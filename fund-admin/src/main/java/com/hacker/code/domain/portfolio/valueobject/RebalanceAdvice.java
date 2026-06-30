package com.hacker.code.domain.portfolio.valueobject;

import com.hacker.code.domain.strategy.entity.StrategyResult;
import com.hacker.code.domain.strategy.valueobject.MarketStatus;
import lombok.Value;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Value
public class RebalanceAdvice {

    LocalDate tradeDate;
    MarketStatus marketStatus;
    List<StrategyResult> subResults;
    List<Position> mergedPositions;

    public RebalanceAdvice(LocalDate tradeDate, MarketStatus marketStatus, List<StrategyResult> subResults) {
        this(tradeDate, marketStatus, subResults, null);
    }

    public RebalanceAdvice(LocalDate tradeDate, MarketStatus marketStatus, List<StrategyResult> subResults, List<Position> mergedPositions) {
        this.tradeDate = tradeDate;
        this.marketStatus = marketStatus;
        this.subResults = subResults == null ? new ArrayList<>() : subResults;
        this.mergedPositions = mergedPositions == null ? new ArrayList<>() : mergedPositions;
    }
}
