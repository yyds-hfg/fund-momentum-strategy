package com.hacker.code.application.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class StrategyResultDTO {

    private Long id;
    private LocalDate tradeDate;
    private String strategyType;
    private String marketStatus;
    private BigDecimal totalWeight;
    private List<PositionDTO> positions = new ArrayList<>();
}
