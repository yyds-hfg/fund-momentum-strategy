package com.hacker.code.application.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class RebalanceAdviceDTO {

    private LocalDate tradeDate;
    private String marketStatus;
    private List<StrategyResultDTO> subResults = new ArrayList<>();
    private List<PositionDTO> mergedPositions = new ArrayList<>();
}
