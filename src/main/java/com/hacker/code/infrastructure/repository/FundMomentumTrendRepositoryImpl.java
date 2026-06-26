package com.hacker.code.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hacker.code.domain.strategy.entity.FundMomentumTrend;
import com.hacker.code.domain.strategy.repository.FundMomentumTrendRepository;
import com.hacker.code.domain.strategy.valueobject.MomentumTrend;
import com.hacker.code.domain.strategy.valueobject.StrategyType;
import com.hacker.code.infrastructure.mapper.FundMomentumTrendMapper;
import com.hacker.code.infrastructure.persistence.po.FundMomentumTrendPO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class FundMomentumTrendRepositoryImpl implements FundMomentumTrendRepository {

    private final FundMomentumTrendMapper fundMomentumTrendMapper;

    @Override
    public void saveAll(List<FundMomentumTrend> trends) {
        if (trends == null || trends.isEmpty()) {
            return;
        }
        for (FundMomentumTrend trend : trends) {
            fundMomentumTrendMapper.insert(toPO(trend));
        }
    }

    @Override
    public Optional<FundMomentumTrend> findByStrategyTypeAndFundCodeAndTradeDate(StrategyType strategyType,
                                                                                  String fundCode,
                                                                                  LocalDate tradeDate) {
        FundMomentumTrendPO po = fundMomentumTrendMapper.selectByStrategyTypeAndFundCodeAndTradeDate(
                strategyType.name(), fundCode, tradeDate);
        return Optional.ofNullable(po).map(this::toDomain);
    }

    @Override
    public List<FundMomentumTrend> findByStrategyTypeAndTradeDate(StrategyType strategyType, LocalDate tradeDate) {
        return fundMomentumTrendMapper.selectByStrategyTypeAndTradeDate(strategyType.name(), tradeDate).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteByStrategyTypeAndTradeDate(StrategyType strategyType, LocalDate tradeDate) {
        QueryWrapper<FundMomentumTrendPO> wrapper = new QueryWrapper<>();
        wrapper.eq("strategy_type", strategyType.name())
                .eq("trade_date", tradeDate);
        fundMomentumTrendMapper.delete(wrapper);
    }

    private FundMomentumTrend toDomain(FundMomentumTrendPO po) {
        FundMomentumTrend trend = new FundMomentumTrend();
        trend.setId(po.getId());
        trend.setStrategyType(StrategyType.valueOf(po.getStrategyType()));
        trend.setFundCode(po.getFundCode());
        trend.setTradeDate(po.getTradeDate());
        trend.setSlope7(po.getSlope7());
        trend.setSlope14(po.getSlope14());
        trend.setSlope20(po.getSlope20());
        trend.setSigma(po.getSigma());
        trend.setTrend(MomentumTrend.valueOf(po.getTrend()));
        trend.setDescription(po.getDescription());
        trend.setCreatedAt(po.getCreateTime());
        trend.setUpdatedAt(po.getUpdateTime());
        return trend;
    }

    private FundMomentumTrendPO toPO(FundMomentumTrend trend) {
        FundMomentumTrendPO po = new FundMomentumTrendPO();
        po.setId(trend.getId());
        po.setStrategyType(trend.getStrategyType().name());
        po.setFundCode(trend.getFundCode());
        po.setTradeDate(trend.getTradeDate());
        po.setSlope7(trend.getSlope7());
        po.setSlope14(trend.getSlope14());
        po.setSlope20(trend.getSlope20());
        po.setSigma(trend.getSigma());
        po.setTrend(trend.getTrend().name());
        po.setDescription(trend.getDescription());
        return po;
    }
}
