package com.hacker.code.domain.strategy.valueobject;

import lombok.Getter;

@Getter
public enum MarketStatus {
    STRONG("强势"),
    WEAK("弱势");

    private final String description;

    MarketStatus(String description) {
        this.description = description;
    }
}
