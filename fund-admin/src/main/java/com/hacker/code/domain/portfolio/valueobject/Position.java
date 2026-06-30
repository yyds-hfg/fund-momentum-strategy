package com.hacker.code.domain.portfolio.valueobject;

import com.hacker.code.domain.strategy.valueobject.StrategyType;
import lombok.Value;

import java.math.BigDecimal;

@Value
public class Position {

    String fundCode;
    String fundName;
    BigDecimal weight;
    StrategyType sourceStrategy;
    String reason;
}
