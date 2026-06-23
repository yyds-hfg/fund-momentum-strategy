package com.hacker.code.domain.strategy.service;

import com.hacker.code.domain.fund.valueobject.Nav;
import com.hacker.code.domain.strategy.valueobject.Volatility;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class VolatilityCalculatorTest {

    private final VolatilityCalculator calculator = new VolatilityCalculator();

    @Test
    void shouldCalculateVolatility() {
        List<Nav> history = List.of(
                nav("2024-01-01", "100"),
                nav("2024-01-02", "101"),
                nav("2024-01-03", "100"),
                nav("2024-01-04", "102"),
                nav("2024-01-05", "101"),
                nav("2024-01-08", "103")
        );

        Volatility volatility = calculator.calculate(history, 5);

        assertTrue(volatility.getValue().compareTo(BigDecimal.ZERO) > 0);
    }

    private Nav nav(String date, String close) {
        return new Nav(LocalDate.parse(date), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal(close), 0L);
    }
}
