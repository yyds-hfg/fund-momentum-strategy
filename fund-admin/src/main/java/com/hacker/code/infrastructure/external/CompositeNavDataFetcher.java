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
import java.util.Collections;
import java.util.List;

/**
 * 组合净值获取器。
 * <p>
 * 历史净值优先使用东方财富 ETF K 线；东方财富失败时回退到腾讯财经 K 线。
 * 新浪财经实时行情仅用于获取最新一日行情，不再混入历史序列，避免污染动量计算。
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

        log.warn("腾讯财经获取 {} 历史净值也失败，历史数据返回空。如需实时行情请调用 fetchLatest", fundCode);
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
