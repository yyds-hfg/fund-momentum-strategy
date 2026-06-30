package com.hacker.code.domain.strategy.valueobject;

import lombok.Value;

import java.math.BigDecimal;

@Value
public class Momentum {

    int windowDays;
    BigDecimal value;
}
