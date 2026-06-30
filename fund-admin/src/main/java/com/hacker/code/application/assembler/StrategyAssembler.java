package com.hacker.code.application.assembler;

import com.hacker.code.application.dto.PositionDTO;
import com.hacker.code.application.dto.RebalanceAdviceDTO;
import com.hacker.code.application.dto.StrategyResultDTO;
import com.hacker.code.domain.portfolio.valueobject.Position;
import com.hacker.code.domain.portfolio.valueobject.RebalanceAdvice;
import com.hacker.code.domain.strategy.entity.StrategyResult;
import org.springframework.stereotype.Component;

import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class StrategyAssembler {

    public StrategyResultDTO toDTO(StrategyResult result) {
        StrategyResultDTO dto = new StrategyResultDTO();
        dto.setId(result.getId());
        dto.setTradeDate(result.getTradeDate());
        dto.setStrategyType(enumName(result.getStrategyType(), Enum::name));
        dto.setMarketStatus(enumName(result.getMarketStatus(), Enum::name));
        dto.setTotalWeight(result.getTotalWeight());
        dto.getPositions().addAll(
                result.getPositions().stream()
                        .map(this::toDTO)
                        .collect(Collectors.toList())
        );
        return dto;
    }

    public PositionDTO toDTO(Position position) {
        PositionDTO dto = new PositionDTO();
        dto.setFundCode(position.getFundCode());
        dto.setFundName(position.getFundName());
        dto.setWeight(position.getWeight());
        dto.setSourceStrategy(enumName(position.getSourceStrategy(), Enum::name));
        dto.setReason(position.getReason());
        return dto;
    }

    public RebalanceAdviceDTO toDTO(RebalanceAdvice advice) {
        RebalanceAdviceDTO dto = new RebalanceAdviceDTO();
        dto.setTradeDate(advice.getTradeDate());
        dto.setMarketStatus(enumName(advice.getMarketStatus(), Enum::name));
        dto.getSubResults().addAll(
                advice.getSubResults().stream()
                        .map(this::toDTO)
                        .collect(Collectors.toList())
        );
        if (advice.getMergedPositions() != null) {
            dto.getMergedPositions().addAll(
                    advice.getMergedPositions().stream()
                            .map(this::toDTO)
                            .collect(Collectors.toList())
            );
        }
        return dto;
    }

    private <E extends Enum<E>, R> R enumName(E value, Function<E, R> mapper) {
        return value == null ? null : mapper.apply(value);
    }
}
