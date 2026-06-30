package com.hacker.code.domain.strategy.service;

import com.hacker.code.domain.fund.valueobject.Nav;
import com.hacker.code.domain.strategy.valueobject.Momentum;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class MomentumCalculator {

    public Momentum calculate(List<Nav> navHistory, int windowDays) {
        if (navHistory == null || navHistory.size() < windowDays + 1) {
            return new Momentum(windowDays, BigDecimal.ZERO);
        }
        Nav current = navHistory.get(navHistory.size() - 1);
        Nav past = navHistory.get(navHistory.size() - 1 - windowDays);

        BigDecimal value = current.getCloseNav().subtract(past.getCloseNav())
                .divide(past.getCloseNav(), 6, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        return new Momentum(windowDays, value);
    }
}
