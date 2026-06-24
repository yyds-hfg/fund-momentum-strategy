package com.hacker.code.infrastructure.external.tencent;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hacker.code.domain.fund.service.MarketDataFetcher;
import com.hacker.code.domain.fund.valueobject.MarketOverview;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * 腾讯财经 A 股指数 K 线获取器（兜底）。
 * 提供沪深两市历史成交量与收盘价；不支持成交额与资金流向。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TencentMarketDataFetcher implements MarketDataFetcher {

    private static final String TENCENT_KLINE_URL = "https://web.ifzq.gtimg.cn/appstock/app/fqkline/get";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public List<MarketOverview> fetchMarketOverviewHistory(LocalDate startDate, LocalDate endDate) {
        try {
            List<IndexKline> shKlines = fetchIndexKlines("sh000001", startDate, endDate);
            List<IndexKline> szKlines = fetchIndexKlines("sz399001", startDate, endDate);

            Map<LocalDate, MarketOverview.MarketOverviewBuilder> builders = new TreeMap<>();
            for (IndexKline k : shKlines) {
                builders.computeIfAbsent(k.date, d -> MarketOverview.builder().tradeDate(d))
                        .shClose(k.close)
                        .shVolume(k.volume);
            }
            for (IndexKline k : szKlines) {
                builders.computeIfAbsent(k.date, d -> MarketOverview.builder().tradeDate(d))
                        .szClose(k.close)
                        .szVolume(k.volume);
            }

            List<MarketOverview> result = new ArrayList<>();
            for (MarketOverview.MarketOverviewBuilder builder : builders.values()) {
                MarketOverview overview = builder.source("tencent").build();
                result.add(enrichTotals(overview));
            }
            return result;
        } catch (Exception e) {
            log.error("Failed to fetch market overview history from Tencent: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @Override
    public MarketOverview fetchLatestMarketOverview() {
        LocalDate today = LocalDate.now();
        List<MarketOverview> list = fetchMarketOverviewHistory(today.minusDays(5), today);
        if (list.isEmpty()) {
            return null;
        }
        return list.get(list.size() - 1);
    }

    private List<IndexKline> fetchIndexKlines(String symbol, LocalDate startDate, LocalDate endDate) {
        String url = TENCENT_KLINE_URL
                + "?param=" + symbol + ",day,"
                + startDate + "," + endDate + ",1000,";
        try {
            String response = restTemplate.getForObject(url, String.class);
            return parseKlines(symbol, response);
        } catch (Exception e) {
            log.warn("Failed to fetch Tencent kline for {}: {}", symbol, e.getMessage());
            return Collections.emptyList();
        }
    }

    private List<IndexKline> parseKlines(String symbol, String response) {
        if (response == null || response.isBlank()) {
            return Collections.emptyList();
        }
        List<IndexKline> result = new ArrayList<>();
        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode dayNode = root.path("data").path(symbol).path("day");
            if (dayNode.isMissingNode() || !dayNode.isArray()) {
                return Collections.emptyList();
            }
            for (JsonNode node : dayNode) {
                if (!node.isArray() || node.size() < 6) {
                    continue;
                }
                try {
                    LocalDate date = LocalDate.parse(node.get(0).asText());
                    BigDecimal close = new BigDecimal(node.get(2).asText());
                    long volume = new BigDecimal(node.get(5).asText()).longValue();
                    result.add(new IndexKline(date, close, volume));
                } catch (Exception e) {
                    log.warn("Failed to parse Tencent kline item: {}", node);
                }
            }
        } catch (Exception e) {
            log.error("Failed to parse Tencent kline response for {}: {}", symbol, e.getMessage());
        }
        return result;
    }

    private MarketOverview enrichTotals(MarketOverview overview) {
        long totalVolume = nullToZero(overview.getShVolume()) + nullToZero(overview.getSzVolume());
        return overview.toBuilder()
                .totalVolume(totalVolume)
                .build();
    }

    private long nullToZero(Long value) {
        return value == null ? 0L : value;
    }

    private record IndexKline(LocalDate date, BigDecimal close, long volume) {
    }
}
