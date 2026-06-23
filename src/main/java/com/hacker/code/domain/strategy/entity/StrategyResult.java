package com.hacker.code.domain.strategy.entity;

import com.hacker.code.domain.portfolio.valueobject.Position;
import com.hacker.code.domain.strategy.valueobject.MarketStatus;
import com.hacker.code.domain.strategy.valueobject.StrategyType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class StrategyResult {

    private Long id;
    private LocalDate tradeDate;
    private StrategyType strategyType;
    private MarketStatus marketStatus;
    private BigDecimal totalWeight;
    private List<Position> positions = new ArrayList<>();
    private LocalDateTime createdAt;

    public void addPosition(Position position) {
        this.positions.add(position);
    }
}
