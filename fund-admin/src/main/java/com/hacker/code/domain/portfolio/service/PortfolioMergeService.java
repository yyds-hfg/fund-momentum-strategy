package com.hacker.code.domain.portfolio.service;

import com.hacker.code.domain.portfolio.valueobject.Position;
import com.hacker.code.domain.portfolio.valueobject.RebalanceAdvice;
import com.hacker.code.domain.strategy.entity.StrategyResult;
import com.hacker.code.domain.strategy.valueobject.MarketStatus;
import com.hacker.code.domain.strategy.valueobject.StrategyType;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 组合合并服务。
 * <p>
 * 将多个子策略（如平衡型、积极型）的持仓按基金代码合并，
 * 生成一个总持仓视图，避免同一基金在多个策略中重复出现。
 */
@Service
public class PortfolioMergeService {

    private static final int SCALE = 6;

    /**
     * 合并子策略持仓。
     *
     * @param tradeDate  交易日期
     * @param marketStatus 市场状态
     * @param subResults 子策略结果列表
     * @return 包含原始子策略结果与合并后总持仓的调仓建议
     */
    public RebalanceAdvice merge(LocalDate tradeDate, MarketStatus marketStatus, List<StrategyResult> subResults) {
        List<Position> mergedPositions = mergePositions(subResults);
        return new RebalanceAdvice(tradeDate, marketStatus, new ArrayList<>(subResults), mergedPositions);
    }

    private List<Position> mergePositions(List<StrategyResult> subResults) {
        // 按基金代码汇总权重与原因
        Map<String, MergedPositionBuilder> builderMap = new LinkedHashMap<>();

        for (StrategyResult result : subResults) {
            for (Position position : result.getPositions()) {
                builderMap.computeIfAbsent(position.getFundCode(),
                                k -> new MergedPositionBuilder(position.getFundCode(), position.getFundName()))
                        .add(position.getWeight(), result.getStrategyType(), position.getReason());
            }
        }

        if (builderMap.isEmpty()) {
            return new ArrayList<>();
        }

        // 按权重降序排列
        List<MergedPositionBuilder> builders = builderMap.values().stream()
                .sorted(Comparator.comparing(MergedPositionBuilder::getWeight).reversed())
                .collect(Collectors.toList());

        // 归一化到 100%（子策略权重之和可能因弱势减仓而不等于 1）
        BigDecimal totalWeight = builders.stream()
                .map(MergedPositionBuilder::getWeight)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<Position> merged = new ArrayList<>();
        if (totalWeight.compareTo(BigDecimal.ZERO) == 0) {
            return merged;
        }

        for (MergedPositionBuilder builder : builders) {
            BigDecimal normalizedWeight = builder.getWeight()
                    .divide(totalWeight, SCALE, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.ONE)
                    .setScale(4, RoundingMode.HALF_UP);
            merged.add(new Position(
                    builder.getFundCode(),
                    builder.getFundName(),
                    normalizedWeight,
                    StrategyType.MERGED,
                    builder.buildReason()
            ));
        }

        return merged;
    }

    private static class MergedPositionBuilder {

        private final String fundCode;
        private final String fundName;
        private BigDecimal weight = BigDecimal.ZERO;
        private final List<String> reasons = new ArrayList<>();

        MergedPositionBuilder(String fundCode, String fundName) {
            this.fundCode = fundCode;
            this.fundName = fundName;
        }

        void add(BigDecimal weight, StrategyType strategyType, String reason) {
            this.weight = this.weight.add(weight);
            if (reason != null && !reason.isBlank()) {
                reasons.add(strategyType.getDescription() + ": " + reason);
            }
        }

        String getFundCode() {
            return fundCode;
        }

        String getFundName() {
            return fundName;
        }

        BigDecimal getWeight() {
            return weight;
        }

        String buildReason() {
            if (reasons.isEmpty()) {
                return "";
            }
            return String.join(" | ", reasons);
        }
    }
}
