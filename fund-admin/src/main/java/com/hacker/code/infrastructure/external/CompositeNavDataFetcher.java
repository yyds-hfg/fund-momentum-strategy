package com.hacker.code.infrastructure.external;

import com.hacker.code.domain.fund.service.NavDataFetcher;
import com.hacker.code.domain.fund.valueobject.Nav;
import com.hacker.code.infrastructure.external.eastmoney.EastMoneyKlineNavFetcher;
import com.hacker.code.infrastructure.external.sina.SinaFinanceNavClient;
import com.hacker.code.infrastructure.external.tencent.TencentKlineNavFetcher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 组合净值获取器。
 * 优先使用东方财富获取历史 K 线；如果东方财富取不到，则回退到新浪财经获取最新行情。
 */
@Slf4j
@Component
@Primary
@RequiredArgsConstructor
public class CompositeNavDataFetcher implements NavDataFetcher {

    private final EastMoneyKlineNavFetcher eastMoneyFetcher;
    private final TencentKlineNavFetcher tencentFetcher;
    private final SinaFinanceNavClient sinaFetcher;

    @Override
    public List<Nav> fetchHistory(String fundCode, LocalDate startDate, LocalDate endDate) {
        List<Nav> history = eastMoneyFetcher.fetchHistory(fundCode, startDate, endDate);
        if (!history.isEmpty()) {
            return history;
        }

        log.warn("EastMoney 获取 {} 历史净值失败，尝试腾讯财经兜底", fundCode);
        history = tencentFetcher.fetchHistory(fundCode, startDate, endDate);
        if (!history.isEmpty()) {
            return history;
        }

        log.warn("腾讯财经获取 {} 历史净值失败，回退到新浪最新行情", fundCode);
        Nav latest = sinaFetcher.fetchLatest(fundCode);
        if (latest != null) {
            List<Nav> fallback = new ArrayList<>();
            fallback.add(latest);
            return fallback;
        }
        return Collections.emptyList();
    }

    @Override
    public Nav fetchLatest(String fundCode) {
        Nav nav = eastMoneyFetcher.fetchLatest(fundCode);
        if (nav != null) {
            return nav;
        }
        nav = tencentFetcher.fetchLatest(fundCode);
        if (nav != null) {
            return nav;
        }
        return sinaFetcher.fetchLatest(fundCode);
    }
}
