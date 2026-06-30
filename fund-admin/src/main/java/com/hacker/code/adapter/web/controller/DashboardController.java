package com.hacker.code.adapter.web.controller;

import com.hacker.code.application.assembler.StrategyAssembler;
import com.hacker.code.application.dto.*;
import com.hacker.code.application.service.DashboardAppService;
import com.hacker.code.application.service.MarketDataAppService;
import com.hacker.code.application.service.StrategyExecutionAppService;
import com.hacker.code.domain.shared.util.TradeDateUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 策略看板接口。
 */
@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardAppService dashboardAppService;
    private final MarketDataAppService marketDataAppService;
    private final StrategyExecutionAppService strategyExecutionAppService;
    private final StrategyAssembler strategyAssembler;

    @GetMapping("/api/data")
    public DashboardDTO data() {
        return dashboardAppService.getDashboardData();
    }

    @GetMapping("/api/backtest")
    public List<BacktestRecordDTO> backtestRecords() {
        return dashboardAppService.getBacktestRecords();
    }

    @GetMapping("/api/backtest/{id}")
    public BacktestDetailDTO backtestDetail(@PathVariable(name = "id") Long id) {
        return dashboardAppService.getBacktestDetail(id);
    }

    @GetMapping("/api/momentum-ranks")
    public List<FundMomentumRanksGroupDTO> allMomentumRanks() {
        return dashboardAppService.getAllMomentumRankGroupsForRealtime();
    }

    @GetMapping("/api/volume-trend")
    public List<MarketOverviewDTO> volumeTrend(@RequestParam(name = "days", defaultValue = "60") int days) {
        return marketDataAppService.getVolumeTrend(TradeDateUtil.determineEffectiveTradeDate(), days);
    }

    @GetMapping("/api/capital-flow-trend")
    public List<MarketOverviewDTO> capitalFlowTrend(@RequestParam(name = "days", defaultValue = "60") int days) {
        return marketDataAppService.getCapitalFlowTrend(TradeDateUtil.determineEffectiveTradeDate(), days);
    }

    @GetMapping("/api/recommended-positions")
    public RebalanceAdviceDTO recommendedPositions() {
        return strategyAssembler.toDTO(strategyExecutionAppService.calculateWeeklyStrategy(TradeDateUtil.determineEffectiveTradeDate()));
    }

}
