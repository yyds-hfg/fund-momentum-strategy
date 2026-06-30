package com.hacker.code.infrastructure.external;

import com.hacker.code.domain.fund.service.MarketDataFetcher;
import com.hacker.code.domain.fund.valueobject.MarketOverview;
import com.hacker.code.infrastructure.external.eastmoney.EastMoneyMarketDataFetcher;
import com.hacker.code.infrastructure.external.sina.SinaMarketDataFetcher;
import com.hacker.code.infrastructure.external.tencent.TencentMarketDataFetcher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

/**
 * 组合市场数据获取器。
 * 优先使用东方财富获取历史数据；实时快照失败时回退到新浪财经。
 */
@Slf4j
@Component
@Primary
@RequiredArgsConstructor
public class CompositeMarketDataFetcher implements MarketDataFetcher {

    private final EastMoneyMarketDataFetcher eastMoneyFetcher;
    private final TencentMarketDataFetcher tencentFetcher;
    private final SinaMarketDataFetcher sinaFetcher;

    @Override
    public List<MarketOverview> fetchMarketOverviewHistory(LocalDate startDate, LocalDate endDate) {
        List<MarketOverview> history = eastMoneyFetcher.fetchMarketOverviewHistory(startDate, endDate);
        if (!history.isEmpty()) {
            return history;
        }
        log.warn("EastMoney 获取市场概况历史失败，尝试腾讯财经兜底");
        history = tencentFetcher.fetchMarketOverviewHistory(startDate, endDate);
        if (!history.isEmpty()) {
            return history;
        }
        log.warn("腾讯财经获取市场概况历史失败，Sina 不支持历史数据兜底");
        return Collections.emptyList();
    }

    @Override
    public MarketOverview fetchLatestMarketOverview() {
        MarketOverview overview = eastMoneyFetcher.fetchLatestMarketOverview();
        if (overview != null) {
            return overview;
        }
        log.warn("EastMoney 获取最新市场概况失败，尝试腾讯财经兜底");
        overview = tencentFetcher.fetchLatestMarketOverview();
        if (overview != null) {
            return overview;
        }
        log.warn("腾讯财经获取最新市场概况失败，回退到新浪快照");
        return sinaFetcher.fetchLatestMarketOverview();
    }
}
