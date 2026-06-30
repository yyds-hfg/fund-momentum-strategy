package com.hacker.code.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hacker.code.application.dto.BacktestResponse;
import com.hacker.code.domain.fund.repository.NavDataRepository;
import com.hacker.code.domain.fund.valueobject.Nav;
import com.hacker.code.domain.portfolio.valueobject.Position;
import com.hacker.code.domain.portfolio.valueobject.RebalanceAdvice;
import com.hacker.code.domain.shared.util.TradeDateUtil;
import com.hacker.code.domain.strategy.repository.BacktestRecordRepository;
import com.hacker.code.infrastructure.persistence.po.BacktestRecordPO;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.IsoFields;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 回测应用服务。
 * <p>
 * 按每周最后一个 A 股交易日调仓，持有到下一调仓日，按收盘净值计算收益。
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

        List<LocalDate> rebalanceDates = collectWeeklyTradeDates(startDate, endDate);
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
        if (advice.getMergedPositions().isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal totalWeight = BigDecimal.ZERO;
        BigDecimal weightedReturn = BigDecimal.ZERO;

        for (Position position : advice.getMergedPositions()) {
            BigDecimal holdingReturn = calculateHoldingReturn(position.getFundCode(), tradeDate, nextTradeDate);
            if (holdingReturn == null) {
                continue;
            }
            weightedReturn = weightedReturn.add(holdingReturn.multiply(position.getWeight()));
            totalWeight = totalWeight.add(position.getWeight());
        }

        if (totalWeight.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return weightedReturn.divide(totalWeight, SCALE, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateHoldingReturn(String fundCode, LocalDate buyDate, LocalDate sellDate) {
        List<Nav> buyHistory = navDataRepository.findByDateRange(fundCode, buyDate.minusDays(10), buyDate);
        List<Nav> sellHistory = navDataRepository.findByDateRange(fundCode, sellDate.minusDays(10), sellDate);

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

    /**
     * 收集区间内的每周最后一个 A 股交易日。
     */
    private List<LocalDate> collectWeeklyTradeDates(LocalDate startDate, LocalDate endDate) {
        List<LocalDate> tradeDates = TradeDateUtil.tradeDatesBetween(startDate, endDate);
        if (tradeDates.isEmpty()) {
            return new ArrayList<>();
        }

        List<LocalDate> weeklyDates = new ArrayList<>();
        LocalDate lastWeekEnd = null;
        int lastWeekOfYear = -1;
        for (LocalDate date : tradeDates) {
            int weekOfYear = date.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
            int year = date.get(IsoFields.WEEK_BASED_YEAR);
            int weekKey = year * 100 + weekOfYear;
            if (lastWeekEnd == null) {
                lastWeekEnd = date;
                lastWeekOfYear = weekKey;
            } else if (weekKey != lastWeekOfYear) {
                weeklyDates.add(lastWeekEnd);
                lastWeekEnd = date;
                lastWeekOfYear = weekKey;
            } else {
                lastWeekEnd = date;
            }
        }
        weeklyDates.add(lastWeekEnd);
        return weeklyDates;
    }
}
