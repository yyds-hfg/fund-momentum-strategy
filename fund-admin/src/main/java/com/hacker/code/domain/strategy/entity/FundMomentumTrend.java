package com.hacker.code.domain.strategy.entity;

import com.hacker.code.domain.strategy.valueobject.MomentumTrend;
import com.hacker.code.domain.strategy.valueobject.StrategyType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class FundMomentumTrend {

    private Long id;
    private StrategyType strategyType;
    private String fundCode;
    private LocalDate tradeDate;
    private BigDecimal slope7;
    private BigDecimal slope14;
    private BigDecimal slope20;
    private BigDecimal sigma;
    private MomentumTrend trend;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
