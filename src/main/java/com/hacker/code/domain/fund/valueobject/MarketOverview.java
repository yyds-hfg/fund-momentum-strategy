package com.hacker.code.domain.fund.valueobject;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder(toBuilder = true)
public class MarketOverview {

    private final LocalDate tradeDate;

    private final Long shVolume;
    private final BigDecimal shAmount;
    private final Long szVolume;
    private final BigDecimal szAmount;
    private final Long totalVolume;
    private final BigDecimal totalAmount;

    private final BigDecimal mainInflow;
    private final BigDecimal superLargeInflow;
    private final BigDecimal largeInflow;
    private final BigDecimal mediumInflow;
    private final BigDecimal smallInflow;
    private final BigDecimal northBoundInflow;

    private final BigDecimal shClose;
    private final BigDecimal szClose;

    private final String source;
}
