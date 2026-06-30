package com.hacker.code.domain.strategy.service;

import com.hacker.code.domain.fund.valueobject.Nav;
import com.hacker.code.domain.strategy.entity.StrategyConfig;
import com.hacker.code.domain.strategy.valueobject.MomentumTrend;
import com.hacker.code.domain.strategy.valueobject.MomentumTrendResult;
import com.hacker.code.domain.strategy.valueobject.StrategyType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MomentumTrendCalculatorTest {

    private final MomentumCalculator momentumCalculator = new MomentumCalculator();
    private final UpQualityCalculator upQualityCalculator = new UpQualityCalculator();
    private final MomentumTrendCalculator calculator = new MomentumTrendCalculator(momentumCalculator, upQualityCalculator);

    @Test
    void shouldClassifySharpUpTrendForAcceleratingGrowth() {
        // 指数加速上涨：增长率本身也在提升，动量分数持续上升
        List<Nav> history = buildAcceleratingHistory(80, 0.0005);
        StrategyConfig config = buildConfig();

        MomentumTrendResult result = calculator.calculate(history, config);

        assertNotNull(result);
        assertEquals(MomentumTrend.SHARP_UP, result.getTrend());
    }

    @Test
    void shouldClassifySharpDownTrendForAcceleratingDecline() {
        // 指数加速下跌
        List<Nav> history = buildAcceleratingHistory(80, -0.0005);
        StrategyConfig config = buildConfig();

        MomentumTrendResult result = calculator.calculate(history, config);

        assertNotNull(result);
        assertEquals(MomentumTrend.SHARP_DOWN, result.getTrend());
    }

    @Test
    void shouldClassifyFlatTrendForSidewaysPrices() {
        List<Nav> history = buildFlatHistory(80);
        StrategyConfig config = buildConfig();

        MomentumTrendResult result = calculator.calculate(history, config);

        assertNotNull(result);
        assertTrue(result.getTrend() == MomentumTrend.FLAT || result.getTrend() == MomentumTrend.FLAT_UP
                        || result.getTrend() == MomentumTrend.FLAT_DOWN,
                "Expected flat-ish trend but was " + result.getTrend());
    }

    @Test
    void shouldReturnNullForInsufficientData() {
        List<Nav> history = buildAcceleratingHistory(10, 0.0005);
        StrategyConfig config = buildConfig();

        assertNull(calculator.calculate(history, config));
    }

    private List<Nav> buildAcceleratingHistory(int days, double exponent) {
        List<Nav> history = new ArrayList<>();
        for (int i = 0; i < days; i++) {
            double close = 100 * Math.exp(exponent * i * i);
            history.add(nav(LocalDate.of(2024, 1, 1).plusDays(i), close));
        }
        return history;
    }

    private List<Nav> buildFlatHistory(int days) {
        List<Nav> history = new ArrayList<>();
        for (int i = 0; i < days; i++) {
            double close = 100 + (i % 2 == 0 ? 0.2 : -0.2);
            history.add(nav(LocalDate.of(2024, 1, 1).plusDays(i), close));
        }
        return history;
    }

    private Nav nav(LocalDate date, double close) {
        return new Nav(date, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.valueOf(close), 0L);
    }

    private StrategyConfig buildConfig() {
        StrategyConfig config = new StrategyConfig();
        config.setStrategyType(StrategyType.BALANCED);
        config.setShortMomentumWindow(5);
        config.setLongMomentumWindow(20);
        return config;
    }
}
