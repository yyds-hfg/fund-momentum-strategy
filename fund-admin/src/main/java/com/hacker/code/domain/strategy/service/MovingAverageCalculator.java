package com.hacker.code.domain.strategy.service;

import com.hacker.code.domain.fund.valueobject.Nav;
import com.hacker.code.domain.strategy.valueobject.MovingAverage;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class MovingAverageCalculator {

    public MovingAverage calculate(List<Nav> navHistory, int windowDays) {
        if (navHistory == null || navHistory.size() < windowDays) {
            return new MovingAverage(windowDays, BigDecimal.ZERO);
        }
        List<Nav> window = navHistory.subList(navHistory.size() - windowDays, navHistory.size());
        BigDecimal sum = window.stream()
                .map(Nav::getCloseNav)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal avg = sum.divide(BigDecimal.valueOf(windowDays), 6, RoundingMode.HALF_UP);
        return new MovingAverage(windowDays, avg);
    }
}
