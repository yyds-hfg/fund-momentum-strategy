package com.hacker.code.application.service;

import com.hacker.code.domain.fund.entity.Fund;
import com.hacker.code.domain.fund.repository.FundRepository;
import com.hacker.code.domain.fund.repository.NavDataRepository;
import com.hacker.code.domain.fund.service.FundDomainService;
import com.hacker.code.domain.fund.valueobject.Nav;
import com.hacker.code.domain.portfolio.service.LowVolatilityWeightingService;
import com.hacker.code.domain.portfolio.service.PortfolioMergeService;
import com.hacker.code.domain.portfolio.valueobject.Position;
import com.hacker.code.domain.portfolio.valueobject.RebalanceAdvice;
import com.hacker.code.domain.strategy.entity.StrategyConfig;
import com.hacker.code.domain.strategy.entity.StrategyResult;
import com.hacker.code.domain.strategy.repository.StrategyConfigRepository;
import com.hacker.code.domain.strategy.repository.StrategyResultRepository;
import com.hacker.code.domain.strategy.service.ETFScreeningService;
import com.hacker.code.domain.strategy.service.MarketEnvironmentService;
import com.hacker.code.domain.strategy.valueobject.MarketSignal;
import com.hacker.code.domain.strategy.valueobject.MarketStatus;
import com.hacker.code.domain.strategy.valueobject.ScreenedETF;
import com.hacker.code.domain.strategy.valueobject.StrategyType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 策略执行应用服务（用例编排）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StrategyExecutionAppService {

    private static final String BENCHMARK_CODE = "000852";
    private static final int MARKET_FILTER_MA = 120;

    private final FundRepository fundRepository;
    private final FundDomainService fundDomainService;
    private final NavDataRepository navDataRepository;
    private final StrategyConfigRepository strategyConfigRepository;
    private final StrategyResultRepository strategyResultRepository;
    private final ETFScreeningService screeningService;
    private final LowVolatilityWeightingService weightingService;
    private final MarketEnvironmentService marketEnvironmentService;
    private final PortfolioMergeService portfolioMergeService;

    /**
     * 执行并持久化每周策略结果。
     */
    @Transactional
    public RebalanceAdvice executeWeeklyStrategy(LocalDate tradeDate) {
        RebalanceAdvice advice = calculateWeeklyStrategy(tradeDate);

        // 持久化子结果
        for (StrategyResult result : advice.getSubResults()) {
            strategyResultRepository.save(result);
        }

        log.info("Strategy execution completed. Sub-results: {}", advice.getSubResults().size());
        return advice;
    }

    /**
     * 纯计算，不持久化。用于实时推荐与策略执行保持一致。
     */
    public RebalanceAdvice calculateWeeklyStrategy(LocalDate tradeDate) {
        log.info("Calculating weekly strategy for {}", tradeDate);

        List<Fund> candidates = fundDomainService.getCandidatePool().stream()
                .filter(fund -> !BENCHMARK_CODE.equals(fund.getFundCode()))
                .collect(Collectors.toList());

        int maxWindow = calculateMaxWindow();
        LocalDate startDate = tradeDate.minusDays(maxWindow + 30);
        Map<String, List<Nav>> navHistoryMap = loadNavHistory(candidates, startDate, tradeDate);

        MarketSignal marketSignal = marketEnvironmentService.judge(tradeDate, MARKET_FILTER_MA);
        MarketStatus marketStatus = marketSignal.isBullish() ? MarketStatus.STRONG : MarketStatus.WEAK;
        log.info("Market status on {}: {}", tradeDate, marketStatus);

        List<StrategyConfig> configs = strategyConfigRepository.findAllEnabled();
        List<StrategyResult> subResults = new ArrayList<>();

        for (StrategyConfig config : configs) {
            StrategyResult result = calculateSingleStrategy(config, tradeDate, candidates, navHistoryMap, marketStatus);
            if (result != null) {
                subResults.add(result);
            }
        }

        return portfolioMergeService.merge(tradeDate, marketStatus, subResults);
    }

    private StrategyResult calculateSingleStrategy(StrategyConfig config,
                                                   LocalDate tradeDate,
                                                   List<Fund> candidates,
                                                   Map<String, List<Nav>> navHistoryMap,
                                                   MarketStatus marketStatus) {
        BigDecimal totalAllocation = resolveTotalAllocation(config, marketStatus);
        if (totalAllocation.compareTo(BigDecimal.ZERO) == 0) {
            log.info("{} strategy has zero allocation under {}", config.getStrategyType(), marketStatus);
            return buildEmptyResult(config, tradeDate, marketStatus);
        }

        List<ScreenedETF> screened = screeningService.screen(candidates, config, navHistoryMap);
        List<Position> positions = weightingService.allocate(screened, config, totalAllocation);

        StrategyResult result = new StrategyResult();
        result.setTradeDate(tradeDate);
        result.setStrategyType(config.getStrategyType());
        result.setMarketStatus(marketStatus);
        result.setTotalWeight(positions.stream().map(Position::getWeight).reduce(BigDecimal.ZERO, BigDecimal::add));
        positions.forEach(result::addPosition);
        return result;
    }

    private BigDecimal resolveTotalAllocation(StrategyConfig config, MarketStatus marketStatus) {
        BigDecimal base = config.getAllocationRatio();
        if (marketStatus == MarketStatus.STRONG) {
            return base;
        }
        // 弱势市场：平衡型减半，积极型清仓
        if (config.getStrategyType() == StrategyType.BALANCED) {
            return base.multiply(new BigDecimal("0.5"));
        }
        return BigDecimal.ZERO;
    }

    private StrategyResult buildEmptyResult(StrategyConfig config, LocalDate tradeDate, MarketStatus marketStatus) {
        StrategyResult result = new StrategyResult();
        result.setTradeDate(tradeDate);
        result.setStrategyType(config.getStrategyType());
        result.setMarketStatus(marketStatus);
        result.setTotalWeight(BigDecimal.ZERO);
        return result;
    }

    private Map<String, List<Nav>> loadNavHistory(List<Fund> candidates, LocalDate startDate, LocalDate endDate) {
        return candidates.stream()
                .collect(Collectors.toMap(
                        Fund::getFundCode,
                        fund -> navDataRepository.findByDateRange(fund.getFundCode(), startDate, endDate)
                ));
    }

    private int calculateMaxWindow() {
        return strategyConfigRepository.findAllEnabled().stream()
                .mapToInt(config -> Math.max(
                        Math.max(config.getShortMomentumWindow(), config.getLongMomentumWindow()),
                        Math.max(config.getMaWindow(), config.getVolatilityWindow())
                ))
                .max()
                .orElse(120);
    }
}
