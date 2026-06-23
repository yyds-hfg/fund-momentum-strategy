package com.hacker.code.infrastructure.repository;

import com.hacker.code.domain.strategy.entity.StrategyConfig;
import com.hacker.code.domain.strategy.repository.StrategyConfigRepository;
import com.hacker.code.domain.strategy.valueobject.Frequency;
import com.hacker.code.domain.strategy.valueobject.StrategyType;
import com.hacker.code.infrastructure.mapper.StrategyConfigMapper;
import com.hacker.code.infrastructure.persistence.po.StrategyConfigPO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class StrategyConfigRepositoryImpl implements StrategyConfigRepository {

    private final StrategyConfigMapper strategyConfigMapper;

    @Override
    public Optional<StrategyConfig> findByType(StrategyType strategyType) {
        return Optional.ofNullable(strategyConfigMapper.selectByType(strategyType.name()))
                .map(this::toDomain);
    }

    @Override
    public List<StrategyConfig> findAllEnabled() {
        return strategyConfigMapper.selectAllEnabled().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void save(StrategyConfig config) {
        strategyConfigMapper.insert(toPO(config));
    }

    @Override
    public void update(StrategyConfig config) {
        StrategyConfigPO po = toPO(config);
        po.setId(config.getId());
        strategyConfigMapper.updateById(po);
    }

    private StrategyConfig toDomain(StrategyConfigPO po) {
        StrategyConfig config = new StrategyConfig();
        config.setId(po.getId());
        config.setStrategyType(StrategyType.valueOf(po.getStrategyType()));
        config.setShortMomentumWindow(po.getShortMomentumWindow());
        config.setLongMomentumWindow(po.getLongMomentumWindow());
        config.setUpDaysThreshold(po.getUpDaysThreshold());
        config.setMaWindow(po.getMaWindow());
        config.setVolatilityWindow(po.getVolatilityWindow());
        config.setMaxHoldingCount(po.getMaxHoldingCount());
        config.setSingleWeightCap(po.getSingleWeightCap());
        config.setRebalancingFrequency(Frequency.valueOf(po.getRebalancingFrequency()));
        config.setCoolingPeriodDays(po.getCoolingPeriodDays());
        config.setAllocationRatio(po.getAllocationRatio());
        config.setStatus(po.getStatus());
        return config;
    }

    private StrategyConfigPO toPO(StrategyConfig config) {
        StrategyConfigPO po = new StrategyConfigPO();
        po.setStrategyType(config.getStrategyType().name());
        po.setShortMomentumWindow(config.getShortMomentumWindow());
        po.setLongMomentumWindow(config.getLongMomentumWindow());
        po.setUpDaysThreshold(config.getUpDaysThreshold());
        po.setMaWindow(config.getMaWindow());
        po.setVolatilityWindow(config.getVolatilityWindow());
        po.setMaxHoldingCount(config.getMaxHoldingCount());
        po.setSingleWeightCap(config.getSingleWeightCap());
        po.setRebalancingFrequency(config.getRebalancingFrequency().name());
        po.setCoolingPeriodDays(config.getCoolingPeriodDays());
        po.setAllocationRatio(config.getAllocationRatio());
        po.setStatus(config.getStatus());
        return po;
    }
}
