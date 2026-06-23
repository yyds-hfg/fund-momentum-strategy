package com.hacker.code.domain.shared.event;

import com.hacker.code.domain.strategy.entity.StrategyResult;
import lombok.Getter;

import java.util.List;

@Getter
public class StrategyCalculatedEvent extends DomainEvent {

    private final List<StrategyResult> results;

    public StrategyCalculatedEvent(List<StrategyResult> results) {
        super();
        this.results = results;
    }
}
