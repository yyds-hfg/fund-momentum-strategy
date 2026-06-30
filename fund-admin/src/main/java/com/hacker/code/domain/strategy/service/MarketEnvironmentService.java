package com.hacker.code.domain.strategy.service;

import com.hacker.code.domain.fund.repository.NavDataRepository;
import com.hacker.code.domain.fund.valueobject.Nav;
import com.hacker.code.domain.strategy.valueobject.MarketSignal;
import com.hacker.code.domain.strategy.valueobject.MovingAverage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MarketEnvironmentService {

    public static final String BENCHMARK_CODE = "000852";

    private final NavDataRepository navDataRepository;
    private final MovingAverageCalculator movingAverageCalculator;

    public MarketSignal judge(LocalDate tradeDate, int maWindow) {
        List<Nav> history = navDataRepository.findByDateRange(BENCHMARK_CODE, tradeDate.minusDays(maWindow + 30), tradeDate);
        if (history.isEmpty()) {
            return new MarketSignal(BENCHMARK_CODE, maWindow, false);
        }
        Nav latest = history.get(history.size() - 1);
        MovingAverage ma = movingAverageCalculator.calculate(history, maWindow);
        boolean bullish = latest.getCloseNav().compareTo(ma.getValue()) > 0;
        return new MarketSignal(BENCHMARK_CODE, maWindow, bullish);
    }

}
