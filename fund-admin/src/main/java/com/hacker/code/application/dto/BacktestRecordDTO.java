package com.hacker.code.application.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class BacktestRecordDTO {

    private Long id;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal annualReturn;
    private BigDecimal maxDrawdown;
    private BigDecimal sharpeRatio;
}
