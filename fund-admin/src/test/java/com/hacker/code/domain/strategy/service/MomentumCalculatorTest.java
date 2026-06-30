package com.hacker.code.domain.strategy.service;

import com.hacker.code.domain.fund.valueobject.Nav;
import com.hacker.code.domain.strategy.valueobject.Momentum;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MomentumCalculatorTest {

    private final MomentumCalculator calculator = new MomentumCalculator();

    @Test
    void shouldCalculateMomentum() {
        List<Nav> history = List.of(
                nav("2024-01-01", "100"),
                nav("2024-01-02", "101"),
                nav("2024-01-03", "102"),
                nav("2024-01-04", "103"),
                nav("2024-01-05", "104"),
                nav("2024-01-08", "110")
        );

        Momentum momentum = calculator.calculate(history, 5);

        // (110 - 100) / 100 * 100 = 10%
        assertEquals(0, momentum.getValue().compareTo(BigDecimal.valueOf(10.0)));
    }

    private Nav nav(String date, String close) {
        return new Nav(LocalDate.parse(date), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal(close), 0L);
    }
}
