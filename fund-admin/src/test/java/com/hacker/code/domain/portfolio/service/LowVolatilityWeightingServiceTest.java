package com.hacker.code.domain.portfolio.service;

import com.hacker.code.domain.portfolio.valueobject.Position;
import com.hacker.code.domain.strategy.entity.StrategyConfig;
import com.hacker.code.domain.strategy.valueobject.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LowVolatilityWeightingServiceTest {

    private final LowVolatilityWeightingService weightingService = new LowVolatilityWeightingService();

    @Test
    void shouldAllocateWeightsByInverseVolatility() {
        StrategyConfig config = new StrategyConfig();
        config.setStrategyType(StrategyType.BALANCED);
        config.setSingleWeightCap(new BigDecimal("0.5"));

        List<ScreenedETF> etfs = List.of(
                buildEtf("510300", "沪深300", new BigDecimal("0.20")),
                buildEtf("159915", "创业板", new BigDecimal("0.40")),
                buildEtf("518880", "黄金", new BigDecimal("0.10"))
        );

        List<Position> positions = weightingService.allocate(etfs, config, BigDecimal.ONE);

        assertEquals(3, positions.size());
        BigDecimal totalWeight = positions.stream()
                .map(Position::getWeight)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals(0, totalWeight.compareTo(BigDecimal.ONE));

        // 波动率最低的黄金应获得最高权重
        Position gold = positions.stream().filter(p -> "518880".equals(p.getFundCode())).findFirst().orElseThrow();
        Position chiNext = positions.stream().filter(p -> "159915".equals(p.getFundCode())).findFirst().orElseThrow();
        assertTrue(gold.getWeight().compareTo(chiNext.getWeight()) > 0);
    }

    @Test
    void shouldRespectSingleWeightCapAndNormalize() {
        StrategyConfig config = new StrategyConfig();
        config.setStrategyType(StrategyType.BALANCED);
        config.setSingleWeightCap(new BigDecimal("0.35"));

        List<ScreenedETF> etfs = List.of(
                buildEtf("510300", "沪深300", new BigDecimal("0.05")),
                buildEtf("159915", "创业板", new BigDecimal("0.10")),
                buildEtf("518880", "黄金", new BigDecimal("0.05"))
        );

        List<Position> positions = weightingService.allocate(etfs, config, BigDecimal.ONE);

        assertEquals(3, positions.size());
        for (Position position : positions) {
            assertTrue(position.getWeight().compareTo(config.getSingleWeightCap()) <= 0,
                    () -> position.getFundCode() + " weight " + position.getWeight() + " exceeds cap " + config.getSingleWeightCap());
        }

        BigDecimal totalWeight = positions.stream()
                .map(Position::getWeight)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals(0, totalWeight.compareTo(BigDecimal.ONE),
                () -> "Total weight should be 1, but was " + totalWeight);
    }

    @Test
    void shouldReturnEmptyForEmptyInput() {
        StrategyConfig config = new StrategyConfig();
        assertTrue(weightingService.allocate(List.of(), config, BigDecimal.ONE).isEmpty());
    }

    private ScreenedETF buildEtf(String fundCode, String fundName, BigDecimal volatility) {
        return ScreenedETF.builder()
                .fundCode(fundCode)
                .fundName(fundName)
                .closeNav(BigDecimal.ONE)
                .shortMomentum(new Momentum(10, BigDecimal.ZERO))
                .longMomentum(new Momentum(20, BigDecimal.ZERO))
                .upQuality(new UpQuality(20, BigDecimal.ZERO))
                .volatility(new Volatility(20, volatility))
                .movingAverage(new MovingAverage(20, BigDecimal.ONE))
                .build();
    }
}
