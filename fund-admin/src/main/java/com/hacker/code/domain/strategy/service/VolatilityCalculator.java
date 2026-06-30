package com.hacker.code.domain.strategy.service;

import com.hacker.code.domain.fund.valueobject.Nav;
import com.hacker.code.domain.strategy.valueobject.Volatility;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
public class VolatilityCalculator {

    public Volatility calculate(List<Nav> navHistory, int windowDays) {
        if (navHistory == null || navHistory.size() < windowDays + 1) {
            return new Volatility(windowDays, BigDecimal.ZERO);
        }
        List<Nav> window = navHistory.subList(navHistory.size() - windowDays - 1, navHistory.size());

        List<BigDecimal> returns = new ArrayList<>();
        for (int i = 1; i < window.size(); i++) {
            BigDecimal prev = window.get(i - 1).getCloseNav();
            BigDecimal curr = window.get(i).getCloseNav();
            if (prev.compareTo(BigDecimal.ZERO) == 0) {
                returns.add(BigDecimal.ZERO);
            } else {
                BigDecimal dailyReturn = curr.divide(prev, 6, RoundingMode.HALF_UP).subtract(BigDecimal.ONE);
                returns.add(dailyReturn);
            }
        }

        BigDecimal mean = returns.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(returns.size()), 6, RoundingMode.HALF_UP);

        BigDecimal variance = returns.stream()
                .map(r -> r.subtract(mean).pow(2))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(returns.size()), 6, RoundingMode.HALF_UP);

        BigDecimal std = variance.sqrt(new MathContext(6, RoundingMode.HALF_UP));
        return new Volatility(windowDays, std);
    }
}
