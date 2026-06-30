package com.hacker.code.domain.portfolio.service;

import com.hacker.code.domain.portfolio.valueobject.Position;
import com.hacker.code.domain.strategy.entity.StrategyConfig;
import com.hacker.code.domain.strategy.valueobject.ScreenedETF;
import com.hacker.code.domain.strategy.valueobject.StrategyType;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 低波动率权重分配服务。
 * <p>
 * 基于逆波动率进行初始分配，对超过单仓上限的部分进行截断，
 * 并将剩余权重在未达上限的标的中递归分配，直至权重和精确等于目标总仓位。
 */
@Service
public class LowVolatilityWeightingService {

    private static final BigDecimal HUNDRED = new BigDecimal("100");
    private static final int SCALE = 8;

    public List<Position> allocate(List<ScreenedETF> screenedETFs, StrategyConfig config, BigDecimal totalAllocation) {
        if (screenedETFs == null || screenedETFs.isEmpty() || totalAllocation.compareTo(BigDecimal.ZERO) == 0) {
            return new ArrayList<>();
        }

        List<WeightedTarget> targets = initializeTargets(screenedETFs, config, totalAllocation);
        distributeRemaining(targets, totalAllocation, config.getSingleWeightCap());

        return buildPositions(targets, config.getStrategyType());
    }

    private List<WeightedTarget> initializeTargets(List<ScreenedETF> screenedETFs, StrategyConfig config, BigDecimal totalAllocation) {
        BigDecimal totalInverseVol = screenedETFs.stream()
                .map(etf -> inverseVol(etf.getVolatility().getValue()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalInverseVol.compareTo(BigDecimal.ZERO) == 0) {
            return new ArrayList<>();
        }

        List<WeightedTarget> targets = new ArrayList<>();
        for (ScreenedETF etf : screenedETFs) {
            BigDecimal inverseVol = inverseVol(etf.getVolatility().getValue());
            BigDecimal rawWeight = inverseVol.divide(totalInverseVol, SCALE, RoundingMode.HALF_UP)
                    .multiply(totalAllocation);
            targets.add(new WeightedTarget(etf, rawWeight));
        }
        return targets;
    }

    /**
     * 迭代分配剩余权重，直到总权重等于目标仓位或所有标的都触及上限。
     */
    private void distributeRemaining(List<WeightedTarget> targets, BigDecimal totalAllocation, BigDecimal singleCap) {
        if (targets.isEmpty()) {
            return;
        }

        // 先执行一次上限截断
        for (WeightedTarget target : targets) {
            if (target.weight.compareTo(singleCap) > 0) {
                target.weight = singleCap;
                target.capped = true;
            }
        }

        // 迭代再分配剩余权重
        for (int iteration = 0; iteration < targets.size() * 2; iteration++) {
            BigDecimal currentTotal = targets.stream()
                    .map(t -> t.weight)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal residual = totalAllocation.subtract(currentTotal);
            if (residual.abs().compareTo(BigDecimal.valueOf(1e-6)) <= 0) {
                break;
            }

            List<WeightedTarget> activeTargets = targets.stream()
                    .filter(t -> !t.capped)
                    .toList();

            if (activeTargets.isEmpty()) {
                break;
            }

            BigDecimal activeTotal = activeTargets.stream()
                    .map(t -> t.weight)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            if (activeTotal.compareTo(BigDecimal.ZERO) == 0) {
                // 未达上限标的无权重，按等权分配剩余
                BigDecimal equalShare = residual.divide(BigDecimal.valueOf(activeTargets.size()), SCALE, RoundingMode.HALF_UP);
                for (WeightedTarget target : activeTargets) {
                    target.weight = target.weight.add(equalShare);
                    if (target.weight.compareTo(singleCap) > 0) {
                        target.weight = singleCap;
                        target.capped = true;
                    }
                }
            } else {
                // 按当前权重比例放大或缩小
                BigDecimal scaleFactor = activeTotal.add(residual)
                        .divide(activeTotal, SCALE, RoundingMode.HALF_UP);
                for (WeightedTarget target : activeTargets) {
                    target.weight = target.weight.multiply(scaleFactor);
                    if (target.weight.compareTo(singleCap) > 0) {
                        target.weight = singleCap;
                        target.capped = true;
                    }
                }
            }
        }

        // 最终精修：如果仍有微小残差，加到权重最大且未达上限的标的上
        BigDecimal finalTotal = targets.stream()
                .map(t -> t.weight)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal finalResidual = totalAllocation.subtract(finalTotal);
        if (finalResidual.abs().compareTo(BigDecimal.valueOf(1e-6)) > 0) {
            targets.stream()
                    .filter(t -> !t.capped)
                    .max(Comparator.comparing(t -> t.weight))
                    .ifPresent(t -> t.weight = t.weight.add(finalResidual).max(BigDecimal.ZERO));
        }
    }

    private List<Position> buildPositions(List<WeightedTarget> targets, StrategyType strategyType) {
        List<Position> positions = new ArrayList<>();
        for (WeightedTarget target : targets) {
            if (target.weight.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            positions.add(new Position(
                    target.etf.getFundCode(),
                    target.etf.getFundName(),
                    target.weight.setScale(4, RoundingMode.HALF_UP),
                    strategyType,
                    buildReason(target.etf)
            ));
        }
        return positions;
    }

    private BigDecimal inverseVol(BigDecimal volatility) {
        if (volatility == null || volatility.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.ONE.divide(volatility, SCALE, RoundingMode.HALF_UP);
    }

    private String buildReason(ScreenedETF etf) {
        return String.format("动量%.2f%%，上涨占比%.1f%%，波动%.4f，收盘%.4f > MA%.4f",
                etf.getLongMomentum().getValue(),
                etf.getUpQuality().getUpDaysRatio().multiply(HUNDRED),
                etf.getVolatility().getValue(),
                etf.getCloseNav(),
                etf.getMovingAverage().getValue());
    }

    private static class WeightedTarget {

        final ScreenedETF etf;
        BigDecimal weight;
        boolean capped;

        WeightedTarget(ScreenedETF etf, BigDecimal weight) {
            this.etf = etf;
            this.weight = weight;
        }
    }
}
