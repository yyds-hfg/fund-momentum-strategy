package com.hacker.code.application.service;

import com.hacker.code.domain.fund.entity.Fund;
import com.hacker.code.domain.fund.repository.NavDataRepository;
import com.hacker.code.domain.fund.service.FundDomainService;
import com.hacker.code.domain.fund.valueobject.Nav;
import com.hacker.code.domain.strategy.entity.FundMomentumTrend;
import com.hacker.code.domain.strategy.entity.StrategyConfig;
import com.hacker.code.domain.strategy.repository.FundMomentumTrendRepository;
import com.hacker.code.domain.strategy.repository.StrategyConfigRepository;
import com.hacker.code.domain.strategy.service.MomentumTrendCalculator;
import com.hacker.code.domain.strategy.valueobject.MomentumTrendResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MomentumTrendAppService {

    private final FundDomainService fundDomainService;
    private final NavDataRepository navDataRepository;
    private final StrategyConfigRepository strategyConfigRepository;
    private final FundMomentumTrendRepository momentumTrendRepository;
    private final MomentumTrendCalculator momentumTrendCalculator;

    public void computeAndSaveForTradeDate(LocalDate tradeDate) {
        List<Fund> candidates = fundDomainService.getCandidatePool().stream()
                .filter(fund -> !"000852".equals(fund.getFundCode()))
                .collect(Collectors.toList());

        for (StrategyConfig config : strategyConfigRepository.findAllEnabled()) {
            int maxWindow = Math.max(config.getLongMomentumWindow(),
                    Math.max(config.getMaWindow(), config.getVolatilityWindow()));
            LocalDate startDate = tradeDate.minusDays(maxWindow + 80);

            momentumTrendRepository.deleteByStrategyTypeAndTradeDate(config.getStrategyType(), tradeDate);

            List<FundMomentumTrend> trends = new ArrayList<>();
            for (Fund fund : candidates) {
                List<Nav> history = navDataRepository.findByDateRange(fund.getFundCode(), startDate, tradeDate);
                MomentumTrendResult result = momentumTrendCalculator.calculate(history, config);
                if (result == null) {
                    continue;
                }
                FundMomentumTrend trend = new FundMomentumTrend();
                trend.setStrategyType(config.getStrategyType());
                trend.setFundCode(fund.getFundCode());
                trend.setTradeDate(tradeDate);
                trend.setSlope7(result.getSlope7());
                trend.setSlope14(result.getSlope14());
                trend.setSlope20(result.getSlope20());
                trend.setSigma(result.getSigma());
                trend.setTrend(result.getTrend());
                trend.setDescription(result.getDescription());
                trends.add(trend);
            }

            momentumTrendRepository.saveAll(trends);
            log.info("已计算并保存 {} {} 的动量趋势，共 {} 条", tradeDate, config.getStrategyType(), trends.size());
        }
    }
}
