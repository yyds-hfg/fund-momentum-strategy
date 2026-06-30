package com.hacker.code.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hacker.code.application.dto.BacktestResponse;
import com.hacker.code.domain.fund.repository.NavDataRepository;
import com.hacker.code.domain.fund.valueobject.Nav;
import com.hacker.code.domain.portfolio.valueobject.Position;
import com.hacker.code.domain.portfolio.valueobject.RebalanceAdvice;
import com.hacker.code.domain.strategy.repository.BacktestRecordRepository;
import com.hacker.code.infrastructure.persistence.po.BacktestRecordPO;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 回测应用服务。
 * 按每周最后一个交易日调仓，持有到下一调仓日，按收盘净值计算收益。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BacktestAppService {

    private static final BigDecimal RISK_FREE_RATE = new BigDecimal("0.02");
    private static final int SCALE = 8;
    private static final double WEEKS_PER_YEAR = 52.0;

    private final StrategyExecutionAppService strategyExecutionAppService;
    private final NavDataRepository navDataRepository;
    private final BacktestRecordRepository backtestRecordRepository;
    private final ObjectMapper objectMapper;

    @SneakyThrows
    public BacktestResponse runBacktest(LocalDate startDate, LocalDate endDate) {
        log.info("Running backtest from {} to {}", startDate, endDate);

        List<LocalDate> rebalanceDates = collectFridays(startDate, endDate);
        if (rebalanceDates.size() < 2) {
            throw new IllegalArgumentException("回测区间至少需要包含两个调仓日");
        }

        List<Map<String, Object>> weeklyRecords = new ArrayList<>();
        BigDecimal nav = BigDecimal.ONE;
        BigDecimal peakNav = BigDecimal.ONE;
        BigDecimal maxDrawdown = BigDecimal.ZERO;
        List<BigDecimal> weeklyReturns = new ArrayList<>();

        for (int i = 0; i < rebalanceDates.size() - 1; i++) {
            LocalDate tradeDate = rebalanceDates.get(i);
            LocalDate nextTradeDate = rebalanceDates.get(i + 1);

            RebalanceAdvice advice = strategyExecutionAppService.executeWeeklyStrategy(tradeDate);
            BigDecimal weeklyReturn = calculateWeeklyReturn(advice, tradeDate, nextTradeDate);

            nav = nav.multiply(BigDecimal.ONE.add(weeklyReturn));
            if (nav.compareTo(peakNav) > 0) {
                peakNav = nav;
            }
            BigDecimal drawdown = peakNav.subtract(nav)
                    .divide(peakNav, SCALE, RoundingMode.HALF_UP);
            if (drawdown.compareTo(maxDrawdown) > 0) {
                maxDrawdown = drawdown;
            }
            weeklyReturns.add(weeklyReturn);

            Map<String, Object> record = new HashMap<>();
            record.put("date", tradeDate.toString());
            record.put("nav", nav.setScale(4, RoundingMode.HALF_UP));
            record.put("weeklyReturn", weeklyReturn.setScale(4, RoundingMode.HALF_UP));
            record.put("drawdown", drawdown.setScale(4, RoundingMode.HALF_UP));
            weeklyRecords.add(record);
        }

        int weeks = weeklyReturns.size();
        BigDecimal annualReturn = BigDecimal.ZERO;
        BigDecimal sharpeRatio = BigDecimal.ZERO;
        if (weeks > 0) {
            double years = weeks / WEEKS_PER_YEAR;
            if (years > 0) {
                annualReturn = BigDecimal.valueOf(Math.pow(nav.doubleValue(), 1.0 / years) - 1);
            }

            BigDecimal meanReturn = weeklyReturns.stream()
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(weeks), SCALE, RoundingMode.HALF_UP);
            BigDecimal variance = weeklyReturns.stream()
                    .map(r -> r.subtract(meanReturn).pow(2))
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(weeks), SCALE, RoundingMode.HALF_UP);
            BigDecimal weeklyStd = variance.sqrt(new MathContext(SCALE, RoundingMode.HALF_UP));
            BigDecimal annualStd = weeklyStd.multiply(BigDecimal.valueOf(Math.sqrt(WEEKS_PER_YEAR)));

            if (annualStd.compareTo(BigDecimal.ZERO) > 0) {
                sharpeRatio = annualReturn.subtract(RISK_FREE_RATE)
                        .divide(annualStd, SCALE, RoundingMode.HALF_UP);
            }
        }

        BacktestRecordPO po = new BacktestRecordPO();
        po.setStartDate(startDate);
        po.setEndDate(endDate);
        po.setStrategyTypes("BALANCED,ACTIVE");
        po.setAnnualReturn(annualReturn.setScale(4, RoundingMode.HALF_UP));
        po.setMaxDrawdown(maxDrawdown.setScale(4, RoundingMode.HALF_UP));
        po.setSharpeRatio(sharpeRatio.setScale(4, RoundingMode.HALF_UP));
        po.setDetailJson(objectMapper.writeValueAsString(weeklyRecords));
        backtestRecordRepository.save(po);

        BacktestResponse response = new BacktestResponse();
        response.setId(po.getId());
        response.setStartDate(startDate);
        response.setEndDate(endDate);
        response.setAnnualReturn(po.getAnnualReturn());
        response.setMaxDrawdown(po.getMaxDrawdown());
        response.setSharpeRatio(po.getSharpeRatio());

        log.info("Backtest completed. Weeks: {}, Final NAV: {}, Annual return: {}, Max drawdown: {}, Sharpe: {}",
                weeks, nav, annualReturn, maxDrawdown, sharpeRatio);
        return response;
    }

    private BigDecimal calculateWeeklyReturn(RebalanceAdvice advice, LocalDate tradeDate, LocalDate nextTradeDate) {
        if (advice.getSubResults().isEmpty()) {
            return BigDecimal.ZERO;
        }

        // 合并同一 ETF 的总暴露
        Map<String, BigDecimal> aggregatedWeights = new HashMap<>();
        for (var result : advice.getSubResults()) {
            for (Position position : result.getPositions()) {
                aggregatedWeights.merge(position.getFundCode(), position.getWeight(), BigDecimal::add);
            }
        }

        BigDecimal totalWeight = BigDecimal.ZERO;
        BigDecimal weightedReturn = BigDecimal.ZERO;

        for (Map.Entry<String, BigDecimal> entry : aggregatedWeights.entrySet()) {
            String fundCode = entry.getKey();
            BigDecimal weight = entry.getValue();
            BigDecimal holdingReturn = calculateHoldingReturn(fundCode, tradeDate, nextTradeDate);
            if (holdingReturn == null) {
                continue;
            }
            weightedReturn = weightedReturn.add(holdingReturn.multiply(weight));
            totalWeight = totalWeight.add(weight);
        }

        if (totalWeight.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return weightedReturn.divide(totalWeight, SCALE, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateHoldingReturn(String fundCode, LocalDate buyDate, LocalDate sellDate) {
        List<Nav> buyHistory = navDataRepository.findByDateRange(fundCode, buyDate.minusDays(5), buyDate);
        List<Nav> sellHistory = navDataRepository.findByDateRange(fundCode, sellDate.minusDays(5), sellDate);

        Nav buyNav = buyHistory.stream()
                .filter(n -> !n.getDate().isAfter(buyDate))
                .reduce((a, b) -> b)
                .orElse(null);
        Nav sellNav = sellHistory.stream()
                .filter(n -> !n.getDate().isAfter(sellDate))
                .reduce((a, b) -> b)
                .orElse(null);

        if (buyNav == null || sellNav == null || buyNav.getCloseNav().compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        return sellNav.getCloseNav().subtract(buyNav.getCloseNav())
                .divide(buyNav.getCloseNav(), SCALE, RoundingMode.HALF_UP);
    }

    private List<LocalDate> collectFridays(LocalDate startDate, LocalDate endDate) {
        List<LocalDate> dates = new ArrayList<>();
        LocalDate date = startDate;
        while (!date.getDayOfWeek().equals(DayOfWeek.FRIDAY)) {
            date = date.plusDays(1);
        }
        while (!date.isAfter(endDate)) {
            dates.add(date);
            date = date.plusWeeks(1);
        }
        return dates;
    }
}
