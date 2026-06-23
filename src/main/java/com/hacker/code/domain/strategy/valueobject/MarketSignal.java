package com.hacker.code.domain.strategy.valueobject;

import lombok.Value;

@Value
public class MarketSignal {

    String benchmarkCode;
    int maWindow;
    boolean bullish;
}
