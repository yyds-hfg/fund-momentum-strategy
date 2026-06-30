package com.hacker.code.domain.strategy.service;

import com.hacker.code.domain.fund.valueobject.Nav;
import com.hacker.code.domain.strategy.valueobject.UpQuality;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UpQualityCalculatorTest {

    private final UpQualityCalculator calculator = new UpQualityCalculator();

    @Test
    void shouldCalculateUpDaysRatio() {
        List<Nav> history = List.of(
                nav("2024-01-01", "100"), // base
                nav("2024-01-02", "101"), // up
                nav("2024-01-03", "100"), // down
                nav("2024-01-04", "102"), // up
                nav("2024-01-05", "101"), // down
                nav("2024-01-08", "103")  // up
        );

        UpQuality quality = calculator.calculate(history, 5);

        // 5 days, 3 up -> 60%
        assertEquals(0, quality.getUpDaysRatio().compareTo(new BigDecimal("0.6")));
    }

    private Nav nav(String date, String close) {
        return new Nav(LocalDate.parse(date), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal(close), 0L);
    }
}
