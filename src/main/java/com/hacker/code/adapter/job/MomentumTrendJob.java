package com.hacker.code.adapter.job;

import com.hacker.code.application.service.MomentumTrendAppService;
import com.hacker.code.domain.shared.util.TradeDateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class MomentumTrendJob {

    private final MomentumTrendAppService momentumTrendAppService;

    /**
     * 开盘前：计算 T-1 的动量趋势（此时 T 日数据尚未产生）。
     */
    @Scheduled(cron = "0 30 3 ? * MON-FRI")
    public void morningCompute() {
        LocalDate tradeDate = TradeDateUtil.determineEffectiveTradeDate();
        log.info("开盘前动量趋势计算启动，tradeDate={}", tradeDate);
        momentumTrendAppService.computeAndSaveForTradeDate(tradeDate);
    }

    /**
     * 收盘后：在净值同步完成后，重新计算 T 日的动量趋势。
     */
    @Scheduled(cron = "0 30 16 ? * MON-FRI")
    public void afternoonCompute() {
        LocalDate tradeDate = TradeDateUtil.determineEffectiveTradeDate();
        log.info("收盘后动量趋势计算启动，tradeDate={}", tradeDate);
        momentumTrendAppService.computeAndSaveForTradeDate(tradeDate);
    }

}
