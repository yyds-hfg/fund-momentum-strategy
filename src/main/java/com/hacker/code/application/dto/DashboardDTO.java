package com.hacker.code.application.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class DashboardDTO {

    private LocalDate tradeDate;
    private String marketStatus;
    private BigDecimal totalWeight;

    // 市场数据
    private List<MarketOverviewDTO> volumeTrend = new ArrayList<>();
    private MarketOverviewDTO latestCapitalFlow;
    private List<MarketOverviewDTO> capitalFlowTrend = new ArrayList<>();

    // 推荐持仓
    private List<PositionDTO> recommendedPositions = new ArrayList<>();

    // 保留：策略执行结果仓位（与推荐一致）
    private List<PositionDTO> positions = new ArrayList<>();

    // 回测记录
    private List<BacktestRecordDTO> backtestRecords = new ArrayList<>();
}
