package com.hacker.code.domain.portfolio.service;

import com.hacker.code.domain.portfolio.valueobject.Position;
import com.hacker.code.domain.strategy.entity.StrategyConfig;
import com.hacker.code.domain.strategy.valueobject.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LowVolatilityWeightingServiceTest {

    private final LowVolatilityWeightingService weightingService = new LowVolatilityWeightingService();

    @Test
    void shouldAllocateByInverseVolatility() {
        StrategyConfig config = new StrategyConfig();
        config.setStrategyType(StrategyType.BALANCED);
        config.setSingleWeightCap(new BigDecimal("0.6"));

        ScreenedETF etf1 = ScreenedETF.builder()
                .fundCode("A")
                .fundName("A-ETF")
                .volatility(new Volatility(10, new BigDecimal("0.02")))
                .longMomentum(new Momentum(20, BigDecimal.TEN))
                .upQuality(new UpQuality(20, new BigDecimal("0.6")))
                .movingAverage(new MovingAverage(60, BigDecimal.ONE))
                .closeNav(BigDecimal.TEN)
                .build();

        ScreenedETF etf2 = ScreenedETF.builder()
                .fundCode("B")
                .fundName("B-ETF")
                .volatility(new Volatility(10, new BigDecimal("0.04")))
                .longMomentum(new Momentum(20, BigDecimal.TEN))
                .upQuality(new UpQuality(20, new BigDecimal("0.6")))
                .movingAverage(new MovingAverage(60, BigDecimal.ONE))
                .closeNav(BigDecimal.TEN)
                .build();

        List<Position> positions = weightingService.allocate(List.of(etf1, etf2), config, new BigDecimal("0.70"));

        assertEquals(2, positions.size());
        // etf1 波动小，权重应更高
        assertTrue(positions.get(0).getWeight().compareTo(positions.get(1).getWeight()) > 0);
        BigDecimal total = positions.stream().map(Position::getWeight).reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals(0, total.compareTo(new BigDecimal("0.70")));
    }
}
