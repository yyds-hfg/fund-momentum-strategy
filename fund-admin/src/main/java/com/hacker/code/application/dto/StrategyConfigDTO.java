package com.hacker.code.application.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class StrategyConfigDTO {

    private Long id;
    private String strategyType;
    private String strategyName;
    private Integer shortMomentumWindow;
    private Integer longMomentumWindow;
    private BigDecimal upDaysThreshold;
    private Integer maWindow;
    private Integer volatilityWindow;
    private Integer maxHoldingCount;
    private BigDecimal singleWeightCap;
    private String rebalancingFrequency;
    private Integer coolingPeriodDays;
    private BigDecimal allocationRatio;
    private Integer status;
}
