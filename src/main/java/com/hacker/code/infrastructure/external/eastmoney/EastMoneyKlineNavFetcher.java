package com.hacker.code.infrastructure.external.eastmoney;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hacker.code.domain.fund.service.NavDataFetcher;
import com.hacker.code.domain.fund.valueobject.Nav;
import com.hacker.code.infrastructure.config.CrawlerProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 东方财富 K 线数据获取器。
 * 由于新浪财经历史数据接口不稳定，本系统使用东方财富 ETF K 线数据作为历史净值来源，
 * 数据字段包含开盘、收盘、最高、最低、成交量，完全满足策略计算需求。
 */
@Slf4j
@Component
@Primary
@RequiredArgsConstructor
public class EastMoneyKlineNavFetcher implements NavDataFetcher {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final CrawlerProperties crawlerProperties;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public List<Nav> fetchHistory(String fundCode, LocalDate startDate, LocalDate endDate) {
        String secId = resolveSecId(fundCode);
        String url = buildUrl(secId, startDate, endDate);

        try {
            String response = restTemplate.getForObject(url, String.class);
            return parseResponse(response);
        } catch (Exception e) {
            log.error("Failed to fetch history NAV from EastMoney for {}: {}", fundCode, e.getMessage());
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

    private String buildUrl(String secId, LocalDate startDate, LocalDate endDate) {
        return crawlerProperties.getEastMoneyKlineUrl()
                + "?secid=" + secId
                + "&fields1=f1,f2,f3,f4,f5,f6"
                + "&fields2=f51,f52,f53,f54,f55,f56"
                + "&klt=101"
                + "&fqt=0"
                + "&beg=" + startDate.format(DateTimeFormatter.BASIC_ISO_DATE)
                + "&end=" + endDate.format(DateTimeFormatter.BASIC_ISO_DATE);
    }

    private List<Nav> parseResponse(String response) {
        if (response == null || response.isBlank()) {
            return Collections.emptyList();
        }
        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode dataNode = root.path("data");
            if (dataNode.isMissingNode() || !dataNode.has("klines")) {
                return Collections.emptyList();
            }
            JsonNode klines = dataNode.path("klines");
            List<Nav> result = new ArrayList<>();
            for (JsonNode kline : klines) {
                Nav nav = parseKline(kline.asText());
                if (nav != null) {
                    result.add(nav);
                }
            }
            return result;
        } catch (Exception e) {
            log.error("Failed to parse EastMoney response: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private Nav parseKline(String kline) {
        // 格式：日期,开盘,收盘,最高,最低,成交量,...
        String[] parts = kline.split(",");
        if (parts.length < 6) {
            return null;
        }
        try {
            return new Nav(
                    LocalDate.parse(parts[0], DATE_FORMATTER),
                    new BigDecimal(parts[1]),
                    new BigDecimal(parts[3]),
                    new BigDecimal(parts[4]),
                    new BigDecimal(parts[2]),
                    new BigDecimal(parts[5]).longValue()
            );
        } catch (Exception e) {
            log.warn("Failed to parse kline item: {}", kline);
            return null;
        }
    }

    private String resolveSecId(String fundCode) {
        Objects.requireNonNull(fundCode, "fundCode must not be null");
        String code = fundCode.trim();
        if (code.startsWith("6") || code.startsWith("5") || code.startsWith("58") || code.startsWith("68") || code.startsWith("88") || code.startsWith("89")) {
            return "1." + code;
        }
        return "0." + code;
    }
}
