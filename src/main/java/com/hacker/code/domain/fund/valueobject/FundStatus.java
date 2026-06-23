package com.hacker.code.domain.fund.valueobject;

import lombok.Getter;

@Getter
public enum FundStatus {
    DISABLED(0),
    ENABLED(1);

    private final int code;

    FundStatus(int code) {
        this.code = code;
    }
}
