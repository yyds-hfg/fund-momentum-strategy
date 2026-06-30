package com.hacker.code.domain.portfolio.service;

import com.hacker.code.domain.portfolio.valueobject.Position;
import com.hacker.code.domain.portfolio.valueobject.RebalanceAdvice;
import com.hacker.code.domain.strategy.entity.StrategyResult;
import com.hacker.code.domain.strategy.valueobject.MarketStatus;
import com.hacker.code.domain.strategy.valueobject.StrategyType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PortfolioMergeServiceTest {

    private final PortfolioMergeService mergeService = new PortfolioMergeService();

    @Test
    void shouldMergePositionsByFundCode() {
        StrategyResult balanced = new StrategyResult();
        balanced.setStrategyType(StrategyType.BALANCED);
        balanced.addPosition(new Position("510300", "沪深300ETF", new BigDecimal("0.3"), StrategyType.BALANCED, "平衡策略"));
        balanced.addPosition(new Position("159915", "创业板ETF", new BigDecimal("0.2"), StrategyType.BALANCED, "平衡策略"));

        StrategyResult active = new StrategyResult();
        active.setStrategyType(StrategyType.ACTIVE);
        active.addPosition(new Position("510300", "沪深300ETF", new BigDecimal("0.4"), StrategyType.ACTIVE, "积极策略"));
        active.addPosition(new Position("518880", "黄金ETF", new BigDecimal("0.1"), StrategyType.ACTIVE, "积极策略"));

        RebalanceAdvice advice = mergeService.merge(LocalDate.of(2024, 6, 14), MarketStatus.STRONG,
                List.of(balanced, active));

        assertEquals(2, advice.getSubResults().size());
        assertEquals(3, advice.getMergedPositions().size());

        BigDecimal totalWeight = advice.getMergedPositions().stream()
                .map(Position::getWeight)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals(0, totalWeight.compareTo(BigDecimal.ONE));

        Position merged510300 = advice.getMergedPositions().stream()
                .filter(p -> "510300".equals(p.getFundCode()))
                .findFirst()
                .orElseThrow();
        assertEquals(0, merged510300.getWeight().compareTo(new BigDecimal("0.7")));
        assertEquals(StrategyType.MERGED, merged510300.getSourceStrategy());
        assertTrue(merged510300.getReason().contains("平衡策略"));
        assertTrue(merged510300.getReason().contains("积极策略"));
    }

    @Test
    void shouldHandleEmptySubResults() {
        RebalanceAdvice advice = mergeService.merge(LocalDate.of(2024, 6, 14), MarketStatus.WEAK, List.of());
        assertTrue(advice.getMergedPositions().isEmpty());
    }
}
