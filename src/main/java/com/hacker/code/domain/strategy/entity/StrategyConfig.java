package com.hacker.code.domain.strategy.entity;

import com.hacker.code.domain.strategy.valueobject.Frequency;
import com.hacker.code.domain.strategy.valueobject.StrategyType;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class StrategyConfig {

    private Long id;
    private StrategyType strategyType;
    private Integer shortMomentumWindow;
    private Integer longMomentumWindow;
    private BigDecimal upDaysThreshold;
    private Integer maWindow;
    private Integer volatilityWindow;
    private Integer maxHoldingCount;
    private BigDecimal singleWeightCap;
    private Frequency rebalancingFrequency;
    private Integer coolingPeriodDays;
    private BigDecimal allocationRatio;
    private Integer status;
}
