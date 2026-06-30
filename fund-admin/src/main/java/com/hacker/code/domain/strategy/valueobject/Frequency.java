package com.hacker.code.domain.strategy.valueobject;

import lombok.Getter;

@Getter
public enum Frequency {
    WEEKLY("周度"),
    MONTHLY("月度");

    private final String description;

    Frequency(String description) {
        this.description = description;
    }
}
