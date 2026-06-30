package com.hacker.code.domain.strategy.valueobject;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
public class ScreenedETF {

    String fundCode;
    String fundName;
    BigDecimal closeNav;
    Momentum shortMomentum;
    Momentum longMomentum;
    UpQuality upQuality;
    Volatility volatility;
    MovingAverage movingAverage;
}
