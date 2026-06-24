package com.hacker.code.application.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class MarketDataSyncResult {

    private LocalDate startDate;
    private LocalDate endDate;
    private int totalDays;
    private int successDays;
    private int failedDays;
    private List<String> errors = new ArrayList<>();

    public void addError(String error) {
        this.errors.add(error);
    }
}
