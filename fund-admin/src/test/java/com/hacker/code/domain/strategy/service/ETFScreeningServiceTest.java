package com.hacker.code.domain.strategy.service;

import com.hacker.code.domain.fund.entity.Fund;
import com.hacker.code.domain.fund.valueobject.FundStatus;
import com.hacker.code.domain.fund.valueobject.FundType;
import com.hacker.code.domain.fund.valueobject.Nav;
import com.hacker.code.domain.strategy.entity.StrategyConfig;
import com.hacker.code.domain.strategy.valueobject.Frequency;
import com.hacker.code.domain.strategy.valueobject.ScreenedETF;
import com.hacker.code.domain.strategy.valueobject.StrategyType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ETFScreeningServiceTest {

    private final MomentumCalculator momentumCalculator = new MomentumCalculator();
    private final UpQualityCalculator upQualityCalculator = new UpQualityCalculator();
    private final VolatilityCalculator volatilityCalculator = new VolatilityCalculator();
    private final MovingAverageCalculator movingAverageCalculator = new MovingAverageCalculator();
    private final ETFScreeningService screeningService = new ETFScreeningService(
            momentumCalculator, upQualityCalculator, volatilityCalculator, movingAverageCalculator
    );

    @Test
    void shouldScreenETFs() {
        StrategyConfig config = new StrategyConfig();
        config.setStrategyType(StrategyType.BALANCED);
        config.setShortMomentumWindow(2);
        config.setLongMomentumWindow(5);
        config.setUpDaysThreshold(new BigDecimal("0.5"));
        config.setMaWindow(3);
        config.setVolatilityWindow(5);
        config.setMaxHoldingCount(2);

        Fund fund = new Fund();
        fund.setFundCode("510300");
        fund.setFundName("沪深300ETF");
        fund.setFundType(FundType.WIDE_BASE);
        fund.setStatus(FundStatus.ENABLED);

        List<Nav> history = List.of(
                nav("2024-01-01", "100"),
                nav("2024-01-02", "101"),
                nav("2024-01-03", "100"),
                nav("2024-01-04", "102"),
                nav("2024-01-05", "101"),
                nav("2024-01-08", "103")
        );

        List<ScreenedETF> result = screeningService.screen(List.of(fund), config, Map.of("510300", history));

        assertEquals(1, result.size());
        assertEquals("510300", result.get(0).getFundCode());
        assertTrue(result.get(0).getLongMomentum().getValue().compareTo(BigDecimal.ZERO) > 0);
    }

    private Nav nav(String date, String close) {
        return new Nav(LocalDate.parse(date), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal(close), 0L);
    }
}
