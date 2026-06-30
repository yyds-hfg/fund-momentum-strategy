package com.hacker.code.infrastructure.external.eastmoney;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hacker.code.domain.fund.service.MarketDataFetcher;
import com.hacker.code.domain.fund.valueobject.MarketOverview;
import com.hacker.code.infrastructure.config.CrawlerProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * 东方财富 A 股市场数据获取器。
 * 数据来源：
 * 1. 指数 K 线（上证 1.000001、深证 0.399001）=> 成交量/成交额/收盘价
 * 2. 大盘资金流向历史 K 线 => 主力/超大单/大单/中单/小单净流入
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EastMoneyMarketDataFetcher implements MarketDataFetcher {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter BASIC_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    private static final String SH_INDEX = "1.000001";
    private static final String SZ_INDEX = "0.399001";

    private final CrawlerProperties crawlerProperties;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public List<MarketOverview> fetchMarketOverviewHistory(LocalDate startDate, LocalDate endDate) {
        try {
            // 1. 获取沪深两市指数 K 线（成交量/成交额/收盘价）
            List<IndexKline> shKlines = fetchIndexKlines(SH_INDEX, startDate, endDate);
            List<IndexKline> szKlines = fetchIndexKlines(SZ_INDEX, startDate, endDate);

            // 2. 获取大盘资金流向历史
            List<CapitalFlowKline> flowKlines = fetchCapitalFlowKlines(startDate, endDate);

            // 3. 按日期合并
            Map<LocalDate, MarketOverview.MarketOverviewBuilder> builders = new TreeMap<>();

            for (IndexKline k : shKlines) {
                builders.computeIfAbsent(k.date, d -> MarketOverview.builder().tradeDate(d))
                        .shVolume(k.volume)
                        .shAmount(k.amount)
                        .shClose(k.close);
            }
            for (IndexKline k : szKlines) {
                builders.computeIfAbsent(k.date, d -> MarketOverview.builder().tradeDate(d))
                        .szVolume(k.volume)
                        .szAmount(k.amount)
                        .szClose(k.close);
            }
            for (CapitalFlowKline f : flowKlines) {
                builders.computeIfAbsent(f.date, d -> MarketOverview.builder().tradeDate(d))
                        .mainInflow(f.main)
                        .superLargeInflow(f.superLarge)
                        .largeInflow(f.large)
                        .mediumInflow(f.medium)
                        .smallInflow(f.small);
            }

            List<MarketOverview> result = new ArrayList<>();
            for (MarketOverview.MarketOverviewBuilder builder : builders.values()) {
                MarketOverview overview = builder.source("eastmoney").build();
                result.add(enrichTotals(overview));
            }
            return result;
        } catch (Exception e) {
            log.error("Failed to fetch market overview history from EastMoney: {}", e.getMessage(), e);
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

    private List<IndexKline> fetchIndexKlines(String secId, LocalDate startDate, LocalDate endDate) {
        String url = crawlerProperties.getEastMoneyKlineUrl()
                + "?secid=" + secId
                + "&ut=" + crawlerProperties.getEastMoneyUt()
                + "&fields1=f1,f2,f3,f4,f5,f6,f7"
                + "&fields2=f51,f52,f53,f54,f55,f56,f57"
                + "&klt=101&fqt=0&rtntype=6"
                + "&beg=" + startDate.format(BASIC_DATE_FORMATTER)
                + "&end=" + endDate.format(BASIC_DATE_FORMATTER);

        String response = restTemplate.getForObject(url, String.class);
        return parseIndexKlines(response);
    }

    private List<IndexKline> parseIndexKlines(String response) {
        if (response == null || response.isBlank()) {
            return Collections.emptyList();
        }
        List<IndexKline> result = new ArrayList<>();
        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode klines = root.path("data").path("klines");
            if (klines.isMissingNode() || !klines.isArray()) {
                return Collections.emptyList();
            }
            for (JsonNode node : klines) {
                String line = node.asText();
                String[] parts = line.split(",");
                if (parts.length < 7) {
                    continue;
                }
                try {
                    result.add(new IndexKline(
                            LocalDate.parse(parts[0], DATE_FORMATTER),
                            new BigDecimal(parts[2]),
                            new BigDecimal(parts[5]).longValue(),
                            new BigDecimal(parts[6])
                    ));
                } catch (Exception e) {
                    log.warn("Failed to parse index kline item: {}", line);
                }
            }
        } catch (Exception e) {
            log.error("Failed to parse index klines response: {}", e.getMessage());
        }
        return result;
    }

    private List<CapitalFlowKline> fetchCapitalFlowKlines(LocalDate startDate, LocalDate endDate) {
        String url = crawlerProperties.getEastMoneyCapitalFlowUrl()
                + "?secid=" + SH_INDEX
                + "&secid2=" + SZ_INDEX
                + "&ut=" + crawlerProperties.getEastMoneyUt()
                + "&fields1=f1,f2,f3,f7"
                + "&fields2=f51,f52,f53,f54,f55,f56,f57,f58,f59,f60,f61,f62,f63,f64,f65"
                + "&klt=101&lmt=0&rtntype=6"
                + "&beg=" + startDate.format(BASIC_DATE_FORMATTER)
                + "&end=" + endDate.format(BASIC_DATE_FORMATTER);

        String response = restTemplate.getForObject(url, String.class);
        return parseCapitalFlowKlines(response);
    }

    private List<CapitalFlowKline> parseCapitalFlowKlines(String response) {
        if (response == null || response.isBlank()) {
            return Collections.emptyList();
        }
        List<CapitalFlowKline> result = new ArrayList<>();
        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode klines = root.path("data").path("klines");
            if (klines.isMissingNode() || !klines.isArray()) {
                return Collections.emptyList();
            }
            for (JsonNode node : klines) {
                String line = node.asText();
                String[] parts = line.split(",");
                if (parts.length < 6) {
                    continue;
                }
                try {
                    // f51=日期, f52=主力净流入, f53=小单净流入, f54=中单净流入, f55=大单净流入, f56=超大单净流入
                    result.add(new CapitalFlowKline(
                            LocalDate.parse(parts[0], DATE_FORMATTER),
                            new BigDecimal(parts[2]),
                            new BigDecimal(parts[6]),
                            new BigDecimal(parts[5]),
                            new BigDecimal(parts[4]),
                            new BigDecimal(parts[3])
                    ));
                } catch (Exception e) {
                    log.warn("Failed to parse capital flow kline item: {}", line);
                }
            }
        } catch (Exception e) {
            log.error("Failed to parse capital flow response: {}", e.getMessage());
        }
        return result;
    }

    private MarketOverview enrichTotals(MarketOverview overview) {
        long totalVolume = nullToZero(overview.getShVolume()) + nullToZero(overview.getSzVolume());
        BigDecimal totalAmount = nullToZero(overview.getShAmount()).add(nullToZero(overview.getSzAmount()));
        return overview.toBuilder()
                .totalVolume(totalVolume)
                .totalAmount(totalAmount)
                .build();
    }

    private long nullToZero(Long value) {
        return value == null ? 0L : value;
    }

    private BigDecimal nullToZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private record IndexKline(LocalDate date, BigDecimal close, long volume, BigDecimal amount) {
    }

    private record CapitalFlowKline(LocalDate date, BigDecimal main, BigDecimal superLarge,
                                     BigDecimal large, BigDecimal medium, BigDecimal small) {
    }
}
