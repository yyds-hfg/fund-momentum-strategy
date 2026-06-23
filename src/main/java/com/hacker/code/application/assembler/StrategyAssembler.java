package com.hacker.code.application.assembler;

import com.hacker.code.application.dto.PositionDTO;
import com.hacker.code.application.dto.RebalanceAdviceDTO;
import com.hacker.code.application.dto.StrategyResultDTO;
import com.hacker.code.domain.portfolio.valueobject.Position;
import com.hacker.code.domain.portfolio.valueobject.RebalanceAdvice;
import com.hacker.code.domain.strategy.entity.StrategyResult;
import org.springframework.stereotype.Component;

@Component
public class StrategyAssembler {

    public StrategyResultDTO toDTO(StrategyResult result) {
        StrategyResultDTO dto = new StrategyResultDTO();
        dto.setId(result.getId());
        dto.setTradeDate(result.getTradeDate());
        dto.setStrategyType(result.getStrategyType().name());
        dto.setMarketStatus(result.getMarketStatus().name());
        dto.setTotalWeight(result.getTotalWeight());
        for (Position position : result.getPositions()) {
            dto.getPositions().add(toDTO(position));
        }
        return dto;
    }

    public PositionDTO toDTO(Position position) {
        PositionDTO dto = new PositionDTO();
        dto.setFundCode(position.getFundCode());
        dto.setFundName(position.getFundName());
        dto.setWeight(position.getWeight());
        dto.setSourceStrategy(position.getSourceStrategy().name());
        dto.setReason(position.getReason());
        return dto;
    }

    public RebalanceAdviceDTO toDTO(RebalanceAdvice advice) {
        RebalanceAdviceDTO dto = new RebalanceAdviceDTO();
        dto.setTradeDate(advice.getTradeDate());
        dto.setMarketStatus(advice.getMarketStatus().name());
        for (StrategyResult result : advice.getSubResults()) {
            dto.getSubResults().add(toDTO(result));
        }
        return dto;
    }
}
