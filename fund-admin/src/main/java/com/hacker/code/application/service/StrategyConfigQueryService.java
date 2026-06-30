package com.hacker.code.application.service;

import com.hacker.code.application.dto.StrategyConfigDTO;
import com.hacker.code.domain.strategy.entity.StrategyConfig;
import com.hacker.code.domain.strategy.repository.StrategyConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StrategyConfigQueryService {

    private final StrategyConfigRepository strategyConfigRepository;

    public List<StrategyConfigDTO> findAllEnabled() {
        return strategyConfigRepository.findAllEnabled().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public StrategyConfigDTO findById(Long id) {
        return strategyConfigRepository.findAllEnabled().stream()
                .filter(c -> c.getId().equals(id))
                .findFirst()
                .map(this::toDTO)
                .orElseThrow(() -> new IllegalArgumentException("Strategy config not found: " + id));
    }

    public void update(StrategyConfigDTO dto) {
        StrategyConfig config = strategyConfigRepository.findAllEnabled().stream()
                .filter(c -> c.getId().equals(dto.getId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Strategy config not found: " + dto.getId()));

        config.setShortMomentumWindow(dto.getShortMomentumWindow());
        config.setLongMomentumWindow(dto.getLongMomentumWindow());
        config.setUpDaysThreshold(dto.getUpDaysThreshold());
        config.setMaWindow(dto.getMaWindow());
        config.setVolatilityWindow(dto.getVolatilityWindow());
        config.setMaxHoldingCount(dto.getMaxHoldingCount());
        config.setSingleWeightCap(dto.getSingleWeightCap());
        config.setCoolingPeriodDays(dto.getCoolingPeriodDays());
        config.setAllocationRatio(dto.getAllocationRatio());

        strategyConfigRepository.update(config);
    }

    private StrategyConfigDTO toDTO(StrategyConfig config) {
        StrategyConfigDTO dto = new StrategyConfigDTO();
        dto.setId(config.getId());
        dto.setStrategyType(config.getStrategyType().name());
        dto.setStrategyName(config.getStrategyType().getDescription());
        dto.setShortMomentumWindow(config.getShortMomentumWindow());
        dto.setLongMomentumWindow(config.getLongMomentumWindow());
        dto.setUpDaysThreshold(config.getUpDaysThreshold());
        dto.setMaWindow(config.getMaWindow());
        dto.setVolatilityWindow(config.getVolatilityWindow());
        dto.setMaxHoldingCount(config.getMaxHoldingCount());
        dto.setSingleWeightCap(config.getSingleWeightCap());
        dto.setRebalancingFrequency(config.getRebalancingFrequency().name());
        dto.setCoolingPeriodDays(config.getCoolingPeriodDays());
        dto.setAllocationRatio(config.getAllocationRatio());
        dto.setStatus(config.getStatus());
        return dto;
    }
}
