package com.hacker.code.domain.strategy.service;

import com.hacker.code.domain.fund.entity.Fund;
import com.hacker.code.domain.fund.valueobject.Nav;
import com.hacker.code.domain.strategy.entity.StrategyConfig;
import com.hacker.code.domain.strategy.valueobject.ScreenedETF;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ETFScreeningService {

    private final MomentumCalculator momentumCalculator;
    private final UpQualityCalculator upQualityCalculator;
    private final VolatilityCalculator volatilityCalculator;
    private final MovingAverageCalculator movingAverageCalculator;

    public List<ScreenedETF> screen(List<Fund> candidates, StrategyConfig config, Map<String, List<Nav>> navHistoryMap) {
        return candidates.stream()
                .map(fund -> buildScreenedETF(fund, config, navHistoryMap.get(fund.getFundCode())))
                .filter(etf -> etf != null)
                .filter(etf -> passesAbsoluteMomentum(etf))
                .filter(etf -> passesUpQuality(etf, config.getUpDaysThreshold()))
                .filter(etf -> passesMovingAverage(etf))
                .sorted(Comparator.comparing((ScreenedETF etf) -> etf.getLongMomentum().getValue()).reversed())
                .limit(config.getMaxHoldingCount())
                .collect(Collectors.toList());
    }

    private ScreenedETF buildScreenedETF(Fund fund, StrategyConfig config, List<Nav> navHistory) {
        if (navHistory == null || navHistory.isEmpty()) {
            return null;
        }
        int maxWindow = Math.max(config.getLongMomentumWindow(), Math.max(config.getMaWindow(), config.getVolatilityWindow()));
        if (navHistory.size() < maxWindow + 1) {
            return null;
        }

        return ScreenedETF.builder()
                .fundCode(fund.getFundCode())
                .fundName(fund.getFundName())
                .closeNav(navHistory.get(navHistory.size() - 1).getCloseNav())
                .shortMomentum(momentumCalculator.calculate(navHistory, config.getShortMomentumWindow()))
                .longMomentum(momentumCalculator.calculate(navHistory, config.getLongMomentumWindow()))
                .upQuality(upQualityCalculator.calculate(navHistory, config.getLongMomentumWindow()))
                .volatility(volatilityCalculator.calculate(navHistory, config.getVolatilityWindow()))
                .movingAverage(movingAverageCalculator.calculate(navHistory, config.getMaWindow()))
                .build();
    }

    private boolean passesAbsoluteMomentum(ScreenedETF etf) {
        return etf.getLongMomentum().getValue().compareTo(BigDecimal.ZERO) > 0;
    }

    private boolean passesUpQuality(ScreenedETF etf, BigDecimal threshold) {
        return etf.getUpQuality().getUpDaysRatio().compareTo(threshold) > 0;
    }

    private boolean passesMovingAverage(ScreenedETF etf) {
        return etf.getCloseNav().compareTo(etf.getMovingAverage().getValue()) > 0;
    }
}
