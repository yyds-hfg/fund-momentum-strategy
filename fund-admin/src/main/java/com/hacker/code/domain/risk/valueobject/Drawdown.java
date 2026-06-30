package com.hacker.code.domain.risk.valueobject;

import lombok.Value;

import java.math.BigDecimal;

@Value
public class Drawdown {

    BigDecimal peakNav;
    BigDecimal currentNav;
    BigDecimal drawdownRatio;

    public boolean exceeds(BigDecimal threshold) {
        return drawdownRatio.compareTo(threshold) > 0;
    }
}
