package com.hacker.code.infrastructure.repository;

import com.hacker.code.domain.portfolio.valueobject.Position;
import com.hacker.code.domain.strategy.entity.StrategyResult;
import com.hacker.code.domain.strategy.repository.StrategyResultRepository;
import com.hacker.code.domain.strategy.valueobject.MarketStatus;
import com.hacker.code.domain.strategy.valueobject.StrategyType;
import com.hacker.code.infrastructure.mapper.StrategyPositionMapper;
import com.hacker.code.infrastructure.mapper.StrategyResultMapper;
import com.hacker.code.infrastructure.persistence.po.StrategyPositionPO;
import com.hacker.code.infrastructure.persistence.po.StrategyResultPO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class StrategyResultRepositoryImpl implements StrategyResultRepository {

    private final StrategyResultMapper strategyResultMapper;
    private final StrategyPositionMapper strategyPositionMapper;

    @Override
    public void save(StrategyResult result) {
        StrategyResultPO po = toPO(result);
        strategyResultMapper.insert(po);
        result.setId(po.getId());

        for (Position position : result.getPositions()) {
            StrategyPositionPO positionPO = toPO(po.getId(), position);
            strategyPositionMapper.insert(positionPO);
        }
    }

    @Override
    public Optional<StrategyResult> findLatest(StrategyType strategyType) {
        return Optional.ofNullable(strategyResultMapper.selectLatest(strategyType.name()))
                .map(this::toDomain);
    }

    @Override
    public List<StrategyResult> findByDate(LocalDate tradeDate) {
        return strategyResultMapper.selectByDate(tradeDate).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<StrategyResult> findByDateRange(LocalDate startDate, LocalDate endDate) {
        return strategyResultMapper.selectByDateRange(startDate, endDate).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    private StrategyResult toDomain(StrategyResultPO po) {
        StrategyResult result = new StrategyResult();
        result.setId(po.getId());
        result.setTradeDate(po.getTradeDate());
        result.setStrategyType(StrategyType.valueOf(po.getStrategyType()));
        result.setMarketStatus(MarketStatus.valueOf(po.getMarketStatus()));
        result.setTotalWeight(po.getTotalWeight());
        result.setCreatedAt(po.getCreateTime());

        List<Position> positions = strategyPositionMapper.selectByResultId(po.getId()).stream()
                .map(this::toPosition)
                .collect(Collectors.toList());
        result.setPositions(positions);
        return result;
    }

    private StrategyResultPO toPO(StrategyResult result) {
        StrategyResultPO po = new StrategyResultPO();
        po.setTradeDate(result.getTradeDate());
        po.setStrategyType(result.getStrategyType().name());
        po.setMarketStatus(result.getMarketStatus().name());
        po.setTotalWeight(result.getTotalWeight());
        return po;
    }

    private Position toPosition(StrategyPositionPO po) {
        return new Position(
                po.getFundCode(),
                po.getFundName(),
                po.getWeight(),
                StrategyType.valueOf(po.getSourceStrategy()),
                po.getReason()
        );
    }

    private StrategyPositionPO toPO(Long resultId, Position position) {
        StrategyPositionPO po = new StrategyPositionPO();
        po.setResultId(resultId);
        po.setFundCode(position.getFundCode());
        po.setFundName(position.getFundName());
        po.setWeight(position.getWeight());
        po.setSourceStrategy(position.getSourceStrategy().name());
        po.setReason(position.getReason());
        return po;
    }
}
