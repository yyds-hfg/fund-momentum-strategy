package com.hacker.code.domain.strategy.valueobject;

import lombok.Getter;

@Getter
public enum StrategyType {
    ACTIVE("积极型"),
    BALANCED("平衡型"),
    MERGED("合并持仓");

    private final String description;

    StrategyType(String description) {
        this.description = description;
    }
}
