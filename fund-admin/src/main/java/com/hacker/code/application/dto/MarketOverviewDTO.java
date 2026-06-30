package com.hacker.code.application.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class MarketOverviewDTO {

    private LocalDate tradeDate;

    private Long shVolume;
    private BigDecimal shAmount;
    private Long szVolume;
    private BigDecimal szAmount;
    private Long totalVolume;
    private BigDecimal totalAmount;

    private BigDecimal mainInflow;
    private BigDecimal superLargeInflow;
    private BigDecimal largeInflow;
    private BigDecimal mediumInflow;
    private BigDecimal smallInflow;
    private BigDecimal northBoundInflow;

    private BigDecimal shClose;
    private BigDecimal szClose;

    private String source;
}
