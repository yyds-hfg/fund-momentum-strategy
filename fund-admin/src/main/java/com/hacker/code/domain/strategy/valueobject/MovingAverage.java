package com.hacker.code.domain.strategy.valueobject;

import lombok.Value;

import java.math.BigDecimal;

@Value
public class MovingAverage {

    int windowDays;
    BigDecimal value;
}
