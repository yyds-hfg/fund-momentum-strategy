package com.hacker.code.domain.strategy.valueobject;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class MomentumTrendResult {

    private BigDecimal slope7;
    private BigDecimal slope14;
    private BigDecimal slope20;
    private BigDecimal sigma;
    private MomentumTrend trend;
    private String description;
}
