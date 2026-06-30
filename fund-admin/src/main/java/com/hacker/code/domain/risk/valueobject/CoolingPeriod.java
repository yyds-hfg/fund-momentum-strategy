package com.hacker.code.domain.risk.valueobject;

import lombok.Value;

import java.time.LocalDate;

@Value
public class CoolingPeriod {

    LocalDate startDate;
    int minHoldDays;

    public boolean isCooling(LocalDate currentDate) {
        return currentDate.isBefore(startDate.plusDays(minHoldDays));
    }
}
