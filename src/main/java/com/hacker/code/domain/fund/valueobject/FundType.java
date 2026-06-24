package com.hacker.code.domain.fund.valueobject;

import lombok.Getter;

@Getter
public enum FundType {
    WIDE_BASE("宽基"),
    SECTOR("行业/主题"),
    BOND("债券"),
    GOLD("商品"),
    OVERSEAS("海外"),
    OTHER("其他");

    private final String description;

    FundType(String description) {
        this.description = description;
    }
}
