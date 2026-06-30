package com.hacker.code.domain.portfolio.service;

import com.hacker.code.domain.portfolio.valueobject.Position;
import com.hacker.code.domain.strategy.entity.StrategyConfig;
import com.hacker.code.domain.strategy.valueobject.ScreenedETF;
import com.hacker.code.domain.strategy.valueobject.StrategyType;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
public class LowVolatilityWeightingService {

    private static final BigDecimal HUNDRED = new BigDecimal("100");
    private static final int SCALE = 6;

    public List<Position> allocate(List<ScreenedETF> screenedETFs, StrategyConfig config, BigDecimal totalAllocation) {
        if (screenedETFs == null || screenedETFs.isEmpty()) {
            return new ArrayList<>();
        }

        BigDecimal totalInverseVol = screenedETFs.stream()
                .map(etf -> inverseVol(etf.getVolatility().getValue()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalInverseVol.compareTo(BigDecimal.ZERO) == 0) {
            return new ArrayList<>();
        }

        List<Position> positions = new ArrayList<>();
        for (ScreenedETF etf : screenedETFs) {
            BigDecimal inverseVol = inverseVol(etf.getVolatility().getValue());
            BigDecimal weight = inverseVol.divide(totalInverseVol, SCALE, RoundingMode.HALF_UP)
                    .multiply(totalAllocation);
            positions.add(new Position(
                    etf.getFundCode(),
                    etf.getFundName(),
                    weight,
                    config.getStrategyType(),
                    buildReason(etf)
            ));
        }

        return applySingleCapAndNormalize(positions, config.getSingleWeightCap(), totalAllocation);
    }

    private BigDecimal inverseVol(BigDecimal volatility) {
        if (volatility == null || volatility.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.ONE.divide(volatility, SCALE, RoundingMode.HALF_UP);
    }

    private List<Position> applySingleCapAndNormalize(List<Position> positions, BigDecimal cap, BigDecimal totalAllocation) {
        BigDecimal total = positions.stream()
                .map(Position::getWeight)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (total.compareTo(BigDecimal.ZERO) == 0) {
            return positions;
        }

        List<Position> result = new ArrayList<>();
        for (Position pos : positions) {
            BigDecimal normalized = pos.getWeight().divide(total, SCALE, RoundingMode.HALF_UP).multiply(totalAllocation);
            if (normalized.compareTo(cap) > 0) {
                normalized = cap;
            }
            result.add(new Position(pos.getFundCode(), pos.getFundName(), normalized, pos.getSourceStrategy(), pos.getReason()));
        }

        // 截断后重新归一化到总仓位
        BigDecimal newTotal = result.stream()
                .map(Position::getWeight)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (newTotal.compareTo(totalAllocation) != 0 && newTotal.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal factor = totalAllocation.divide(newTotal, SCALE, RoundingMode.HALF_UP);
            List<Position> finalResult = new ArrayList<>();
            for (Position pos : result) {
                BigDecimal adjusted = pos.getWeight().multiply(factor).min(cap);
                finalResult.add(new Position(pos.getFundCode(), pos.getFundName(), adjusted, pos.getSourceStrategy(), pos.getReason()));
            }
            return finalResult;
        }

        return result;
    }

    private String buildReason(ScreenedETF etf) {
        return String.format("动量%.2f%%，上涨占比%.1f%%，波动%.4f，收盘%.4f > MA%.4f",
                etf.getLongMomentum().getValue(),
                etf.getUpQuality().getUpDaysRatio().multiply(HUNDRED),
                etf.getVolatility().getValue(),
                etf.getCloseNav(),
                etf.getMovingAverage().getValue());
    }
}
