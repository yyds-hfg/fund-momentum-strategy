package com.hacker.code.infrastructure.external.sina;

import com.hacker.code.domain.fund.service.NavDataFetcher;
import com.hacker.code.domain.fund.valueobject.Nav;
import com.hacker.code.domain.shared.util.MarketCodeUtil;
import com.hacker.code.infrastructure.config.CrawlerProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

/**
 * 新浪财经实时行情获取器（备选）。
 * 由于新浪财经历史净值接口已不稳定，本实现仅用于获取最新一日行情。
 * 历史数据默认由 {@link com.hacker.code.infrastructure.external.eastmoney.EastMoneyKlineNavFetcher} 提供。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SinaFinanceNavClient implements NavDataFetcher {

    private final CrawlerProperties crawlerProperties;
    private final RestTemplate sinaRestTemplate;

    @Override
    public List<Nav> fetchHistory(String fundCode, LocalDate startDate, LocalDate endDate) {
        log.warn("Sina history NAV API is unstable. Please use EastMoneyKlineNavFetcher for history data.");
        return Collections.emptyList();
    }

    @Override
    public Nav fetchLatest(String fundCode) {
        String market = resolveMarket(fundCode);
        String url = crawlerProperties.getSinaRealtimeUrl()
                .replace("{market}", market)
                .replace("{code}", fundCode);
        try {
            String response = sinaRestTemplate.getForObject(url, String.class);
            return parseResponse(fundCode, response);
        } catch (Exception e) {
            log.error("Failed to fetch realtime quote from Sina for {}: {}", fundCode, e.getMessage());
            return null;
        }
    }

    private Nav parseResponse(String fundCode, String response) {
        if (response == null || !response.contains("=")) {
            return null;
        }
        int start = response.indexOf("\"") + 1;
        int end = response.lastIndexOf("\"");
        if (start <= 0 || end <= start) {
            return null;
        }
        String content = response.substring(start, end);
        String[] parts = content.split(",");
        if (parts.length < 33) {
            log.warn("Unexpected Sina realtime response format for {}", fundCode);
            return null;
        }
        // 格式：名称,今日开盘价,昨日收盘价,当前价,今日最高价,今日最低价,...,成交量,成交额,...,日期,时间
        BigDecimal open = new BigDecimal(parts[1]);
        BigDecimal close = new BigDecimal(parts[3]);
        BigDecimal high = new BigDecimal(parts[4]);
        BigDecimal low = new BigDecimal(parts[5]);
        long volume = new BigDecimal(parts[8]).longValue();
        LocalDate date = LocalDate.parse(parts[30], DateTimeFormatter.ISO_LOCAL_DATE);

        return new Nav(date, open, high, low, close, volume);
    }

    private String resolveMarket(String fundCode) {
        return MarketCodeUtil.sinaMarket(fundCode);
    }
}
