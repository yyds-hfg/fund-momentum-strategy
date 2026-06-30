package com.hacker.code.domain.portfolio.service;

import com.hacker.code.domain.portfolio.valueobject.RebalanceAdvice;
import com.hacker.code.domain.strategy.entity.StrategyResult;
import com.hacker.code.domain.strategy.valueobject.MarketStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class PortfolioMergeService {

    public RebalanceAdvice merge(LocalDate tradeDate, MarketStatus marketStatus, List<StrategyResult> subResults) {
        return new RebalanceAdvice(tradeDate, marketStatus, new ArrayList<>(subResults));
    }
}
