package com.hacker.code.adapter.web.controller;

import com.hacker.code.application.dto.MarketDataSyncResult;
import com.hacker.code.application.dto.MarketOverviewDTO;
import com.hacker.code.application.service.MarketDataAppService;
import com.hacker.code.domain.shared.util.TradeDateUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/market-data")
@RequiredArgsConstructor
public class MarketDataController {

    private final MarketDataAppService marketDataAppService;

    @PostMapping("/sync")
    public MarketDataSyncResult sync(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        if (startDate != null && endDate != null) {
            return marketDataAppService.syncMarketDataHistory(startDate, endDate);
        }
        return marketDataAppService.syncLatestMarketData(TradeDateUtil.determineEffectiveTradeDate());
    }

    @GetMapping("/overview")
    public MarketOverviewDTO overview(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tradeDate) {
        LocalDate date = tradeDate == null ? TradeDateUtil.determineEffectiveTradeDate() : tradeDate;
        return marketDataAppService.getMarketOverview(date);
    }

    @GetMapping("/volume-trend")
    public List<MarketOverviewDTO> volumeTrend(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "60") int days) {
        LocalDate date = endDate == null ? TradeDateUtil.determineEffectiveTradeDate() : endDate;
        return marketDataAppService.getVolumeTrend(date, days);
    }

    @GetMapping("/capital-flow-trend")
    public List<MarketOverviewDTO> capitalFlowTrend(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "60") int days) {
        LocalDate date = endDate == null ? TradeDateUtil.determineEffectiveTradeDate() : endDate;
        return marketDataAppService.getCapitalFlowTrend(date, days);
    }
}
