package com.hacker.code.infrastructure.external.sina;

import com.hacker.code.domain.fund.service.MarketDataFetcher;
import com.hacker.code.domain.fund.valueobject.MarketOverview;
import com.hacker.code.infrastructure.config.CrawlerProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

/**
 * 新浪财经 A 股市场数据获取器（兜底）。
 * 目前仅支持获取沪深两市最新快照（成交量、成交额、收盘价）。
 * 历史数据与资金流向建议由东方财富提供。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SinaMarketDataFetcher implements MarketDataFetcher {

    private final CrawlerProperties crawlerProperties;
    private final RestTemplate sinaRestTemplate;

    @Override
    public List<MarketOverview> fetchMarketOverviewHistory(LocalDate startDate, LocalDate endDate) {
        log.warn("Sina market overview history API is not stable. Returning empty list.");
        return Collections.emptyList();
    }

    @Override
    public MarketOverview fetchLatestMarketOverview() {
        String url = crawlerProperties.getSinaIndexSnapshotUrl();
        try {
            String response = sinaRestTemplate.getForObject(url, String.class);
            return parseSnapshot(response);
        } catch (Exception e) {
            log.error("Failed to fetch latest market overview from Sina: {}", e.getMessage());
            return null;
        }
    }

    private MarketOverview parseSnapshot(String response) {
        if (response == null || !response.contains("=")) {
            return null;
        }

        // 格式示例：
        // var hq_str_s_sh000001="上证指数,3367.60,11.73,0.35,286332246,326278969";
        // var hq_str_s_sz399001="深证成指,10843.41,120.11,1.12,384324676,512341237";
        // 字段：名称, 当前价, 涨跌额, 涨跌幅, 成交量（手）, 成交额（万元）

        MarketOverview.MarketOverviewBuilder builder = MarketOverview.builder().tradeDate(LocalDate.now());
        long totalVolume = 0L;
        BigDecimal totalAmount = BigDecimal.ZERO;

        String[] lines = response.split(";");
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) {
                continue;
            }
            int start = line.indexOf("\"") + 1;
            int end = line.lastIndexOf("\"");
            if (start <= 0 || end <= start) {
                continue;
            }
            String content = line.substring(start, end);
            String[] parts = content.split(",");
            if (parts.length < 6) {
                continue;
            }
            try {
                BigDecimal close = new BigDecimal(parts[1]);
                // 新浪返回的成交量单位是“手”，成交额单位是“万元”，统一转换为股/元
                long volume = new BigDecimal(parts[4]).longValue() * 100;
                BigDecimal amount = new BigDecimal(parts[5]).multiply(BigDecimal.valueOf(10000));
                totalVolume += volume;
                totalAmount = totalAmount.add(amount);

                if (line.contains("s_sh000001")) {
                    builder.shClose(close).shVolume(volume).shAmount(amount);
                } else if (line.contains("s_sz399001")) {
                    builder.szClose(close).szVolume(volume).szAmount(amount);
                }
            } catch (Exception e) {
                log.warn("Failed to parse Sina index snapshot item: {}", content);
            }
        }

        return builder.totalVolume(totalVolume)
                .totalAmount(totalAmount)
                .source("sina")
                .build();
    }
}
