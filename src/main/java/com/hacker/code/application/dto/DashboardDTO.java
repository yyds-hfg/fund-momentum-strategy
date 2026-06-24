package com.hacker.code.application.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class DashboardDTO {

    private LocalDate tradeDate;
    private String marketStatus;
    private List<PositionDTO> positions = new ArrayList<>();
    private List<FundMomentumRanksGroupDTO> momentumRankGroups = new ArrayList<>();
    private List<BacktestRecordDTO> backtestRecords = new ArrayList<>();
}
