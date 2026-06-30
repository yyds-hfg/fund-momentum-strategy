package com.hacker.code.domain.fund.service;

import com.hacker.code.domain.fund.valueobject.MarketOverview;

import java.time.LocalDate;
import java.util.List;

/**
 * A 股市场数据（成交量、资金流向）获取器。
 */
public interface MarketDataFetcher {

    /**
     * 获取指定日期范围的市场概况历史数据（日线）。
     */
    List<MarketOverview> fetchMarketOverviewHistory(LocalDate startDate, LocalDate endDate);

    /**
     * 获取最近一个交易日的市场概况，通常用于盘中或收盘后兜底。
     */
    MarketOverview fetchLatestMarketOverview();
}
