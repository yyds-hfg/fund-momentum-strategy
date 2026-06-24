package com.hacker.code.adapter.job;

import com.hacker.code.application.service.MarketDataAppService;
import com.hacker.code.domain.fund.repository.MarketOverviewRepository;
import com.hacker.code.domain.shared.util.TradeDateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * A 股市场数据同步任务。
 * 工作日 16:35 同步最近交易日的成交量与资金流向。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MarketDataSyncJob implements CommandLineRunner {

    private final MarketDataAppService marketDataAppService;
    private final MarketOverviewRepository marketOverviewRepository;

    @Scheduled(cron = "0 35 16 ? * MON-FRI")
    public void dailySync() {
        LocalDate effectiveDate = TradeDateUtil.determineEffectiveTradeDate();
        log.info("Starting daily market data sync for {}", effectiveDate);
        marketDataAppService.syncLatestMarketData(effectiveDate);
    }

    @Override
    public void run(String... args) {
        try {
            marketOverviewRepository.createTableIfNotExists();
            if (marketOverviewRepository.isEmpty()) {
                LocalDate end = TradeDateUtil.determineEffectiveTradeDate();
                LocalDate start = end.minusDays(90);
                log.info("Market overview table is empty, syncing history from {} to {}", start, end);
                marketDataAppService.syncMarketDataHistory(start, end);
            }
        } catch (Exception e) {
            log.error("Failed to initialize market data sync job: {}", e.getMessage(), e);
        }
    }
}
