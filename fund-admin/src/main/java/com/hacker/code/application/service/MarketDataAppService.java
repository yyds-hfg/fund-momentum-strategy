package com.hacker.code.application.service;

import com.hacker.code.application.dto.MarketDataSyncResult;
import com.hacker.code.application.dto.MarketOverviewDTO;
import com.hacker.code.domain.fund.repository.MarketOverviewRepository;
import com.hacker.code.domain.fund.service.MarketDataFetcher;
import com.hacker.code.domain.fund.valueobject.MarketOverview;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MarketDataAppService {

    private final MarketDataFetcher marketDataFetcher;
    private final MarketOverviewRepository marketOverviewRepository;

    /**
     * 同步最近一个有效交易日的市场数据（用于定时任务）。
     */
    public MarketDataSyncResult syncLatestMarketData(LocalDate effectiveDate) {
        LocalDate startDate = effectiveDate.minusDays(7);
        return syncMarketDataHistory(startDate, effectiveDate);
    }

    /**
     * 同步指定日期范围的市场数据历史。
     */
    public MarketDataSyncResult syncMarketDataHistory(LocalDate startDate, LocalDate endDate) {
        MarketDataSyncResult result = new MarketDataSyncResult();
        result.setStartDate(startDate);
        result.setEndDate(endDate);

        List<MarketOverview> overviews = marketDataFetcher.fetchMarketOverviewHistory(startDate, endDate);
        if (overviews.isEmpty()) {
            result.setFailedDays((int) startDate.datesUntil(endDate.plusDays(1)).count());
            result.addError("从 EastMoney 未获取到任何市场数据");
            return result;
        }

        result.setTotalDays(overviews.size());
        int success = 0;
        for (MarketOverview overview : overviews) {
            try {
                marketOverviewRepository.saveOrUpdate(overview);
                success++;
            } catch (Exception e) {
                result.addError("保存 " + overview.getTradeDate() + " 数据失败: " + e.getMessage());
            }
        }
        result.setSuccessDays(success);
        result.setFailedDays(result.getTotalDays() - success);

        log.info("Market data sync completed. total={}, success={}, failed={}",
                result.getTotalDays(), result.getSuccessDays(), result.getFailedDays());
        return result;
    }

    /**
     * 获取指定日期的市场概况；若不存在则取最近不大于该日期的数据。
     */
    public MarketOverviewDTO getMarketOverview(LocalDate tradeDate) {
        Optional<MarketOverview> optional = marketOverviewRepository.findByTradeDate(tradeDate);
        if (optional.isPresent()) {
            return toDTO(optional.get());
        }
        List<MarketOverview> recent = marketOverviewRepository.findRecent(tradeDate, 1);
        if (recent.isEmpty()) {
            return null;
        }
        return toDTO(recent.get(0));
    }

    /**
     * 获取成交量趋势（近 N 个交易日，按日期升序）。
     */
    public List<MarketOverviewDTO> getVolumeTrend(LocalDate endDate, int days) {
        return marketOverviewRepository.findRecent(endDate, days).stream()
                .sorted(Comparator.comparing(MarketOverview::getTradeDate))
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * 获取资金流向趋势（近 N 个交易日，按日期升序）。
     */
    public List<MarketOverviewDTO> getCapitalFlowTrend(LocalDate endDate, int days) {
        return marketOverviewRepository.findRecent(endDate, days).stream()
                .sorted(Comparator.comparing(MarketOverview::getTradeDate))
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private MarketOverviewDTO toDTO(MarketOverview overview) {
        MarketOverviewDTO dto = new MarketOverviewDTO();
        dto.setTradeDate(overview.getTradeDate());
        dto.setShVolume(overview.getShVolume());
        dto.setShAmount(overview.getShAmount());
        dto.setSzVolume(overview.getSzVolume());
        dto.setSzAmount(overview.getSzAmount());
        dto.setTotalVolume(overview.getTotalVolume());
        dto.setTotalAmount(overview.getTotalAmount());
        dto.setMainInflow(overview.getMainInflow());
        dto.setSuperLargeInflow(overview.getSuperLargeInflow());
        dto.setLargeInflow(overview.getLargeInflow());
        dto.setMediumInflow(overview.getMediumInflow());
        dto.setSmallInflow(overview.getSmallInflow());
        dto.setNorthBoundInflow(overview.getNorthBoundInflow());
        dto.setShClose(overview.getShClose());
        dto.setSzClose(overview.getSzClose());
        dto.setSource(overview.getSource());
        return dto;
    }
}
