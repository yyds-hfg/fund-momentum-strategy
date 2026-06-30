package com.hacker.code.application.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PositionDTO {

    private String fundCode;
    private String fundName;
    private BigDecimal weight;
    private String sourceStrategy;
    private String reason;
}
