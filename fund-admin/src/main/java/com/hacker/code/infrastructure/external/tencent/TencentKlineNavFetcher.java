package com.hacker.code.infrastructure.external.tencent;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hacker.code.domain.fund.service.NavDataFetcher;
import com.hacker.code.domain.shared.util.MarketCodeUtil;
import com.hacker.code.domain.fund.valueobject.Nav;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 腾讯财经 ETF K 线获取器（兜底）。
 * 当东方财富接口不可用时，通过腾讯行情接口获取 ETF 历史 OHLCV 作为净值数据。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TencentKlineNavFetcher implements NavDataFetcher {

    private static final String TENCENT_KLINE_URL = "https://web.ifzq.gtimg.cn/appstock/app/fqkline/get";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public List<Nav> fetchHistory(String fundCode, LocalDate startDate, LocalDate endDate) {
        String symbol = resolveSymbol(fundCode);
        String url = TENCENT_KLINE_URL
                + "?param=" + symbol + ",day,"
                + startDate + "," + endDate + ",1000,";
        try {
            String response = restTemplate.getForObject(url, String.class);
            return parseResponse(symbol, response);
        } catch (Exception e) {
            log.warn("Failed to fetch Tencent kline for {}: {}", fundCode, e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public Nav fetchLatest(String fundCode) {
        List<Nav> history = fetchHistory(fundCode, LocalDate.now().minusDays(10), LocalDate.now());
        if (history.isEmpty()) {
            return null;
        }
        return history.get(history.size() - 1);
    }

    private List<Nav> parseResponse(String symbol, String response) {
        if (response == null || response.isBlank()) {
            return Collections.emptyList();
        }
        List<Nav> result = new ArrayList<>();
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
                    BigDecimal open = new BigDecimal(node.get(1).asText());
                    BigDecimal close = new BigDecimal(node.get(2).asText());
                    BigDecimal high = new BigDecimal(node.get(3).asText());
                    BigDecimal low = new BigDecimal(node.get(4).asText());
                    long volume = new BigDecimal(node.get(5).asText()).longValue();
                    result.add(new Nav(date, open, high, low, close, volume));
                } catch (Exception e) {
                    log.warn("Failed to parse Tencent kline item: {}", node);
                }
            }
        } catch (Exception e) {
            log.error("Failed to parse Tencent kline response for {}: {}", symbol, e.getMessage());
        }
        return result;
    }

    private String resolveSymbol(String fundCode) {
        return MarketCodeUtil.tencentMarket(fundCode) + fundCode.trim();
    }
}
